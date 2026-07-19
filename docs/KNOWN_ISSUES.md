# KNOWN_ISSUES.md

## ✅ 全部已解決

| # | 項目 | 處理 |
|---|---|---|
| 1 | `pending_skills` 孤立風險 | 新增 `releaseStuckProcessing()` 清除卡住的 processing 標記 |
| 2 | saveSkill 原子性喪失 | 改用 `updateChildren()` 原子寫入 solutions + pending_skills |
| 3 | `@StringRes` 編譯警告 | 加 `-Xannotation-default-target=param-property` |
| 4 | CF cooldown 10min → 5min | 縮短重置間隔 |
| 5 | Submission Lock 邊界 bug | rejectedCount 累積而非歸零，有 ACTIVE 時不鎖 |
| 6 | `pending_questions` 孤立風險 | 同 #1 加入 `releaseStuckProcessing()` |
| 7 | 兩個 CF 合併為一個 | 新 `exports.batchProcess` 依序處理 skills→questions |
| 8 | Firebase query 低機率不觸發 | 已改用 `addMessagesListener` 無 query 版本 |
| 9 | `healOrphanedPending` 報錯 | 修復空 cursor 時 `startAfter('')` 的 query 問題 |
| 10 | 跨檔案命名同步 | 已在 AGENTS.md 中有強制規則 |
| 11 | MediaPlayer crash 風險 | 加入 try-catch + safe `release()` helper |
| 12 | Google Sign In 未完成 | 整合 GoogleSignIn API（加 `@Suppress("DEPRECATION")`），完整上線 |
| 13 | `TrendingUp` icon 棄用 | 改用 `Icons.AutoMirrored.Outlined.TrendingUp` |
| 14 | GoogleSignIn deprecated | 加 `@Suppress("DEPRECATION")` 保留原 API（Credential Manager 相容性不足） |
| 15 | `combine` 三 flow 同步問題 | `observeTypingStatus`/`observeChatStatus` 加 `onStart { emit() }` |
| 16 | 前端亂碼檢測 | 增加 `MAX_CHAR_FREQUENCY` 單字頻率檢查，現有規則已達實用標準 |
| 17 | IDE 快取過期 | 已加入 AGENTS.md 文件，使用 `edit` 工具減少觸發 |
| 18 | 中文亂碼問題（4 檔案） | AuthViewModel/AiRepository/NetworkUtils/MessageList 修復 UTF-8 編碼損毀 |
| 19 | 全形 `＠` 信箱格式 | `AuthUtils.normalizeEmail()` 自動轉半形 |
| 20 | 單機切換配對 | 還原為兩台設備流程，移除 PendingAcceptance/SeekerConfirmDialog 等 |
