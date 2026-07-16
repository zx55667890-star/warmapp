# CHANGELOG.md — 更新紀錄

## 2026-07-15
### 修正
- `functions/index.js` `model_status` 路徑編碼 — 模型名含 `.` 導致 RTDB crash，讀寫改為 `encodePath(model.name)`
- `functions/index.js` Gemini 2.5 系列不支援 `responseMimeType: 'application/json'` + `tools: [googleSearch]` 並用，有搜尋的模型不再設定 JSON mode
- FALLBACK 順序重排：`gemini-3.5-flash`（高 429）移至 FALLBACK_4，`gemini-2.5-flash` 改為 FALLBACK_1
- 已部署新版 function（hash: `1a05abccc9b987b06ea94d86e246d880f0e99ec9`）

### 文件
- 3 份 MD 搬入 `docs/` 並刪除重複內容
- 新增 11 份專案文件（架構、依賴、已知問題、風格規範等）
- 啟用 GitHub Pages

## 2026-07-17
### 新增
- `searchOnSerper()` 函式 — 調用 `https://google.serper.dev/search`（取前 3 筆 organic），取代內建 googleSearch
- `useWebFetch` 自訂旗標 — 控制是否先 Serper 搜尋再送模型（繞過 Gen3 Free Tier 429 限制）
- `SERPER_API_KEY` Firebase secret（deployed + pushed）
- Gen3 thinking 語法支援 — `thinkingLevel`（minimal/low/medium/high）透過 `thinkingConfig` 傳遞
- 批次測試按鈕 — ExpertScreen 底部橘色按鈕，20 筆冷門技能
- Per-skill logging — 每個 model log 哪些 accepted/rejected
- **Nunito Sans 字體** — `nunito_sans_regular.ttf` + `nunito_sans_bold.ttf`，完整 14 級 Typography 映射含用途註解
- **6 個 DI module** — CoreModule, AuthModule, ChatModule, ExpertModule, SeekerModule, MediaModule（取代單一 AppModule.kt）
- **資料層 (Data) 區塊** — UserRepository, DataMigrator, Constants 移出 DI

### 變更
- PRIMARY 維持 `gemini-3.1-flash-lite`（無搜尋）
- Model 陣列縮減為 4 個：PRIMARY + FALLBACK_1 (Serper) + FALLBACK_2~3 (內建 googleSearch)
- `slimmedEntries`/`localMapping` 移至 model loop 內建（不重送全部 entry）
- Prompt 增強 — 加入參考網路搜尋指示 + 標籤語言同源規則
- 自癒掃描 try-catch 包覆，不中斷主流程
- **Theme.kt 重寫** — 純深色 `darkColorScheme` 驅動，移除 light/dynamic color
- **AppColors 色值更新** — AccentGreen/AcentBlue/Orange/GradientEnd 換色，玻璃效果加強
- **Color.kt 合併進 AppColors.kt** — 刪除 Color.kt，Purple80/Purple40 等死碼移除
- **MainActivity.kt** — KoinApplication 載入 6 個 modules 取代單一 appModule
- **MODULE_MAP.md** — DI 區塊拆 6 module + 新增資料層區塊 + AiRepository 移至專家區
- `CHANGELOG_OLD.md` + `get_sha1.md` 搬入 `docs/`

### 修正
- 解決 Gen3 Free Tier 無法使用內建 `googleSearch`（429/RESOURCE_EXHAUSTED）
- 重疊排程併發控制透過 atomic transaction claim + 5 分鐘 timeout

### 刪除
- `di/AppModule.kt`（94 行 → 6 個獨立 module 共 158 行）
- `ui/theme/Color.kt`（合併進 AppColors.kt）
- 根目錄 `CHANGELOG_OLD.md`、`get_sha1.md`（搬入 docs/）

## 2026-07-16
### 新增
- `docs/` 文件目錄（PROJECT_STRUCTURE, ARCHITECTURE, MODULE_MAP, etc.）
- `functions/index.js` MODEL 策略調整：PRIMARY 改為 gemini-3.1-flash-lite（無思考設定、無搜尋），4 個 FALLBACK 模型啟用 Google Search grounding
- Prompt 加入「不確定請 REJECT」引導
- `domain/expert/PublishSkillUseCase.kt` — 封裝技能發布邏輯
- `domain/expert/ObserveSolutionsUseCase.kt` — 封裝技能歷史監聽
- `functions/index.js` 孤立 PENDING 自我修復排程（`healOrphanedPending`，每次掃 5 個 user）

### 變更
- `di/ExpertViewModel.kt` → `ui/expert/ExpertViewModel.kt`（含 `ExpertUiState`、`ExpertUiEvent`）
- `di/SeekerViewModel.kt` → `ui/seeker/SeekerViewModel.kt`（含 `SeekerUiState`）
- `ExpertViewModel` 改用注入式 UseCase（`PublishSkillUseCase`、`ObserveSolutionsUseCase`）
- `database.rules.json` 加入 `solutions/$uid/.indexOn: ["status"]`
- 同步更新所有 import（ExpertScreen、AppNavigation、AskQuestionScreen、測試）

### 修正
- `functions/index.js` 路徑編碼問題：Firebase RTDB 不允許 `.`、`#`、`$`、`[`、`]`，改用 base64url 編碼 blacklist/whitelist 路徑

## 2026-07-15
### 新增
- Cloud Function Google Search grounding（`tools: [{ googleSearch: {} }]`）
- QuickLogCard floating overlay 自動 3 秒消失
- 錯誤保留文字、成功清除輸入

### 修正
- `ExpertViewModel.kt` 命名未同步修復（`publishErrorRes` → `publishFeedbackRes`）
- AGP 9.2.1 → 9.3.0

## 2026-07-14
### 新增
- 發布反饋 floating overlay（取代 Snackbar）
- Submission Lock 機制（後端連續 REJECTED 達 3 次鎖 24h + 前端阻擋）
- 亂碼檢測強化（bigram 重複、SKILL_UNLIKELY_CHARS）
- `isLoading` 防連點
- `Lifecycle 2.10.0` 依賴

### 修正
- 啟動閃退 crash（userId.isBlank guard）
- `updateChildren()` → 個別 `setValue()`（避免 persistence 衝突）
- KnowledgeItemCard 隱藏 PENDING 卡片
- Cloud Function processing entry 卡住風險（timeout 5 分鐘）
- Cloud Function 遷移 2nd Gen + SDK 遷移

### 廢棄
- `ExtractLocalTagsUseCase.kt`（dead code）

## 2026-07-13
### 新增
- Cloud Function `batchProcessPendingSkills`（首版）
- 5 模型 fallback 鏈（primary → fallback_1 → ...）
- `config/model_status` 追蹤與重置
- `minInstances: 1`

### 變更
- 客戶端 AI → 後端 Cloud Function
- SDK `@google/generative-ai` → `@google/genai`
- `admin.database()` → `getDatabase()`
- Node.js 20 → 24

## 2026-07-12 之前
- 初始專案建置
- 聊天功能基礎架構
- 登入/註冊流程
- CameraX 整合
- Media3 播放器
- Koin DI 整合
- Compose Navigation 路由
