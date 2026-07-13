const functions = require('firebase-functions');
const admin = require('firebase-admin');
const { GoogleGenerativeAI } = require('@google/generative-ai');

admin.initializeApp();
const db = admin.database();

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

const BATCH_LIMIT = 20;
const MODEL_NAME = 'gemini-3.1-flash-lite';

exports.batchProcessPendingSkills = functions.pubsub
  .schedule('every 5 minutes')
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

    if (entries.length === 0) {
      console.log('No pending skills to process');
      return null;
    }

    console.log(`Processing ${entries.length} pending skills`);

    const prompt = `請為以下每筆資料提取 4 個最核心的關鍵字標籤。
遇到無意義的胡言亂語、鍵盤亂打、重複的符號、或完全無法對應到任何實際場景的內容，請將 tags 設為 ["REJECT"]。

以 JSON Array 格式回傳，每個物件包含 id 和 tags 欄位。
範例格式：
[{"id": "-ABC123", "tags": ["Python", "資料分析", "機器學習", "爬蟲"]}]

資料：${JSON.stringify(entries.map((e) => ({ id: e.id, text: e.text })))}`;

    try {
      const model = genAI.getGenerativeModel({
        model: MODEL_NAME,
      });

      const result = await model.generateContent(prompt);
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
      const batch = [];

      for (const item of parsed) {
        const entry = entries.find((e) => e.id === item.id);
        if (!entry) continue;

        const skillRef = `solutions/${entry.userId}/${item.id}`;
        const isReject =
          item.tags &&
          Array.isArray(item.tags) &&
          item.tags.includes('REJECT');

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
        batch.push(item.id);
      }

      await db.ref().update(updates);
      console.log(`Processed ${batch.length} skills successfully`);
      return null;
    } catch (err) {
      console.error('Batch processing failed:', err);
      return null;
    }
  });
