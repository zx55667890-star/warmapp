const admin = require('firebase-admin');
const { GoogleGenAI, Type } = require('@google/genai');
const { defineSecret } = require('firebase-functions/params');
const { onValueWritten } = require('firebase-functions/v2/database');
const { getDatabase } = require('firebase-admin/database');

admin.initializeApp();
const db = getDatabase();

const geminiApiKey = defineSecret('GEMINI_API_KEY');
const serperApiKey = defineSecret('SERPER_API_KEY');

function encodePath(text) {
  return Buffer.from(text.trim(), 'utf8').toString('base64url');
}

const BATCH_LIMIT_SKILLS = 50;
const BATCH_LIMIT_QUESTIONS = 50;
const PROCESSING_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

const MODELS = [
  // PRIMARY：最 cost-effective 主力，不搜尋
  { name: 'gemini-3.5-flash-lite', label: 'PRIMARY', useSearch: false },

  // FALLBACK_1：同 PRIMARY 但啟用 Serper 外部搜尋
  { name: 'gemini-3.5-flash-lite', label: 'FALLBACK_1', useWebFetch: true },

  // FALLBACK_2：3.1 成本仍低 + Serper
  { name: 'gemini-3.1-flash-lite', label: 'FALLBACK_2', useWebFetch: true },

  // FALLBACK_3~5：Gen3 模型 + Serper + minimal thinking
  { name: 'gemini-3.6-flash',      label: 'FALLBACK_3', useWebFetch: true, thinkingConfig: { thinkingLevel: 'minimal' } },
  { name: 'gemini-3.5-flash',      label: 'FALLBACK_4', useWebFetch: true, thinkingConfig: { thinkingLevel: 'minimal' } },
  { name: 'gemini-3-flash-preview',label: 'FALLBACK_5', useWebFetch: true, thinkingConfig: { thinkingLevel: 'minimal' } },
];

async function generateContentWithRetry(modelConfig, prompt, retries = 3) {
  const ai = new GoogleGenAI({ apiKey: geminiApiKey.value() });
  for (let i = 0; i < retries; i++) {
    try {
      const startTime = Date.now();
      const config = {};
      if (!modelConfig.useSearch) {
        config.responseMimeType = 'application/json';
        config.responseSchema = {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              id: { type: Type.STRING },
              tags: { type: Type.ARRAY, items: { type: Type.STRING } },
              status: { type: Type.STRING, enum: ['ACTIVE', 'REJECTED'] }
            },
            required: ['id', 'tags', 'status']
          }
        };
      }
      if (modelConfig.thinkingConfig) config.thinkingConfig = modelConfig.thinkingConfig;
      if (modelConfig.useSearch) config.tools = [{ googleSearch: {} }];
      const response = await ai.models.generateContent({
        model: modelConfig.name,
        contents: prompt,
        config: config,
      });
      return { response, elapsed: Date.now() - startTime };
    } catch (err) {
      const isRetryable = err.status === 429 || err.status === 503 ||
                          (err.message && err.message.includes('RESOURCE_EXHAUSTED'));
      if (isRetryable && i < retries - 1) {
        const delay = Math.pow(2, i) * 2000;
        console.warn(`Retryable error (${err.status}) for ${modelConfig.name}, retrying in ${delay}ms...`);
        await new Promise(resolve => setTimeout(resolve, delay));
        continue;
      }
      throw err;
    }
  }
}

async function searchOnSerper(query) {
  const res = await fetch('https://google.serper.dev/search', {
    method: 'POST',
    headers: {
      'X-API-KEY': serperApiKey.value(),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ q: query, num: 3 }),
    signal: AbortSignal.timeout(5000),
  });
  if (!res.ok) throw new Error(`Serper API error: ${res.status}`);
  const data = await res.json();
  return (data.organic || []).map(r => `${r.title}: ${r.snippet}`).join('\n');
}

const REPAIR_BATCH_SIZE = 5;
const PENDING_STALE_MS = 10 * 60 * 1000;

async function releaseStuckProcessing(path) {
  const snapshot = await db.ref(path).once('value');
  if (!snapshot.exists()) return;
  const now = Date.now();
  const updates = {};
  snapshot.forEach(child => {
    const proc = child.val().processing;
    if (proc && (now - proc) >= PROCESSING_TIMEOUT_MS) {
      updates[`${path}/${child.key}/processing`] = null;
    }
  });
  const keys = Object.keys(updates);
  if (keys.length > 0) {
    await db.ref().update(updates);
    console.log(`Released ${keys.length} stuck entries from ${path}`);
  }
}

