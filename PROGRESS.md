# 專案進度紀錄

## 專案資訊
- **專案名稱**: Android 聊天 App
- **技術架構**: Jetpack Compose + Firebase Realtime Database / Storage
- **目前階段**: 架構重構 + 配對系統完成，邁向 UI 打磨階段

---

## 已完成功能

### 51. 架構重構：事件分離 + Domain Layer + 滾動管理 + 生命週期 + 導航邊界
- **狀態**: ✅ 完成
- **說明**:
  - 事件分離：scrollTrigger / newMessageCount 從 ChatUiState 移至 SharedFlow
  - Domain Layer：3 個 UseCase（ObserveMessages / SendTextMessage / RecallMessage）
  - ChatScrollManager composable 統一管理滾動邏輯
  - VideoPlayerDialog / VoiceMessageBubble 加入 LifecycleEventObserver
  - showCameraCapture / showVoiceRecording 移出 ChatUiState 回歸本機狀態

### 52. Repository Interface + ExoPlayer SaveState + 恢復 ViewModel 完整實作
- **狀態**: ✅ 完成
- **說明**:
  - MessageRepositoryInterface 定義 14 個 public methods
  - VideoPlayerDialog 用 rememberSaveable 儲存播放位置
  - ExpertViewModel / SeekerViewModel 從 stub 恢復完整實作
  - 移除 Hilt 依賴（因 AGP 9.x 不相容）

### 53. Koin DI + Navigation Compose 遷移 + 棄用 API 清理
- **狀態**: ✅ 完成
- **說明**:
  - Koin 4.1.0 取代 Hilt（無需 Gradle plugin）
  - Navigation Compose NavHost 取代 NavigationViewModel（3 route）
  - 4 個 Factory 檔案刪除，ChatScreen 接收 NavArgs
  - 8 處棄用 API 清理（0 warnings）

### 54. 帳號系統：Firebase Auth + DataMigration + 登出
- **狀態**: ✅ 完成
- **說明**:
  - Firebase Auth（Email + Google Sign-In）取代 ANDROID_ID
  - DataMigrator 將舊 deviceId 路徑資料遷移至 uid 路徑
  - 全量 myDeviceId → userId（22 個檔案）
  - UserRepository + 暱稱基礎架構

### 55. 暱稱設定 UI + ChatTopBar/OpponentProfileDialog 顯示 + AuthViewModel 測試
- **狀態**: ✅ 完成

### 56. 功能補強：暱稱驗證 + 登出確認 + 自己的暱稱
- **狀態**: ✅ 完成

### 57. 離線支援：磁碟持久化 + keepSynced + 離線指示器
- **狀態**: ✅ 完成

### 58. 模糊化 AI 回答與真人配對邊界 + 黑名單機制
- **狀態**: ✅ 完成
- **說明**: 無專家時保持 matching；rejectExpertMatch 寫入黑名單；startAiPreview 3s timer

### 59. 角色路由拆分 + Theme 顏色統一 + 專家儀表板重構 + Cloud Functions
- **狀態**: ✅ 完成
- **新增**: Color.kt / RoleSelectScreen / AskQuestionScreen / MatchingDialog / AppTabRow / Cloud Functions 3 支

### 60. AskQuestionScreen 大改版 + 圖片預覽 + ChatInputBar 樣式同步
- **狀態**: ✅ 完成

### 61. 相機/相簿全螢幕顯示區分（isCameraCapture 旗標串接）
- **狀態**: ✅ 完成

### 62. 影片上傳修復：putBytes → putFile
- **狀態**: ✅ 完成

### 63. 影片暫停（pending）點擊防誤觸
- **狀態**: ✅ 完成

### 64. VideoPlayerDialog 雙重修復
- **狀態**: ✅ 完成

### 65. 相機照片全螢幕黑邊修復
- **狀態**: ✅ 完成

### 66. ScrollToBottomButton 初始修正
- **狀態**: ✅ 完成

### 67. FullScreenImageDialog 滑動時 ContentScale 修正
- **狀態**: ✅ 完成

### 68. FullScreenImageDialog 頁碼標示
- **狀態**: ✅ 完成

### 69. 傳送後一鍵到底按鈕誤出現修正
- **狀態**: ✅ 完成

### 70. AskQuestionScreen 媒體上傳：圖片/影片/語音隨問題送出
- **狀態**: ✅ 完成
- **新增**: SendMedia data class；SeekerViewModel 注入 MediaUploader

