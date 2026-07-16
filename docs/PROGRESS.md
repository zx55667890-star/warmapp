# PROGRESS.md — 專案進度與狀態

## 已完成

### 第 1、2 輪：客戶端 AI 基礎架構與優化
- [x] SolutionItem data class（id, questionId, expertise, tags, timestamp）
- [x] ExpertRepository.saveSolution() / listenToSolutionHistory()
- [x] ExpertViewModel.submitSolution() + solutionHistory 型別更新
- [x] KnowledgeItemCard 與 QuickLogCard 實作
- [x] FlowRow 標籤晶片顯示
- [x] QuickLogCard 重複檢測（expertise 比對）
- [x] 5 模型輪換、RPM/RPD 配額管理、時間校正
- [x] SDK 遷移至 google-genai:1.61.0
- [x] 重構移除 TagViewModel、dead code fetchTagsFromAi
- [x] QuickLogCard 兩步確認與手動標籤編輯流程
- [x] 全域代碼優化（Modifier 順序、依賴管理、lint/test/build）

### 第 3 輪：AI 標籤提取遷移至 Backend Cloud Function
- [x] **SolutionItem.kt** — 加入 `SkillStatus` 列舉（ACTIVE/PENDING/REJECTED）及 `status` 欄位
- [x] **ExpertRepository.kt** — 加入 `checkBlacklist()` / `checkWhitelist()` / `saveSkill()`（pending queue 寫入）
- [x] **ExpertViewModel.kt** — 移除 `SharedPreferences`、`fetchTagsFromAi()`、`submitSolution()`；加入 `publishSkill()` 完整流程
- [x] **AppModule.kt** — Koin 註冊改為 `viewModel { ExpertViewModel(get()) }`
- [x] **ExpertScreen.kt** — QuickLogCard 簡化為輸入+發布；KnowledgeItemCard 狀態顯示（PENDING spinner / REJECTED 紅字）
- [x] **ExpertInputValidator.kt** — 強化重複檢測規則（連續字元、相鄰配對等）
- [x] **functions/index.js** — 建立 `batchProcessPendingSkills` Cloud Function
- [x] **functions/package.json** — Node 20 + `@google/generative-ai`
- [x] **database.rules.json** — 新增 `pending_skills`、`tags_blacklist`、`tags_whitelist` 路徑規則與 `.indexOn`
- [x] **Cloud Function 部署成功** — 已設定 Gemini API key，排程正常運作

### 第 4 輪：Cloud Function 優化（5 模型 fallback + 2nd Gen + SDK 遷移）
- [x] **遷移至 2nd Gen + Secret Manager** — `firebase-functions/v2/scheduler` + `defineSecret('GEMINI_API_KEY')`，取代 `functions.config()`
- [x] **5 模型 fallback 鏈** — primary → fallback_1 → ... → fallback_4，同一次執行內依序嘗試
- [x] **Model-specific thinkingConfig** — Gen3 用 `thinkingLevel: 'minimal'`，Gen2.5 用 `thinkingBudget: 0`
- [x] **503 retry** — `generateContentWithRetry`，最多 3 次 (2s/4s backoff)
- [x] **`config/model_status`** — 429/RESOURCE_EXHAUSTED 才標記 EXHAUSTED，全數耗盡時自動重置
- [x] **`minInstances: 1`** — 減少冷啟動延遲
- [x] **SDK 遷移 `@google/generative-ai` → `@google/genai`** — 舊 SDK 已 EOL（2025/8/31），不支援 `thinkingConfig`
- [x] **`database.rules.json`** — 新增 `config/` 路徑規則
- [x] **5 模型速度測試完成** — `gemini-3.1-flash-lite` 為 PRIMARY（~0.7s），`gemini-3-flash-preview` 最慢（~17s）

### 第 5 輪：Cloud Function 依賴升級（Node 24 + firebase-admin v14 + firebase-functions v7 RC）
- [x] **engines.node 20 → 24** — 原生 runtime 升級至 Node.js 24
- [x] **firebase-admin ^12.0.0 → ^14.0.0** — 跳兩大版，移除 legacy namespace
- [x] **firebase-functions ^5.0.0 → 7.3.0-rc.0** — v7.2.x 不支援 admin v14，需用 RC
- [x] **`admin.database()` → `getDatabase()`** — 修復 firebase-admin v14 breaking change
- [x] **部署成功** — Runtime ID `nodejs24`，function state ACTIVE

