# CHANGELOG.md — 更新紀錄

> Round 15 以前的紀錄已搬移至 [CHANGELOG_OLD.md](CHANGELOG_OLD.md)


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

---

## 2026-07-19 — Round 17：DB triggered CF + Hybrid Matching + gemini-embedding-2

### 修改檔案
- `functions/index.js` — 刪除 `batchProcess` 排程；新增 `processSkillsOnWrite` / `processQuestionsOnWrite`（DB triggered）；`getEmbedding()` 改用 `gemini-embedding-2`；`matchQuestionByTags()` 重寫為 per-candidate hybrid scoring

### 變更
- **排程 → DB triggered**：`batchProcess`（`onSchedule`，every 1 min）刪除，改為 `processSkillsOnWrite` + `processQuestionsOnWrite`（`onValueWritten`），寫入 `pending_skills/{id}` / `pending_questions/{id}` 立即觸發
- **Embedding 模型遷移**：`gemini-embedding-001` → `gemini-embedding-2`（所有 skill 重新計算 embedding）
- **Hybrid 匹配演算法**：`matchQuestionByTags()` 從純 Jaccard 改為每個 candidate 獨立三層判斷：
  - tagJ > 0 → hybrid：`0.3 × tagJ + 0.7 × embedSim`，threshold 0.4
  - tagJ = 0 但 embedSim > 0.7 → 純 embedding 放行（同義詞支援）
  - 兩者都不符 → 跳過該 candidate
- **tags_whitelist 清空**（embedding 模型不相容，需重跑）
- **per-candidate threshold**：每個 candidate 有自己的 threshold（不再全域統一）

### 修正
- 匹配 `return` bug（寫在 `for` 迴圈內導致整支函式提前跳出，無 log）
- Cross-topic 誤配（睡眠 vs 理財）：tagJ=0 情境需高 threshold（0.7）才放行
- tag 同義詞無法配對（「理財」vs「理財規劃」）：embedding 降級通道補救

### 部署
- Cloud Function 部署成功（`processSkillsOnWrite` + `processQuestionsOnWrite`）

---

## 2026-07-19 — Round 16：全面 Bug 清除 + 單機配對還原 + 中文亂碼修復

### 修改檔案
- `functions/index.js` — 合併兩 CF 為 `batchProcess`；還原 `pending_acceptance`→`taken`；`releaseStuckProcessing()` 清理卡住標記；Submission Lock 跨批次累積 rejectedCount；cooldown 10min→5min；`healOrphanedPending` 空 cursor try-catch
- `data/Constants.kt` — `pending_acceptance` 狀態、`activeChatRoomId` 前綴常數
- `ui/seeker/SeekerViewModel.kt` — 還原單機配對、`activeChatRoomId` 前綴 `"ai_"`、`checkReconnection` 處理 `PENDING_ACCEPTANCE`
- `ui/seeker/AskQuestionScreen.kt` — `isPendingAcceptance` 狀態傳遞
- `ui/seeker/MatchingOverlay.kt` — 不同狀態文字
- `ui/expert/ExpertViewModel.kt` — `acceptGlobalAssignment()`、`startGlobalAssignListener` `LaunchedEffect(userId)`
- `ui/expert/ExpertScreen.kt` — feedback banner 位置修正
- `ui/expert/components/QuickLogCard.kt` — FeedbackBanner 參數 refactor
- `ui/navigation/AppNavigation.kt` — 聆聽器啟動改 `LaunchedEffect(userId)`
- `database.rules.json` — `questions` 節點加 `.read: "auth.uid !== null"`
- `domain/seeker/ObserveQuestionStatusUseCase.kt` — `QuestionStatus.PendingAcceptance`
- `domain/expert/ExpertInputValidator.kt` — bigram 檢測、`SKILL_UNLIKELY_CHARS`
- `data/repository/ExpertRepository.kt` — callbackFlow 無狀態化
- `app/.../ExpertViewModelTest.kt` — 測試案例擴充
- `ui/auth/AuthViewModel.kt` — 中文亂碼修復
- `ui/expert/AiRepository.kt` — 中文亂碼修復
- `util/NetworkUtils.kt` — 中文亂碼修復
- `ui/chat/MessageList.kt` — 中文亂碼修復
- `data/repository/AuthRepository.kt` — `@Suppress("DEPRECATION")` on GoogleSignIn
- `ui/common/AuthUtils.kt` — `normalizeEmail()` 全形 `＠`→半形

### 變更
- **還原單機配對流程** — CF 匹配後直接 `taken`，移除 `PendingAcceptance`/`ExpertAccepted` 狀態、`SeekerConfirmDialog`、「也設為經驗」按鈕
- **GoogleSignIn 保留原 API** — Credential Manager 相容性不足，加 `@Suppress("DEPRECATION")` 保留 `GoogleSignIn` 類別
- **`AuthUtils.normalizeEmail()`** — 全形 `＠` 自動轉半形 `@`
- **Cloud Function 合併** — `batchProcessPendingSkills` + `batchProcessPendingQuestions` 合併為單一 `batchProcess`，依序處理 skills→questions，消除 API 競爭

### 修正
- **中文亂碼修復（4 檔案）** — AuthViewModel/AiRepository/NetworkUtils/MessageList 的 UTF-8 中文字串誤存為 Latin-1 編碼
- **MediaPlayer crash** — try-catch + safe release helper
- **`combine` 三 flow 同步** — `onStart { emit(default) }`
- **`releaseStuckProcessing()`** — 清理卡住的 processing 標記（skills + questions）
- **Submission Lock 邊界** — 跨批次累積 rejectedCount
- **CF cooldown 10min→5min**
- **`companion object { combine(...) }` 初始值問題**

### Git
- 約 36+ 次提交（至此累計）

---