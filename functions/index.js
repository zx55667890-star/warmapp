const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { GoogleGenerativeAI } = require('@google/generative-ai');
const { defineSecret } = require('firebase-functions/params');

admin.initializeApp();
const db = admin.database();

const geminiApiKey = defineSecret('GEMINI_API_KEY');

const BATCH_LIMIT = 20;

const MODELS = [
  { name: 'gemini-3.1-flash-lite', label: 'PRIMARY' },
  { name: 'gemini-3.5-flash', label: 'FALLBACK_1' },
  { name: 'gemini-3-flash-preview', label: 'FALLBACK_2' },
  { name: 'gemini-2.5-flash', label: 'FALLBACK_3' },
  { name: 'gemini-2.5-flash-lite', label: 'FALLBACK_4' },
];

async function generateContentWithRetry(modelName, prompt, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      const genAI = new GoogleGenerativeAI(geminiApiKey.value());
      const model = genAI.getGenerativeModel({ model: modelName });
      return await model.generateContent(prompt);
    } catch (err) {
      const isQuotaError = err.status === 429 ||
                          (err.message && err.message.includes('RESOURCE_EXHAUSTED'));
      if (isQuotaError && i < retries - 1) {
        const delay = Math.pow(2, i) * 2000;
        console.warn(`Quota error for ${modelName}, retrying in ${delay}ms... (attempt ${i + 1})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        continue;
      }
      throw err;
    }
  }
}

exports.batchProcessPendingSkills = functions.runWith({ secrets: [geminiApiKey] })
  .pubsub.schedule('every 1 minutes')
  .onRun(async () => {
    const snapshot = await db
      .ref('pending_skills')
      .orderByChild('timestamp')
      .limitToFirst(BATCH_LIMIT)
      .once('value');

    const entries = [];
    snapshot.forEach((child) => {
      entries.push({ id: child.key, ...child.val() });
    });

    if (entries.length === 0) return null;

    const statusSnapshot = await db.ref('config/model_status').once('value');
    const modelStatus = statusSnapshot.val() || {};

    let candidates = MODELS.filter(m => modelStatus[m.name] !== 'EXHAUSTED');
    if (candidates.length === 0) {
      console.warn('All models EXHAUSTED, resetting status');
      await db.ref('config/model_status').set({});
      candidates = MODELS;
    }

    let lastError = null;
    for (const model of candidates) {
      console.log(`Processing ${entries.length} skills with model: ${model.name} (${model.label})`);

      const prompt = `請為以下每筆資料提取 4 個最核心的關鍵字標籤。
遇到無意義的胡言亂語、鍵盤亂打、重複的符號、或完全無法對應到任何實際場景的內容，請將 tags 設為 ["REJECT"]。

以 JSON Array 格式回傳，每個物件包含 id 和 tags 欄位。
範例格式：
[{"id": "-ABC123", "tags": ["Python", "資料分析", "機器學習", "爬蟲"]}]

資料：${JSON.stringify(entries.map((e) => ({ id: e.id, text: e.text })))}`;

      try {
        const result = await generateContentWithRetry(model.name, prompt);
        const text = result.response.text();

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

        const updates = {};
        for (const item of parsed) {
          const entry = entries.find((e) => e.id === item.id);
          if (!entry) continue;

          const skillRef = `solutions/${entry.userId}/${item.id}`;
          const isReject = item.tags && Array.isArray(item.tags) && item.tags.includes('REJECT');

          if (isReject) {
            updates[`${skillRef}/status`] = 'REJECTED';
            updates[`${skillRef}/tags`] = [];
            updates[`tags_blacklist/${entry.text}`] = true;
          } else {
            const tags = Array.isArray(item.tags) ? item.tags.slice(0, 4) : [];
            updates[`${skillRef}/status`] = 'ACTIVE';
            updates[`${skillRef}/tags`] = tags;
            updates[`tags_whitelist/${entry.text}/tags`] = tags;
          }
          updates[`pending_skills/${item.id}`] = null;
        }

        await db.ref().update(updates);
        console.log(`Successfully processed batch using ${model.name}`);
        return null;

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
    return null;
  });
