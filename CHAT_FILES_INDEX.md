# ui/ 模組檔案索引

## `ui.seeker` — 提問者端

| 檔案 | 負責 |
|------|------|
| `AskQuestionScreen.kt` | 主畫面（瘦身版 ~110 行）：僅留狀態管理與流程控制 |
| `SelectedMedia.kt` | 媒體資料模型（`data class SelectedMedia(uri, isVideo, isVoice)`） |
| `RoleSelectScreen.kt` | 角色選擇入口：Canvas 動畫圖示（提問者氣泡浮動 / 專家燈泡呼吸）+ 兩張卡片 + 底部登出按鈕 |
| `MatchingDialog.kt` | 配對中 Dialog（載入動畫） |
| `SeekerConfirmDialog.kt` | 專家接受後確認 Dialog |
| `components/BackgroundGlow.kt` | 背景光暈 Modifier 擴充（`drawBackgroundGlow()`）：單色 `#2631C9` + 黑色 radial 暗角（`Black→Transparent`，半徑 `width*4`） |
| `components/AskQuestionHeader.kt` | 上方 ✨ 歡迎標題 |
| `components/AskQuestionInputBar.kt` | 輸入膠囊 + LazyRow 預覽 + `MediaPreviewItem` |
| `components/AttachmentBottomSheet.kt` | 底部附件彈窗 + 三卡片（相簿/相機/錄音） |

## `ui.expert` — 專家端

| 檔案 | 負責 |
|------|------|
| `ExpertScreen.kt` | 儀表板：影響力 Card + QuickLogCard + 知識庫列表 + 返回按鈕 |
| `ExpertDialogs.kt` | ExpertAssignDialog（全局接單）+ ExpertWaitingDialog（等待提問者） |

## `ui.common` — 共用 UI 元件

| 檔案 | 負責 |
|------|------|
| `OfflineBanner.kt` | 離線狀態指示器（橘底白字） |
| `AuthUtils.kt` | 驗證邏輯集中（validatePassword / validateNickname / isAllowedEmail / ALLOWED_DOMAINS） |
| `CompactTextField.kt` | 統一樣式輸入框（暗色背景 + 白色文字 + cursor 顏色） |
| `LoadingOverlay.kt` | 全螢幕半透明載入遮罩（CircularProgressIndicator） |
| `ToastOverlay.kt` | 浮動 Toast 訊息（頂部中央，3 秒自動消失） |
| `UiText.kt` | 封裝字串的 sealed class（Dynamic 動態 / Resource 支援 stringResource），ViewModel 不再硬編碼 |

## `ui.theme` — 主題

| 檔案 | 負責 |
|------|------|
| `Color.kt` | AppColors 物件（9 個命名顏色常數） |
| `Theme.kt` | MaterialTheme 設定（亮/暗色調色盤） |

## `ui.chat` — 聊天室主畫面（22 檔，已重組為 4 子目錄）

### 根層級（6 檔）

| 檔案 | 負責 |
|------|------|
| `ChatScreen.kt` | 主畫面 Composable；接收 NavArgs（expertId/expertText/expertDate）；已移除 `drawBackgroundGlow()`、`statusBarsPadding()`、`imePadding()`，僅留 `fillMaxSize().clickable{focusManager.clearFocus()}` |
| `ChatViewModel.kt` | ViewModel：StateFlow\<ChatUiState\> + SharedFlow\<ChatEvent\>、UseCase 協調；由 Koin viewModel() 注入 |
| `ChatMediaSender.kt` | 多媒體發送/Job 管理器：獨立處理圖片/影片/語音上傳與 pending/cancel 邏輯（原 ChatMediaManager）；傳入 isCameraCapture |
| `ChatUiState.kt` | data class：純畫面資料（無事件欄位） |
| `ChatEvent.kt` | sealed class：一次性事件（ScrollToBottom / ShowSnackbar / OpenCamera 等） |
| `ChatScrollManager.kt` | 滾動邏輯管理（初始滾動、鍵盤、打字、已讀標記） |

### `chat/bubble/` — 氣泡與多媒體渲染（7 檔）

| 檔案 | 負責 |
|------|------|
| `ChatBubble.kt` | 氣泡外殼：拼裝 BubbleStatusMetadata + BubbleContent + BubbleContextMenu，處理長按/高亮 |
| `BubbleContent.kt` | 內容工廠：根據資料型態分流渲染文字/圖片網格/影片縮圖/語音氣泡；pending overlay 防誤觸、allMediaUrls 優先遠端 URL |
| `BubbleContextMenu.kt` | 長按選單：複製（ClipboardManager）/ 回覆 / 收回（DropdownMenu） |
| `BubbleStatusMetadata.kt` | 狀態資訊：時間文字 + 已讀/未讀小圓點 |
| `ImageGrid.kt` | 圖片/影片網格排版（1–9 張自適應 + 圖片/影片 badge） |
| `VideoThumbnail.kt` | 影片縮圖（MediaMetadataRetriever 提取第一幀） |
| `VoiceMessageBubble.kt` | 語音氣泡（弧形聲波動畫 + MediaPlayer + Lifecycle 感知） |