async function healOrphanedPending() {
  let usersSnapshot;
  try {
    const cursorSnapshot = await db.ref('config/repair_cursor').once('value');
    const cursor = cursorSnapshot.val() || '';

    let query = db.ref('users').orderByKey().limitToFirst(REPAIR_BATCH_SIZE);
    if (cursor) {
      query = query.startAfter(cursor);
    }
    usersSnapshot = await query.once('value');
  } catch (err) {
    console.error('Repair scan query failed, resetting cursor:', err.message);
    await db.ref('config/repair_cursor').set('');
    return;
  }

  const users = usersSnapshot.val();
  if (!users) {
    console.log('Repair scan: no more users to check, resetting cursor');
    await db.ref('config/repair_cursor').set('');
    return;
  }

  const uids = Object.keys(users);
  const lastKey = uids[uids.length - 1];
  let repaired = 0;

  for (const uid of uids) {
    const solutionsSnapshot = await db.ref(`solutions/${uid}`)
      .orderByChild('status')
      .equalTo('PENDING')
      .once('value');

    const solutions = solutionsSnapshot.val();
    if (!solutions) continue;

    const now = Date.now();
    for (const [skillId, data] of Object.entries(solutions)) {
      const timestamp = data.timestamp || 0;
      if (now - timestamp < PENDING_STALE_MS) continue;

      const pendingSnapshot = await db.ref(`pending_skills/${skillId}`).once('value');
      if (pendingSnapshot.exists()) continue;

      await db.ref(`pending_skills/${skillId}`).set({
        userId: uid,
        text: data.expertise || '',
        timestamp: now,
      });
      repaired++;
    }
  }

  await db.ref('config/repair_cursor').set(lastKey);
  console.log(`Repair scan: checked ${uids.length} users, cursor=${lastKey}, repaired=${repaired}`);
}

// ─────────────────────────────────────────────
// 單一排程：依序處理 pending_skills → pending_questions
// 合併兩個 CF 減少 API 競爭 (原 batchProcessPendingSkills + batchProcessPendingQuestions)
// ─────────────────────────────────────────────

