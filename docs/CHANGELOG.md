# CHANGELOG.md — 更新紀錄

## 已解決問題一覽（合併自 KNOWN_ISSUES.md）

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

## 2026-07-19 — Round 15：修復匹配路徑不一致 + 專家經驗發佈 UI

### 修改檔案
- `functions/index.js` — `matchQuestionByTags()` 讀取路徑 `experiences` → `active_experiences`
- `data/repository/ExpertRepository.kt` — `expertId` → `authorId` 欄位名稱修正
- `ui/expert/components/QuickLogCard.kt` — 新增「也設為配對經驗」按鈕
- `ui/expert/ExpertScreen.kt` — 串接 `onPublishExperience`
- `data/repository/MatchingRepository.kt` + `di/CoreModule.kt` — 路徑常數修正
- `database.rules.json` — `active_experiences` 補 `authorId`/`timestamp` 驗證 + `.indexOn: ["status"]`
- `strings.xml` — 新增 `expert_label_experience_sync` + `expert_toast_experience_published`

### 變更
- `matchQuestionByTags()` 讀取路徑對齊客戶端寫入路徑
- QuickLogCard 新增次級按鈕「也設為配對經驗」

### 部署
- Cloud Function 部署成功（`batchProcessPendingSkills` + `batchProcessPendingQuestions`）
- Database Rules 部署成功（`active_experiences` 規則生效）

---

## 2026-07-19 — Round 13 復原修復

### 修正
- 還原 17 個 `ui/chat/` 檔案到 Round 13 前狀態
- **ChatMediaSender** — `onPendingRemoved` 成功觸發、try-catch、`onScrollToBottom`
- **ChatViewModel** — `onMessageAdded` 實際插入、`filteredMessages` dedup、`isChatActive` Snackbar
- **ChatScrollManager** — 移除 `totalItems > 0` 檢查
- **MessageRepository** — `sendMessageWithFields` 加入 failure listener
- **ChatScreen** — 強制 `isDarkTheme = true`、背景色、statusBarsPadding、imePadding、SnackbarHost
- **ChatTopBar/QuestionBanner** — 硬編碼色碼改 AppColors
- **BubbleContent** — Pending spinner 暗背景移除、置中
- **Firebase query listener bug** — `orderByChild("timestamp").limitToLast(N)` 不觸發，改用直接 `addValueEventListener`
- **`initChat` 總是更新 `_userId`** — ViewModel 重用時 userId 不再卡在舊值
- **文字樂觀更新** — `sendMessage` 直接插入 `optimistic_` 暫存訊息
- **Observer try-catch** — 防止 collect block 崩潰殺死整體觀察協程

---

## 2026-07-18 — Round 14：提問端 AI 標籤生成管線 + 非同步 Tag 配對

### 新增
- **提問端 AI 標籤生成管線** — 仿照專家端 `pending_skills` 模式，新增 `pending_questions` 路徑 + `batchProcessPendingQuestions` Cloud Function
- **Tag 相似度配對** — Cloud Function 以 Jaccard 相似度（門檻 0.15）進行配對
- **`Constants.kt`** — 新增 `PENDING_QUESTIONS` 路徑常數、`FirebaseFields` 擴充

### 修改檔案
- `functions/index.js` — 新增 `batchProcessPendingQuestions` + `matchQuestionByTags()`
- `data/Constants.kt` — `PENDING_QUESTIONS` 路徑 + `FirebaseFields` 新欄位
- `data/repository/QuestionRepository.kt` — `sendQuestion()` 入隊 `pending_questions/{id}`
- `ui/seeker/SeekerViewModel.kt` — 移除 `matchCoordinator.matchAndAssignExpert()`
- `database.rules.json` — 新增 `pending_questions` 路徑 + `.indexOn`
- `ui/chat/ChatScreen.kt` — SnackbarHost 修復
- `ui/chat/components/ChatTopBar.kt` — 硬編碼色碼改 AppColors
- 17 個 `ui/chat/` 檔案 — Round 13 復原 + 選擇性修復

### 變更
- 排程 `5min→1min`，批量上限 `20→50`
- `QuestionRepository.sendQuestion()` 同步寫入 `pending_questions/{id}`

### 已知問題
- `combine` 三 flow 同步問題 — 僅三者皆有新值時才 emit
- `orderByChild("timestamp")` query listener 不觸發 — 改用 direct listener

