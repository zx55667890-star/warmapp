# AGENTS.md — 專案對話紀錄與 AI 指南

## 🛑 強制規則 (CRITICAL)
- **查閱最新文檔：** 在回答任何關於 API、SDK 或技術實作的語法前，**必須**使用搜尋工具查詢最新的官方文檔。
- **Constants.kt 優先：** 修改 Firebase 路徑、欄位名稱或狀態值時，優先更新 `app/src/main/java/com/example/myapplication/data/Constants.kt`，而非直接寫字串。
- **status 欄位型別：** `SolutionItem.status` 是 `SkillStatus` 列舉型別（非 `String`），比對時用 `solution.status == SkillStatus.PENDING` 而非比對 `.name`。
- **UI 字串：** 所有使用者可見文字必須出自 `strings.xml`；動態 Toast 用 `ShowToastRaw`，固定訊息用 `ShowToast(resId)`。
- **標註來源：** 必須在回答中附上你參考的官方文檔連結。
- **版本對齊：** 請嚴格遵守下方「技術棧」中指定的版本，不要提供舊版語法。
- **強制使用 MCP 工具：** 本環境已綁定 Firebase MCP Server。排查資料庫問題（如讀取 `pending_skills`、查閱狀態）時，**嚴禁**要求我手動前往 Firebase Console 查看，你必須主動調用 MCP 工具來獲取或修改資料。

## 📦 專案概述 & 技術棧
- **專案目標：** 知識技能記錄平台 Android App (warmapp)，包含專家模式 (Expert) 的知識標籤提取。
- **核心技術：** Kotlin, Jetpack Compose, Material3, Koin (DI), Coil, Media3, CameraX.
- **Firebase：** Realtime Database, Auth, Storage, Functions, Messaging.
- **Cloud Functions Runtime：** Node.js 24
- **Backend Dependencies：** `firebase-admin@^13.0.0`、`firebase-functions@7.2.5`、`@google/genai@^2.10.0`、`nodemailer@^6.9.0`
- **Android GenAI SDK：** `com.google.genai:google-genai:1.61.0`（注意：`response.text()` 為 `String?`，需處理 null）。
- **Backend GenAI SDK：** `@google/genai:^2.10.0`（注意：`response.text` 為 property，非 method）。
- **Lifecycle 版本：** `2.10.0`（`lifecycle-runtime-compose` 需與 `lifecycle-runtime-ktx` 一致，否則啟動閃退）

## 🏗️ 核心架構與現有運作機制 (Context)
1. **AI 標籤提取（Backend Cloud Function）：**
   - 專家發布技能 → 前端驗證 → 直接 PENDING 寫入 `pending_skills`（無 blocking 讀取，~1s 內完成）
   - Cloud Function `batchProcessPendingSkills` 每 1 分鐘批次處理（最多 20 筆），2nd Gen (`firebase-functions/v2/scheduler`)
   - 使用 `@google/genai` SDK，Secret via `defineSecret('GEMINI_API_KEY')`
   - **注意：** `getDatabase()` from `firebase-admin/database`（firebase-admin v11+ 皆支援，非 v14 only）
   - 5 模型 fallback 鏈（primary → fallback_1 → ...），含 model-specific thinkingConfig（Gen3 → `thinkingLevel: 'minimal'`，Gen2.5 → `thinkingBudget: 0`）
   - 503 自動 retry（2s / 4s backoff），429/RESOURCE_EXHAUSTED 才標記 EXHAUSTED
   - `minInstances: 1` 保持 warm instance
   - **Concurrency 保護**：讀取後立即寫入 `processing: timestamp`，重疊觸發時跳過正在處理的 entries（timeout 5 分鐘）
   - **強制 JSON 輸出**：`responseMimeType: 'application/json'` 確保 Gemini 回傳結構化資料
   - 結果寫回 `solutions/{userId}/{skillId}`（status: ACTIVE / REJECTED + tags）
   - 白名單快取存入 `tags_whitelist/{text}/tags`，黑名單存入 `tags_blacklist/{text}`
   - **Submission Lock**：連續 3 次 REJECTED → 寫入 `users/{uid}/submissionLock/lockedUntil = now + 24h`，前端自動擋下發布
2. **資料庫路徑：**
   - `solutions/{userId}/{skillId}` — 技能記錄（含 status 欄位）
   - `pending_skills/{skillId}` — 排隊等待 AI 處理（含 `processing` 時間戳記防重複）
   - `config/model_status` — 各模型配額狀態（EXHAUSTED）
   - `tags_blacklist/{text}` — 被拒絕的文字黑名單
   - `tags_whitelist/{text}/tags` — 已被 AI 認證的標籤快取
   - `users/{uid}/submissionLock` — 提交鎖定狀態（`rejectedCount`、`lockedUntil`）