async function processSkills() {
  console.log('[Skills] start');
  try {
    await healOrphanedPending();
    console.log('[Skills] self-heal scan completed');
  } catch (err) {
    console.error('[Skills] self-heal scan failed:', err.message);
  }

  try {
    await releaseStuckProcessing('pending_skills');
  } catch (err) {
    console.error('[Skills] releaseStuckProcessing failed:', err.message);
  }

  const snapshot = await db.ref('pending_skills')
    .orderByChild('timestamp')
    .limitToFirst(BATCH_LIMIT_SKILLS)
    .once('value');

  const entries = [];
  snapshot.forEach(child => entries.push({ id: child.key, ...child.val() }));
  if (entries.length === 0) { console.log('[Skills] none found'); return; }

  const now = Date.now();
  const claimResults = await Promise.all(entries.map(async (entry) => {
    let claimed = false;
    await db.ref(`pending_skills/${entry.id}`).transaction(cur => {
      if (cur === null) return cur;
      if (cur.processing && (now - cur.processing) < PROCESSING_TIMEOUT_MS) return;
      cur.processing = now;
      claimed = true;
      return cur;
    });
    return { entry, claimed };
  }));

  const processable = claimResults.filter(r => r.claimed).map(r => r.entry);
  if (processable.length === 0) { console.log('[Skills] all in progress'); return; }
  console.log(`[Skills] claimed ${processable.length}`);

  // Step 1: blacklist
  const blResults = await Promise.all(processable.map(async (entry) => {
    const bl = await db.ref(`tags_blacklist/${encodePath(entry.text)}`).once('value');
    return { entry, isBlacklisted: bl.exists() };
  }));

  const updates = {};
  let aiEntries = [];
  const lockTrackers = {};
  const acceptedEntries = [];

  for (const { entry, isBlacklisted } of blResults) {
    if (isBlacklisted) {
      updates[`solutions/${entry.userId}/${entry.id}/status`] = 'REJECTED';
      updates[`solutions/${entry.userId}/${entry.id}/tags`] = [];
      updates[`pending_skills/${entry.id}`] = null;
      const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
      t.rejectedCount = (t.rejectedCount || 0) + 1;
    } else {
      aiEntries.push(entry);
    }
  }

  // Step 2: whitelist（精確比對）
  const wlResults = await Promise.all(aiEntries.map(async (entry) => {
    const wl = await db.ref(`tags_whitelist/${encodePath(entry.text)}`).once('value');
    const val = wl.val();
    const tags = val && val.tags;
    return { entry, cachedTags: Array.isArray(tags) && tags.length > 0 ? tags : null };
  }));

  let remaining = [];

  for (const { entry, cachedTags } of wlResults) {
    if (cachedTags) {
      updates[`solutions/${entry.userId}/${entry.id}/status`] = 'ACTIVE';
      updates[`solutions/${entry.userId}/${entry.id}/tags`] = cachedTags;
      updates[`pending_skills/${entry.id}`] = null;
      updates[`active_experiences/${entry.id}`] = { authorId: entry.userId, text: entry.text, timestamp: now, status: 'active', isOnline: true };
      acceptedEntries.push({ id: entry.id, text: entry.text });
      const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
      t.hasActive = true;
    } else {
      remaining.push(entry);
    }
  }

  // Step 2.5: whitelist（語意快取，embedding 相似度）
  if (remaining.length > 0) {
    const semanticResults = await Promise.all(remaining.map(async (entry) => {
      try {
        const embedding = await getEmbedding(entry.text);
        const semanticTags = await findSemanticCachedTags(entry.text, embedding, updates);
        return { entry, semanticTags, embedding };
      } catch (err) {
        console.warn(`[Skills][SemanticCache] embedding failed for "${entry.text.substring(0, 20)}": ${err.message}`);
        return { entry, semanticTags: null, embedding: null };
      }
    }));
    const newRemaining = [];
    for (const { entry, semanticTags, embedding } of semanticResults) {
      if (semanticTags) {
        updates[`solutions/${entry.userId}/${entry.id}/status`] = 'ACTIVE';
        updates[`solutions/${entry.userId}/${entry.id}/tags`] = semanticTags;
        updates[`pending_skills/${entry.id}`] = null;
        updates[`active_experiences/${entry.id}`] = { authorId: entry.userId, text: entry.text, timestamp: now, status: 'active', isOnline: true };
        acceptedEntries.push({ id: entry.id, text: entry.text, embedding });
        const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
        t.hasActive = true;
      } else {
        // 把 embedding 暫存在 entry 上，LLM 後直接存入 whitelist 不需重複計算
        entry._precomputedEmbedding = embedding;
        newRemaining.push(entry);
      }
    }
    remaining = newRemaining;
  }

  if (remaining.length === 0) {
    await db.ref().update(updates);
    if (acceptedEntries.length > 0) {
      const embedResults = await Promise.allSettled(acceptedEntries.map(async ({ id, text }) => {
        const embedding = await getEmbedding(text);
        if (embedding) {
          await db.ref(`active_experiences/${id}/embedding`).set(embedding);
        }
      }));
      const succeeded = embedResults.filter(r => r.status === 'fulfilled').length;
      if (succeeded < acceptedEntries.length) {
        console.log(`[Skills] embedding: ${succeeded}/${acceptedEntries.length} succeeded`);
      }
    }
    console.log(`[Skills] all ${processable.length} resolved via cache`);
    return;
  }

  // Step 3: AI 5-model pipeline
  const modelStatus = (await db.ref('config/model_status').once('value')).val() || {};
  let candidates = MODELS.filter(m => modelStatus[encodePath(m.name)] !== 'EXHAUSTED');
  if (candidates.length === 0) {
    console.warn('[Skills] all models EXHAUSTED');
    const lastReset = (await db.ref('config/last_reset').once('value')).val();
    if (lastReset && (Date.now() - lastReset) < 300000) { console.error('[Skills] aborting'); return; }
    await db.ref('config/last_reset').set(Date.now());
    await db.ref('config/model_status').set({});
    candidates = MODELS;
  }

  for (const model of candidates) {
    if (remaining.length === 0) break;
    console.log(`[Skills] model ${model.name} (${model.label}), ${remaining.length} entries`);

    const localMapping = new Map();
    const slimmed = remaining.map((e, i) => { const v = i.toString(); localMapping.set(v, e); return { id: v, text: e.text }; });

    let searchContext = '';
    if (model.useWebFetch) {
      const sr = await Promise.all(slimmed.map(async (se) => {
        try {
          const snippets = await searchOnSerper(se.text);
          return `Entry "${se.id}": "${se.text}"\n搜尋結果：\n${snippets}`;
        } catch (err) {
          return `Entry "${se.id}": "${se.text}"\n搜尋結果：（搜尋失敗）`;
        }
      }));
      searchContext = '以下是網路搜尋結果，請仔細參考這些資訊來輔助判斷技能的真實性：\n\n' + sr.join('\n\n') + '\n\n';
    }

    const prompt = searchContext + `請判斷以下每筆資料是否為真實、有意義的專業技能描述。
判斷原則：
- 如果內容是具體、可理解的專業技能，請提取 4 個最能描述該技能的核心關鍵字（標籤應涵蓋該領域的具體名詞與相關概念詞彙）
- 如果內容是無意義的胡言亂語或無法對應到真實場景，請將 status 設為 "REJECTED"
- 請仔細參考上述網路搜尋結果（若有提供）來協助判斷
- 遇到無法明確判斷時，寧可 REJECTED 也不要勉強給標籤
- 標籤必須使用與技能描述相同的語言（若描述為中文，標籤也必須是中文）

 以 JSON Array 格式回傳，每個物件包含 id、tags 和 status 欄位。status 為 "ACTIVE"（接受）或 "REJECTED"（拒絕）。
資料：${JSON.stringify(slimmed)}`;

    try {
      const apiModel = model.useWebFetch ? { ...model, useSearch: false } : model;
      const { response, elapsed } = await generateContentWithRetry(apiModel, prompt);
      console.log(`[Skills] ${model.name} responded in ${elapsed}ms`);
      const text = response.text || '';
      if (!text) throw new Error('AI 回傳空內容');

      let parsed;
      try { parsed = JSON.parse(text); } catch {
        const m = text.match(/\[[\s\S]*\]/);
        if (m) parsed = JSON.parse(m[0]); else throw new Error('無法解析 AI 回應為 JSON');
      }

      const modelAccepted = [];
      const newRejected = [];
      for (const item of parsed) {
        const entry = localMapping.get(item.id?.toString());
        if (!entry) continue;
        const isReject = item.status === 'REJECTED' || (Array.isArray(item.tags) && item.tags.includes('REJECT'));
        const tags = Array.isArray(item.tags) ? item.tags.filter(t => t !== 'REJECT').slice(0, 4) : [];

        if (isReject) { newRejected.push(entry); } else {
          const sr = `solutions/${entry.userId}/${entry.id}`;
          const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
          updates[`${sr}/status`] = 'ACTIVE';
          updates[`${sr}/tags`] = tags;
          updates[`tags_whitelist/${encodePath(entry.text)}/tags`] = tags;
          updates[`tags_whitelist/${encodePath(entry.text)}/source`] = 'llm';
          if (entry._precomputedEmbedding) {
            updates[`tags_whitelist/${encodePath(entry.text)}/embedding`] = entry._precomputedEmbedding;
          }
          updates[`pending_skills/${entry.id}`] = null;
          updates[`active_experiences/${entry.id}`] = { authorId: entry.userId, text: entry.text, timestamp: now, status: 'active', isOnline: true };
          acceptedEntries.push({ id: entry.id, text: entry.text });
          modelAccepted.push(entry);
          t.hasActive = true;
        }
      }
      if (modelAccepted.length > 0) console.log(`[Skills] ${model.label} accepted:`, modelAccepted.map(e => e.text));
      if (newRejected.length > 0) console.log(`[Skills] ${model.label} rejected:`, newRejected.map(e => e.text));
      remaining = newRejected;
    } catch (err) {
      console.error(`[Skills] model ${model.name} failed:`, err.message);
      if (err.status === 429 || (err.message && err.message.includes('RESOURCE_EXHAUSTED'))) {
        try { await db.ref(`config/model_status/${encodePath(model.name)}`).set('EXHAUSTED'); } catch (_) {}
      }
    }
  }

  // Final reject
  if (remaining.length > 0) {
    for (const entry of remaining) {
      const sr = `solutions/${entry.userId}/${entry.id}`;
      const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
      updates[`${sr}/status`] = 'REJECTED';
      updates[`${sr}/tags`] = [];
      updates[`tags_blacklist/${encodePath(entry.text)}`] = true;
      updates[`pending_skills/${entry.id}`] = null;
      updates[`active_experiences/${entry.id}`] = null;
      t.rejectedCount = (t.rejectedCount || 0) + 1;
    }
  }

  for (const [uid, t] of Object.entries(lockTrackers)) {
    const prev = (await db.ref(`users/${uid}/submissionLock/rejectedCount`).once('value')).val() || 0;
    const total = prev + (t.rejectedCount || 0);
    updates[`users/${uid}/submissionLock/rejectedCount`] = total;
    updates[`users/${uid}/submissionLock/lockedUntil`] = (!t.hasActive && total >= 3) ? Date.now() + 86400000 : 0;
  }

  await db.ref().update(updates);

  // Embedding: pre-compute for all newly accepted skills
  if (acceptedEntries.length > 0) {
    const embedResults = await Promise.allSettled(acceptedEntries.map(async ({ id, text }) => {
      const embedding = await getEmbedding(text);
      if (embedding) {
        await db.ref(`active_experiences/${id}/embedding`).set(embedding);
      }
    }));
    const succeeded = embedResults.filter(r => r.status === 'fulfilled').length;
    if (succeeded < acceptedEntries.length) {
      console.log(`[Skills] embedding: ${succeeded}/${acceptedEntries.length} succeeded`);
    }
  }

  console.log('[Skills] complete');
}

