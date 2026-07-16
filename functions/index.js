const admin = require('firebase-admin');
const { GoogleGenAI, Type } = require('@google/genai');
const { defineSecret } = require('firebase-functions/params');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const { getDatabase } = require('firebase-admin/database');

admin.initializeApp();
const db = getDatabase();

const geminiApiKey = defineSecret('GEMINI_API_KEY');

function encodePath(text) {
  return Buffer.from(text, 'utf8').toString('base64url');
}

const BATCH_LIMIT = 20;
const PROCESSING_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

const MODELS = [
  // PRIMARY：最高 RPD 主力，default minimal thinking，不搜尋
  { name: 'gemini-3.1-flash-lite', label: 'PRIMARY', useSearch: false },

  // FALLBACK：開啟搜尋，需要保留思考能力
  // 順序依速度/可用性排列，429 高的放最後
  { name: 'gemini-3-flash-preview', label: 'FALLBACK_1', useSearch: true },
  { name: 'gemini-2.5-flash-lite',  label: 'FALLBACK_2', useSearch: true },
  { name: 'gemini-2.5-flash',       label: 'FALLBACK_3', useSearch: true },
  { name: 'gemini-3.5-flash',       label: 'FALLBACK_4', useSearch: true },
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
    schedule: 'every 5 minutes',
    secrets: [geminiApiKey],
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
      .limitToFirst(BATCH_LIMIT)
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

      const prompt = `請判斷以下每筆資料是否為真實、有意義的專業技能描述。
判斷原則：
- 如果內容是具體、可理解的專業技能，請提取 4 個最核心的關鍵字標籤
- 如果內容是無意義的胡言亂語或無法對應到真實場景，請將 status 設為 "REJECTED"
- 如果你對該技能描述感到猶豫，或者該名詞看起來很新但不確定是否真實，請回傳 REJECTED，交由後續的系統進行查證
- 遇到無法明確判斷時，寧可 REJECTED 也不要勉強給標籤
- 標籤必須使用與技能描述相同的語言（若描述為中文，標籤也必須是中文）

 以 JSON Array 格式回傳，每個物件包含 id、tags 和 status 欄位。status 為 "ACTIVE"（接受）或 "REJECTED"（拒絕）。
資料：${JSON.stringify(slimmedEntries)}`;

      try {
        const { response, elapsed } = await generateContentWithRetry(model, prompt);
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