### 71. 移除 AppTabRow — 由 RoleSelectScreen 取代
- **狀態**: ✅ 完成

### 72. Auth 系統強化：驗證碼、密碼強度、domain 白名單、提示彈窗
- **狀態**: ✅ 完成

### 73. 聊天室 MessageList 空白狀態移除 + 轉場殘影修復
- **狀態**: ✅ 完成

### 74. Bug 修復：密碼驗證提示、註冊欄位殘留、彈窗卡住、Google 服務檢查
- **狀態**: ✅ 完成

### 75. AuthScreen 鍵盤改為 ADJUST_NOTHING + 三按鈕初始畫面 + 返回行為統一
- **狀態**: ✅ 完成

### 76. 錯誤提示改為浮動疊層（不推擠按鈕）
- **狀態**: ✅ 完成

### 77. 重設密碼頁面加入驗證碼欄位 + 路徑分離
- **狀態**: ✅ 完成

### 78. 驗證碼每日限制（重新加回）
- **狀態**: ✅ 完成

### 79. 新密碼頁面 + Cloud Function resetPassword + 降級寄信 + 跳轉登入
- **狀態**: ✅ 完成

### 80. 除錯 Log 加強
- **狀態**: ✅ 完成

### 81. Bug 修復：MatchingRepository 欄位不一致 + SeekerViewModel 死碼修復
- **狀態**: ✅ 完成

### 82. Auth 系統重構：DRY 驗證 + Race Condition + NavHost 整合 + UI 元件拆分
- **狀態**: ✅ 完成
- **新增**: AuthUtils / ToastOverlay / LoadingOverlay / CompactTextField / Route / WelcomePanel / LoginForm / ResetPasswordPanel
- **說明**: AuthScreen 610→155 行；7 個檔案抽出

### 83. 重構深化：UiText 密封類別 + AuthRepository 協程化 + 輸入層分離
- **狀態**: ✅ 完成
- **新增**: UiText sealed class（Dynamic / Resource）
- **說明**: AuthRepository 7 個 callback 改 suspend fun；輸入狀態回歸 UI 層 rememberSaveable

### 84. Bug 修復：AuthViewModel 缺少 flow.update import
- **狀態**: ✅ 完成

### 85. UI/UX 優化：導航轉場動畫
- **狀態**: ✅ 完成

### 86. UI/UX 優化：ExpertScreen Toast → Snackbar
- **狀態**: ✅ 完成

### 87. UI/UX 優化：移除未使用 import + 硬編碼修正
- **狀態**: ✅ 完成

### 88. UI/UX 優化：MatchingDialog 圖示改為 CircularProgressIndicator
- **狀態**: ✅ 完成

### 89. UI/UX 優化：AskQuestionScreen Snackbar 送出回饋 + Scaffold 容器
- **狀態**: ✅ 完成

### 90. FCM 推播通知（Push Notifications）
- **狀態**: ✅ 完成
- **新增**: FcmService / Cloud Functions 2 支 database trigger
- **說明**: 新訊息推播 / 專家接受推播 / 深連結自動導航

### 91. Bug 修復：ChatInputBar 缺少 FontWeight import
- **狀態**: ✅ 完成

### 92. DrawerContent 搜尋欄提示詞位置修正
- **狀態**: ✅ 完成

### 93. DrawerContent 支援 Google 頭像與暱稱同步
- **狀態**: ✅ 完成

### 94. AskQuestionScreen 還原金色背景光暈 + 輸入框居中
- **狀態**: ✅ 完成

### 95. Bug 修復：SeekerViewModel 殘留 TODO() 導致 Runtime 崩潰
- **狀態**: ✅ 完成

### 96. AskQuestionScreen 加入 BackHandler 返回支援
- **狀態**: ✅ 完成

### 97. 修復 SDK 路徑設定（local.properties）
- **狀態**: ✅ 完成

### 98. 背景光暈完整迭代（琥珀 → 藍色 → 黑色疊層 → 最終方案）
- **狀態**: ✅ 完成
- **最終**: BackgroundGlow.kt 單色 #2631C9 + 黑色 radial 暗角

### 99. 系統列黑色條問題（最終修復）
- **狀態**: ✅ 完成
- **說明**: 真正原因為 modifier 順序錯誤（.background 在 safeDrawing 前）