async function processQuestions() {
  console.log('[Questions] start');
  try {
    await releaseStuckProcessing('pending_questions');
  } catch (err) {
    console.error('[Questions] releaseStuckProcessing failed:', err.message);
  }

  const snapshot = await db.ref('pending_questions')
    .orderByChild('timestamp')
    .limitToFirst(BATCH_LIMIT_QUESTIONS)
    .once('value');

  const entries = [];
  snapshot.forEach(child => entries.push({ id: child.key, ...child.val() }));
  if (entries.length === 0) { console.log('[Questions] none found'); return; }

  const now = Date.now();
  const claimResults = await Promise.all(entries.map(async (entry) => {
    let claimed = false;
    await db.ref(`pending_questions/${entry.id}`).transaction(cur => {
      if (cur === null) return cur;
      if (cur.processing && (now - cur.processing) < PROCESSING_TIMEOUT_MS) return;
      cur.processing = now;
      claimed = true;
      return cur;
    });
    return { entry, claimed };
  }));

  const processable = claimResults.filter(r => r.claimed).map(r => r.entry);
  if (processable.length === 0) { console.log('[Questions] all in progress'); return; }
  console.log(`[Questions] claimed ${processable.length}`);

  // Step 1: blacklist
  const blResults = await Promise.all(processable.map(async (entry) => {
    const bl = await db.ref(`tags_blacklist/${encodePath(entry.text)}`).once('value');
    return { entry, isBlacklisted: bl.exists() };
  }));

  const updates = {};
  let aiEntries = [];

  for (const { entry, isBlacklisted } of blResults) {
    if (isBlacklisted) {
      updates[`questions/${entry.id}/status`] = 'cancelled';
      updates[`pending_questions/${entry.id}`] = null;
    } else {
      aiEntries.push(entry);
    }
  }

  // Step 2: whitelist（精確比對）
  const wlResults = await Promise.all(aiEntries.map(async (entry) => {
    const wl = await db.ref(`tags_whitelist/${encodePath(entry.text)}`).once('value');
    const val = wl.val();
    const cached = val && val.tags;
    return { entry, cachedTags: Array.isArray(cached) && cached.length > 0 ? cached : null };
  }));

  let remaining = [];

  for (const { entry, cachedTags } of wlResults) {
    if (cachedTags) {
      updates[`questions/${entry.id}/tags`] = cachedTags;
      updates[`questions/${entry.id}/text`] = entry.text;
      updates[`questions/${entry.id}/authorId`] = entry.userId;
      updates[`questions/${entry.id}/timestamp`] = entry.timestamp;
      updates[`questions/${entry.id}/status`] = 'matching';
      updates[`questions/${entry.id}/expertId`] = '';
      updates[`pending_questions/${entry.id}`] = null;
    } else {
      remaining.push(entry);
    }
  }

  // Step 2.5: whitelist（語意快取，embedding 相似度）
  const semanticCachedQuestions = [];
  if (remaining.length > 0) {
    const semanticResults = await Promise.all(remaining.map(async (entry) => {
      try {
        const embedding = await getEmbedding(entry.text);
        const semanticTags = await findSemanticCachedTags(entry.text, embedding, updates);
        return { entry, semanticTags, embedding };
      } catch (err) {
        console.warn(`[Questions][SemanticCache] embedding failed for "${entry.text.substring(0, 20)}": ${err.message}`);
        return { entry, semanticTags: null, embedding: null };
      }
    }));
    const newRemaining = [];
    for (const { entry, semanticTags, embedding } of semanticResults) {
      if (semanticTags) {
        updates[`questions/${entry.id}/tags`] = semanticTags;
        updates[`questions/${entry.id}/text`] = entry.text;
        updates[`questions/${entry.id}/authorId`] = entry.userId;
        updates[`questions/${entry.id}/timestamp`] = entry.timestamp;
        updates[`questions/${entry.id}/status`] = 'matching';
        updates[`questions/${entry.id}/expertId`] = '';
        updates[`pending_questions/${entry.id}`] = null;
        semanticCachedQuestions.push({ entry, tags: semanticTags });
      } else {
        entry._precomputedEmbedding = embedding;
        newRemaining.push(entry);
      }
    }
    remaining = newRemaining;
  }

  if (Object.keys(updates).length > 0) {
    await db.ref().update(updates);
    for (const { entry, cachedTags } of wlResults.filter(r => r.cachedTags)) {
      await matchQuestionByTags(entry.id, cachedTags);
    }
  }

  if (remaining.length === 0) {
    for (const item of semanticCachedQuestions) {
      console.log(`[QMatch][SemanticCache] calling matchQuestionByTags for ${item.entry.id}`);
      await matchQuestionByTags(item.entry.id, item.tags);
    }
    console.log('[Questions] all resolved via cache');
    return;
  }

  // Step 3: AI pipeline
  const modelStatus = (await db.ref('config/model_status').once('value')).val() || {};
  let candidates = MODELS.filter(m => modelStatus[encodePath(m.name)] !== 'EXHAUSTED');
  if (candidates.length === 0) {
    console.warn('[Questions] all models EXHAUSTED');
    const lastReset = (await db.ref('config/last_reset').once('value')).val();
    if (lastReset && (Date.now() - lastReset) < 300000) { console.error('[Questions] aborting'); return; }
    await db.ref('config/last_reset').set(Date.now());
    await db.ref('config/model_status').set({});
    candidates = MODELS;
  }

  const allAcceptedQuestions = [];

  for (const model of candidates) {
    if (remaining.length === 0) break;
    console.log(`[Questions] model ${model.name} (${model.label}), ${remaining.length} entries`);

    const localMapping = new Map();
    const slimmed = remaining.map((e, i) => { const v = i.toString(); localMapping.set(v, e); return { id: v, text: e.text }; });

    let searchContext = '';
    if (model.useWebFetch) {
      const sr = await Promise.all(slimmed.map(async (se) => {
        try {
          const snippets = await searchOnSerper(se.text);
          return `Entry "${se.id}": "${se.text}"\n搜尋結果：\n${snippets}`;
        } catch (err) {
          return `Entry "${se.id}": "${se.text}"\n搜尋結果：（搜尋失敗）`;
        }
      }));
      searchContext = '以下是網路搜尋結果：\n\n' + sr.join('\n\n') + '\n\n';
    }

    const prompt = searchContext + `請判斷以下每筆資料是否為真實、有意義的提問描述。
判斷原則：
- 如果內容是具體、可理解的提問，請提取 4 個最能描述該問題的核心關鍵字（標籤應涵蓋該領域的具體名詞與相關概念詞彙）
- 如果內容是無意義內容，請將 status 設為 "REJECTED"
- 請仔細參考上述網路搜尋結果（若有提供）來協助判斷
- 遇到無法明確判斷時，寧可 REJECTED 也不要勉強給標籤
- 標籤必須使用與提問相同的語言（若描述為中文，標籤也必須是中文）

以 JSON Array 格式回傳，每個物件包含 id、tags 和 status 欄位。status 為 "ACTIVE"（接受）或 "REJECTED"（拒絕）。
資料：${JSON.stringify(slimmed)}`;

    try {
      const apiModel = model.useWebFetch ? { ...model, useSearch: false } : model;
      const { response, elapsed } = await generateContentWithRetry(apiModel, prompt);
      console.log(`[Questions] ${model.name} responded in ${elapsed}ms`);
      const text = response.text || '';
      if (!text) throw new Error('AI 回傳空內容');

      let parsed;
      try { parsed = JSON.parse(text); } catch {
        const m = text.match(/\[[\s\S]*\]/);
        if (m) parsed = JSON.parse(m[0]); else throw new Error('無法解析 AI 回應為 JSON');
      }

      const newRejected = [];
      for (const item of parsed) {
        const entry = localMapping.get(item.id?.toString());
        if (!entry) continue;
        const isReject = item.status === 'REJECTED' || (Array.isArray(item.tags) && item.tags.includes('REJECT'));
        const tags = Array.isArray(item.tags) ? item.tags.filter(t => t !== 'REJECT').slice(0, 4) : [];

        if (isReject) { newRejected.push(entry); } else {
          allAcceptedQuestions.push({ entry, tags });
          updates[`questions/${entry.id}/tags`] = tags;
          updates[`questions/${entry.id}/text`] = entry.text;
          updates[`questions/${entry.id}/authorId`] = entry.userId;
          updates[`questions/${entry.id}/timestamp`] = entry.timestamp;
          updates[`questions/${entry.id}/status`] = 'matching';
          updates[`questions/${entry.id}/expertId`] = '';
          updates[`tags_whitelist/${encodePath(entry.text)}/tags`] = tags;
          updates[`tags_whitelist/${encodePath(entry.text)}/source`] = 'llm';
          if (entry._precomputedEmbedding) {
            updates[`tags_whitelist/${encodePath(entry.text)}/embedding`] = entry._precomputedEmbedding;
          }
          updates[`pending_questions/${entry.id}`] = null;
        }
      }
      remaining = newRejected;
    } catch (err) {
      console.error(`[Questions] model ${model.name} failed:`, err.message);
      if (err.status === 429 || (err.message && err.message.includes('RESOURCE_EXHAUSTED'))) {
        try { await db.ref(`config/model_status/${encodePath(model.name)}`).set('EXHAUSTED'); } catch (_) {}
      }
    }
  }

  if (remaining.length > 0) {
    for (const entry of remaining) {
      updates[`questions/${entry.id}/status`] = 'cancelled';
      updates[`tags_blacklist/${encodePath(entry.text)}`] = true;
      updates[`pending_questions/${entry.id}`] = null;
    }
  }

  await db.ref().update(updates);
  for (const item of semanticCachedQuestions) {
    console.log(`[QMatch][SemanticCache] calling matchQuestionByTags for ${item.entry.id}`);
    await matchQuestionByTags(item.entry.id, item.tags);
  }
  for (const item of allAcceptedQuestions) {
    console.log(`[QMatch] calling matchQuestionByTags for ${item.entry.id}`);
    await matchQuestionByTags(item.entry.id, item.tags);
  }
  console.log('[Questions] complete');
}