### 測試
- ✅ 提問「淘寶要怎麼樣從台灣退貨回去？」生成標籤 `["淘寶","退貨","台灣","物流"]`，PRIMARY 751ms

---

## 2026-07-18 — Round 13：全面 AppColors 主題化

### 變更
- **~120 處硬編碼色碼移除，~60 處 `isSystemInDarkTheme()` 砍掉，~10 處 emoji 改 Material Icon，~15 處新增動畫**
- 主題基底（3 檔案）— AppColors 色值調整、Theme 純深色 darkColorScheme、Type Nunito Sans
- 共用元件（7 檔案）— LoadingOverlay/ToastOverlay/CompactTextField/OfflineBanner 等全 AppColors
- 登入模組（5 檔案）— AuthScreen/WelcomePanel/LoginForm/ResetPasswordPanel/NicknameSettingsDialog
- 導航（2 檔案）— AppNavigation Scaffold Transparent
- 專家模組（6 檔案）— ExpertScreen/ExpertDialogs/QuickLogCard
- 提問者（11 檔案）— AskQuestionScreen/RoleSelectScreen/MatchingOverlay 等
- 聊天（12 檔案）— ChatTopBar/ChatBubble/BubbleContent 等
- 相機（6 檔案）+ 錄音（1 檔案）

### 修改檔案（36+ UI 檔案）

### Git
- 約 280+ 次提交
- 第 13 輪變更：36+ 個 UI 檔案

---

## 2026-07-17 — Round 12：主題系統重構 + DI 拆分

### 新增
- **Nunito Sans 字體** — `nunito_sans_regular.ttf` + `nunito_sans_bold.ttf`
- **6 個 DI module** — CoreModule, AuthModule, ChatModule, ExpertModule, SeekerModule, MediaModule

### 修改檔案
- `ui/theme/AppColors.kt` — 從 Color.kt 遷出獨立檔
- `ui/theme/Color.kt` — 合併進 AppColors.kt 後刪除
- `ui/theme/Theme.kt` — 純深色 darkColorScheme
- `ui/theme/Type.kt` — Nunito Sans 完整 14 級 Typography
- `di/CoreModule.kt`, `di/AuthModule.kt`, `di/ChatModule.kt`, `di/ExpertModule.kt`, `di/SeekerModule.kt`, `di/MediaModule.kt` — 新增
- `di/AppModule.kt` — 刪除（94 行 → 6 modules 共 158 行）
- `ui/MainActivity.kt` — KoinApplication 載入 6 modules
- `docs/MODULE_MAP.md` — DI 區塊更新
- 根目錄 `CHANGELOG_OLD.md`、`get_sha1.md` 搬入 `docs/`

### Git
- 約 53+ 次提交

---

## 2026-07-17 — Round 11：Serper 外部搜尋（避開 Gen3 Free Tier 限制）

### 新增
- `searchOnSerper()` 函式 — 調用 `https://google.serper.dev/search`
- `useWebFetch` 自訂旗標
- `SERPER_API_KEY` Firebase secret
- Gen3 thinking 語法支援 — `thinkingLevel`
- 批次測試按鈕（ExpertScreen 底部，20 筆冷門技能）
- Per-skill logging

### 修改檔案
- `functions/index.js` — Serper 搜尋整合、Model 陣列縮減為 4 個
- `ui/expert/ExpertScreen.kt` — 批次測試按鈕

### 模型測試結果（2026/7/17）
- `gemini-3.1-flash-lite` + Serper：731ms，2/2 接受 ✅
- `gemini-3-flash-preview` + Serper + thinkingLevel `low`：待測試

### Git
- 約 42+ 次提交

---

## 2026-07-16 — Round 10：AGP 升級 + ExpertViewModel 命名同步修復

### 修改檔案
- `gradle/libs.versions.toml` — AGP 9.2.1 → 9.3.0
- `ui/expert/ExpertViewModel.kt` — `publishErrorRes` → `publishFeedbackRes`
- `docs/` — API 文件更新

### 修正
- ExpertViewModel 命名未同步導致 IDE cascading 錯誤

---

## 2026-07-15 — Round 9：發布反饋改為 floating overlay