### 第 9 輪：firebase-functions 降級至 stable（移除 RC 依賴）
- [x] **firebase-admin ^14.0.0 → ^13.0.0** — v14 對專案無實際影響，降回 v13 以相容 stable firebase-functions
- [x] **firebase-functions 7.3.0-rc.0 → 7.2.5** — 改用 latest stable，消除 RC 部署警告
- [x] **getDatabase() 無需回退** — firebase-admin v13 亦支援 modular API，index.js 不需改動
- [x] **npm install 成功** — 套件數減少 57 個（移除 RC 依賴），lockfile 更新

### 第 6 輪：全面修復未解決問題（編輯流程、效能優化、排程縮短、dead code 清理、測試強化）
- [x] **新增 `Constants.kt`** — 統一路徑(`FirebasePaths`)、欄位(`FirebaseFields`)、狀態值(`StatusValues`)常數物件
- [x] **刪除 `ExtractLocalTagsUseCase.kt` dead code** — 已確認無任何程式碼或 DI 註冊依賴此檔案
- [x] **Cloud Function 排程 5 → 1 → 5 分鐘** — 考量 cooldown 10 分鐘，恢復 5 分鐘排程減少空轉
- [x] **檢查 firebase-functions 版本狀態** — v7.2.5 仍不支援 firebase-admin v14，`7.3.0-rc.0` 仍為唯一選擇
- [x] **ExpertRepository 重構為 `.await()` 寫法** — 移除 `suspendCancellableCoroutine`，改用 `kotlinx.coroutines.tasks.await()`
- [x] **新增 `editSkill()` 方法** — 更新 solutions 資料 + 重新寫入 pending_skills 佇列觸發 AI 重新分析
- [x] **KnowledgeItemCard 編輯按鈕實作** — 點擊後彈出 `SkillEditDialog`，可修改技能描述後重新提交 AI 分析
- [x] **PENDING 狀態隱藏編輯按鈕** — 避免在 AI 分析中編輯
- [x] **修正並擴充單元測試** — `ExpertViewModelTest.kt` 修正建構子、新增 edit skill 測試案例
- [x] **移除客戶端 blocking 讀取** — 刪除 `checkBlacklist()` / `checkWhitelist()`，publish 直接寫 PENDING，不再卡轉圈
- [x] **Cloud Function 伺服端處理黑/白名單** — 批次讀取後先檢查 blacklist（→REJECTED）→ whitelist（→ACTIVE）→ 僅剩餘項目送 AI
- [x] **database.rules.json 收緊權限** — `tags_blacklist` / `tags_whitelist` 設為 admin-only（`false`）
- [x] **All 硬編碼路徑/欄位/狀態替換為常數** — `FirebasePaths`、`FirebaseFields`、`StatusValues` 物件統整
- [x] **所有 5 模型速度測試** — 2026/7/14 隔離實測結果：
  - PRIMARY (`gemini-3.1-flash-lite`): **1083ms** ✅（5 entries baseline）
  - FALLBACK_1 (`gemini-2.5-flash`): **1247ms** ✅
  - FALLBACK_2 (`gemini-2.5-flash-lite`): **980ms** ✅（含一次 503 retry）
  - FALLBACK_3 (`gemini-3.5-flash`): **26632ms** ❌（26.6s，極慢）
  - FALLBACK_4 (`gemini-3-flash-preview`): **19551ms** ❌（19.6s，慢）
  - 結論：PRIMARY 的 `gemini-3.1-flash-lite` 仍是速度/準確度平衡最佳選擇

### 第 7 輪：大型重構 — callbackFlow、狀態提升、資安加固（根據 code review）
- [x] **ExpertRepository 改為 callbackFlow** — `listenToSolutionHistory()` / `observeExpertStatus()` 回傳 `Flow`，Repository 真正無狀態（移除 `statusListener`、`currentUserId`）
- [x] **ExpertViewModel 改用 Flow collect** — `viewModelScope.launch { flow.collect { ... } }`，自動管理 listener 生命週期
- [x] **ExpertScreen 狀態提升** — 抽離 `ExpertScreenContent` 純展示層 stateless composable（接收 `ExpertUiState` + 6 個 lambda）
- [x] **`saveSkill()` 原子寫入** — 兩個獨立 `setValue()` 改為單一 `updateChildren()`（避免部分失敗髒資料）
- [x] **`ExpertInputValidator` 常數化** — 6 個魔法數字改為具名 private const val（`MIN_SKILL_LENGTH`、`UNIQUE_CHAR_RATIO_THRESHOLD` 等）
- [x] **`setExpertOnline()` 改為接收 userId** — 不再依賴 Repository 內部儲存的 `currentUserId`
- [x] **database.rules.json 移除 `$other` 萬用字元** — 不再允許已驗證用戶任意寫入未定義路徑
- [x] **database.rules.json 強化 `tags` 驗證** — 從 `".validate": "true"` 改為逐項字串型態 + 50 字限制
- [x] **strings.xml 補齊 21 條字串資源** — 所有 Toast、錯誤提示、UI 文字外部化
- [x] **`ExpertUiEvent.ShowToast` 使用 `@StringRes`** — 固定訊息用資源 ID，動態訊息用 `ShowToastRaw`
- [x] **Cloud Function `responseMimeType: 'application/json'`** — 強制 Gemini 回傳 JSON，減少解析異常
- [x] **Cloud Function 遷移 2nd Gen** — `firebase-functions/v2/scheduler` + `defineSecret('GEMINI_API_KEY')` + `minInstances: 1`
- [x] **Cloud Function SDK 遷移** — `@google/generative-ai` → `@google/genai`（支援 thinkingConfig）
- [x] **Cloud Function `responseMimeType: 'application/json'`** — 強制 Gemini 回傳 JSON
- [x] **Cloud Function processing flag 併發控制** — atomic transaction claim + 5 分鐘 timeout，重疊排程時跳過正在處理的 entries
- [x] **Cloud Function 所有模型 EXHAUSTED 時 reset cooldown** — 10 分鐘冷卻防無限重試
- [x] **Test 更新** — `Dispatchers.setMain(testDispatcher)` + `runTest` + `advanceUntilIdle()`

