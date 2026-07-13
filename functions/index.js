const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { GoogleGenerativeAI } = require('@google/generative-ai');

admin.initializeApp();
const db = admin.database();

const genAI = new GoogleGenerativeAI(functions.config().gemini.api_key);

const BATCH_LIMIT = 20;

// 設定模型優先級
const PRIMARY_MODEL = 'gemini-3.1-flash-lite';
const FALLBACK_MODELS = [
  'gemini-3.5-flash',
  'gemini-3-flash-preview',
  'gemini-2.5-flash',
  'gemini-2.5-flash-lite'
];

// 指數退避重試邏輯
async function generateContentWithRetry(modelName, prompt, retries = 3) {
  for (let i = 0; i < retries; i++) {
    try {
      const model = genAI.getGenerativeModel({ model: modelName });
      return await model.generateContent(prompt);
    } catch (err) {
      const isQuotaError = err.status === 429 || 
                          (err.message && err.message.includes('RESOURCE_EXHAUSTED'));
      
      // 如果是配額錯誤且還有重試次數，則等待後重試
      if (isQuotaError && i < retries - 1) {
        const delay = Math.pow(2, i) * 2000; // 2s, 4s
        console.warn(`Quota error for ${modelName}, retrying in ${delay}ms... (attempt ${i + 1})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        continue;
      }
      throw err; // 若耗盡重試次數仍失敗，拋出錯誤
    }
  }
}

exports.batchProcessPendingSkills = functions.pubsub
  .schedule('every 1 minutes')
  .onRun(async () => {
    // 1. 讀取模型狀態與配置
    const statusSnapshot = await db.ref('config/model_status').once('value');
    const modelStatus = statusSnapshot.val() || {}; // { 'modelName': 'ACTIVE' | 'EXHAUSTED' }
    
    // 決定要嘗試的模型：優先選 PRIMARY，若已耗盡則選第一個 ACTIVE 的 FALLBACK
    let targetModel = PRIMARY_MODEL;
    if (modelStatus[PRIMARY_MODEL] === 'EXHAUSTED') {
      targetModel = FALLBACK_MODELS.find(m => modelStatus[m] !== 'EXHAUSTED') || PRIMARY_MODEL;
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

    if (entries.length === 0) return null;

    console.log(`Processing ${entries.length} skills with model: ${targetModel}`);

    const prompt = `請為以下每筆資料提取 4 個最核心的關鍵字標籤。
遇到無意義的胡言亂語、鍵盤亂打、重複的符號、或完全無法對應到任何實際場景的內容，請將 tags 設為 ["REJECT"]。

以 JSON Array 格式回傳，每個物件包含 id 和 tags 欄位。
範例格式：
[{"id": "-ABC123", "tags": ["Python", "資料分析", "機器學習", "爬蟲"]}]

資料：${JSON.stringify(entries.map((e) => ({ id: e.id, text: e.text })))}`;

    try {
      const result = await generateContentWithRetry(targetModel, prompt);
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
      console.log(`Successfully processed batch using ${targetModel}`);
      return null;

    } catch (err) {
      console.error(`Final failure for ${targetModel}:`, err);
      // 如果確定是 RPD 耗盡 (例如重試後依然失敗)，標記該模型為 EXHAUSTED
      if (err.status === 429 || (err.message && err.message.includes('RESOURCE_EXHAUSTED'))) {
        await db.ref(`config/model_status/${targetModel}`).set('EXHAUSTED');
        console.warn(`Model ${targetModel} marked as EXHAUSTED`);
      }
      return null;
    }
  });