exports.processSkillsOnWrite = onValueWritten(
  {
    ref: '/pending_skills/{skillId}',
    secrets: [geminiApiKey, serperApiKey],
  },
  async (event) => {
    if (event.data.before.exists() || !event.data.after.exists()) return;
    console.log('[Trigger] new pending_skills entry, processing...');
    try { await processSkills(); } catch (err) { console.error('processSkills failed:', err.message, err.stack); }
  }
);

exports.processQuestionsOnWrite = onValueWritten(
  {
    ref: '/pending_questions/{questionId}',
    secrets: [geminiApiKey, serperApiKey],
  },
  async (event) => {
    if (event.data.before.exists() || !event.data.after.exists()) return;
    console.log('[Trigger] new pending_questions entry, processing...');
    try { await processQuestions(); } catch (err) { console.error('processQuestions failed:', err.message, err.stack); }
  }
);

// ─────────────────────────────────────────────
// 提問端：標籤生成 + Tag 相似度配對
// ─────────────────────────────────────────────

function computeTagJaccard(tagsA, tagsB) {
  const setA = new Set(tagsA.filter(t => t && typeof t === 'string'));
  const setB = new Set(tagsB.filter(t => t && typeof t === 'string'));
  if (setA.size === 0 || setB.size === 0) return 0;
  const intersect = new Set([...setA].filter(t => setB.has(t)));
  const union = new Set([...setA, ...setB]);
  return intersect.size / union.size;
}