### 第 8 輪：Crash 修復 + saveSkill 改寫 + 亂碼檢測強化 + Submission Lock
- [x] **ExpertViewModel isBlank guard** — `initializeExpertStatus()` / `listenToSolutions()` 加入 `userId.isBlank()` 保護 + try/catch，防止未登入/空白時 crash
- [x] **AppNavigation LaunchedEffect guard** — 加入 `userId.isNotBlank()` 條件避免空白觸發 Firebase 操作
- [x] **Repository callbackFlow isBlank guard** — `listenToSolutionHistory()` / `observeExpertStatus()` 空白 userId 時直接發空結果並 close，避免 Firebase permission denied crash
- [x] **publishSkill/publishExperience isBlank guard** — 加入 `userId.isBlank()` 提前 return 並 Toast「請先登入」
- [x] **saveSkill/editSkill 改為個別 setValue** — 取代 multi-path `updateChildren()`，避免與 `setPersistenceEnabled(true)` 的本地快取衝突
- [x] **新增 isLoading 防連點** — `publishSkill()` 中 `isLoading` 用 `_uiState.update` 非同步設為 true，發布完成後恢復 false
- [x] **ExpertInputValidator bigram 重複檢查** — `windowed(2)` 檢測二元組重複，擋下「吃黑吃黑」型繞過
- [x] **ExpertInputValidator SKILL_UNLIKELY_CHARS** — 新增 15 個不合理字元（哦呢嗎吧額喔誒欸啦嘛呀喲嘅誰該）
- [x] **KnowledgeItemCard 隱藏 PENDING 卡片** — 不再顯示 spinner 與「AI 分析中...」
- [x] **KnowledgeItemCard ACTIVE 標籤顯示** — 使用 `SuggestionChip` 展示 AI 產生的 tags
- [x] **Submission Lock（後端）** — Cloud Function 追蹤使用者連續 REJECTED 次數，達 3 次寫入 `users/{uid}/submissionLock/lockedUntil = now + 24h`
- [x] **Submission Lock（前端）** — `observeSubmissionLock()` 監聽鎖定狀態，`publishSkill()` 阻擋發布並 Toast
- [x] **Cloud Function AI prompt 強化** — 明確要求 REJECT 無意義句子，加入具體範例（「燒烤是黑子黑吃黑」等）
- [x] **函數部署 2 次成功** — revision 已更新（含 Node.js 24 runtime）

### 第 9 輪：發布反饋改為 floating overlay（Snackbar → Box 疊層 Card）
- [x] **ExpertScreen bridge**: `onClearPublishError` → `onClearPublishFeedback`
- [x] **ExpertScreenContent**: 同上參數更名
- [x] **Scaffold 內容包裹 Box**: `innerPadding` 移至 Box，LazyColumn 不再含 `padding(innerPadding)`
- [x] **新增 floating overlay Card**: `publishFeedbackRes` / `publishFeedbackIsError` 狀態驅動的懸浮 Card，顯示在 Box 疊層上方，3 秒自動消失
- [x] **QuickLogCard 清理**: 移除 `publishError` / `onClearError` 參數，改為 `onClearFeedback`；移除 inline error `Text`
- [x] **BUILD SUCCESSFUL**: CLI 編譯通過

### 第 10 輪：AGP 升級 + ExpertViewModel 命名未同步修復
- [x] **AGP 9.2.1 → 9.3.0** — `gradle/libs.versions.toml` 升級，Gradle 9.5.0 相容
- [x] **ExpertViewModel `publishErrorRes` → `publishFeedbackRes`** — 補上第 9 輪遺漏的命名同步：`ExpertUiState` 欄位、`clearPublishFeedback()` 方法、成功時 3 秒 overlay 顯示、catch 區塊清理

