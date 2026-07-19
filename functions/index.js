const admin = require('firebase-admin');
const { GoogleGenAI, Type } = require('@google/genai');
const { defineSecret } = require('firebase-functions/params');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const { getDatabase } = require('firebase-admin/database');

admin.initializeApp();
const db = getDatabase();

const geminiApiKey = defineSecret('GEMINI_API_KEY');
const serperApiKey = defineSecret('SERPER_API_KEY');

function encodePath(text) {
  return Buffer.from(text, 'utf8').toString('base64url');
}

const BATCH_LIMIT_SKILLS = 50;
const BATCH_LIMIT_QUESTIONS = 50;
const PROCESSING_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

const MODELS = [
  // PRIMARY：最高 RPD 主力，不搜尋，快速濾掉已知技能
  { name: 'gemini-3.1-flash-lite', label: 'PRIMARY', useSearch: false },

  // FALLBACK_1：同 PRIMARY 但啟用 Serper 外部搜尋
  { name: 'gemini-3.1-flash-lite', label: 'FALLBACK_1', useWebFetch: true },

  // FALLBACK_2~3：內建 googleSearch（Gen2 支援）
  { name: 'gemini-2.5-flash-lite', label: 'FALLBACK_2', useSearch: true },
  { name: 'gemini-2.5-flash',      label: 'FALLBACK_3', useSearch: true },

  // FALLBACK_4~5：Gen3 模型 + Serper + minimal thinking
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

  // Step 2: whitelist
  const wlResults = await Promise.all(aiEntries.map(async (entry) => {
    const wl = await db.ref(`tags_whitelist/${encodePath(entry.text)}/tags`).once('value');
    const tags = wl.val();
    return { entry, cachedTags: Array.isArray(tags) && tags.length > 0 ? tags : null };
  }));

  let remaining = [];

  for (const { entry, cachedTags } of wlResults) {
    if (cachedTags) {
      updates[`solutions/${entry.userId}/${entry.id}/status`] = 'ACTIVE';
      updates[`solutions/${entry.userId}/${entry.id}/tags`] = cachedTags;
      updates[`pending_skills/${entry.id}`] = null;
      const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
      t.hasActive = true;
    } else {
      remaining.push(entry);
    }
  }

  if (remaining.length === 0) {
    await db.ref().update(updates);
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
          updates[`pending_skills/${entry.id}`] = null;
          t.hasActive = true;
        }
      }
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

  // Step 2: whitelist
  const wlResults = await Promise.all(aiEntries.map(async (entry) => {
    const wl = await db.ref(`tags_whitelist/${encodePath(entry.text)}/tags`).once('value');
    const cached = wl.val();
    return { entry, cachedTags: Array.isArray(cached) && cached.length > 0 ? cached : null };
  }));

  let remaining = [];

  for (const { entry, cachedTags } of wlResults) {
    if (cachedTags) {
      updates[`questions/${entry.id}/tags`] = cachedTags;
      updates[`pending_questions/${entry.id}`] = null;
    } else {
      remaining.push(entry);
    }
  }

  if (Object.keys(updates).length > 0) {
    await db.ref().update(updates);
    for (const { entry, cachedTags } of wlResults.filter(r => r.cachedTags)) {
      await matchQuestionByTags(entry.id, cachedTags);
    }
  }

  if (remaining.length === 0) { console.log('[Questions] all resolved via cache'); return; }

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
          updates[`tags_whitelist/${encodePath(entry.text)}/tags`] = tags;
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
  for (const item of allAcceptedQuestions) {
    await matchQuestionByTags(item.entry.id, item.tags);
  }
  console.log('[Questions] complete');
}

exports.batchProcess = onSchedule(
  {
    schedule: '* * * * *',
    secrets: [geminiApiKey, serperApiKey],
    minInstances: 1,
  },
  async () => {
    console.log('batchProcess started (skills → questions)');
    try { await processSkills(); } catch (err) { console.error('processSkills failed:', err.message, err.stack); }
    try { await processQuestions(); } catch (err) { console.error('processQuestions failed:', err.message, err.stack); }
    console.log('batchProcess complete');
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

  const matchPromises = [];
  expSnap.forEach(child => {
    const exp = child.val();
    if (!exp || !exp.isOnline || rejectedExperts.has(exp.authorId)) return;
    matchPromises.push((async () => {
      const solSnap = await db.ref(`solutions/${exp.authorId}`)
        .orderByChild('status').equalTo('ACTIVE').once('value');
      const expertTags = new Set();
      solSnap.forEach(sol => {
        const tags = sol.child('tags').val();
        if (Array.isArray(tags)) tags.forEach(t => expertTags.add(t));
      });
      const tagJaccard = computeTagJaccard(questionTags, [...expertTags]);
      const textJaccard = questionText ? computeTextJaccard(questionText, exp.text || '') : 0;
      const combined = Math.max(tagJaccard, textJaccard);
      return { exp, jaccard: combined, tagJaccard, textJaccard };
    })());
  });

  const matches = (await Promise.all(matchPromises))
    .filter(m => m.jaccard >= MATCH_TAG_THRESHOLD)
    .sort((a, b) => b.jaccard - a.jaccard || b.exp.timestamp - a.exp.timestamp);

  if (matches.length > 0) {
    const best = matches[0];
    await db.ref(`questions/${questionId}`).update({
      expertId: best.exp.authorId,
      status: 'taken',
      matchedExpText: best.exp.text,
      matchedExpTimestamp: best.exp.timestamp,
    });
    console.log(`[QMatch] ${questionId} → expert ${best.exp.authorId} (combined=${best.jaccard.toFixed(3)}, tagJ=${best.tagJaccard.toFixed(3)}, textJ=${best.textJaccard.toFixed(3)})`);
    return true;
  }
  const details = matches.length > 0 ? `best=${matches[0].jaccard.toFixed(3)}` : 'no candidates above threshold';
  console.log(`[QMatch] ${questionId}: ${details} (tagJ_best=${matches.length > 0 ? matches[0].tagJaccard.toFixed(3) : 'N/A'})`);
  return false;
}


