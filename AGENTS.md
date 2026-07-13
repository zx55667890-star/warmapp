# AGENTS.md — 專案對話紀錄與 AI 指南

## 🛑 強制規則 (CRITICAL)
- **查閱最新文檔：** 在回答任何關於 API、SDK 或技術實作的語法前，**必須**使用搜尋工具查詢最新的官方文檔。
- **標註來源：** 必須在回答中附上你參考的官方文檔連結。
- **版本對齊：** 請嚴格遵守下方「技術棧」中指定的版本，不要提供舊版語法。

## 📦 專案概述 & 技術棧
- **專案目標：** 知識技能記錄平台 Android App (warmapp)，包含專家模式 (Expert) 的知識標籤提取。
- **核心技術：** Kotlin, Jetpack Compose, Material3, Koin (DI), Coil, Media3, CameraX.
- **Firebase：** Realtime Database, Auth, Storage, Functions, Messaging.
- **GenAI SDK：** `com.google.genai:google-genai:1.61.0` (注意：`response.text()` 為 `String?`，需處理 null)。

## 🏗️ 核心架構與現有運作機制 (Context)
1. **AI 標籤提取（Backend Cloud Function）：**
   - 專家發布技能 → 前端驗證 → 黑/白名單快取 → PENDING 寫入 `pending_skills`
   - Cloud Function `batchProcessPendingSkills` 每 5 分鐘批次處理（最多 20 筆）
   - 使用 `@google/generative-ai` SDK，模型 `gemini-3.1-flash-lite`
   - 結果寫回 `solutions/{userId}/{skillId}`（status: ACTIVE / REJECTED + tags）
   - 白名單快取存入 `tags_whitelist/{text}/tags`，黑名單存入 `tags_blacklist/{text}`
2. **資料庫路徑：**
   - `solutions/{userId}/{skillId}` — 技能記錄（含 status 欄位）
   - `pending_skills/{skillId}` — 排隊等待 AI 處理
   - `tags_blacklist/{text}` — 被拒絕的文字黑名單
   - `tags_whitelist/{text}/tags` — 已被 AI 認證的標籤快取
3. **前端流程（publishSkill）：**
   - local 驗證 → duplicate 檢查 → blacklist 查詢 → whitelist 查詢（有快取則直接 ACTIVE）→ PENDING 寫入
4. **排程限制：**
   - Cloud Function 每 5 分鐘執行一次，PENDING 轉換最長等待 5 分鐘
5. **廢棄的客戶端 AI：**
   - `ExtractLocalTagsUseCase.kt` 不再被呼叫（dead code）
   - 舊的 `submitSolution()` 已被移除
6. **專案規範：**
   - **Compose Modifier：** 遵守慣例順序 (required → modifier → optional)。
   - **資源檔：** 無 `colors.xml` 或 `backup_rules.xml`。
   - **依賴管理：** 統一使用 `libs.versions.toml`。

## 🛠️ 開發與測試指令
- **建構：** `.\gradlew.bat assembleDebug --daemon --parallel` (若快取過期加 `--no-configuration-cache`)
- **測試：** 需安裝至實機測試。已知問題請查閱 `PROGRESS.md`。