### Git
- [x] 約 36+ 次提交，已全部推送至 main
- [x] 第 7 輪變更：16 個 source 檔案（14 修改 + 1 新增 + 1 刪除）
- [x] 第 8 輪變更：9 個 source 檔案修改
- [x] 第 9 輪變更：1 個檔案修改
- [x] 第 10 輪變更：2 個檔案修改（`gradle/libs.versions.toml` + `ExpertViewModel.kt`）

### 第 11 輪：Serper 外部搜尋取代內建 googleSearch（避開 Gen3 Free Tier 限制）
- [x] **批次測試按鈕** — ExpertScreen.kt 底部橘色按鈕，20 筆冷門技能
- [x] **`searchOnSerper()` 函式** — 調用 `https://google.serper.dev/search`，取前 3 筆 organic 結果，5s timeout
- [x] **`useWebFetch` 自訂旗標** — 新增於 MODELS 物件，控制是否先做 Serper 搜尋再餵給模型
- [x] **`SERPER_API_KEY` Firebase secret** — 設為 `7c8de270780a9fe51442063fa25e3894ab6fa838`，deploy 成功
- [x] **Gen3 thinking 語法支援** — `thinkingLevel`（minimal/low/medium/high），透過 `thinkingConfig` 傳遞
- [x] **Prompt 增強** — 加入「請仔細參考上述網路搜尋結果」指示 + 標籤語言同源規則
- [x] **`slimmedEntries`/`localMapping` 移至 model loop 內建** — 不再重送全部 entry，每個 fallback 只送剩餘項目
- [x] **Per-skill logging** — 每個 model 結束後 log 哪些 accepted/rejected
- [x] **自癒掃描 try-catch** — 不中斷主流程
- [x] **Model 陣列縮減** — PRIMARY + FALLBACK_1 (Serper) + FALLBACK_2~3 (內建 googleSearch)

### 模型測試結果（2026/7/17）
- `gemini-3.1-flash-lite` + Serper：731ms，2/2 接受 ✅
- `gemini-3-flash-preview` + Serper（預設 thinking high）：24s，2/2 拒 ❌
- `gemini-3-flash-preview` + Serper + thinkingLevel `minimal`：10.6s，1/2 接受 ⚠️
- `gemini-3.5-flash` + Serper + thinkingLevel `minimal`：6.1s，2/2 接受 ✅
- `gemini-3-flash-preview` + Serper + thinkingLevel `low`：剛 deploy，待測試
- 當前部署：`gemini-3-flash-preview` + thinkingLevel `low` + Serper（ace6396）

### Git
- [x] 約 42+ 次提交，已全部推送至 main
- [x] 第 11 輪變更：1 個檔案修改（`functions/index.js`）

### 第 12 輪：主題系統重構（AppColors → Theme → Type + DI 拆分）
- [x] **AppColors 獨立檔案** — 從 `Color.kt` 中遷出 `object AppColors` 成獨立檔
- [x] **Color.kt 合併進 AppColors.kt** — 刪除 Color.kt，Purple80/Purple40 等死碼一併移除
- [x] **色值更新** — AccentGreen `#34D399`、AccentBlue `#60A5FA`、GradientEnd `#2DD4BF`、玻璃效果加強（10%/5%）
- [x] **Theme.kt 重寫** — 純深色 `darkColorScheme`，完整映射 `AppColors` 到 Material3 色彩角色，移除 light scheme / dynamic color
- [x] **Nunito Sans 字體** — 加入 `nunito_sans_regular.ttf` + `nunito_sans_bold.ttf`，完整 14 級 Typography 映射
- [x] **Type.kt 用途註解** — 每級文字角色標示中文用途（displayLarge=頁面大標題、bodyLarge=正文...）
- [x] **DI 拆分** — `AppModule.kt` 刪除，拆分為 6 個 feature module（Core/Auth/Chat/Expert/Seeker/Media）
- [x] **MainActivity.kt** — `KoinApplication` 改用 `modules(coreModule, authModule, ...)`
- [x] **MODULE_MAP.md 更新** — DI 區塊拆 6 module + 新增「資料層」區塊 + AiRepository 移至專家區
- [x] **根目錄清理** — `CHANGELOG_OLD.md` + `get_sha1.md` 搬入 `docs/`

### Git
- [x] 約 53+ 次提交，已全部推送至 main
- [x] 第 12 輪變更：13+ 個檔案（新增 9 檔、刪除 3 檔、修改 6+ 檔）

