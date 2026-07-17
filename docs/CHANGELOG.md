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

## 2026-07-17 (Round 13)
### 新增
- `NicknameSettingsDialog` — AppColors 主題化 + isSaving loading 狀態 + 自動關閉
- `GradientButton` 共用 composable（ForgotPasswordPanel + NewPasswordForm 共用漸層按鈕）
- `PendingOverlay()` 抽取共用 composable（BubbleContent 載入覆蓋）
- `ProfileStat` 小元件（OpponentProfileDialog 統計行）

### 變更
- **AppColors 色值調整** — AccentGreen `#34D399`、AccentBlue `#60A5FA`、AccentOrange `#F97316`、玻璃效果透明度 6%→10%（stroke）/ 3%→5%（fill）
- **Theme.kt 全面重寫** — 砍掉 dynamicColor、isSystemInDarkTheme()、LightColorScheme；DarkColorScheme 全面對接 AppColors
- **Type.kt 全面重寫** — 加入 Nunito Sans 字型，完整 13 級字型層次
- **共 ~120 處硬編碼色碼移除，~60 處 isSystemInDarkTheme() 砍掉，~10 處 emoji 改 Material Icon，~15 處新增動畫**

#### 一、主題基底（3 檔案）
- `ui/theme/Color.kt`（AppColors）— 調整強調色色值，提高玻璃效果透明度，砍掉未使用的紫色/粉色色票
- `ui/theme/Theme.kt` — 全面重寫，純深色 darkColorScheme，完整對接 AppColors
- `ui/theme/Type.kt` — 全面重寫，Nunito Sans 字型 + 13 級 Typography

#### 二、共用元件（7 檔案）
- `ui/common/LoadingOverlay.kt` — Color.Black→DarkBackground，旋圈 AccentGreen+呼吸動畫，加入淡入淡出動畫
- `ui/common/ToastOverlay.kt` — 底色→SurfaceMedium+玻璃邊框，位置改用 statusBarsPadding()+top=16.dp，滑入滑出動畫
- `ui/common/CompactTextField.kt` — 背景→SurfaceDark/SurfaceMedium（聚焦），1dp 邊框（聚焦變綠），游標 AccentGreen，placeholder TextMuted
- `ui/common/OfflineBanner.kt` — 底色 StatusPending 黃色，文字色 DarkBackground
- `ui/components/FullScreenImageDialog.kt` — 背景 DarkBackground，頁碼改小圓點（≤7）或數字（>7），加入 statusBarsPadding+navigationBarsPadding
- `ui/components/RatingDialog.kt` — 砍掉 isSystemInDarkTheme()，emoji 星星→Material Icon（Filled/Outlined Star），顏色 StatusPending
- `ui/components/ScrollToBottomButton.kt` — FAB 背景 SurfaceMedium，圖標色 AccentGreen
- `ui/components/VideoPlayerDialog.kt` — 背景 DarkBackground，Slider AccentGreen，播放/關閉 emoji→Material Icon，錯誤 StatusError

#### 三、登入模組（5 檔案）
- `ui/auth/AuthScreen.kt` — 背景 DarkBackground，錯誤移至 TopCenter+滑入動畫，AnimatedContent+淡入上滑轉場
- `ui/auth/WelcomePanel.kt` — 三按鈕建立主/次/弱層級，Logo 呼吸光暈，五階段進場動畫
- `ui/auth/LoginForm.kt` — 標題「歡迎回來/建立帳號」+副標，分三區，GradientButton
- `ui/auth/ResetPasswordPanel.kt` — 全部 AppColors，GradientButton 共用 composable
- `ui/auth/NicknameSettingsDialog.kt` — 深色化 SurfaceDark，AppColors，綠色徽章+1.2s 自動關閉

#### 四、導航（2 檔案）
- `ui/navigation/AppNavigation.kt` — Scaffold containerColor→Transparent（讓 BackgroundGlow 穿透），修復 Box 嵌套
- `ui/navigation/Route.kt` — 無改動

#### 五、專家模組（6 檔案）
- `ui/expert/ExpertScreen.kt` — 刪除 fadeSlideIn()，GRADIENT 按鈕文字→DarkBackground
- `ui/expert/ExpertDialogs.kt` — 砍掉全部 isSystemInDarkTheme()（~15 處），emoji→Icons.AutoAwesome
- `ui/expert/components/QuickLogCard.kt` — 按鈕文字→DarkBackground
- `ui/expert/components/FeedbackBanner.kt` — 無改動（已乾淨）
- `ui/expert/components/KnowledgeItemCard.kt` + EmptyKnowledgeCard — 無改動

