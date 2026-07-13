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
1. **模型與配額 (GenAI)：**
   - 4 個 Gemini 模型 Round-robin 輪換。
   - **RPM：** 滑動 60 秒窗口 (in-memory)。
   - **RPD：** 太平洋時間 (America/Los_Angeles) 午夜重置，持久化於 SharedPreferences。
   - **Quota Ban：** 遇到 `Quota exceeded` 或 `429` 則封鎖至太平洋午夜。
2. **時間處理：**
   - 依賴 Firebase `.info/serverTimeOffset` 校正，超時 (3s) 則降級使用本地時間。
3. **專案規範：**
   - **Compose Modifier：** 遵守慣例順序 (required → modifier → optional)。
   - **資源檔：** 無 `colors.xml` 或 `backup_rules.xml`。
   - **依賴管理：** 統一使用 `libs.versions.toml`。

## 🛠️ 開發與測試指令
- **建構：** `.\gradlew.bat assembleDebug --daemon --parallel` (若快取過期加 `--no-configuration-cache`)
- **測試：** 需安裝至實機測試。已知問題請查閱 `PROGRESS.md`。