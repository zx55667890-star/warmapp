# CHANGELOG.md — 更新紀錄

## 2026-07-16
### 新增
- `docs/` 文件目錄（PROJECT_STRUCTURE, ARCHITECTURE, MODULE_MAP, etc.）
- `functions/index.js` MODEL 策略調整：PRIMARY 改為 gemini-3.1-flash-lite（無思考設定、無搜尋），4 個 FALLBACK 模型啟用 Google Search grounding
- Prompt 加入「不確定請 REJECT」引導

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