#### 六、提問者主畫面（4 檔案）
- `ui/seeker/AskQuestionScreen.kt` — Scaffold containerColor→DarkBackground，額度 StatusError/TextGray
- `ui/seeker/RoleSelectScreen.kt` — 背景 DarkBackground，抽屜邊框 BorderGray，AskerIcon AccentBlue，ExpertIcon AccentGreen，燈泡 StatusPending
- `ui/seeker/MatchingOverlay.kt` — 遮罩 DarkBackground.copy(0.7f)，旋圈 AccentGreen，取消 TextGray
- `ui/seeker/MatchingDialog.kt` — 砍掉 isSystemInDarkTheme()（6 處），彈窗 SurfaceDark，旋圈 AccentGreen
- `ui/seeker/SeekerConfirmDialog.kt` — 砍掉 isSystemInDarkTheme()（5 處），全部 AppColors，確認 AccentGreen

#### 七、提問者子元件（6 檔案）
- `ui/seeker/components/AskQuestionHeader.kt` — emoji→Icons.AutoAwesome+呼吸光暈，引導副標
- `ui/seeker/components/AskQuestionInputBar.kt` — 11 處硬寫色碼全改，砍掉 isSystemInDarkTheme()（2 處）
- `ui/seeker/components/AttachmentBottomSheet.kt` — 砍掉 isSystemInDarkTheme()（3 處），Sheet SurfaceDark，DragHandle BorderGray
- `ui/seeker/components/BackgroundGlow.kt` — 單色→綠藍雙色光暈（AccentGreen+AccentBlue）
- `ui/seeker/components/DrawerContent.kt` — 10 處硬寫色碼全改，搜尋框 SurfaceMedium，游標 AccentGreen
- `ui/seeker/components/FullSettingsScreen.kt` — 全螢幕 DarkBackground，返回 24→40dp+SurfaceMedium，登出 StatusError

#### 八、聊天元件（6 檔案）
- `ui/chat/components/ChatTopBar.kt`（含 QuestionBanner）— 砍掉 isDarkTheme（12 處），TopBar SurfaceDark，HelpOutline→AutoMirrored
- `ui/chat/components/ChatBottomArea.kt` — isDarkTheme 預設 true，preview 文字改 when 表達式
- `ui/chat/components/ChatInputBar.kt` — 8 處硬寫色碼全改，結束卡片 StatusError.copy(0.08f)+紅色描邊，Warning icon
- `ui/chat/components/MessageList.kt` — 載入中文字 TextGray
- `ui/chat/components/ReplyPreviewBar.kt` — 砍掉 isDarkTheme（3 處），背景 SurfaceMedium
- `ui/chat/components/TypingIndicator.kt` — 砍掉 isSystemInDarkTheme()，文字/圓點 TextGray

#### 九、聊天泡泡（5 檔案）
- `ui/chat/bubble/ChatBubble.kt` — 砍掉 isSystemInDarkTheme()（4 處），頭像 AccentBlue/DarkBackground
- `ui/chat/bubble/BubbleContent.kt` — 砍掉 isSystemInDarkTheme()（5 處），抽出 PendingOverlay()
- `ui/chat/bubble/BubbleContextMenu.kt` — DropdownMenu 深色化 SurfaceMedium+14dp 圓角
- `ui/chat/bubble/BubbleStatusMetadata.kt` — 砍掉 isSystemInDarkTheme()（3 處），已讀 AccentGreen
- `ui/chat/bubble/VideoThumbnail.kt` — 背景 SurfaceDark，"▶"→Icons.Default.PlayArrow
- `ui/chat/bubble/VoiceMessageBubble.kt` — 波形動態 contentColor（DarkBackground/TextWhite）

#### 十、聊天主畫面與對話框（4 檔案）
- `ui/chat/ChatScreen.kt` — Scaffold containerColor=DarkBackground，isDarkTheme→true 常數
- `ui/chat/dialog/EndChatConfirmDialog.kt` — 砍掉 isSystemInDarkTheme()（4 處），確認 StatusError（破壞性操作）
- `ui/chat/dialog/OpponentProfileDialog.kt` — 砍掉 isSystemInDarkTheme()（11 處），拆 ProfileStat 元件

#### 十一、相機模組（6 檔案）
- `ui/camera/CameraCaptureScreen.kt` — 背景 DarkBackground，對焦框 StatusPending，錄影計時底 DarkBackground.copy(0.5f)
- `ui/camera/CameraControlButtons.kt` — 閃光燈 TextWhite/StatusPending，快門 TextWhite/StatusError/DarkBackground
- `ui/camera/CameraPreviewActions.kt` — 丟棄 StatusError，傳送 AccentGreen
- `ui/camera/ImagePreviewScreen.kt` — 背景 DarkBackground，"▶"→Icons.Default.PlayArrow
- `ui/camera/VideoPreviewPlayer.kt` — 背景 DarkBackground，Slider AccentGreen，重播/控制列底 DarkBackground.copy(0.6f)

#### 十二、錄音模組（1 檔案）
- `ui/voice/VoiceRecordingScreen.kt` — 遮罩 DarkBackground.copy(0.85f)，按鈕 StatusError，取消 TextGray

### 刪除
- `ui/theme/Color.kt`（已合併進 AppColors.kt）

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