### 100. AppNavigation 重構：Box 外層包裹全螢幕背景光暈
- **狀態**: ✅ 完成

### 101. BackgroundGlow 簡化：多層漸層 → 單色 + 黑色 radial 暗角
- **狀態**: ✅ 完成

### 102. 移除提問頁面左上角漢堡選單按鈕
- **狀態**: ✅ 完成

### 103. Google 登入卡住修復：加上 20 秒逾時保護
- **狀態**: ✅ 完成

### 104. 架構修正批次：#5 路徑常數、#6 MediaUploader、#7 鍵盤修復、#3 Domain UseCase、#4 MatchCoordinator
- **狀態**: ✅ 完成
- **說明**: Route 常數集中 / StoragePath 統一上傳路徑 / Drawer 鍵盤修復 / 7 個 auth UseCase / MatchCoordinator 抽出

### 105. 背景光暈最終方案確定 + 黑色條真正原因修復
- **狀態**: ✅ 完成

### 106. FullSettingsScreen + DrawerContent 搜尋欄
- **狀態**: ✅ 完成

### 107. AiRepository.generateExpertTags() + 本地降級斷詞
- **狀態**: ✅ 完成

### 108. ExpertScreen 結構化輸入 + AI 關聯標籤
- **狀態**: ✅ 完成

### 109. MatchingRepository Jaccard 相似度升級
- **狀態**: ✅ 完成
- **說明**: Jaccard = intersectSize / unionSize，閥值 0.08

### 110. 每日提問配額防禦（max 3/day + 防多開）
- **狀態**: ✅ 完成
- **說明**: ValidateQuestionQuotaUseCase + quotaError state + UI 提示

### 111. AGENTS.md 規則強化
- **狀態**: ✅ 完成

### 112. SeekerViewModel 瘦身：抽離 3 個 UseCase（441→293 行）
- **狀態**: ✅ 完成
- **新增**: ValidateQuestionQuotaUseCase / ObserveQuestionStatusUseCase / SendQuestionMediaUseCase

### 113. 全螢幕 MatchingOverlay 取代 Snackbar + 延遲 activeChatRoomId
- **狀態**: ✅ 完成
- **新增**: MatchingOverlay.kt 全螢幕配對中覆蓋層

### 114. 配對流程修正：不跳轉 + 收起鍵盤 + 預防閃退
- **狀態**: ✅ 完成
- **說明**:
  - AI 回應不再觸發 activeChatRoomId，等待真人配對
  - onAiChatroomReady 改為空 lambda
  - 送出問題自動 focusManager.clearFocus()
  - startAiPreview 補上 try-catch 防止 Gemini API 崩潰

---

## 接下來的開發目標 (TODO)

### 115. UI 自動化測試：WelcomePanel Compose UI Test
- **狀態**: ✅ 完成
- **說明**:
  - 新增 `app/src/androidTest/java/.../ui/auth/WelcomePanelTest.kt`（10 test cases）
  - WelcomePanel.kt 加入 `testTag("googleSignInLoading")` 支援載入狀態測試
  - 測試 cover：所有 UI 元素顯示、載入狀態（ProgressIndicator + 按鈕禁用）、所有 callback 觸發（登入/註冊/Google/略過/條款/隱私/checkbox）
  - 使用 Compose UI Test `v2.createComposeRule()`（StandardTestDispatcher）
  - 編譯無錯誤/無警告

### 116. MatchingOverlay 動畫強化：進場 fadeIn + 尋找中打點動畫
- **狀態**: ✅ 完成
- **說明**:
  - MatchingOverlay.kt 新增「正在為您尋找合適的專家」尾綴循環打點動畫（0→3 點，1.5s 循環）
  - AskQuestionScreen.kt 以 `AnimatedVisibility` + `fadeIn/fadeOut(300ms)` 包裹 MatchingOverlay，進出場不再硬切

### 117. Bug 修復：取消配對未清除 Firebase 資料
- **狀態**: ✅ 完成
- **說明**:
  - `cancelMatching()` 原只將 question status 設為 "cancelled"，未刪除資料
  - 改為 `removeValue()` 刪除 `questions/<id>` + `chatrooms/ai_<id>` 兩個節點
  - 加入 `Log.e` 輸出刪除失敗原因

### 118.
- [ ] 待規劃

### 119.
- [ ] 待規劃
