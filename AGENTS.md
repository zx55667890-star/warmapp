# AGENTS.md — 專案對話紀錄

## 專案概述
Android App（warmapp），使用 Jetpack Compose + Firebase + Koin，目標是建立一個知識技能記錄平台，包含專家模式（Expert）的知識標籤提取功能。

## 技術棧
- Kotlin, Jetpack Compose, Material3
- Firebase (Realtime Database, Auth, Storage, Functions, Messaging)
- Koin (DI)
- Google GenAI SDK (`com.google.genai:google-genai:1.61.0`)
- Coil, Media3 ExoPlayer, CameraX

## 關鍵對話摘要

### 1. 模型輪換機制
- 5 個 Gemini 模型輪換（後移除 3 Flash Preview），round-robin 分配請求
- `AtomicInteger` 搭配 `(rawIndex and Int.MAX_VALUE) % models.size` 實現

### 2. 配額管理（RPM / RPD）
- **RPM**: 滑動 60 秒窗口，in-memory `mutableListOf<Long>()`
- **RPD**: 太平洋午夜重置，持久化到 `SharedPreferences`（`Set<String>` 時間戳）
- RPD 窗口 = `America/Los_Angeles` 時區的 `todayStartMs()`，非滑動 24h

### 3. 伺服器時間校正
- Firebase `.info/serverTimeOffset` 取得偏差
- `withTimeoutOrNull(3000)` 防死鎖，超時降級回本地時間

### 4. 配額封鎖（Quota Ban）
- 偵測 `Quota exceeded` 或 HTTP `429` → 封鎖至太平洋午夜
- 舊 ban 遷移：`migrateOldBans()` 校正超過明日午夜的封鎖

### 5. Thinking Budget
- Lite 模型：不支援 thinking，傳空 config
- 非 Lite 模型：`thinkingBudget(0)` 關閉 thinking（3 Flash Preview 無效）

### 6. SDK 遷移
- `com.google.ai.client.generativeai:generativeai:0.9.0` → `com.google.genai:google-genai:1.61.0`
- `AiRepository.kt` 和 `ExtractLocalTagsUseCase.kt` 都使用新 `Client` API
- `response.text()` 為 `String?`，需處理 null

### 7. 打包衝突
- `META-INF/INDEX.LIST` 和 `META-INF/DEPENDENCIES` 排除

## 建構指令
```powershell
cd C:\Users\zx556\warmapp
.\gradlew.bat assembleDebug --daemon --parallel
# 快取過期時加 --no-configuration-cache
```

## 測試
- 目前無整合測試流程，只能裝到裝置上手動測試

## 已知待解決問題
見 PROGRESS.md「未解決問題」