### `chat/components/` — 基礎/複合排版元件（6 檔）

| 檔案 | 負責 |
|------|------|
| `ChatTopBar.kt` | 頂部標題列：返回 + 角色 + 結束/返回按鈕 + QuestionBanner |
| `MessageList.kt` | LazyColumn 訊息列表 + 載入提示 + TypingIndicator + ScrollToBottomButton（無空白狀態提示） |
| `ChatBottomArea.kt` | ReplyPreviewBar + ChatInputBar 組合 |
| `ChatInputBar.kt` | 輸入列：文字輸入（ImeAction.Send）+ 傳送/錄音 + BottomSheet 選單 |
| `ReplyPreviewBar.kt` | 回覆預覽橫條 |
| `TypingIndicator.kt` | 對方正在輸入動畫 |

### `chat/dialog/` — 彈窗控制（3 檔）

| 檔案 | 負責 |
|------|------|
| `ChatDialogHost.kt` | Dialog 條件渲染入口：相機/錄音/結束確認/評分/全螢幕圖片/影片播放器/對手資料 |
| `EndChatConfirmDialog.kt` | 結束對話確認 Dialog |
| `OpponentProfileDialog.kt` | 對手個人資料 Dialog |

## `ui.components` — 共用 UI 元件

| 檔案 | 負責 |
|------|------|
| `FullScreenImageDialog.kt` | 全螢幕圖片瀏覽器（滑動/縮放/拖移）；isCameraCaptureList → 每圖 Crop/Fit；多圖時頂部頁碼 badge |
| `VideoPlayerDialog.kt` | 影片播放器 Dialog（ExoPlayer + TextureView；DefaultDataSource 支援本地/遠端 URI） |
| `RatingDialog.kt` | 評分 Dialog（星星 + 意見） |
| `ScrollToBottomButton.kt` | 底部滾動按鈕（滑入/滑出動畫；reverseLayout 方向 + totalMessages 冷卻 800ms） |

## `ui.camera` — 相機功能

| 檔案 | 負責 |
|------|------|
| `CameraCaptureScreen.kt` | CameraX 相機入口：整合子元件，拍照/錄影/預覽/傳送（含 CameraTipText、RecordingTimerBanner） |
| `CameraControlButtons.kt` | 快門 + 閃光燈三態 + 鏡頭翻轉 |
| `CameraPreviewActions.kt` | 拍攝後丟棄/傳送按鈕 |
| `CameraViewModel.kt` | CameraX 生命週期 + 狀態管理 |
| `ImagePreviewScreen.kt` | 拍攝後圖片/影片預覽 |
| `VideoPreviewPlayer.kt` | 影片預覽播放器（MediaPlayer + TextureView） |

## `domain.chat` — 商業邏輯 UseCase

| 檔案 | 負責 |
|------|------|
| `SendMediaUseCase.kt` | 圖片/影片/語音上傳 + 寫入 DB；接受 isCameraCapture 參數 |
| `ObserveMessagesUseCase.kt` | Firebase listener → Flow（callbackFlow） |
| `SendTextMessageUseCase.kt` | 純文字訊息發送（含回覆） |
| `RecallMessageUseCase.kt` | 訊息收回（pending / confirmed + media 刪除） |

## `ui.auth` — 帳號系統

| 檔案 | 負責 |
|------|------|
| `AuthScreen.kt` | NavHost route 入口（~155 行）：路由邏輯（WelcomePanel / LoginForm / ForgotPasswordPanel / NewPasswordForm 四畫面切換）；`DisposableEffect` 強制 `ADJUST_NOTHING`；Google Sign-In 流程（Play Services 檢查 → launcher → 取得 idToken → signInWithGoogle）；導航事件收集（ShowLoginForm）、toast 收集、登入成功導航、`resetSent` AlertDialog |
| `AuthViewModel.kt` | Auth state 管理（~360 行）：StateFlow&lt;AuthUiState&gt;（僅業務欄位：isLoading / error(UiText?) / isRegisterMode / isLoggedIn / verificationSent / verificationSentTo / verificationLastSentAt / resetSent / resetLastSentAt / resetVerificationLastSentAt / showNewPasswordForm）；Channel&lt;UiText&gt; toastEvent（單一 Event Channel 消除 race condition）；Channel&lt;NavigationEvent&gt; navigateEvent；所有方法使用 `viewModelScope.launch { try { ... } catch { ... } }`；`submit(email, password, confirmPassword, nickname, code)` 一次性接收輸入 |
| `NicknameSettingsDialog.kt` | 暱稱修改對話框（即時驗證 + 寫入 UserRepository） |
| `WelcomePanel.kt` | 初始登入頁面（AppName Logo + Google 登入按鈕 + 密碼登入按鈕 + 註冊按鈕 + 同意條款 Checkbox + 略過） |
| `LoginForm.kt` | 登入/註冊表單面板：5 個 `rememberSaveable` 輸入欄位（email/password/confirmPassword/nickname/verificationCode），按鈕點擊時一次性傳遞給 ViewModel |
| `ResetPasswordPanel.kt` | 重設密碼面板：含 `ForgotPasswordPanel`（email + 驗證碼 + 傳送按鈕 + 下一步）與 `NewPasswordForm`（新密碼 + 確認新密碼 + 確認），各自持有 `rememberSaveable` 輸入狀態 |

