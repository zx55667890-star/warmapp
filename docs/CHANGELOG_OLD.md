# 專案進度紀錄 (封存 Round 16 ~ 17)

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
