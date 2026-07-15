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
- [x] **Cloud Function 排程 5 → 1 分鐘** — 最大等待時間從 5 分鐘縮短至 1 分鐘
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

### Git
- [x] 約 35+ 次提交，已全部推送至 main
- [x] 第 7 輪變更：16 個 source 檔案（14 修改 + 1 新增 + 1 刪除）
- [x] 第 8 輪變更：9 個 source 檔案修改

## 未解決問題

1. **`pending_skills` `.indexOn`** — 已在 `database.rules.json` 補上，Log 已無 warning。現有資料待 Firebase 自動生成索引。

2. **`@StringRes` annotation target 警告** — `ShowToast.resId: Int` 上的 `@StringRes` 在 Kotlin 2.x 編譯時觸發 KT-73255（future default 將同時 apply 到 field）。可加 `-Xannotation-default-target=param-property` 或改用 `@param:` target 消除。

3. **Cloud Function processing entry 卡住風險** — 若 function crash 在 claim 之後、寫回結果之前，entry 會被標記 `processing` 而擱置最多 5 分鐘（`PROCESSING_TIMEOUT_MS`）。可考慮實作異常情況下的 cleanup 邏輯。

4. **Cloud Function reset cooldown 可能遺失模型狀態** — 若所有模型 EXHAUSTED，需等 10 分鐘 cooldown 才會重置。期間 PENDING skills 會累積不處理。可考慮加入手動觸發機制或逐步縮短 cooldown。

5. **Constants.kt 負責路徑一致性的維護負擔** — 所有 Firebase 路徑/欄位/狀態值集中於一檔，修改時需注意不影響既有資料結構。未來可考慮加入 migration 版本控制。

6. **saveSkill 失去原子性** — `updateChildren()` 改回個別 `setValue()` 後，若 `solutions` 寫入成功但 `pending_skills` 失敗，會產生孤立 solution 狀態不一致。目前可接受（雲端函數排程處理時僅讀取 `pending_skills`），但建議實作清理機制。

7. **Submission Lock 同批次邊界情況** — 若同一批次中同一使用者有多筆 entry，混雜 ACTIVE 與 REJECTED 時，`hasActive` 會將 rejectedCount 歸零而不觸發鎖。此為低機率場景（ACTIVE 與 REJECTED 同時出現才受影響）。

### ✅ 已解決（第 9 輪清理）

- **firebase-functions 倚賴 RC 版本** — 降級 firebase-admin ^13.0.0 + firebase-functions 7.2.5 stable，不再需要 RC
- **Deploy 時出現 "outdated firebase-functions" warning** — 切換至 latest stable 後消除
- **ExpertInputValidator 錯誤訊息外部化** — 已在第 7 輪完成
- **無意義句子前端檢測漏網之魚** — 由後端 AI 兜底，可接受
- **npm install firebase-functions@latest 警告** — 已使用 latest stable 7.2.5，警告消除

## 模型清單（Cloud Function fallback 順序）
| 順序 | 模型 | 速度 (2026/7/14 benchmark, 5 entries) | 備註 |
|------|------|------|------|
| PRIMARY | `gemini-3.1-flash-lite` | **1083ms** | ~1s/5筆，最快且穩定 |
| FALLBACK_1 | `gemini-2.5-flash` | **1247ms** | thinkingBudget: 0，穩定 |
| FALLBACK_2 | `gemini-2.5-flash-lite` | **980ms** | 偶發503但限流快滿 |
| FALLBACK_3 | `gemini-3.5-flash` | **26632ms** | 26.6s，極慢，考慮降級或移除 |
| FALLBACK_4 | `gemini-3-flash-preview` | **19551ms** | 19.6s，關thinking仍慢 |
