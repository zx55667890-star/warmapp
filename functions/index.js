const admin = require('firebase-admin');
const { GoogleGenAI } = require('@google/genai');
const { defineSecret } = require('firebase-functions/params');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const { getDatabase } = require('firebase-admin/database');

admin.initializeApp();
const db = getDatabase();

const geminiApiKey = defineSecret('GEMINI_API_KEY');

const BATCH_LIMIT = 20;
const PROCESSING_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

const MODELS = [
  { name: 'gemini-3.1-flash-lite', label: 'PRIMARY', thinkingConfig: { thinkingLevel: 'minimal' } },
  { name: 'gemini-2.5-flash', label: 'FALLBACK_1', thinkingConfig: { thinkingBudget: 0 } },
  { name: 'gemini-2.5-flash-lite', label: 'FALLBACK_2', thinkingConfig: { thinkingBudget: 0 } },
  { name: 'gemini-3.5-flash', label: 'FALLBACK_3', thinkingConfig: { thinkingLevel: 'minimal' } },
  { name: 'gemini-3-flash-preview', label: 'FALLBACK_4', thinkingConfig: { thinkingLevel: 'minimal' } },
];

async function generateContentWithRetry(modelName, prompt, thinkingConfig, retries = 3) {
  const ai = new GoogleGenAI({ apiKey: geminiApiKey.value() });
  for (let i = 0; i < retries; i++) {
    try {
      const startTime = Date.now();
      const response = await ai.models.generateContent({
        model: modelName,
        contents: prompt,
        config: {
          thinkingConfig,
          responseMimeType: 'application/json',
        },
      });
      return { response, elapsed: Date.now() - startTime };
    } catch (err) {
      const isRetryable = err.status === 429 || err.status === 503 ||
                          (err.message && err.message.includes('RESOURCE_EXHAUSTED'));
      if (isRetryable && i < retries - 1) {
        const delay = Math.pow(2, i) * 2000;
        console.warn(`Retryable error (${err.status}) for ${modelName}, retrying in ${delay}ms... (attempt ${i + 1})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        continue;
      }
      throw err;
    }
  }
}

exports.batchProcessPendingSkills = onSchedule(
  {
    schedule: 'every 1 minutes',
    secrets: [geminiApiKey],
    minInstances: 1,
  },
  async () => {
    console.log('batchProcessPendingSkills started');

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
      const blSnapshot = await db.ref(`tags_blacklist/${entry.text}`).once('value');
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
      const wlSnapshot = await db.ref(`tags_whitelist/${entry.text}/tags`).once('value');
      const tags = wlSnapshot.val();
      return { entry, cachedTags: Array.isArray(tags) && tags.length > 0 ? tags : null };
    });
    const whitelistResults = await Promise.all(whitelistChecks);

    const remainingEntries = [];

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

    let candidates = MODELS.filter(m => modelStatus[m.name] !== 'EXHAUSTED');
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
      console.log(`Processing ${remainingEntries.length} skills with model: ${model.name} (${model.label})`);

      const prompt = `請判斷以下每筆資料是否為真實、有意義的專業技能描述。

判斷原則：
- 如果內容是具體、可理解的專業技能（如「Python爬蟲開發」、「淘寶退貨流程」），請提取 4 個最核心的關鍵字標籤
- 如果內容是無意義的胡言亂語、隨機湊字、看似有詞但組合後無實際意義、或無法對應到任何真實場景，請將 tags 設為 ["REJECT"]
- 例如「車內適合吃新造型靠字」或「愛台破土問題該顧問」這類看似有詞但整體無意義的內容也應 REJECT
- 遇到無法明確判斷時，寧可 REJECT 也不要勉強給標籤

以 JSON Array 格式回傳，每個物件包含 id 和 tags 欄位。
範例格式：
[{"id": "-ABC123", "tags": ["Python", "資料分析", "機器學習", "爬蟲"]}]

資料：${JSON.stringify(remainingEntries.map((e) => ({ id: e.id, text: e.text })))}`;

      try {
        const { response, elapsed } = await generateContentWithRetry(model.name, prompt, model.thinkingConfig);
        console.log(`Model ${model.name} responded in ${elapsed}ms`);
        const text = response.text || '';
        if (!text) {
          throw new Error('AI 回傳空內容');
        }

        let parsed;
        try {
          parsed = JSON.parse(text);
        } catch {
          const jsonMatch = text.match(/\[[\s\S]*\]/);
          if (jsonMatch) {
            parsed = JSON.parse(jsonMatch[0]);
          } else {
            throw new Error('無法解析 AI 回應為 JSON');
          }
        }

        for (const item of parsed) {
          const entry = remainingEntries.find((e) => e.id === item.id);
          if (!entry) continue;

          const skillRef = `solutions/${entry.userId}/${item.id}`;
          const isReject = item.tags && Array.isArray(item.tags) && item.tags.includes('REJECT');
          const t = lockTrackers[entry.userId] || (lockTrackers[entry.userId] = { rejectedCount: 0, hasActive: false });

          if (isReject) {
            updates[`${skillRef}/status`] = 'REJECTED';
            updates[`${skillRef}/tags`] = [];
            updates[`tags_blacklist/${entry.text}`] = true;
            t.rejectedCount = (t.rejectedCount || 0) + 1;
          } else {
            const tags = Array.isArray(item.tags) ? item.tags.slice(0, 4) : [];
            updates[`${skillRef}/status`] = 'ACTIVE';
            updates[`${skillRef}/tags`] = tags;
            updates[`tags_whitelist/${entry.text}/tags`] = tags;
            t.hasActive = true;
          }
          updates[`pending_skills/${entry.id}`] = null;
        }

        // Apply submission lock based on consecutive rejection count
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
        console.log(`Successfully processed batch using ${model.name} (${elapsed}ms)`);
        return;

      } catch (err) {
        console.error(`Model ${model.name} (${model.label}) failed:`, err);
        lastError = err;

        if (err.status === 429 || (err.message && err.message.includes('RESOURCE_EXHAUSTED'))) {
          await db.ref(`config/model_status/${model.name}`).set('EXHAUSTED');
          console.warn(`Model ${model.name} marked as EXHAUSTED`);
        }
      }
    }

    console.error('All models exhausted. Last error:', lastError);
  }
);