## `data.model` — 資料模型

| 檔案 | 負責 |
|------|------|
| `ChatMessage.kt` | 訊息資料模型（含 isCameraCapture 旗標，區分相機/相簿照片） |

## `data.repository` — 資料存取層

| 檔案 | 負責 |
|------|------|
| `MessageRepository.kt` | Firebase Database CRUD（實作 `MessageRepositoryInterface`） |
| `MessageRepositoryInterface.kt` | Repository 介面（14 methods，利於測試注入 Fake） |
| `MessageRepositoryFactory.kt` | MessageRepository 工廠（依 chatroomId 建立不同 Firebase ref） |
| `ExpertRepository.kt` | 專家狀態/經驗/線上/全局接單 |
| `MatchingRepository.kt` | Bigram 配對邏輯與專家指派（實作 `MatchingRepositoryInterface`） |
| `MatchingRepositoryInterface.kt` | 配對介面（`matchAndAssignExpert`） |
| `QuestionRepository.kt` | 問題提問/狀態監聽/接受/拒絕/評分/重連檢查 |
| `MediaUploader.kt` | Firebase Storage 圖片/影片/語音上傳 + 刪除 |
| `AiRepository.kt` | AI 即時回覆整合（Gemini API） |
| `AuthRepository.kt` | FirebaseAuth 封裝、全部異步方法使用 `suspendCancellableCoroutine` 改為 `suspend fun`：`login(email, password)`、`register(email, password)`、`signInWithGoogle(idToken)`、`sendPasswordReset(email)`、`resetPasswordCloudFunction(email, verificationCode, newPassword)`、`generateVerificationCode(email, prefix)`（每日 3 次限制 + prefix 區分註冊/重設）、`verifyVerificationCode(email, code, prefix): Boolean`（比對後自動刪除）；`translateError()` 將 Firebase 異常轉為中文提示；Logcat 分 `RegCode` / `ResetCode` |
| `DataMigrator.kt` | ANDROID_ID → Firebase uid 資料遷移 |
| `UserRepository.kt` | 使用者暱稱存取 |

## `ui.navigation` — 導航

| 檔案 | 負責 |
|------|------|
| `AppNavigation.kt` | NavHost（5 route：auth / role_select / ask / expert / chat）+ `Box(drawBackgroundGlow)` 外層全螢幕背景 + Scaffold（內層 SnackbarHost）；`isLoggedIn` 決定 startDestination；登出 AlertDialog（依序 reset ViewModels + signOut + navigate）；`LaunchedEffect` 自動導航至聊天室 |
| `Route.kt` | 路由字串常數（AUTH / ROLE_SELECT / ASK / EXPERT / CHAT） |

## `di/` — 依賴注入（Koin）

| 檔案 | 負責 |
|------|------|
| `AppModule.kt` | Koin 模組定義（`single`：FirebaseDatabase/FirebaseStorage/FirebaseAuth/SharedPreferences/9 Repository；`viewModel`：4 ViewModel） |
| `ExpertViewModel.kt` | 專家端 ViewModel：線上狀態 / 經驗管理 / 全局接單 / 聊天導航 |
| `SeekerViewModel.kt` | 提問端 ViewModel：發問 / 配對 / 確認專家 / 評分 / 重連 |

## `ui.voice` — 錄音功能

| 檔案 | 負責 |
|------|------|
| `VoiceRecordingScreen.kt` | 錄音 Dialog（波形 + 秒數動畫 + 送出/取消） |
| `VoiceRecordingViewModel.kt` | MediaRecorder 管理 + 音頻位準 + 計時器 |

## `app/src/test/` — 單元測試

| 檔案 | 負責 |
|------|------|
| `ExpertViewModelTest.kt` | 4 tests：global assign listener 狀態轉換、accept/initial navigation |
| `SeekerViewModelTest.kt` | 4 tests：listenToMyQuestionStatus 四種狀態處理 |
| `AuthViewModelTest.kt` | 10 tests：login/register（含密碼不一致/暱稱空白驗證）/ sendPasswordReset / logout / toggleMode；使用 `coEvery` + `runTest` + `testDispatcher.scheduler.advanceUntilIdle()` 配合 suspend 函式 |