3. **Repository 設計：**
   - `ExpertRepository` 為**無狀態（Stateless）**
   - `listenToSolutionHistory()` 與 `observeExpertStatus()` 使用 `callbackFlow` 回傳 `Flow`，ViewModel 透過 `viewModelScope.launch { collect }` 自動管理 listener 生命週期。兩者皆有 `userId.isBlank()` guard，空白時直接發空結果並 close，避免 Firebase 無權限 crash。
   - `saveSkill()` 使用個別 `setValue()` 寫入 solutions + pending_skills（避免 `updateChildren()` 搭配 `setPersistenceEnabled(true)` 的衝突）
   - `cleanup()` 僅處理 experience cleanup（listener 由 callbackFlow 的 `awaitClose` 自行清除）
   - 路徑/欄位/狀態值使用 `Constants.kt` 中的 `FirebasePaths` / `FirebaseFields` / `StatusValues` 常數
4. **前端流程（publishSkill）：**
   - local 驗證（`ExpertInputValidator.validate()` → ViewModel 內部驗證，QuickLogCard 不再自行驗證）
   - duplicate 檢查
   - `userId.isBlank()` guard（未登入提示）
   - `isSubmissionLocked` 檢查（被鎖提示：「您的技能多次無法通過驗證，請於24小時後再試」）
   - 直接 PENDING 寫入（無 blocking 讀取）
   - Cloud Function server 端依序：blacklist 檢查 → whitelist 檢查 → AI 分析（僅通過前兩關的資料）
5. **UI 架構（State Hoisting）：**
   - `ExpertScreen`（stateful bridge）：注入 ViewModel，解析 event，將 state + lambda 傳給 content
   - `ExpertScreenContent`（stateless）：純展示層，接收 `ExpertUiState` + 6 個 callback lambda，可獨立 Preview/測試
   - `KnowledgeItemCard`、`SkillEditDialog`、`QuickLogCard` 皆為 stateless composable
   - **PENDING 技能卡片完全隱藏**（不顯示在列表中），ACTIVE 才顯示 AI 標籤，REJECTED 才顯示「內容無法辨識」
6. **字串管理：**
   - UI 文字全數存放於 `strings.xml`（35+ 條 resource）
   - `ExpertUiEvent.ShowToast` 使用 `@StringRes Int`（固定訊息），`ShowToastRaw` 處理動態錯誤訊息
   - ViewModel 中不再有硬編碼 Toast 字串
7. **排程限制：**
   - Cloud Function 每 1 分鐘執行一次，PENDING 轉換最長等待 1 分鐘
8. **廢棄的客戶端 AI：**
   - `ExtractLocalTagsUseCase.kt` 已刪除（dead code）
   - 舊的 `submitSolution()` 已被移除
9. **亂碼偵測（前端 + 後端雙層）：**
   - **前端 `ExpertInputValidator`：**
     - 唯一字元比 < 0.4（`UNIQUE_CHAR_RATIO_THRESHOLD`）
     - 連續重複字元 ≥ 3（`MAX_CONSECUTIVE_DUPLICATES`）
     - 相鄰重複配對 ≥ 3（`MAX_ADJACENT_PAIRS`）
     - 二元組（bigram）重複出現 ≥ 2 次（`BIGRAM_REPEAT_THRESHOLD`）
     - 技能不應出現字元（`SKILL_UNLIKELY_CHARS`）：哦呢嗎吧額喔誒欸啦嘛呀喲嘅誰該
   - **後端 AI Prompt：** 明確要求 REJECT「看似有詞但整體無意義」的內容，包含具體範例
10. **Submission Lock 機制：**
    - 後端 Cloud Function 處理時追蹤每位使用者的連續 REJECTED 次數
    - 任一技能 ACTIVE → 歸零
    - REJECTED → +1
    - 達 3 次 → 寫入 `users/{uid}/submissionLock/lockedUntil = now + 24h`
    - 前端 `ExpertViewModel.observeSubmissionLock()` 監聽鎖定狀態，`publishSkill()` 自動擋下
11. **專案規範：**
    - **Compose Modifier：** 遵守慣例順序 (required → modifier → optional)。
    - **資源檔：** 無 `colors.xml` 或 `backup_rules.xml`。
    - **依賴管理：** 統一使用 `libs.versions.toml`。

## 🛠️ 開發與測試指令
- **建構：** `.\gradlew.bat assembleDebug --daemon --parallel` (若快取過期加 `--no-configuration-cache`)
- **清理建構：** 若遇到 Windows 檔案鎖定（merger.xml），先 `.\gradlew.bat clean` 再重試
- **部署 Cloud Function：** `firebase deploy --only functions --force`
- **查看 Function Log：** `firebase functions:log --only batchProcessPendingSkills`
- **測試：** 需安裝至實機測試。已知問題請查閱 `PROGRESS.md`。
- **RTDB 操作：** 使用 Firebase MCP 工具 `firebase_realtimedatabase_get_data` / `firebase_realtimedatabase_set_data`（需指定 `databaseUrl: https://warmhelpapp-default-rtdb.firebaseio.com`）
