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

async function healOrphanedPending() {
  const cursorSnapshot = await db.ref('config/repair_cursor').once('value');
  const cursor = cursorSnapshot.val() || '';

  const usersSnapshot = await db.ref('users')
    .orderByKey()
    .startAfter(cursor)
    .limitToFirst(REPAIR_BATCH_SIZE)
    .once('value');

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

exports.batchProcessPendingSkills = onSchedule(
  {
    schedule: '* * * * *',
    secrets: [geminiApiKey, serperApiKey],
    minInstances: 1,
  },
  async () => {
    console.log('batchProcessPendingSkills started');
    try {

    try {
      await healOrphanedPending();
      console.log('Self-heal scan completed');
    } catch (err) {
      console.error('Self-heal scan failed, continuing main processing:', err.message);
    }

    const snapshot = await db
      .ref('pending_skills')
      .orderByChild('timestamp')
      .limitToFirst(BATCH_LIMIT_SKILLS)
      .once('value');

    const entries = [];
    snapshot.forEach((child) => {
      entries.push({ id: child.key, ...child.val() });
    });

    if (entries.length === 0) {
      console.log('No pending skills found');
      return;
    }

    // Atomically claim entries via transaction (race-condition-safe)
    const now = Date.now();
    const claimResults = await Promise.all(entries.map(async (entry) => {
      let claimed = false;
      await db.ref(`pending_skills/${entry.id}`).transaction((currentData) => {
        if (currentData === null) return currentData;
        if (currentData.processing && (now - currentData.processing) < PROCESSING_TIMEOUT_MS) {
          return;
        }
        currentData.processing = now;
        claimed = true;
        return currentData;
      });
      return { entry, claimed };
    }));

    const processableEntries = claimResults.filter(r => r.claimed).map(r => r.entry);

    if (processableEntries.length === 0) {
      console.log('All entries are currently being processed by another invocation');
      return;
    }

    console.log(`Claimed ${processableEntries.length} entries via atomic transaction`);

    // Step 1: Check blacklist for each entry in parallel
    const blacklistChecks = processableEntries.map(async (entry) => {
      const blSnapshot = await db.ref(`tags_blacklist/${encodePath(entry.text)}`).once('value');
      return { entry, isBlacklisted: blSnapshot.exists() };
    });
    const blacklistResults = await Promise.all(blacklistChecks);

    const updates = {};
    const aiEntries = [];

    // Track consecutive rejection count per user for submission lock
    const lockTrackers = {};

    for (const { entry, isBlacklisted } of blacklistResults) {
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

    // Step 2: Check whitelist for remaining entries in parallel
    const whitelistChecks = aiEntries.map(async (entry) => {
      const wlSnapshot = await db.ref(`tags_whitelist/${encodePath(entry.text)}/tags`).once('value');
      const tags = wlSnapshot.val();
      return { entry, cachedTags: Array.isArray(tags) && tags.length > 0 ? tags : null };
    });
    const whitelistResults = await Promise.all(whitelistChecks);

    let remainingEntries = [];

    for (const { entry, cachedTags } of whitelistResults) {
      if (cachedTags) {
        updates[`solutions/${entry.userId}/${entry.id}/status`] = 'ACTIVE';
        updates[`solutions/${entry.userId}/${entry.id}/tags`] = cachedTags;
        updates[`pending_skills/${entry.id}`] = null;
        const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });
        t.hasActive = true;
      } else {
        remainingEntries.push(entry);
      }
    }

    if (remainingEntries.length === 0) {
      await db.ref().update(updates);
      console.log(`All ${processableEntries.length} entries resolved via blacklist/whitelist cache, no AI call needed`);
      return;
    }

    // Normal mode: check model_status
    const statusSnapshot = await db.ref('config/model_status').once('value');
    const modelStatus = statusSnapshot.val() || {};

    let candidates = MODELS.filter(m => modelStatus[encodePath(m.name)] !== 'EXHAUSTED');
    if (candidates.length === 0) {
      console.warn('All models EXHAUSTED');
      const resetSnapshot = await db.ref('config/last_reset').once('value');
      const lastReset = resetSnapshot.val();
      const RESET_COOLDOWN_MS = 10 * 60 * 1000;
      if (lastReset && (Date.now() - lastReset) < RESET_COOLDOWN_MS) {
        console.error('Models exhausted and recently reset; aborting to avoid infinite retry loop');
        return;
      }
      await db.ref('config/last_reset').set(Date.now());
      await db.ref('config/model_status').set({});
      console.log('Model status reset after cooldown');
      candidates = MODELS;
    }

    let lastError = null;

    for (const model of candidates) {
      if (remainingEntries.length === 0) break;

      console.log(`Processing ${remainingEntries.length} skills with model: ${model.name} (${model.label})`);

      const localMapping = new Map();
      const slimmedEntries = remainingEntries.map((entry, index) => {
        const virtualId = index.toString();
        localMapping.set(virtualId, entry);
        return { id: virtualId, text: entry.text };
      });

      // 若 useWebFetch，先對每筆 entry 做 Serper 搜尋
      let searchContext = '';
      if (model.useWebFetch) {
        const searchResults = await Promise.all(slimmedEntries.map(async (se) => {
          try {
            const snippets = await searchOnSerper(se.text);
            return `Entry "${se.id}": "${se.text}"\n搜尋結果：\n${snippets}`;
          } catch (err) {
            console.warn(`Search failed for "${se.text}": ${err.message}`);
            return `Entry "${se.id}": "${se.text}"\n搜尋結果：（搜尋失敗）`;
          }
        }));
        searchContext = '以下是網路搜尋結果，請仔細參考這些資訊來輔助判斷技能的真實性：\n\n' + searchResults.join('\n\n') + '\n\n';
      }

      const prompt = searchContext + `請判斷以下每筆資料是否為真實、有意義的專業技能描述。
判斷原則：
- 如果內容是具體、可理解的專業技能，請提取 4 個最能描述該技能的核心關鍵字（例如具體名詞、工具或平台名稱，而非抽象分類詞如「跨境物流」「客服」等）
- 如果內容是無意義的胡言亂語或無法對應到真實場景，請將 status 設為 "REJECTED"
- 請仔細參考上述網路搜尋結果（若有提供）來協助判斷
- 遇到無法明確判斷時，寧可 REJECTED 也不要勉強給標籤
- 標籤必須使用與技能描述相同的語言（若描述為中文，標籤也必須是中文）

 以 JSON Array 格式回傳，每個物件包含 id、tags 和 status 欄位。status 為 "ACTIVE"（接受）或 "REJECTED"（拒絕）。
資料：${JSON.stringify(slimmedEntries)}`;

      try {
        const apiModel = model.useWebFetch ? { ...model, useSearch: false } : model;
        const { response, elapsed } = await generateContentWithRetry(apiModel, prompt);
        console.log(`Model ${model.name} responded in ${elapsed}ms`);
        const text = response.text || '';
        if (!text) throw new Error('AI 回傳空內容');

        let parsed;
        try {
          parsed = JSON.parse(text);
        } catch {
          const jsonMatch = text.match(/\[[\s\S]*\]/);
          if (jsonMatch) parsed = JSON.parse(jsonMatch[0]);
          else throw new Error('無法解析 AI 回應為 JSON');
        }

        const newlyRejectedEntries = [];
        const acceptedEntries = [];

        for (const item of parsed) {
          const entry = localMapping.get(item.id?.toString());
          if (!entry) continue;

          const isReject = item.status === 'REJECTED' ||
            (Array.isArray(item.tags) && item.tags.includes('REJECT'));
          const tags = Array.isArray(item.tags)
            ? item.tags.filter(t => t !== 'REJECT').slice(0, 4)
            : [];

          if (isReject) {
            newlyRejectedEntries.push(entry);
          } else {
            acceptedEntries.push({ text: entry.text, tags });
            const skillRef = `solutions/${entry.userId}/${entry.id}`;
            const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });

            updates[`${skillRef}/status`] = 'ACTIVE';
            updates[`${skillRef}/tags`] = tags;
            updates[`tags_whitelist/${encodePath(entry.text)}/tags`] = tags;
            updates[`pending_skills/${entry.id}`] = null;
            t.hasActive = true;
          }
        }

        console.log(`${model.label} accepted:`, JSON.stringify(acceptedEntries.map(e => e.text)));
        console.log(`${model.label} rejected:`, JSON.stringify(newlyRejectedEntries.map(e => e.text)));
        remainingEntries = newlyRejectedEntries;

      } catch (err) {
        console.error(`Model ${model.name} (${model.label}) failed:`, err);
        lastError = err;
        if (err.status === 429 || (err.message && err.message.includes('RESOURCE_EXHAUSTED'))) {
          try {
            await db.ref(`config/model_status/${encodePath(model.name)}`).set('EXHAUSTED');
          } catch (pathErr) {
            console.error(`Failed to mark ${model.name} as EXHAUSTED:`, pathErr);
          }
        }
      }
    }

    if (remainingEntries.length > 0) {
      console.log(`Final reject for ${remainingEntries.length} items:`, JSON.stringify(remainingEntries.map(e => e.text)));
      for (const entry of remainingEntries) {
        const skillRef = `solutions/${entry.userId}/${entry.id}`;
        const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });

        updates[`${skillRef}/status`] = 'REJECTED';
        updates[`${skillRef}/tags`] = [];
        updates[`tags_blacklist/${encodePath(entry.text)}`] = true;
        updates[`pending_skills/${entry.id}`] = null;
        t.rejectedCount = (t.rejectedCount || 0) + 1;
      }
    }

    for (const [uid, t] of Object.entries(lockTrackers)) {
      const finalCount = t.hasActive ? 0 : (t.rejectedCount || 0);
      updates[`users/${uid}/submissionLock/rejectedCount`] = finalCount;
      if (finalCount >= 3) {
        updates[`users/${uid}/submissionLock/lockedUntil`] = Date.now() + 86400000;
      } else {
        updates[`users/${uid}/submissionLock/lockedUntil`] = 0;
      }
    }

    await db.ref().update(updates);
    console.log('Batch process complete. Updated database.');
    } catch (err) {
      console.error('batchProcessPendingSkills failed:', err.message, err.stack);
    }
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
  if (currentStatus !== 'matching' && currentStatus !== 'pending_acceptance') return false;
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
      status: 'pending_acceptance',
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

