# CHANGELOG.md — 更新紀錄

> Round 17 以前的紀錄已搬移至 [CHANGELOG_OLD.md](CHANGELOG_OLD.md)


## 2026-07-22 — Round 21：輸入驗證修正（疊字誤判 + 相似重複檢查）

### 修改檔案
- `app/.../domain/expert/ExpertInputValidator.kt` — 移除過度嚴格的 singletons 檢查
- `app/.../ui/expert/ExpertViewModel.kt` — 新增 `computeTextSimilarity()` bigram Jaccard 重複比對

### 變更
- **相似內容檢測**：發布技能前除精確比對外，新增 bigram Jaccard ≥ 0.7 的相似度檢查，防止高度重複的技能進入知識庫
- **疊字誤判修正**：移除 `SINGLETONS_THRESHOLD` + `ADJACENT_CHECK_MIN_LENGTH` 判定路徑，正常中文疊字（慢慢、常常）不再被誤判為 GIBBERISH

### 修正
- 中文疊字（慢慢、常常、等等）觸發 singletons ≥ 3 判斷，被誤判為亂碼
- 近似的技能文字（如「分享怎麼分配薪水才不會月底吃」vs「怎麼分配薪水才不會月底吃」）未檢測為重複

### 部署
- APK build 成功

---

## 2026-07-22 — Round 20：同批次本地語意快取（localWhitelist）

### 修改檔案
- `functions/index.js` — `findSemanticCachedTags()` 新增 `localWhitelist` 參數；`processSkills()` + `processQuestions()` 新增 `localWhitelist` 陣列與 Step 3.5

### 變更
- **同批次本地語意快取**：同一批次（一次 `processSkills()` / `processQuestions()` 調用）內，後筆 entry 可透過 `localWhitelist` 比對前筆 LLM accept 的 tags，避免重複呼叫 LLM
- **Step 3.5**：LLM 全拒的 entry（通過所有 6 模型仍被 reject）再比對同批次已 accept 的條目，若 cosine similarity ≥ 0.75 則改為 accept，減少被黑名單誤殺

### 修正
- **跨 entry 快取失效**：同一批次處理的多筆 pending_skills/questions，因 whitelist 寫入在函數末尾才 `update()`，後筆無法看到前筆的 tags，導致語意快取 miss。新增 `localWhitelist` 陣列在記憶體中累積，`findSemanticCachedTags()` 同時檢查 DB + 本地快取

### 部署
- Cloud Function 部署成功（`processSkillsOnWrite` + `processQuestionsOnWrite`）

---

## 2026-07-21 — Round 19：sendQuestion 原子化 + CF defense-in-depth

### 修改檔案
- `app/.../data/repository/QuestionRepository.kt` — `sendQuestion()` 改為 `updateChildren()` 多路徑原子寫入
- `functions/index.js` — CF 三處 tags 寫入點同時補 `text`/`authorId`/`timestamp`/`status`/`expertId`

### 變更
- **sendQuestion 原子化**：原本 `setValue(questions)` → `addOnSuccessListener { setValue(pending_questions) }` 改為單一 `updateChildren()` 一次寫入兩個節點，消除 race condition
- **CF defense-in-depth**：無論是精確 whitelist、語意快取、AI pipeline，只要寫入 `questions/{id}/tags` 就同時補齊基本欄位，確保 CF 先處理時 question 節點也是完整的

### 修正
- **question 資料遺失**：CF 先抵達時用 `tags` 獨自建立了 question 節點（缺少 text/status），app 後續 `setValue()` 蓋掉 `tags`，導致資料庫只剩殘缺 entry

### 部署
- Cloud Function 部署成功（`processQuestionsOnWrite`）
- APK build 成功（15s incremental）

---

## 2026-07-20 — Round 18：語意快取（Semantic Whitelist Cache）

### 修改檔案
- `functions/index.js` — 新增 `findSemanticCachedTags()`、`SEMANTIC_CACHE_THRESHOLD`；`encodePath()` 補 `.trim()`；補 `orderByKey()`；`source` 欄位；修 early return bug
- `app/.../ui/auth/WelcomePanel.kt` — 移除「略過直接開始」按鈕及 `onSkip` 參數
- `app/.../ui/auth/AuthScreen.kt` — 移除 `onSkip` 參數及傳遞
- `app/.../ui/navigation/AppNavigation.kt` — 移除 `onSkip` 回呼（含 anonymous sign-in）
- `app/.../ui/auth/WelcomePanelTest.kt` — 移除 `skipClickTriggersCallback` 測試及所有 `onSkip = {}`

### 變更
- **語意快取**：`tags_whitelist` 新增 `embedding` 欄位（`gemini-embedding-2`），精確比對 miss 後計算新問題 embedding，跟 whitelist 所有條目比 cosine similarity，≥ threshold 直接複用 tags
- **threshold 0.75**：`SEMANTIC_CACHE_THRESHOLD = 0.88 → 0.75`（根據三題理財問題實測 cosine：0.76~0.82）
- **不寫入新 entry**：語意快取命中時僅回傳 tags，不再將新文字寫入 whitelist，避免 database 膨脹
- **`source` 欄位**：whitelist entry 新增 `source: "llm"`（模型算的）或 `source: "semantic_cache"`（快取繼承）
- `encodePath()` 補 `text.trim()` 避免前後空白導致不同 key

### 修正
- `findSemanticCachedTags()` 缺少 `orderByKey()` 導致 Admin SDK 查詢永遠回傳空
- 語意快取命中後 early return 前未呼叫 `matchQuestionByTags()`，導致被快取的問題無法配對專家

### 刪除
- WelcomePanel「略過直接開始」測試按鈕及整條 call chain（WelcomePanel → AuthScreen → AppNavigation → test）

### 部署
- Cloud Function 部署成功（`processSkillsOnWrite` + `processQuestionsOnWrite`）