const MATCH_TAG_THRESHOLD = 0.15;
const TAG_FALLBACK_THRESHOLD = 0.7;
const HYBRID_TAG_WEIGHT = 0.3;
const HYBRID_EMBED_WEIGHT = 0.7;
const HYBRID_THRESHOLD = 0.25;

function getBigrams(text) {
  const clean = String(text).replace(/\s+/g, '');
  if (clean.length < 2) return new Set([clean]);
  const set = new Set();
  for (let i = 0; i < clean.length - 1; i++) set.add(clean.substring(i, i + 2));
  return set;
}

function computeTextJaccard(textA, textB) {
  const setA = getBigrams(textA);
  const setB = getBigrams(textB);
  if (setA.size === 0 || setB.size === 0) return 0;
  const intersect = new Set([...setA].filter(b => setB.has(b)));
  const union = new Set([...setA, ...setB]);
  return intersect.size / union.size;
}

async function getEmbedding(text) {
  const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent?key=${geminiApiKey.value()}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ model: 'models/gemini-embedding-2', content: { parts: [{ text }] } }),
    signal: AbortSignal.timeout(10000),
  });
  if (!res.ok) { const errBody = await res.text(); throw new Error(`Embedding API error ${res.status}: ${errBody}`); }
  const data = await res.json();
  return data.embedding?.values;
}