### 修改檔案
- `ui/expert/ExpertScreen.kt` — `onClearPublishError` → `onClearPublishFeedback`；Scaffold 包裹 Box；新增 floating overlay Card
- `ui/expert/components/QuickLogCard.kt` — 移除 inline error Text，改為 `onClearFeedback`
- `ui/expert/ExpertViewModel.kt` — `publishFeedbackRes` 命名同步、3 秒自動消失

### 廢棄
- QuickLogCard 的 `publishError` 參數

---

## 2026-07-15 — Cloud Function 修正

### 修正
- `model_status` 路徑編碼 — 模型名含 `.` 導致 RTDB crash
- Gemini 2.5 系列不支援 `responseMimeType: 'application/json'` + `tools: [googleSearch]` 並用
- FALLBACK 順序重排

---

## 2026-07-14 — Round 8：Crash 修復 + saveSkill 改寫 + 亂碼檢測 + Submission Lock

### 修改檔案
- `ui/expert/ExpertViewModel.kt` — userId.isBlank guard、submissionLock 監聽、isLoading 防連點
- `data/repository/ExpertRepository.kt` — callbackFlow guard、`updateChildren()` → 個別 `setValue()`
- `domain/expert/ExpertInputValidator.kt` — bigram 重複檢測、`SKILL_UNLIKELY_CHARS`
- `ui/expert/ExpertScreen.kt` — PENDING 隱藏卡片、ACTIVE 顯示標籤
- `ui/navigation/AppNavigation.kt` — LaunchedEffect guard
- `functions/index.js` — AI prompt 強化、Submission Lock 機制
- `strings.xml` — 新增 2 條字串

### 修正
- 啟動閃退 crash
- `updateChildren()` + `setPersistenceEnabled(true)` 衝突
- 亂碼檢測不足
- 惡意連續發布無阻擋機制

---

## 2026-07-14 — Round 7：大型重構（callbackFlow、狀態提升、資安加固）

### 新增檔案
- `data/Constants.kt` — FirebasePaths / FirebaseFields / StatusValues 常數

### 修改檔案（已列於 PROGRESS 第 7 輪）

### 關鍵決策
- Repository 無狀態化 — callbackFlow `awaitClose` 自動清除 listener
- `saveSkill()` 用 `updateChildren()` 原子寫入（後因 persistence 衝突在第 8 輪回退為個別 `setValue()`）
- State Hoisting — ExpertScreenContent 可獨立 Preview/測試
- 字串外部化 — ShowToast 使用 `@StringRes`
- 移除 `$other` 萬用字元規則

---

## 2026-07-13 — Round 6：全面修復（編輯流程、效能優化、測試強化）

### 修改檔案
- `domain/expert/ExtractLocalTagsUseCase.kt` — 刪除（dead code）
- `data/repository/ExpertRepository.kt` — `.await()` 寫法、`editSkill()`
- `ui/expert/ExpertViewModel.kt` — SkillEditDialog 狀態
- `ui/expert/ExpertScreen.kt` — 編輯按鈕、SkillEditDialog
- `functions/index.js` — 排程 5→1 分鐘
- `app/.../ExpertViewModelTest.kt` — 測試擴充

---

## 2026-07-13 — Cloud Function 初版

### 新增
- Cloud Function `batchProcessPendingSkills`（首版）
- 5 模型 fallback 鏈
- `config/model_status` 追蹤與重置
- `minInstances: 1`
- `responseMimeType: 'application/json'`

### 變更
- 客戶端 AI → 後端 Cloud Function
- SDK `@google/generative-ai` → `@google/genai`
- `admin.database()` → `getDatabase()`
- Node.js 20 → 24

---

## 2026-07-12 之前
- 初始專案建置
- 聊天功能基礎架構
- 登入/註冊流程
- CameraX 整合
- Media3 播放器
- Koin DI 整合
- Compose Navigation 路由

## Git 備註
- 約 280+ 次提交（至 Round 13），已全部推送至 main
- 第 7 輪：16 個 source 檔案
- 第 8 輪：9 個 source 檔案
- 第 9 輪：1 個檔案
- 第 10 輪：2 個檔案
- 第 11 輪：1 個檔案（`functions/index.js`）
- 第 12 輪：13+ 個檔案
- 第 13 輪：36+ 個 UI 檔案
- Round 14+：記載於對應區段
