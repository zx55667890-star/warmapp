# CHANGELOG.md — 更新紀錄

> Round 17 以前的紀錄已搬移至 [CHANGELOG_OLD.md](CHANGELOG_OLD.md)


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