// 語意快取閾值：cosine similarity 超過此值視為「同語意」，直接複用標籤
const SEMANTIC_CACHE_THRESHOLD = 0.75;
const SEMANTIC_CACHE_MAX_ENTRIES = 500; // 最多比對筆數，避免資料庫太大時效能惡化

/**
 * 在 tags_whitelist 中尋找語意上最接近 text 的條目，若相似度 >= 閾值則回傳其 tags。
 * 同時會將 text 也寫入 whitelist（複用找到的 tags）以加速未來查詢。
 * @param {string} text 要比對的原始文字
 * @param {number[]} textEmbedding 已算好的 embedding（避免重複呼叫 API）
 * @param {object} updates 現有的 Firebase batch updates 物件（會直接追加寫入）
 * @returns {string[]|null} 命中時回傳 tags 陣列；未命中回傳 null
 */
async function findSemanticCachedTags(text, textEmbedding, updates) {
  if (!textEmbedding) return null;
  try {
    const wlSnap = await db.ref('tags_whitelist').orderByKey().limitToLast(SEMANTIC_CACHE_MAX_ENTRIES).once('value');
    if (!wlSnap.exists()) return null;
    let bestSim = 0;
    let bestTags = null;
    wlSnap.forEach(child => {
      const val = child.val();
      const embedding = val && val.embedding;
      const tags = val && val.tags;
      if (!Array.isArray(embedding) || !Array.isArray(tags) || tags.length === 0) return;
      const sim = computeCosineSimilarity(textEmbedding, embedding);
      if (sim > bestSim) { bestSim = sim; bestTags = tags; }
    });
    if (bestSim >= SEMANTIC_CACHE_THRESHOLD && bestTags) {
      console.log(`[SemanticCache] hit sim=${bestSim.toFixed(3)} for "${text.substring(0, 30)}..."`);
      return bestTags;
    }
    return null;
  } catch (err) {
    console.warn('[SemanticCache] lookup failed:', err.message);
    return null;
  }
}