exports.batchProcessPendingQuestions = onSchedule(
  {
    schedule: '* * * * *',
    secrets: [geminiApiKey, serperApiKey],
    minInstances: 1,
  },
  async () => {
    console.log('batchProcessPendingQuestions started');
    try {
      const snapshot = await db
        .ref('pending_questions')
        .orderByChild('timestamp')
        .limitToFirst(BATCH_LIMIT_QUESTIONS)
        .once('value');

      const entries = [];
      snapshot.forEach(child => entries.push({ id: child.key, ...child.val() }));
      if (entries.length === 0) { console.log('No pending questions found'); return; }

      const now = Date.now();
      const claimResults = await Promise.all(entries.map(async (entry) => {
        let claimed = false;
        await db.ref(`pending_questions/${entry.id}`).transaction((cur) => {
          if (cur === null) return cur;
          if (cur.processing && (now - cur.processing) < PROCESSING_TIMEOUT_MS) return;
          cur.processing = now;
          claimed = true;
          return cur;
        });
        return { entry, claimed };
      }));

      const processable = claimResults.filter(r => r.claimed).map(r => r.entry);
      if (processable.length === 0) { console.log('All pending questions already being processed'); return; }
      console.log(`Claimed ${processable.length} pending questions`);

      // Step 1: Blacklist check
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

      // Step 2: Whitelist check
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

      // Apply cached updates before AI
      if (Object.keys(updates).length > 0) {
        await db.ref().update(updates);
        // Run tag-based matching for whitelist-resolved questions
        for (const { entry, cachedTags } of wlResults.filter(r => r.cachedTags)) {
          await matchQuestionByTags(entry.id, cachedTags);
        }
      }

      if (remaining.length === 0) {
        console.log('All questions resolved via blacklist/whitelist cache');
        return;
      }

      // Step 3: AI 5-model analysis
      const statusSnapshot = await db.ref('config/model_status').once('value');
      const modelStatus = statusSnapshot.val() || {};
      let candidates = MODELS.filter(m => modelStatus[encodePath(m.name)] !== 'EXHAUSTED');
      if (candidates.length === 0) {
        console.warn('All models EXHAUSTED');
        const resetSnap = await db.ref('config/last_reset').once('value');
        const lastReset = resetSnap.val();
        if (lastReset && (Date.now() - lastReset) < 600000) { console.error('Models exhausted, aborting'); return; }
        await db.ref('config/last_reset').set(Date.now());
        await db.ref('config/model_status').set({});
        candidates = MODELS;
      }

      let lastError = null;
      const allAcceptedQuestions = [];

      for (const model of candidates) {
        if (remaining.length === 0) break;
        console.log(`Processing ${remaining.length} questions with model: ${model.name} (${model.label})`);

        const localMapping = new Map();
        const slimmed = remaining.map((entry, idx) => {
          const vid = idx.toString();
          localMapping.set(vid, entry);
          return { id: vid, text: entry.text };
        });

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
- 如果內容是具體、可理解的提問，請提取 4 個最能描述該問題的核心關鍵字（例如具體名詞、工具或平台名稱，而非抽象分類詞）
- 如果內容是無意義內容，請將 status 設為 "REJECTED"
- 請仔細參考上述網路搜尋結果（若有提供）來協助判斷
- 遇到無法明確判斷時，寧可 REJECTED 也不要勉強給標籤
- 標籤必須使用與提問相同的語言

以 JSON Array 格式回傳，每個物件包含 id、tags 和 status 欄位。status 為 "ACTIVE"（接受）或 "REJECTED"（拒絕）。
資料：${JSON.stringify(slimmed)}`;

        try {
          const apiModel = model.useWebFetch ? { ...model, useSearch: false } : model;
          const { response, elapsed } = await generateContentWithRetry(apiModel, prompt);
          console.log(`Model ${model.name} responded in ${elapsed}ms`);
          const text = response.text || '';
          if (!text) throw new Error('AI 回傳空內容');

          let parsed;
          try { parsed = JSON.parse(text); }
          catch {
            const match = text.match(/\[[\s\S]*\]/);
            if (match) parsed = JSON.parse(match[0]);
            else throw new Error('無法解析 AI 回應為 JSON');
          }

          const newlyRejected = [];
          const modelAccepted = [];

          for (const item of parsed) {
            const entry = localMapping.get(item.id?.toString());
            if (!entry) continue;

            const isReject = item.status === 'REJECTED' ||
              (Array.isArray(item.tags) && item.tags.includes('REJECT'));
            const tags = Array.isArray(item.tags)
              ? item.tags.filter(t => t !== 'REJECT').slice(0, 4)
              : [];

            if (isReject) {
              newlyRejected.push(entry);
            } else {
              modelAccepted.push({ entry, tags });
              allAcceptedQuestions.push({ entry, tags });
              const qRef = `questions/${entry.id}`;
              updates[`${qRef}/tags`] = tags;
              updates[`tags_whitelist/${encodePath(entry.text)}/tags`] = tags;
              updates[`pending_questions/${entry.id}`] = null;
            }
          }

          console.log(`${model.label} accepted ${parsed.length - newlyRejected.length}, rejected ${newlyRejected.length}`);
          remaining = newlyRejected;

        } catch (err) {
          console.error(`Model ${model.name} (${model.label}) failed:`, err);
          lastError = err;
          if (err.status === 429 || (err.message && err.message.includes('RESOURCE_EXHAUSTED'))) {
            try { await db.ref(`config/model_status/${encodePath(model.name)}`).set('EXHAUSTED'); }
            catch (pe) { console.error('Failed to mark model EXHAUSTED:', pe); }
          }
        }
      }

      // Final reject for remaining
      if (remaining.length > 0) {
        console.log(`Final reject for ${remaining.length} questions`);
        for (const entry of remaining) {
          updates[`questions/${entry.id}/status`] = 'cancelled';
          updates[`tags_blacklist/${encodePath(entry.text)}`] = true;
          updates[`pending_questions/${entry.id}`] = null;
        }
      }

      await db.ref().update(updates);
      console.log('Tag updates written.');

      // Step 4: Tag-based matching for AI-resolved questions
      for (const item of allAcceptedQuestions) {
        await matchQuestionByTags(item.entry.id, item.tags);
      }

      console.log('batchProcessPendingQuestions complete');
    } catch (err) {
      console.error('batchProcessPendingQuestions failed:', err.message, err.stack);
    }
  }
);