function computeCosineSimilarity(vecA, vecB) {
  if (!vecA || !vecB || vecA.length !== vecB.length || vecA.length === 0) return 0;
  let dot = 0, normA = 0, normB = 0;
  for (let i = 0; i < vecA.length; i++) {
    dot += vecA[i] * vecB[i];
    normA += vecA[i] * vecA[i];
    normB += vecB[i] * vecB[i];
  }
  normA = Math.sqrt(normA);
  normB = Math.sqrt(normB);
  if (normA === 0 || normB === 0) return 0;
  return dot / (normA * normB);
}

async function matchQuestionByTags(questionId, questionTags) {
  const qSnap = await db.ref(`questions/${questionId}`).once('value');
  if (!qSnap.exists()) return false;
  const currentStatus = qSnap.child('status').val();
  if (currentStatus !== 'matching') return false;
  const questionText = qSnap.child('text').val();

  const rejectedExperts = new Set();
  qSnap.child('rejectedExperts').forEach(child => rejectedExperts.add(child.key));

  const expSnap = await db.ref('active_experiences')
    .orderByChild('status').equalTo('active').once('value');

  let qEmbedding = null;
  try {
    qEmbedding = await getEmbedding(questionText);
  } catch (_) {}

  const candidates = [];

  for (const child of Object.values(expSnap.val() || {})) {
    if (!child || !child.isOnline || rejectedExperts.has(child.authorId)) continue;
    const exp = child;

    const solSnap = await db.ref(`solutions/${exp.authorId}`)
      .orderByChild('status').equalTo('ACTIVE').once('value');
    const expertTags = new Set();
    solSnap.forEach(sol => {
      const tags = sol.child('tags').val();
      if (Array.isArray(tags)) tags.forEach(t => expertTags.add(t));
    });

    const tagJaccard = computeTagJaccard(questionTags, [...expertTags]);
    const textJaccard = questionText ? computeTextJaccard(questionText, exp.text || '') : 0;
    const embedSim = (qEmbedding && exp.embedding) ? computeCosineSimilarity(qEmbedding, exp.embedding) : 0;

    let score;
    let threshold;
    if (tagJaccard > 0 && embedSim > 0) {
      score = HYBRID_TAG_WEIGHT * tagJaccard + HYBRID_EMBED_WEIGHT * embedSim;
      threshold = HYBRID_THRESHOLD;
    } else if (tagJaccard > 0) {
      score = tagJaccard;
      threshold = MATCH_TAG_THRESHOLD;
    } else if (embedSim > TAG_FALLBACK_THRESHOLD) {
      score = embedSim;
      threshold = TAG_FALLBACK_THRESHOLD;
    } else {
      continue;
    }
    candidates.push({ exp, tagJaccard, textJaccard, embedSim, score, threshold });
  }

  candidates.sort((a, b) => b.score - a.score || b.exp.timestamp - a.exp.timestamp);
  const best = candidates.find(c => c.score >= c.threshold);

  if (best) {
    await db.ref(`questions/${questionId}`).update({
      expertId: best.exp.authorId,
      status: 'pending_acceptance',
      matchedExpText: best.exp.text,
      matchedExpTimestamp: best.exp.timestamp,
    });
    console.log(`[QMatch] ${questionId} → expert ${best.exp.authorId} (score=${best.score.toFixed(3)}, tagJ=${best.tagJaccard.toFixed(3)}, textJ=${best.textJaccard.toFixed(3)}, embed=${best.embedSim.toFixed(3)})`);
    return true;
  }

  console.log(`[QMatch] ${questionId}: no candidates above threshold (best=${candidates.length > 0 ? `score=${candidates[0].score.toFixed(3)} threshold=${candidates[0].threshold}` : 'N/A'})`);
  return false;
}


