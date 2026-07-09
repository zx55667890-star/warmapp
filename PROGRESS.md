# 專案進度紀錄

## 專案資訊
- **專案名稱**: Android 聊天 App
- **技術架構**: Jetpack Compose + Firebase Realtime Database / Storage
- **目前階段**: UI/UX 功能提升階段

---

## 已完成功能

### 1. 暗色模式適配
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`, `ChatInputBar.kt`, `ChatBubble.kt`, `AppTabRow.kt`, `TypingIndicator.kt`, `ExpertScreen.kt`, `InputQuestionScreen.kt`
- **說明**: 全面使用 `isSystemInDarkTheme()` 動態切換配色，TopBar / 輸入框 / 對話氣泡等皆已支援暗色模式

### 2. 系統狀態列
- **狀態**: ✅ 完成
- **修改檔案**: `MainActivity.kt`, `themes.xml`
- **說明**: 動態切換狀態列圖示顏色，暗色模式下自動顯示為白色

### 3. 聊天室畫面修正
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`
- **說明**: 修復 TopBar 重複 inset padding 導致標題下移問題，改用自訂 `Surface` + `Row` 取代 `TopAppBar`

### 4. 圖片瀏覽器（全螢幕）
- **狀態**: ✅ 完成
- **修改檔案**: `FullScreenImageDialog.kt`
- **說明**:
  - 全螢幕黑色背景顯示（`usePlatformDefaultWidth = false, decorFitsSystemWindows = false`）
  - 左右滑動切換圖片（累積 pan 位移判斷閾值 150px）
  - 雙指捏合縮放（以捏合中心點 `centroid` 為基準，`transformOrigin = TopLeft`）
  - 雙擊放大 / 縮小（以點擊位置為基準，放大至 2.5x）
  - 單擊關閉，放大後可自由拖移圖片
  - 切換圖片時自動重置縮放與位移
  - 移除頂部計數器與 X 關閉按鈕

### 5. 傳送按鈕圖示
- **狀態**: ✅ 完成
- **修改檔案**: `ChatInputBar.kt`, `build.gradle.kts`
- **說明**: 改用 `Icons.AutoMirrored.Filled.Send` 紙飛機圖示，旋轉 45° 校正方向

### 6. Bug 修復：RatingDialog props 不同步
- **狀態**: ✅ 完成
- **修改檔案**: `RatingDialog.kt`
- **說明**: 移除內部 `remember` 複製 props，改直接使用父層傳入的 state，解決點擊星星無反應

### 7. Dialog 暗色模式補強
- **狀態**: ✅ 完成
- **修改檔案**: `MatchingDialog.kt`, `SeekerConfirmDialog.kt`, `EndChatConfirmDialog.kt`, `OpponentProfileDialog.kt`
- **說明**: 為四個對話框補上 `isSystemInDarkTheme()` 動態配色（背景色、文字顏色、分隔線）並統一圓角樣式；`MatchingDialog` 額外加入載入動畫

### 8. 鍵盤自動收起
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`
- **說明**: 聊天室訊息列表區域加入 `Modifier.clickable(indication = null)`，點擊空白處收起鍵盤（`focusManager.clearFocus()`）

### 9. 相機功能 — CameraX 內建相機（Telegram 風格）
- **狀態**: ✅ 完成（多次迭代）
- **新增檔案**: `CameraCaptureScreen.kt`
- **修改檔案**: `ChatInputBar.kt`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, `ImageUtils.kt`, `ChatViewModel.kt`
- **說明**:
  - 使用 **CameraX** 實作內建相機（取代系統相機 Intent）
  - 全螢幕 CameraX 預覽，頂部半透明控制列
  - 底部：[閃光燈 OFF/ON/AUTO 三態切換] — [白色圓形快門] — [翻轉前後鏡頭]
  - 閃光燈 ON 模式使用 `imageCapture.flashMode` 拍照時閃光（非持續手電筒）
  - 切換閃光燈/鏡頭時不重新綁定 CameraX，避免畫面閃爍
  - **拍照**：點擊快門 → 拍照 → 預覽畫面 → 點「傳送」按鈕送出
  - **錄影**：長按快門 → 開始錄影（顯示紅點 + 計時器） → 點擊快門停止 → 預覽（顯示首幀 + ▶ 圖示） → 傳送
  - 錄影自動開啟麥克風（`RECORD_AUDIO` 權限已授予時），無則靜音錄影
  - **EXIF 方向校正**：`ImageUtils.kt` 新增 `readExifOrientation()` + `rotateBitmapIfNeeded()`，壓縮流程改為 讀 EXIF → 解碼 → 旋轉 → 縮放 → JPEG，解決直式照片變橫
  - 拍照/錄影後檔案暫存於 app 快取目錄
  - `ChatViewModel.sendImages()` 自動偵測 MIME type，影片直接上傳 bytes（不壓縮）
  - 新增 `camera-video` 依賴、`CAMERA` + `RECORD_AUDIO` 權限

### 10. Bug 修復：相機上傳後自動滾至底部
- **狀態**: ✅ 完成
- **修改檔案**: `ChatViewModel.kt`, `ChatScreen.kt`
- **說明**:
  - 改用計數器 `scrollTrigger: Int` 取代 `shouldScrollToBottom: Boolean`
  - 拆分兩個 `LaunchedEffect`：`scrollTrigger`（使用者送出）延遲 500ms 等待 Firebase listener 更新；`messages.size`（接收訊息）僅在 `scrollTrigger == 0` 時觸發，避免滾動回彈

### 11. Bug 修復：ExpertDialog 暗色模式、無法關閉、按鈕佈局
- **狀態**: ✅ 完成
- **修改檔案**: `ExpertDialogs.kt`
- **說明**:
  - 暗色模式動態配色：`ExpertAssignDialog` 與 `ExpertWaitingDialog` 背景色/文字色改用 `isSystemInDarkTheme()`
  - `ExpertAssignDialog` 的 `onDismissRequest` 從空操作改為 `onReject()`，可按返回鍵關閉
  - 按鈕移除 `fillMaxWidth()` 解決兩個全寬按鈕擠在同一 Row 的衝突

### 12. Bug 修復：InputQuestionScreen 缺少捲動與載入狀態
- **狀態**: ✅ 完成
- **修改檔案**: `InputQuestionScreen.kt`, `SeekerViewModel.kt`
- **說明**:
  - 外層 Column 加入 `verticalScroll(rememberScrollState())`，避免錯誤訊息在螢幕較小時被折疊
  - 送出按鈕加入 `isSending` 載入狀態，顯示 `CircularProgressIndicator` 並停用按鈕
  - `sendQuestion` 加入 `addOnFailureListener`，失敗時設定 `noExpertsMessage` 顯示錯誤

### 13. Bug 修復：editExperience 空白 ID 導致 UI 卡死
- **狀態**: ✅ 完成
- **修改檔案**: `ExpertRepository.kt`
- **說明**: `editExperience` 在 `experienceId.isBlank()` 時原本直接 `return`，導致 `editSubmitting` 永遠為 `true`。改為呼叫 `onError` 回呼。

### 14. Bug 修復：ChatScreen 快取 expertId 過期、按鈕文字
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`
- **說明**:
  - 移除 `val expertId = seekerViewModel.matchedExpertId` 快取值，改用 `seekerViewModel.matchedExpertId` 直接讀取
  - 「結束對話」按鈕在對話已結束時顯示「返回」

### 15. ChatBubble 影片播放功能
- **狀態**: ✅ 完成（多次迭代）
- **新增檔案**: `VideoPlayerDialog.kt`
- **修改檔案**: `ChatBubble.kt`, `ChatScreen.kt`, `CameraCaptureScreen.kt`, `app/build.gradle.kts`
- **說明**:
  - **播放器**：使用 `TextureView` + `MediaPlayer` 取代 `ExoPlayer` + `PlayerView`，解決 `SurfaceView` 在 `Dialog` 中黑畫面問題
  - **預覽**：相機拍完影片後，點擊 ▶ 圖示會使用 `TextureView` + `MediaPlayer` 實際播放影片
  - **相機提示**：快門上方顯示「點擊拍照、長按錄影」提示文字
  - **聊天室縮圖**：使用 `MediaMetadataRetriever` 從 Firebase Storage 影片網址提取第一幀作為縮圖，取代灰色佔位
  - 點擊影片縮圖開啟全螢幕播放器（點擊暫停/繼續、✕ 關閉）
  - 移除有問題的 `ExoPlayer`/`coil-video` 方案

### 16. 相機底部黑條修正 — 控制按鈕背景
- **狀態**: ✅ 完成
- **修改檔案**: `CameraCaptureScreen.kt`
- **說明**:
  - **問題**：相機底部控制按鈕列（閃光燈/快門/鏡頭切換）後方有 `.background(Color(0xAA000000))` 半透明黑色底條
  - **解決**：移除該 `.background()`，按鈕直接浮在相機畫面上
  - **備註**：此問題曾被誤解為系統導覽列（navigation bar）問題，經歷多次無效嘗試後才發現是控制按鈕背景色。所有相關的 Window 旗標實驗（`setDecorFitsSystemWindows`, `FLAG_LAYOUT_NO_LIMITS`, `ImmersioniveSticky` 等）已全部復原，`CameraCaptureScreen.kt` 與 `MainActivity.kt` 已恢復乾淨狀態

### 17. ChatInputBar 重構 — onCameraClick 回呼
- **狀態**: ✅ 完成
- **修改檔案**: `ChatInputBar.kt`, `ChatScreen.kt`
- **說明**:
  - `ChatInputBar` 移除內部 `showCameraCapture` 狀態與 `Dialog`，改為接受 `onCameraClick: () -> Unit` 回呼
  - 相機觸發邏輯上移至 `ChatScreen` 統一管理（`showCameraCapture` state + Box overlay）

### 18. 影片氣泡尺寸縮小
- **狀態**: ✅ 完成
- **修改檔案**: `ChatBubble.kt`
- **說明**:
  - 影片氣泡加入 `widthIn(max = 130.dp)` 限制最大寬度
  - 加入 `defaultMinSize(minWidth = 80.dp, minHeight = 80.dp)` 設定最小尺寸

### 19. 語音訊息氣泡（WeChat 風格聲波動畫）
- **狀態**: ✅ 完成
- **修改檔案**: `ChatBubble.kt`, `ChatViewModel.kt`
- **說明**:
  - 語音氣泡顯示：秒數（`2"`）+ 右箭頭三角形（▶）+ 3 個 `)` 弧形聲波
  - 使用 `drawArc(startAngle=300°)` 描繪右側弧形波紋（WiFi 風格）
  - 播放中動畫：使用 `rememberInfiniteTransition`，sweepAngle 從 60° 遞增至 130° 再遞減
  - 暫停時：sweepAngle 固定在 100°
  - 弧半徑：2.5/4.5/6.5.dp，Stroke width 2.dp，`StrokeCap.Round`
  - 右側喇叭使用 `Path` 繪製實心三角形
  - 移除波形直條改用弧形聲波
  - 移除喇叭 emoji 與複合 Canvas 繪製的喇叭圖示

### 20. Firebase Storage 規則 — 允許 `chat_voice/` 路徑
- **狀態**: ✅ 完成
- **說明**: 使用者在 Firebase Console 手動更新 Storage 規則，開放 `chat_voice/` 路徑的讀寫權限，解決語音上傳權限錯誤

### 21. 時間戳記移除日期，僅保留時間
- **狀態**: ✅ 完成
- **修改檔案**: `ChatBubble.kt`, `ChatScreen.kt`
- **說明**:
  - 訊息時間戳改為永遠顯示 `HH:mm`（如 `14:30`），不再顯示日期
  - 移除 `Calendar` 的日期判斷邏輯（同天顯示 `HH:mm`，跨天顯示 `MM/dd HH:mm`）

### 22. 效能優化：ChatBubble 參數重構
- **狀態**: ✅ 完成
- **修改檔案**: `ChatBubble.kt`, `ChatScreen.kt`
- **說明**:
  - `ChatBubble` 移除 `showTime` 參數，改為直接接收已計算好的 `timeText: String?`
  - 時間格式化邏輯上移至 `ChatScreen` 的 `itemsIndexed` 中處理
  - 減少 `ChatBubble` 內部重組與 `remember` 開銷

### 23. 效能優化：滾動卡頓修復
- **狀態**: ✅ 完成
- **修改檔案**: `ChatBubble.kt`, `ChatScreen.kt`
- **說明**:
  - `ChatScreen`：
    - `SimpleDateFormat` 實例快取在 `LazyColumn` 外，避免每幀重複建立
    - `isRead` 改用 `remember(msg.readBy, isMine, myDeviceId)`，只在資料變更時重新計算
    - `globalImageUrls` 改用 `derivedStateOf`，只在 `messages`/`pendingMessages` 真正變更時才計算（原 `remember(allMessages)` 因每次重組產生新 List 而無法正確快取）
  - `ChatBubble`：
    - **`MediaMetadataRetriever.setDataSource()`** 移到 `Dispatchers.IO` 背景執行緒，避免主執行緒阻塞
    - **移除 `rememberInfiniteTransition`**（每秒 60 幀重繪），改用 `LaunchedEffect` + `delay(300)` 循環，僅在播放時每 300ms 更新一次波紋索引
    - 清理未使用的 `androidx.compose.animation.core.*` import

### 24. Bug 修復：相機開啟時鍵盤跳出
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`
- **說明**: `onCameraClick` 在設定 `showCameraCapture = true` 前先呼叫 `focusManager.clearFocus()` 收起鍵盤

### 25. 影片播放器全面改用 ExoPlayer（Media3）+ 磁碟快取
- **狀態**: ✅ 完成
- **修改檔案**: `VideoPlayerDialog.kt`, `app/build.gradle.kts`
- **說明**:
  - 移除 `MediaPlayer` + `TextureView` + `SurfaceTextureListener`
  - 改用 `ExoPlayer` + `CacheDataSource` + 50MB 磁碟快取（`SimpleCache` + `LeastRecentlyUsedCacheEvictor`）
  - 使用 `TextureView` 直接接收 ExoPlayer 影片輸出，避免 `PlayerView` 在 Dialog 中的 `SurfaceView` 黑畫面問題
  - 加入 `CircularProgressIndicator` 載入 spinner（緩衝中顯示）
  - 加入錯誤處理（播放失敗顯示錯誤訊息）
  - 自訂播放/暫停 UI（關閉 PlayerView 內建控制器，保留原有的進度條與關閉按鈕）

### 26. 重構：CameraCaptureScreen / ChatViewModel 檔案拆分
- **狀態**: ✅ 完成
- **新增檔案**:
  - `RecordingTimerBanner.kt` — 錄影中紅點 + 計時器
  - `VideoPreviewPlayer.kt` — 影片預覽播放器（MediaPlayer + TextureView + Slider）
  - `CameraPreviewActions.kt` — 丟棄/傳送按鈕
  - `MessageRepository.kt` — Firebase Database 全部 CRUD 操作
  - `MediaUploader.kt` — Firebase Storage 圖片/語音上傳
- **修改檔案**: `CameraCaptureScreen.kt` (583→411 行), `ChatViewModel.kt` (410→210 行)
- **說明**:
  - `CameraCaptureScreen` 提取 3 個元件，快門按鈕因 gesture 與 camera state 耦合保留 inline
  - `ChatViewModel` 提取 `MessageRepository`（DB 操作）與 `MediaUploader`（Storage 上傳），ViewModel 僅保留 state 管理與協調
  - 所有新增檔案各約 30-80 行，符合單一職責原則
  - 已編譯通過（`./gradlew assembleDebug`）

---

### 27. 語音動畫優化：Animatable + LinearEasing
- **狀態**: ✅ 完成
- **修改檔案**: `VoiceMessageBubble.kt`
- **說明**:
  - 將原本的 `LaunchedEffect` + `delay(300)` 輪詢更新波紋索引，改為 `Animatable` + `LinearEasing` + `infiniteRepeatable` 驅動聲波動畫
  - 動畫過渡更加流暢（原本每 300ms 跳躍更新，現在每幀平滑過渡）
  - 播放中波紋從 `0f` → `1f` 循環，暫停時固定在 `0f`
  - 移除 `index` 狀態與 `LaunchedEffect` 輪詢邏輯

---

### 28. videoUrl 欄位擴充：資料模型 → ViewModel → UI
- **狀態**: ✅ 完成（多次迭代）
- **修改檔案**: `ChatMessage.kt`, `ChatViewModel.kt`, `ChatBubble.kt`, `ImageGrid.kt`, `CameraCaptureScreen.kt`, `ChatScreen.kt`, `MediaUploader.kt`
- **說明**:
  - `ChatMessage` 資料模型新增 `videoUrl: String = ""` 欄位
  - `ChatViewModel` 新增 `sendVideo(uri, onError)` 函數，直接上傳 bytes（不壓縮）
  - `MediaUploader` 新增 `uploadVideo(uri)` 函數，上傳至 `chat_video/` 路徑
  - `ImageGrid` 接受 `videoUrl` 參數，若存在則在對應位置顯示影片縮圖（`MediaMetadataRetriever` 提取第一幀）
  - `ChatBubble` 將 `videoUrl` 合併至 `urls` 列表，統一傳遞給 `ImageGrid`，移除直接呼叫 `VideoThumbnail`
  - `CameraCaptureScreen` 的 `onImageCaptured` 回調新增 `isVideo: Boolean` 參數
  - `ChatScreen` 根據 `isVideo` 分別呼叫 `sendVideo()` 或 `sendImages()`

---

### 29. UI 動畫強化：animateContentSize
- **狀態**: ✅ 完成
- **修改檔案**: `ChatBubble.kt`
- **說明**: `ChatBubble` 外層 Column 加入 `animateContentSize()`，讓氣泡在內容變化（如圖片載入、文字摺疊/展開）時有平滑過渡動畫

---

### 30. VoiceMessageBubble：DisposableEffect 資源釋放
- **狀態**: ✅ 完成
- **修改檔案**: `VoiceMessageBubble.kt`
- **說明**:
  - 加入 `DisposableEffect(Unit)` 區塊，確保 `MediaPlayer` 在 Composable 離開 Composition 時確實釋放
  - 解決離開聊天室時 MediaPlayer 可能未被釋放或背景繼續播放的問題

---

### 31. VideoPlayerDialog：ExoPlayer 資源釋放邏輯釐清
- **狀態**: ✅ 完成
- **修改檔案**: `VideoPlayerDialog.kt`
- **說明**:
  - `DisposableEffect` 於離開時呼叫 `player.release()` + `cache.release()`
  - `TextureView` 的 `SurfaceTextureListener` 中，`onSurfaceTextureDestroyed` 處不主動釋放 player（避免與 DisposableEffect 競爭狀態）
  - `surfaceTextureAvailable` 與 `surfaceTextureDestroyed` 成對處理，確保 ExoPlayer 在 surface 就緒時才開始播放

---

### 32. CameraCaptureScreen 模組化拆分（第二次迭代）
- **狀態**: ✅ 完成
- **新增檔案**:
  - `CameraPreviewSurface.kt` — 相機預覽綁定邏輯（~60 行）
  - `CameraControlButtons.kt` — 拍照/錄影控制按鈕與手勢邏輯（~120 行）
  - `CameraTipText.kt` — 相機操作提示文字（~30 行）
  - `CameraCaptureUI.kt` — 相機主 UI 佈局（~70 行）
  - `ImagePreviewScreen.kt` — 拍攝後圖片/影片預覽介面（~130 行）
- **修改檔案**: `CameraCaptureScreen.kt` (411→225 行)
- **說明**:
  - 將 `CameraCaptureScreen` 進一步拆分為 5 個獨立檔案，每個組件符合單一職責原則
  - `CameraCaptureScreen.kt` 僅保留 `CameraCaptureScreen` 與 `CameraContentWrapper`，作為整合入口
  - 修復 `previewView` 傳遞為 `null` 的 Bug（`cameraProvider.unbindAll()` 後 `processCameraReady` 設 false，重新綁定時正確傳遞 `previewView`）
  - 修復重複導入、Lambda 括號錯誤、不合法的 `Modifier.weight()` 等編譯錯誤
  - 已編譯通過（`./gradlew assembleDebug`）

---

### 33. ChatScreen / ChatViewModel 重構：ChatUiState + derivedStateOf
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`, `ChatViewModel.kt`
- **說明**:
  - **引入 `ChatUiState`**：定義 `data class ChatUiState` 統一管理所有 UI 狀態（Dialog 顯示、評分數據、全螢幕預覽、影片播放等），`ChatScreen` 由單一 `uiState` 驅動
  - **`derivedStateOf` 優化 `globalImageUrls`**：使用 `derivedStateOf` 計算圖片 URL 列表，只在 `messages`/`pendingMessages` 真正變更時才重新計算，解決每次重組掃描所有訊息的效能問題
  - **`fetchOpponentProfile()`**: 將獲取對手個人資料（評分、幫助次數）的邏輯從 UI 回調移至 ViewModel，遵循 MVVM 模式
  - **狀態清理**: 移除 `ChatScreen` 中散亂的 `remember` 狀態（`showEndConfirmDialog`, `showRatingDialog`, `showCameraCapture`, `showVoiceRecording`, `fullScreenImageUrls`, `fullScreenImageIndex`, `videoUrl`, `showOpponentProfile` 等），全部改用 `uiState`
  - `ChatScreen.kt` 行數從約 400 行降至約 260 行

---

### 34. Bug 修復：CameraCaptureScreen 不慎覆蓋
- **狀態**: ✅ 完成
- **修改檔案**: `CameraCaptureScreen.kt`
- **說明**:
  - 在清理過程中不慎將整個檔案內容覆蓋為 ChatScreen 內容，導致 CameraCaptureScreen 遺失
  - 已還原正確的 `CameraCaptureScreen.kt`（僅含 `CameraCaptureScreen` 與 `CameraContentWrapper`，依賴已拆分的子檔案）
  - 已編譯通過（`./gradlew assembleDebug`）

---

### 35. 語音訊息優化：秒數與波形間距、框體統一、跨裝置秒數不一致修復
- **狀態**: ✅ 完成
- **修改檔案**: `VoiceMessageBubble.kt`, `ChatMessage.kt`, `MessageRepository.kt`, `SendMediaUseCase.kt`, `ChatBubble.kt`
- **說明**:
  - **秒數與波形貼合**: 移除 `Spacer(8.dp)`，改為 `Canvas.offset(x = -5.dp)` 左移重疊，消除波形 Canvas 右側空白造成的視覺間距
  - **框體寬度**: `widthIn(min = 70.dp)` → `min = 60.dp`（配合約 4-5 字寬）
  - **框體高度統一**: 移除 `Row.padding(vertical = 4.dp)` 額外留白；Canvas 高度 24dp → 22dp，與文字行高一致
  - **跨裝置秒數不一致 Bug**: 根因為 `MediaMetadataHelper.getDuration()` 對 Firebase URL 回傳 0，`coerceAtLeast(1)` 強制顯示 1 秒
  - **修複方式**: `ChatMessage` 新增 `voiceDuration: Long` 欄位；上傳時用本地檔案路徑取得正確秒數存入 Firebase；對方讀取時直接使用 `msg.voiceDuration`

---

### 36. 訊息收回支援圖片/影片/錄音 + 權限修正
- **狀態**: ✅ 完成
- **修改檔案**: `MediaUploader.kt`, `SendMediaUseCase.kt`, `ChatViewModel.kt`, `ImageGrid.kt`, `VideoThumbnail.kt`, `VoiceMessageBubble.kt`, `ChatBubble.kt`
- **說明**:
  - **Storage 檔案刪除**: `MediaUploader` 新增 `deleteFilesByUrls()`，依 URL 刪除 Firebase Storage 檔案
  - **Pending 上傳取消**: `ChatViewModel` 新增 `pendingUploadJobs` map + `cancelledPendingIds` set；`sendImages/sendVideo/sendVoice` 追蹤 Job，收回時 cancel coroutine + 跳過後續寫入
  - **`recallMessage()` 擴充**: Pending 訊息取消上傳並移除 UI；已送出訊息同時刪除 DB 節點 + Storage 檔案（`imageUrl`/`imageUrls`/`videoUrl`/`voiceUrl`）
  - **長按觸控修復**: `ImageGrid`、`VideoThumbnail`、`VoiceMessageBubble` 改用 `combinedClickable(onClick, onLongClick)` 取代 `.clickable`，避免攔截父層長按事件
  - **權限修正**: 僅自己的訊息顯示「收回」選單（`isMine` 判斷），對方的訊息長按無反應

---

### 37. ChatInputBar UI 改造：BottomSheet 附選單 + 鍵盤 Send + 獨立按鈕
- **狀態**: ✅ 完成
- **修改檔案**: `ChatInputBar.kt`
- **說明**:
  - DropdownMenu 改為 `ModalBottomSheet`（拍照/相簿選擇/語音）
  - 鍵盤加入 `ImeAction.Send` + `KeyboardActions`
  - 傳送（紙飛機）和錄音分開兩個獨立 IconButton，有文字時傳送啟用、無文字時灰掉

### 38. 語音訊息動畫改進
- **狀態**: ✅ 完成
- **修改檔案**: `VoiceRecordingScreen.kt`, `VoiceMessageBubble.kt`
- **說明**:
  - 錄音秒數改用 `AnimatedContent` 垂直滑入/淡入動畫
  - 播放時倒數顯示（總秒數→剩餘秒數），播放完自動回到總秒數
  - 修復播放完再次點擊無反應（`seekTo(0)`）
  - 取消按鈕從左上 X 改為錄音鈕下方「取消」文字

### 39. 相機取消 + 狀態重置
- **狀態**: ✅ 完成
- **修改檔案**: `CameraCaptureScreen.kt`, `CameraViewModel.kt`
- **說明**:
  - 左上角新增「取消」文字關閉相機
  - `CameraViewModel.resetState()` 重置所有狀態，解決切換模式後重開卡住的問題
  - 取消時自動停止錄影

### 40. ScrollToBottomButton 重構
- **狀態**: ✅ 完成
- **修改檔案**: `ScrollToBottomButton.kt`, `ChatScreen.kt`
- **說明**:
  - 移至訊息列表底部正中央（`Alignment.BottomCenter`）
  - 動畫改為由下而上滑入/由上而下滑出
  - 改用 `KeyboardArrowDown` 圖示（純箭頭無直線），尺寸 36dp
  - 出現邏輯：往上滑隱藏、往下滑顯示、新訊息推入顯示

---

### 41. 鍵盤彈出 + 畫面佈局修正（多輪迭代）
- **狀態**: ✅ 完成
- **修改檔案**: `MainActivity.kt`, `ChatScreen.kt`
- **說明**:
  - `MainActivity.onCreate()` 加入 `window.setSoftInputMode(ADJUST_RESIZE)` 強制視窗在鍵盤彈出時調整大小，避免 manifest 設定被 Compose 覆蓋
  - `Scaffold` 的 `contentWindowInsets` 改為 `WindowInsets.statusBars.only(Top + Horizontal)`，不消耗鍵盤 insets，讓 keyboard insets 傳遞到內層
  - `ChatScreen` 外層 Box 保留 `.imePadding()`，確保整體上移；TopBar 固定在上方，訊息+輸入列一起上推
  - 移除所有內層 `imePadding()` / `systemBarsPadding()`

### 42. 接收訊息自動滾到底部（多次迭代）
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`, `ChatViewModel.kt`, `ChatUiState.kt`
- **說明**:
  - 引入 `newMessageCount: Int` 計數器（`ChatUiState`），每次 `updateDisplayMessages()` 遞增，作為新訊息觸發信號
  - 改用兩階段觸發器：`forceScrollRequest`（強制滾動，用於發送/鍵盤）和 `conditionalScrollRequest`（條件滾動，用於接收）
  - **條件滾動**：接收新訊息後 delay 150ms，檢查 `layoutInfo.visibleItemsInfo.lastOrNull()?.index` 與 `totalItems` 是否相距 ≤ 8（容錯閾值，應付連發訊息），符合才滾動
  - **補滾機制**：第一次條件滾動後 delay 80ms，再次檢查是否真正到底，若未到底則再滾一次
  - `scrollToItem`（無動畫）取代 `animateScrollToItem`，消除動畫延遲
  - 發送訊息後 delay 80ms（`forceScrollRequest`）確保 LazyColumn layout 完成才滾動
  - 移除舊的 `wasAtBottom` / `snapshotFlow` 做法（因 timing race condition 導致判斷不準）

### 43. 歷史訊息載入流暢度優化
- **狀態**: ✅ 完成
- **修改檔案**: `MessageRepository.kt`, `ChatScreen.kt`
- **說明**:
  - `PAGE_SIZE` 30 → 100，減少 Firebase 往返次數
  - 載入提示改為 `Box` 疊層顯示在 LazyColumn 上方（`Alignment.TopCenter`），而非在 LazyColumn 第一項，避免 prepend 時列表跳動
  - 加入 `loadCooldown` 機制，防止 prepend 後 `firstVisibleItemIndex` 仍為 0 而重複觸發載入

### 44. 移除 imageUploadProgress
- **狀態**: ✅ 完成
- **修改檔案**: `ChatUiState.kt`, `ChatViewModel.kt`
- **說明**: `ChatUiState` 刪除 `imageUploadProgress` 欄位；`ChatViewModel` 三個 media send 函式清除所有相關 state 更新，保留 `onProgress = {}` 空 lambda 因介面需求

### 45. VideoPlayerDialog 播放時隱藏關閉按鈕
- **狀態**: ✅ 完成
- **修改檔案**: `VideoPlayerDialog.kt`
- **說明**: ✕ 關閉按鈕加入 `if (!isPlaying)` 條件，播放時自動隱藏，暫停時顯示

### 46. 點擊空白處 ripple 效果關閉
- **狀態**: ✅ 完成
- **修改檔案**: `ChatScreen.kt`
- **說明**: 用於收起鍵盤的 `.clickable` 加入 `interactionSource = MutableInteractionSource()` + `indication = null`，消除點擊空白處的水波漣漪

### 47. ChatScreen 模組化拆分
- **狀態**: ✅ 完成
- **新增檔案**:
  - `MessageList.kt`（~194 行）— LazyColumn + ScrollToBottomButton + TypingIndicator + 載入提示
  - `ChatBottomArea.kt`（~48 行）— ReplyPreviewBar + ChatInputBar
  - `ChatDialogs.kt`（~110 行）— 所有 Dialog 條件渲染（EndChat / Rating / FullScreenImage / VideoPlayer / OpponentProfile / Camera / Voice）
- **修改檔案**: `ChatScreen.kt`（549 → 295 行）
- **說明**:
  - 將單一 549 行的 ChatScreen 按職責拆分為 4 個檔案
  - `ChatScreen.kt` 僅保留：scroll logic LaunchedEffect（~100 行）+ 主佈局 Column（TopBar + MessageList + BottomArea，~70 行）+ 外部 Box + ChatDialogs 呼叫（~30 行）
  - 每個新元件透過回呼與 ChatScreen 溝通，不直接依賴 ViewModel

### 48. ChatViewModel 精簡：Factory 獨立 + sendMedia 共用
- **狀態**: ✅ 完成
- **新增檔案**: `ChatViewModelFactory.kt`（~20 行）
- **修改檔案**: `ChatViewModel.kt`（390 → 306 行）
- **說明**:
  - `ChatViewModelFactory` 抽出為獨立檔案，移除 ViewModel 類別末端的工廠樣板
  - 新增私有 `sendMedia(pendingMsg, upload)` 函式，封裝共同的 media 上傳流程
  - `sendVideo` / `sendImages` / `sendVoice` 從各約 30-40 行降至 5-10 行

### 49. Package 拆分：chat / camera / voice
- **狀態**: ✅ 完成
- **新增資料夾**:
  - `ui/camera/`（6 個檔案）— 相機功能獨立模組
  - `ui/voice/`（2 個檔案）— 錄音功能獨立模組
- **修改檔案**: `ChatDialogs.kt`（加入跨 package import）
- **說明**:
  - 將原 `ui/chat/` 的 31 個檔按功能拆分為三個 package
  - `ui.chat`（17 檔）：聊天室核心
  - `ui.camera`（6 檔）：CameraX 相機（CaptureScreen 已內含 CameraTipText + RecordingTimerBanner）
  - `ui.voice`（2 檔）：錄音

### 50. Package 拆分：建立 ui.components
- **狀態**: ✅ 完成
- **新增資料夾**: `ui/components/`（4 個檔案）— 共用 UI 元件
- **移動檔案**:
  - `FullScreenImageDialog.kt` — 全螢幕圖片瀏覽器
  - `VideoPlayerDialog.kt` — 影片播放器
  - `RatingDialog.kt` — 評分 Dialog
  - `ScrollToBottomButton.kt` — 底部滾動按鈕
- **修改檔案**: `ChatDialogs.kt`, `MessageList.kt`（加入跨 package import）
- **說明**:
  - 將不依賴 `ui.chat` 的通用元件抽出為獨立 package
  - `ui.chat` 從 21 檔降至 17 檔

---

### 51. 架構重構：事件分離 + Domain Layer + 滾動管理 + 生命週期 + 導航邊界
- **狀態**: ✅ 完成
- **新增檔案**（6 個）:
  - `ChatEvent.kt` — sealed class 定義一次性事件（ScrollToBottom / ShowSnackbar / OpenCamera / OpenVoiceRecorder / ChatEndedByOther）
  - `ChatScrollManager.kt` — 滾動邏輯 composable（初始滾動、鍵盤、打字、已讀）
  - `ChatDialogHost.kt` — Dialog 條件渲染（取代 ChatDialogs）
  - `ObserveMessagesUseCase.kt` — Firebase listener 包裝為 Flow（callbackFlow）
  - `SendTextMessageUseCase.kt` — 純文字訊息發送 UseCase
  - `RecallMessageUseCase.kt` — 訊息收回 UseCase（pending + confirmed + media 刪除）
- **修改檔案**（10 個）:
  - `ChatUiState.kt` — 移除 `scrollTrigger` / `newMessageCount` / `sendErrorMessage` / `showCameraCapture` / `showVoiceRecording`
  - `ChatViewModel.kt` — 新增 `SharedFlow<ChatEvent>`、注入 UseCase 取代直接操作 MessageRepository
  - `ChatViewModelFactory.kt` — 注入 5 個 UseCase
  - `ChatScreen.kt` — 改用 `ChatScrollManager` + `ChatDialogHost`；`justSentMessage` 移除；events collect 取代 state 輪詢
  - `ChatDialogs.kt` → `ChatDialogHost.kt`（重新命名 + 改接收獨立 dialog state）
  - `VideoPlayerDialog.kt` — 加入 `LifecycleEventObserver`（ON_PAUSE 暫停、ON_STOP release）
  - `VoiceMessageBubble.kt` — 加入 `LifecycleEventObserver`
  - `ChatBottomArea.kt` — `sendErrorMessage` → `endChatError`（對應 UiState 變更）
- **刪除檔案**: `ChatDialogs.kt`
- **說明**:
  1. **#1 事件分離**：`scrollTrigger` / `newMessageCount` 從 `ChatUiState` 移至 `SharedFlow<ChatEvent>`，避免 State 變更觸發不必要 Recomposition
  2. **#2 Domain Layer**：ViewModel 不再直接操作 `MessageRepository`，改由 3 個新 UseCase 封裝商業邏輯（`ObserveMessagesUseCase` / `SendTextMessageUseCase` / `RecallMessageUseCase`）
  3. **#3 Composition 爆炸**：5 個 LaunchedEffect 滾動邏輯抽出至 `ChatScrollManager` composable；`ChatDialogs` 改名 `ChatDialogHost` 接收獨立 dialog state
  4. **#4 媒體生命週期**：`VideoPlayerDialog`（ExoPlayer）和 `VoiceMessageBubble`（MediaPlayer）加入 `LifecycleEventObserver`，ON_PAUSE 暫停、ON_STOP release
  5. **#5 導航邊界**：`showCameraCapture` / `showVoiceRecording` 移出 `ChatUiState`，改為 `ChatEvent.OpenCamera / OpenVoiceRecorder`，由 ChatScreen 以 `remember` 維護本機 dialog state
- `ui.chat` 檔案數：17 → 19（+ChatEvent / ChatScrollManager / ChatDialogHost，-ChatDialogs）
- `domain.chat` 檔案數：1 → 4（+ObserveMessages / SendTextMessage / RecallMessage UseCase）
- 已編譯通過（`./gradlew assembleDebug`）

---

### 52. Repository Interface + ExoPlayer SaveState + 恢復 ViewModel 完整實作
- **狀態**: ✅ 完成
- **新增檔案**（3 個）:
  - `MessageRepositoryInterface.kt` — 定義 14 個 public methods，利於單元測試注入 FakeRepository
  - `ExpertViewModelFactory.kt` — ViewModelProvider.Factory
  - `SeekerViewModelFactory.kt` — ViewModelProvider.Factory
- **修改檔案**（7 個）:
  - `MessageRepository.kt` — 實作 `MessageRepositoryInterface`，15 個 methods 加 `override`
  - `ObserveMessagesUseCase.kt` — 依賴改為 `MessageRepositoryInterface`
  - `SendTextMessageUseCase.kt` — 依賴改為 `MessageRepositoryInterface`
  - `RecallMessageUseCase.kt` — 依賴改為 `MessageRepositoryInterface`
  - `VideoPlayerDialog.kt` — 加入 `rememberSaveable` 保存 `currentPosition`；ON_PAUSE 先存位置；STATE_READY 時 `seekTo` 恢復
  - `ChatViewModelFactory.kt` — 修改為普通 factory（移除 Hilt）
  - `MainActivity.kt` — 移除 Hilt（`@AndroidEntryPoint`、`hiltViewModel()`），改用 `viewModel(factory=)` 手動建立
- **還原檔案**（2 個）:
  - `ExpertViewModel.kt` — 從 stub 恢復完整實作（~170 行）：initializeExpertStatus / setExpertOnline / publishExperience / stopExperience / editExperience / startGlobalAssignListener（直接 ValueEventListener）/ acceptGlobalAssignment / rejectGlobalAssignment / cancelWaiting / cleanup
  - `SeekerViewModel.kt` — 從 stub 恢復完整實作（~200 行）：sendQuestion / listenToMyQuestionStatus（status 轉換：taken→navigate、expert_accepted→confirm、no_experts→error、cancelled→reset）/ acceptExpertMatch / rejectExpertMatch / cancelUserMatching / checkReconnection / checkUserReconnection / submitRating / 60s match timeout
- **刪除檔案**（1 個）: `AppModule.kt`（Hilt artifact）
- **刪除 Hilt 依賴**: 移除 Hilt 2.51.1/2.52/2.53.1 嘗試，revert `build.gradle.kts` / `libs.versions.toml`（因 AGP 9.x 不相容，`BaseExtension` 已改名/移除）
- **測試修正**（2 個）:
  - `ExpertViewModelTest.kt` — 補 import、修正 `simulateSnapshot` 中 `every { child(any()).value }` 干擾特定 child matcher 導致 listener 無法更新 state 的問題
  - `SeekerViewModelTest.kt` — 補 import（`SeekerViewModel`、`MatchingRepositoryInterface`、`QuestionRepository`）
- **說明**:
  - **Repository Interface**: 新增 `MessageRepositoryInterface` 定義 14 個 public methods，UseCase 改依賴介面
  - **ExoPlayer SaveState**: `VideoPlayerDialog` 用 `rememberSaveable` 儲存播放位置，支援 configuration change 後續播
  - **Hilt 暫緩**: 因 AGP 9.x 與 Dagger Hilt Gradle plugin 不相容，維持純手動 Factory 模式
  - **ViewModels 恢復**: ExpertViewModel 和 SeekerViewModel 從被覆寫的 stub 完整恢復（根據 compiled class 反推 method signatures + 既有 tests）
  - **編譯通過**: `./gradlew assembleDebug`✅
  - **測試通過**: `./gradlew testDebugUnitTest` — 9 全綠✅
- `ui.chat` 檔案數：19 → 20（+ChatViewModelFactory 已獨立，+ChatViewModelFactory 重複計入？不變，ChatViewModelFactory 先前已存在為 1 檔，本次新增 ChatDialogHost 取代 ChatDialogs，net 不變）
- `data.repository` 新增：MessageRepositoryInterface（7 檔）
- `di/` 目錄：5 檔（ExpertViewModel、ExpertViewModelFactory、NavigationViewModel、SeekerViewModel、SeekerViewModelFactory）
- `app/src/test/` 新增/修正：3 檔

---

### 53. Koin DI + Navigation Compose 遷移 + 棄用 API 清理
- **狀態**: ✅ 完成
- **新增檔案**（2 個）:
  - `di/AppModule.kt` — Koin 模組（`single`：FirebaseDatabase、FirebaseStorage、SharedPreferences、ANDROID_ID、4 個 Repository；`viewModel`：ExpertViewModel、SeekerViewModel、ChatViewModel）
  - `ui/navigation/AppNavigation.kt` — NavHost（3 route：`"input"` / `"expert"` / `"chat/{chatroomId}/{myRole}/{expertId}/{expertText}/{expertDate}"`）+ Scaffold + AppTabRow；LaunchedEffect 監聽 activeChatRoomId 自動導航
- **修改檔案**（6 個）:
  - `MainActivity.kt` — 改為 `KoinApplication { modules(appModule) }`，移除 Hilt 殘留
  - `ChatScreen.kt` — 移除 `seekerViewModel` / `firebaseDb` 參數；改用 `koinViewModel(key=chatroomId)`；接收 `expertId/expertText/expertDate` 作為 NavArgs
  - `ChatDialogHost.kt` — 移除 `seekerViewModel` 參數；接收 `chatroomId/expertId/expertText/expertDate`；`QuestionRepository` 由 `koinInject()` 注入
  - `AppTabRow.kt` — `TabRow` → `PrimaryTabRow`（棄用 API）
  - `VoiceRecordingViewModel.kt` — `MediaRecorder()` → `MediaRecorder(context)`（API 31+）
  - `VideoCacheManager.kt` — `SimpleCache(File, CacheEvictor)` → `SimpleCache(File, CacheEvictor, DatabaseProvider)`；新增 `media3-database` 依賴
  - `CameraCaptureScreen.kt` — `LocalLifecycleOwner` 改用 `lifecycle-runtime-compose` 路徑；`defaultDisplay` 改用 `currentWindowMetrics`（API 31+）
- **刪除檔案**（4 個）:
  - `di/ExpertViewModelFactory.kt` — 不再需要（Koin viewModel 取代）
  - `di/SeekerViewModelFactory.kt` — 同上
  - `di/NavigationViewModel.kt` — Navigation Compose 取代
  - `ui/chat/ChatViewModelFactory.kt` — 不再需要（Koin viewModel 取代）
- **說明**:
  - **Koin DI 取代 Hilt**：因 AGP 9.x + Hilt Gradle plugin 不相容，改用純 Kotlin DI 框架 Koin 4.1.0（無需 Gradle plugin）
  - **Navigation Compose 取代 NavigationViewModel**：用 `androidx.navigation.compose:2.8.0` + NavHost 管理三個 route，取消全域字串狀態
  - **專家資訊改為 NavArgs**：原先 ChatScreen 透過 `seekerViewModel` 讀取 `matchedExpertId/expertText/expertDate`，現由 Navigation Argument 傳入
  - **ChatDialogHost 獨立**：不再接收 `seekerViewModel`，自行注入 `QuestionRepository`
  - **棄用 API 清理**：共處理 8 處警告（TabRow / MediaRecorder / SimpleCache / LocalLifecycleOwner / defaultDisplay / setTargetAspectRatio / setDecorFitsSystemWindows / SOFT_INPUT_ADJUST_RESIZE）
- **編譯通過**: `./gradlew assembleDebug` ✅（0 warnings）
- **測試通過**: `./gradlew testDebugUnitTest` — 9 全綠 ✅
- `di/` 目錄：5 → 3 檔（刪除 Factory、NavigationViewModel；新增 AppModule）
- `ui.chat` 檔案數：20 → 19（刪除 ChatViewModelFactory）
- 新增 `ui/navigation/`：1 檔

---

### 54. 帳號系統：Firebase Auth + DataMigration + 登出
- **狀態**: ✅ 完成
- **新增檔案**（4 個）:
  - `data/repository/AuthRepository.kt` — 封裝 FirebaseAuth（login / register / logout / state listener）
  - `ui/auth/AuthViewModel.kt` — 登入/註冊 UI state 管理（email/password/loading/error）
  - `ui/auth/AuthScreen.kt` — 登入/註冊畫面（email + 密碼 + 確認密碼）
  - `data/repository/DataMigrator.kt` — 首次登入自動將 ANDROID_ID 路徑資料遷移至 uid 路徑（`experts/{deviceId}` → `experts/{uid}`、`questions.authorId/expertId/rejectedExperts` 更新）
  - `data/repository/UserRepository.kt` — 使用者暱稱存取
- **修改檔案**（22 個）:
  - `gradle/libs.versions.toml` — 新增 `firebaseAuth = "23.2.0"`、`firebase-auth` library
  - `app/build.gradle.kts` — 新增 `firebase-auth` 依賴
  - `di/AppModule.kt` — 新增 FirebaseAuth / AuthRepository / DataMigrator / UserRepository singleton、AuthViewModel
  - `ui/navigation/AppNavigation.kt` — 啟動時檢查 Auth 狀態；未登入顯示 AuthScreen；登入後呼叫 DataMigrator.migrateIfNeeded()；AppTabRow 加入登出 IconButton（ExitToApp 圖示）
  - `data/repository/MessageRepositoryInterface.kt` — myDeviceId → userId
  - `data/repository/MessageRepository.kt` — myDeviceId → userId（replaceAll 17 處）
  - `data/repository/ExpertRepository.kt` — deviceId → userId（4 處 method signature + body）
  - `data/repository/QuestionRepository.kt` — deviceId → userId（4 處）
  - `data/repository/MatchingRepositoryInterface.kt` — deviceId → userId
  - `data/repository/MatchingRepository.kt` — deviceId → userId
  - `data/repository/MediaUploader.kt` — myDeviceId → userId
  - `domain/chat/ObserveMessagesUseCase.kt` — myDeviceId → userId
  - `domain/chat/SendTextMessageUseCase.kt` — myDeviceId → userId
  - `domain/chat/SendMediaUseCase.kt` — myDeviceId → userId（11 處）
  - `di/ExpertViewModel.kt` — deviceId → userId（10 處）
  - `di/SeekerViewModel.kt` — deviceId → userId（7 處）
  - `ui/chat/ChatViewModel.kt` — myDeviceId → userId（17 處）
  - `ui/chat/ChatScreen.kt` — myDeviceId → userId
  - `ui/chat/ChatDialogHost.kt` — myDeviceId → userId
  - `ui/chat/MessageList.kt` — myDeviceId → userId
  - `ui/seeker/InputQuestionScreen.kt` — myDeviceId → userId
  - `ui/expert/ExpertScreen.kt` — myDeviceId → userId
  - `ui/expert/ExpertDialogs.kt` — deviceId → userId
  - `ui/chat/ChatTopBar.kt` — 新增 `opponentNickname` 參數，取代硬編碼「提問者暱稱/專家暱稱」
  - `ui/chat/ChatUiState.kt` — 新增 `opponentNickname` 欄位
- **說明**:
  - **Firebase Auth 取代 ANDROID_ID**：使用者以 Email + 密碼註冊/登入，取得 Firebase uid
  - **全量重新命名**：所有 Repository / UseCase / ViewModel / Screen 中的 `deviceId` / `myDeviceId` 改為 `userId`，值來源從 ANDROID_ID 改為 `authRepository.currentUserId`
  - **資料遷移**：`DataMigrator` 在首次登入時自動將舊 ANDROID_ID 路徑下的資料搬移至 Firebase uid 路徑，遷移標記寫入 SharedPreferences
  - **登出功能**：AppTabRow 右側新增登出圖示，點擊後清除 Auth 狀態並返回登入畫面
  - **暱稱基礎架構**：`UserRepository` + `ChatUiState.opponentNickname`，ChatTopBar 預設顯示「提問者 / 專家」
- **編譯通過**: `./gradlew assembleDebug` ✅（0 warnings）
- **測試通過**: `./gradlew testDebugUnitTest` — 9 全綠 ✅
- `data.repository` 檔案數：7 → 10（+AuthRepository、DataMigrator、UserRepository）
- `ui/` 新增 `auth/` 子目錄：2 檔（AuthScreen、AuthViewModel）
- `ui.chat` 檔案數：19 → 20（+ChatDialogHost 已在#51新增）
- `di/` 目錄：3 檔（AppModule、ExpertViewModel、SeekerViewModel）
- 專案預估總行數約 8000+ 行 Kotlin

---

### 55. 暱稱設定 UI + ChatTopBar/OpponentProfileDialog 顯示 + AuthViewModel 測試
- **狀態**: ✅ 完成
- **新增檔案**（2 個）:
  - `ui/auth/NicknameSettingsDialog.kt` — 暱稱修改對話框（讀取/寫入 UserRepository）
  - `app/src/test/.../AuthViewModelTest.kt` — 11 項測試（login/register/logout/resetPassword state 變更）
- **修改檔案**（7 個）:
  - `ui/navigation/AppNavigation.kt` — AppTabRow 加入 Settings 圖示按鈕 + NicknameSettingsDialog 呼叫
  - `ui/chat/ChatScreen.kt` — 專家角色時從 messages 推斷 opponentId 並擷取暱稱
  - `ui/chat/ChatViewModel.kt` — 注入 UserRepository；`fetchOpponentProfile` 同時取得暱稱
  - `ui/chat/ChatDialogHost.kt` — OpponentProfileDialog 傳遞 `opponentNickname`
  - `ui/chat/OpponentProfileDialog.kt` — 新增 `nickname` 參數，對話框顯示暱稱
  - `di/AppModule.kt` — ChatViewModel 注入 UserRepository
  - `app/src/test/.../ExampleUnitTest.kt` — 移除（佔位檔）
- **說明**:
  - **暱稱設定**：AppTabRow 右側新增齒輪圖示，點擊開啟 NicknameSettingsDialog（可檢視/修改暱稱，寫入 Firebase Realtime Database `users/{uid}/nickname`）
  - **暱稱顯示**：ChatTopBar 顯示對手暱稱（使用 UserRepository.getNickname）；OpponentProfileDialog 新增暱稱欄位
  - **AuthViewModel 測試**：11 項測資覆蓋 init/login/register（含密碼不一致/暱稱空白驗證）/sendPasswordReset/dismissResetSent/logout/toggleMode
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 20 全綠 ✅
- `ui.auth` 檔案數：2 → 3（+NicknameSettingsDialog）
- `app/src/test/` 檔案數：3 → 3（-ExampleUnitTest，+AuthViewModelTest）

---

### 56. 功能補強：暱稱驗證 + 登出確認 + 自己的暱稱
- **狀態**: ✅ 完成
- **修改檔案**（5 個）:
  - `ui/auth/AuthViewModel.kt` — `submit()` 加入 `validateNickname(nickname): String?` 檢查（≤20 字元 + 僅允許文字/數字/空格/底線/連字號/間隔號/句點）
  - `ui/auth/NicknameSettingsDialog.kt` — 輸入時即時驗證 + `isError`/`supportingText` 顯示錯誤 + 儲存前再次驗證
  - `ui/navigation/AppNavigation.kt` — 登出按鈕改為先彈出 `AlertDialog` 確認「確定要登出嗎？」
  - `ui/chat/ChatScreen.kt` — 新增 `myNickname` 狀態，`LaunchedEffect` 擷取自己的暱稱
  - `ui/chat/ChatTopBar.kt` — 新增 `myNickname` 參數，對手名稱下方顯示「我: 暱稱」
- **說明**:
  - 暱稱驗證同時套用在註冊（AuthViewModel.submit）和修改（NicknameSettingsDialog），共用 `validateNickname()` companion function
  - 暱稱允許 Unicode 文字（含中文）、數字、空格、底線、連字號（-）、間隔號（·）、句點（.）
  - 登出確認對話框有「確定登出」和「取消」兩個按鈕
  - ChatTopBar 在 opponent nickname 下方以灰字 12sp 顯示「我: 自己的暱稱」
- **編譯通過**: `./gradlew assembleDebug` ✅（0 warnings）
- **測試通過**: `./gradlew testDebugUnitTest` — 20 全綠 ✅

---

### 57. 離線支援：磁碟持久化 + keepSynced + 離線指示器
- **狀態**: ✅ 完成
- **新增檔案**（1 個）:
  - `ui/common/OfflineBanner.kt` — 離線狀態指示器（`.info/connected` 監聽，`AnimatedVisibility` 滑入/滑出，橘底白字「離線中 — 資料仍可讀取，變更將在恢復連線後同步」）
- **修改檔案**（2 個）:
  - `di/AppModule.kt` — `FirebaseDatabase.getInstance()` 加入 `setPersistenceEnabled(true)` + 4 條關鍵路徑 `keepSynced(true)`（questions / experts / experiences / users）
  - `ui/navigation/AppNavigation.kt` — 登入/未登入畫面頂部加入 `OfflineBanner()`
- **說明**:
  - **磁碟持久化**：`setPersistenceEnabled(true)` 啟用 Firebase SDK 內建磁碟快取，中斷連線時自動使用本地快取，恢復後自動同步
  - **預先同步**：`keepSynced(true)` 對 4 條常用根路徑保持背景監聽，確保離線前已有最新資料
  - **離線指示器**：監聽 `/.info/connected` 虛擬路徑，斷線時顯示橘色 Banner，包含「離線中」提示文字
  - **資源釋放**：`DisposableEffect` 確保離開 Composition 時移除 listener
- **編譯通過**: `./gradlew assembleDebug` ✅（0 warnings）
- **測試通過**: `./gradlew testDebugUnitTest` — 20 全綠 ✅
- `ui.common` 新增 OfflineBanner.kt（4 檔）

---

### 58. 模糊化 AI 回答與真人配對邊界 + 黑名單機制
- **狀態**: ✅ 完成
- **修改檔案**（4 個）:
  - `data/repository/MatchingRepository.kt` — 無專家時不再設 status 為 "no_experts"，改為保持 "matching"（清空 expertId），問題持續開放
  - `data/repository/QuestionRepository.kt` — `rejectExpertMatch()` 擴充清除 `expertId` / `matchedExpText` / `matchedExpTimestamp`
  - `di/SeekerViewModel.kt` — `rejectExpertMatch()` 實作（加入 rejectedExperts + 回到 matching）；新增 `startAiPreview()` timer（3s 後若仍 matching 且無 expertId 則觸發 AI）；`sendQuestion()` 改為同時啟動 AI preview + matching timeout；"no_experts" handler 降為 legacy fallback
  - `app/src/test/.../SeekerViewModelTest.kt` — 更新 test name 反映新行為
- **說明**:
  1. **黑名單修復**：`rejectExpertMatch()` 確實將被拒專家寫入 `rejectedExperts/{expertId}`，並清空 expertId/matchedExpText 回到 matching
  2. **AI 回答後保持配對**：MatchingRepository 不再設 "no_experts" terminal status；AI preview 由 3s timer 觸發（`startAiPreview`），同時 60s timeout 繼續運行，若中途有專家接單則正常進入 `expert_accepted` → `taken` 流程
  3. **靜態相容**：既有 "no_experts" 狀態的問題仍能正常處理（legacy handler）
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 20 全綠 ✅

---

### 59. 角色路由拆分 + Theme 顏色統一 + 專家儀表板重構 + 混合 AI/真人配對 + Cloud Functions
- **狀態**: ✅ 完成
- **新增檔案**（8 個）:
  - `ui/theme/Color.kt` — AppColors 物件（DarkBackground / DarkSurface / AccentBlue / AccentGreen / TextWhite / TextGray / TextLightGray / BorderGray / AccentYellow）
  - `ui/seeker/RoleSelectScreen.kt` — 角色選擇入口（AnimatedAskerIcon / AnimatedExpertIcon Canvas 動畫 + 兩張卡片）
  - `ui/seeker/AskQuestionScreen.kt` — 提問表單 route（問題標題/描述 + 送出/取消配對 + SeekerConfirmDialog）
  - `ui/seeker/MatchingDialog.kt` — 配對中 Dialog（含載入動畫）
  - `ui/common/AppTabRow.kt` — TabRow 元件（已隱藏除 chat 外所有 route）
  - `functions/index.js` — 3 個 Firebase Cloud Function（onQuestionCreated Bigram 配對 / onExpertOnlineChanged 上線即配 / cleanupStaleAcceptances 每分鐘清理過期 expert_accepted）
  - `functions/package.json` — Node 18 + firebase-admin + firebase-functions
- **修改檔案**（7 個）:
  - `ui/navigation/AppNavigation.kt` — 路由改為 role_select / ask / expert / chat；TabRow 判斷條件更新；「input」字串全部取代為「role_select」
  - `ui/seeker/InputQuestionScreen.kt` — 已刪除（拆分為 RoleSelectScreen + AskQuestionScreen）
  - `ui/expert/ExpertScreen.kt` — 改用 theme-aware 顏色（MaterialTheme.colorScheme.surfaceVariant / onSurfaceVariant / primary）；返回按鈕移至底部
  - `di/ExpertViewModel.kt` — 補回 setExpertOnline + listenToSolutions；submitSolution 改由參數傳入 userId
  - `data/repository/ExpertRepository.kt` — saveSolution 原子寫入（solution + ServerValue.increment(1)）+ listenToSolutionHistory
  - `data/repository/MatchingRepository.kt` — 無專家時不再設 no_experts，僅清空 expertId 保持 matching
  - `data/repository/QuestionRepository.kt` — rejectExpertMatch 擴充清除 expertId/matchedExpText/matchedExpTimestamp
- **說明**:
  1. **角色路由拆分**：將 `showQuestionForm` 布林控制改為獨立 NavHost route（role_select + ask），Android 返回鍵行為原生正確
  2. **Theme 顏色集中**：所有 Screen 從 AppColors 常數讀取顏色，未來調色只需改一個檔案
  3. **專家儀表板**：LazyColumn 三區塊（影響力 Card / QuickLogCard / 📜知識庫列表 + 空狀態）
  4. **混合 AI + 真人**：無專家時保持 matching + AI preview 3s timer，背景持續配對；被拒專家寫入 rejectedExperts 黑名單
  5. **Cloud Functions**：Bigram 配對、上線即配、超時清理（需手動 `firebase deploy --only functions`）
  6. **返回閃退修復**：4 處「input」殘留 route 字串改為「role_select」
  7. **兩個 Canvas 動畫圖示**：提問者（對話氣泡浮動）+ 專家（智慧燈泡呼吸發光）
- **編譯通過**: `./gradlew assembleDebug` ✅（0 warnings）
- **測試通過**: `./gradlew testDebugUnitTest` — 20 全綠 ✅
- `ui.seeker` 檔案數：0 → 4（RoleSelectScreen / AskQuestionScreen / MatchingDialog / SeekerConfirmDialog 先前已存在）
- `ui.theme` 新增 Color.kt（2 檔）
- `ui.common` 檔案數：4 → 5（+AppTabRow）
- `ui.expert` 檔案數：2 → 3（+ExpertAssignDialog/ExpertWaitingDialog 先前已存在，+ExpertScreen）
- `functions/` 新增 2 檔

---

### 60. AskQuestionScreen 大改版 + 圖片預覽 + ChatInputBar 樣式同步
- **狀態**: ✅ 完成
- **修改檔案**（3 個）:
  - `ui/seeker/AskQuestionScreen.kt` — 全面改寫
  - `ui/chat/ChatInputBar.kt` — 同步輸入框樣式
- **新增功能**（AskQuestionScreen）:
  1. **背景光暈**：兩個 `radialGradient`（`#361C0A` 琥珀暗金）從左下/右下點亮底部，使用 `drawBehind` 繪製
  2. **Gemini 風格膠囊輸入框**：`Surface(color = #1A1A1E, shape = RoundedCornerShape(32.dp))` 取代舊的 Column 背景
  3. **TextField 容器透明**：`focusedContainerColor = Color.Transparent` + `unfocusedContainerColor = Color.Transparent`，與膠囊背景融為一體
  4. **圖片選取預覽**：選相簿後直接在輸入區上方顯示 56x56dp 圓角縮圖 LazyRow，每張可個別移除（✕ 按鈕），傳送時清空
  5. **UI 微調**：`+` 字改 24sp、傳送按鈕縮為 32dp 圓形、maxLines 5
- **樣式同步**（ChatInputBar）:
  1. 邊框全設 `Color.Transparent`（無框線）
  2. `cursorColor` → `#888888`
  3. 傳送按鈕背景改為中性灰（無綠色）
  4. Column 背景設為 `Color.Black` 與螢幕一致
- **編譯通過**: `./gradlew assembleDebug` ✅
- **檔案變更**: `ui.seeker/AskQuestionScreen.kt` 371 行（大幅改寫）
- **附註**: 相簿選取後僅存 Uri 預覽，上傳/發送尚未與圖片結合
- **已刪除**: `AppColors.DarkSurface` 不再用於 AskQuestionScreen（`#1A1A1E` Surface 直接取代）

---

### 61. 相機/相簿全螢幕顯示區分（isCameraCapture 旗標串接）
- **狀態**: ✅ 完成
- **新增欄位**: `ChatMessage.isCameraCapture: Boolean`
- **修改檔案**: `ChatMessage.kt`, `SendMediaUseCase.kt`, `ChatMediaSender.kt`, `ChatDialogHost.kt`, `ChatUiState.kt`, `ChatScreen.kt`, `FullScreenImageDialog.kt`, `ChatViewModel.kt`
- **說明**:
  - `ChatMessage` 新增 `isCameraCapture: Boolean = false` 欄位
  - `SendMediaUseCase.createPendingMessage()` 接受 `isCameraCapture` 參數
  - `ChatMediaSender.sendImages()/sendVideo()` 接受 `isCameraCapture` 參數（相機傳入 true）
  - `ChatDialogHost` 從相機呼叫時傳入 `isCameraCapture = true`
  - `ChatUiState` 新增 `fullScreenImageIsCameraCapture: Boolean`
  - `ChatScreen.onImageClick` 依 `allMessages.find` 匹配訊息取出 `isCameraCapture`
  - `FullScreenImageDialog` 接收 `isCameraCapture`：true → `ContentScale.Crop`（填滿無黑邊）、false → `ContentScale.Fit`（保留原始比例）
  - `ChatViewModel` observer reconciliation 也複製 `isCameraCapture` 避免 Firebase 回傳後遺失

### 62. 影片上傳修復：putBytes → putFile
- **狀態**: ✅ 完成
- **修改檔案**: `MediaUploader.kt`
- **說明**:
  - 影片上傳原本用 `putBytes()`（整支讀進記憶體 + 10MB 限制 + `ContentResolver.openInputStream` 對 `file://` URI 可能回傳 null）
  - 改為 `putFile(uri, metadata)`（直接串流上傳，支援所有 URI 類型，無檔案大小限制，與語音上傳一致）

### 63. 影片暫停（pending）點擊防誤觸
- **狀態**: ✅ 完成
- **修改檔案**: `BubbleContent.kt`
- **說明**: pending overlay 的 `Box` 加入 `.clickable { }` 吃掉觸控事件，避免上傳完成前點擊本地 `file://` URI 導致 ExoPlayer 播放錯誤

### 64. VideoPlayerDialog 雙重修復
- **狀態**: ✅ 完成
- **修改檔案**: `VideoPlayerDialog.kt`
- **說明**:
  - **Scope bug**：`player = exoPlayer` 原本在 `PlayerView(ctx).apply { }` 區塊內，設到 `PlayerView.player` 而非 composable state，導致 lifecycle 管理、進度輪詢、播放/暫停全部失效。已將 ExoPlayer 建立移至 apply 區塊外
  - **DataSource**：`DefaultHttpDataSource.Factory()` → `DefaultDataSource.Factory(ctx)`（由使用者修正），同時支援本地 `file://` 與遠端 `https://` URI

### 65. 相機照片全螢幕黑邊修復：allMediaUrls 優先級交換
- **狀態**: ✅ 完成
- **修改檔案**: `BubbleContent.kt`
- **說明**: `allMediaUrls` 原先優先取 `localImageUrls`（本地 cache），但 reconciliation 後已確認的相機照片仍保留本地暫存路徑，檔案可能已被系統清除。交換為優先取 `imageUrls`（遠端 Firebase URL），確保檔案存在、`globalImageUrls` 正確匹配、`isCameraCapture` 正常傳遞

### 66. ScrollToBottomButton 初始修正（前一 session）
- **狀態**: ✅ 完成（後續 session 有進一步修正）
- **修改檔案**: `ScrollToBottomButton.kt`
- **說明**: `isAtBottom` 從 `!canScrollForward` 改為 `!canScrollBackward`（`reverseLayout` 語意修正）

### 67. FullScreenImageDialog 滑動時 ContentScale 未隨圖片切換
- **狀態**: ✅ 完成
- **修改檔案**: `FullScreenImageDialog.kt`, `ChatUiState.kt`, `ChatDialogHost.kt`, `ChatScreen.kt`
- **說明**:
  - **Bug**：`FullScreenImageDialog` 只接收單一 `Boolean isCameraCapture`，若從相簿照片（`Fit`）往右滑到相機照片（應為 `Crop`），ContentScale 不會變，仍顯示上下黑邊
  - **修正**：`isCameraCapture` 改為 `isCameraCaptureList: List<Boolean>`，`ChatUiState.fullScreenImageIsCameraCapture` 也從 `Boolean` 改為 `List<Boolean>`
  - `ChatScreen.onImageClick` 中對 `chosenUrls` 每張 URL 查詢 `allMessages` 對應的 `isCameraCapture`，產生等長旗標列表
  - 對話框中依 `currentIndex` 取對應的 ContentScale

### 68. FullScreenImageDialog 頁碼標示（目前張數/總張數）
- **狀態**: ✅ 完成
- **修改檔案**: `FullScreenImageDialog.kt`
- **說明**: 全螢幕檢視時，頂部中央顯示 `currentIndex + 1/total` 半透明黑底白字 badge（`padding(top = 48.dp)`，字體 18sp）；僅多圖時顯示

### 69. 傳送新訊息後一鍵到底按鈕誤出現（完整修正）
- **狀態**: ✅ 完成
- **修改檔案**: `ScrollToBottomButton.kt`
- **說明**:
  - **方向邏輯**：`reverseLayout` 下 `firstVisibleItemIndex` 遞減 = 往底部滑 → 顯示按鈕；遞增 = 往舊訊息滑 → 隱藏
  - **冷卻機制**：`totalMessages` 變動（新訊息送達/接收）時啟動 800ms 冷卻，期間 `animateScrollToItem(0)` 動畫造成的滾動變化全部忽略，只更新 prevIndex/prevOffset
  - **合併 LaunchedEffect**：`isAtBottom`/`currentIndex`/`currentOffset`/`totalMessages` 合併為單一 `LaunchedEffect`，避免競態條件

---

### 70. AskQuestionScreen 媒體上傳：選取圖片/影片/語音隨問題一同送出
- **狀態**: ✅ 完成
- **修改檔案**: `SeekerViewModel.kt`, `AskQuestionScreen.kt`, `AppModule.kt`, `SeekerViewModelTest.kt`
- **新增類型**: `SeekerViewModel.SendMedia` data class（封裝 uri/isVideo/isVoice）
- **說明**:
  - `SeekerViewModel` 新增 `MediaUploader` 依賴
  - `sendQuestion()` 新增 `selectedMedia: List<SendMedia>` 參數，AI 回應寫入後自動上傳媒體
  - 語音：透過 `mediaUploader.sendVoice()` 上傳至 `chat_voice/` 路徑
  - 影片：透過 `mediaUploader.sendImages()` 上傳至 `chat_images/` 路徑，同時設定 `videoUrl`
  - 圖片：透過 `mediaUploader.sendImages()` 上傳至 `chat_images/` 路徑，寫入 `imageUrls`
  - `AskQuestionScreen` 將 `SelectedMedia` 轉換為 `SendMedia` 傳入
  - 所有媒體訊息 `sender` 設為 `"user"`、`senderId` 設為空字串
  - 每則媒體訊息獨立時間戳（`startTimestamp + index`）
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 24 全綠 ✅

---

### 71. 移除 AppTabRow — 功能完全由 RoleSelectScreen 取代
- **狀態**: ✅ 完成
- **刪除檔案**: `ui/common/AppTabRow.kt`
- **修改檔案**: `ui/navigation/AppNavigation.kt`
- **說明**:
  - `AppTabRow` 已全部移除，導航由 `RoleSelectScreen`（角色選擇卡片）取代
  - `AppNavigation.kt` 中所有 `showTabRow` 邏輯與相關 import 清除
  - 初始導航判斷修正：`currentRoute` 為 null 時不再誤判 `showTabRow` 為 true

### 72. Auth 系統強化：驗證碼、密碼強度、domain 白名單、提示彈窗
- **狀態**: ✅ 完成
- **新增檔案**:
  - `functions/index.js` — 新增 `sendVerificationEmail`（nodemailer + Gmail 發送驗證碼郵件）
  - `functions/package.json` — 新增 `nodemailer` 依賴
- **修改檔案**:
  - `ui/auth/AuthScreen.kt` — 註冊頁面重新設計（左上返回箭頭、驗證碼欄位+發送按鈕、移除註冊模式下切換按鈕）；錯誤/成功訊息改為浮動 Surface 彈窗（3 秒自動消失）
  - `ui/auth/AuthViewModel.kt` — 新增 `verificationCode`/`verificationSent`/`verificationSentTo`/`verificationLastSentAt` 狀態；密碼強度檢查（≥8 字元 + 大小寫 + 數字）；新增 `validateNickname()` 共用驗證；登出時重置 `_uiState.value = AuthUiState()`
  - `data/repository/AuthRepository.kt` — 新增 `FirebaseDatabase` 參數；`generateVerificationCode()`（含每日 3 次限制）、`verifyVerificationCode()`（比對後刪除）、`addAuthStateListener()`/`removeAuthStateListener()`
  - `ui/navigation/AppNavigation.kt` — 登出流程：`authViewModel.logout()` → `seekerViewModel.resetToLoggedOutState()` → `expertViewModel.resetToLoggedOutState()` → `authRepository.logout()` → `isLoggedIn = false`
  - `ui/seeker/components/AskQuestionHeader.kt` — Google displayName 自動帶入顯示
  - `ui/seeker/RoleSelectScreen.kt` — 新增登出按鈕（底部灰色文字）
  - `di/AppModule.kt` — `AuthRepository` 注入新增 `firebaseDb` 參數
  - `app/.../res/values/strings.xml` — `default_web_client_id` 仍為佔位值
- **說明**:
  - **Email 驗證簡化**：捨棄複雜 regex + TLD 列表，改為 domain 白名單（gmail.com + 22 個 Yahoo 地區網域 + Outlook/hotmail/live）
  - **發送驗證碼限制**：60 秒冷卻（記憶體內） + 每日 3 次（Firebase RTDB `email_verification/{emailKey}/dailyCount/{date}`）
  - **雲端函式**：`sendVerificationEmail` 監聽 `email_verification/{emailKey}` onCreate，需手動 `firebase functions:config:set gmail.email=... gmail.password=... && firebase deploy --only functions`
  - **驗證碼比對修正**：使用 `verificationSentTo`（發送時鎖定的信箱）而非 `state.email`（輸入框值）
  - **提示彈窗**：畫面正中央灰色 Surface 浮動視窗，3 秒自動消失，不干擾表單操作
  - **Google 登出**：一併清除 Google Sign-In 快取（`GoogleSignIn.getClient().signOut()`），下次登入重新選擇帳號
  - **密碼強度**：至少 8 字元、需含大寫字母、小寫字母、數字

### 73. 聊天室 MessageList 空白狀態移除 + 轉場殘影修復
- **狀態**: ✅ 完成
- **修改檔案**: `ui/chat/components/MessageList.kt`, `ui/navigation/AppNavigation.kt`, `ui/auth/AuthScreen.kt`
- **說明**:
  - `MessageList` 移除 👋 +「開始對話吧！」空白提示（送出問題後聊天室不會是空的）
  - Google 登入返回：`AuthScreen` 加上全螢幕黑色半透明遮罩（含 loading spinner），啟動 intent 前就設 `isLoading = true`
  - 初始導航閃現：`currentRoute` 為 null 時 `showTabRow` 不再誤判

### 74. Bug 修復：密碼驗證提示、註冊欄位殘留、彈窗卡住、Google 服務檢查
- **狀態**: ✅ 完成
- **修改檔案**: `ui/auth/AuthViewModel.kt`, `ui/auth/AuthScreen.kt`
- **說明**:
  - **密碼檢查一次列出**：收集所有缺少的條件（至少 8 字元、大寫字母、小寫字母、數字）一次顯示，而非遇到第一個就返回
  - **註冊欄位殘留**：`toggleMode()` 切換至註冊模式時一併清空 email/password/confirmPassword/暱稱/驗證碼等欄位
  - **彈窗卡住**：`LaunchedEffect(uiState.error)` 在 `error` 變為 `null` 時正確清除 `toastMessage`，避免編輯欄位後彈窗永久停留
  - **Google 服務檢查**：點擊 Google 登入前使用 `GoogleApiAvailability.isGooglePlayServicesAvailable()` 檢查，若不可用顯示「Google 服務不可用」錯誤而非無回應

---

### 75. AuthScreen 鍵盤改為 ADJUST_NOTHING + 三按鈕初始畫面 + 返回行為統一 + UI 精簡
- **狀態**: ✅ 完成
- **修改檔案**: `AuthScreen.kt`, `AuthViewModel.kt`, `AuthRepository.kt`, `gradle.properties`
- **說明**:
  - **鍵盤處理**：移除 `imePadding()` 與 `isImeVisible` 動態 spacer，改用 `DisposableEffect` 強制 `SOFT_INPUT_ADJUST_NOTHING`；登入模式完全不會位移，註冊模式靠 `verticalScroll` 滾動
  - **初始畫面**：從原本「Google 登入 + 登入或註冊」改為三按鈕：「Google 登入」「密碼登入」「註冊」；`toggleMode()` 新增 `register: Boolean?` 參數，讓主頁可分別指定登入或註冊模式
  - **返回行為**：註冊/登入頁面按左上返回箭頭或系統返回手勢（`BackHandler`）一律回到主畫面，不再區分
  - **UI 精簡**：移除登入/註冊頁面頂部「聯絡我們」、登入頁「沒有帳號？註冊」、註冊頁「已有帳號？登入」；註冊頁 top spacer 48→24dp，按鈕間距 24→8dp

### 76. 錯誤提示改為浮動疊層（不推擠按鈕）
- **狀態**: ✅ 完成
- **修改檔案**: `AuthScreen.kt`, `AuthViewModel.kt`
- **說明**:
  - 錯誤/成功 toast 從 Column 內移出，改為 `Box(fillMaxSize)` 內 `Alignment.TopCenter` + `padding(top=340.dp)` 的浮動 `Surface`
  - 不佔 Column 空間，不會推擠下方按鈕
  - `LaunchedEffect(uiState.error)` 觸發後呼叫 `viewModel.setError(null)` 清空 error，確保下次相同錯誤可重複觸發

### 77. 重設密碼頁面加入驗證碼欄位 + 註冊/重設驗證碼路徑分離
- **狀態**: ✅ 完成
- **修改檔案**: `AuthScreen.kt`, `AuthViewModel.kt`, `AuthRepository.kt`
- **說明**:
  - 重設密碼頁面新增驗證碼欄位（Email + 驗證碼 + 「下一步」），流程改為：輸入 Email → 傳送驗證碼 → 輸入驗證碼 → 驗證成功才進入下一步
  - **路徑分離**：註冊驗證碼存 `email_verification/{emailKey}/code`，重設驗證碼存 `email_verification/reset_{emailKey}/code`
  - `generateVerificationCode()` / `verifyVerificationCode()` 新增 `prefix` 參數（預設 `""` 給註冊，重設傳 `"reset_"`）
  - ViewModel 新增 `sendResetVerificationCode()`，使用 `resetVerificationLastSentAt` 獨立計時冷卻
  - Logcat 輸出分為 `RegCode`（註冊）和 `ResetCode`（重設）
  - 兩個頁面的「傳送驗證碼」按鈕原先函式互換導致 bug（重設發送存到註冊路徑 → 驗證不到），已修正

### 78. 驗證碼每日限制（重新加回）
- **狀態**: ✅ 完成
- **修改檔案**: `AuthRepository.kt`, `AuthViewModel.kt`
- **說明**: `generateVerificationCode` 加回 `onLimitReached` 參數，發送前檢查 `email_verification/{emailKey}/dailyCount/{date}`，≥3 次則觸發限制提示「今日驗證碼已達次數限制（3次）」

### 79. 新密碼頁面 + Cloud Function `resetPassword` + 降級寄信 + 跳轉登入
- **狀態**: ✅ 完成
- **修改檔案**: `functions/index.js`, `app/build.gradle.kts`, `AuthScreen.kt`, `AuthViewModel.kt`, `AuthRepository.kt`
- **說明**:
  - 驗證碼成功後導向「輸入新密碼」頁面（標題 + 兩個密碼欄位 + 確認按鈕）
  - 新增 Cloud Function `resetPassword`（`functions.https.onCall`）：檢查 `email_verification/reset_{emailKey}/verified` 旗標 → Admin SDK `updateUser(uid, {password})` → 清除節點
  - `AuthRepository` 新增 `markResetVerified()` 和 `resetPasswordCloudFunction()`；app 新增 `firebase-functions:21.1.0` 依賴
  - Cloud Function 未部署時自動降級：呼叫 `sendPasswordResetEmail` → AlertDialog「重設密碼信件已發送至...」→ 跳登入頁
  - Cloud Function 成功時：跳登入頁 + 浮動提示「密碼重設成功，請用新密碼登入」
  - 重設信件 AlertDialog 點「確定」後自動跳轉登入頁（Email 保留，密碼空白）
  - **注意**：需手動 `firebase deploy --only functions:resetPassword` 部署 Cloud Function 才能啟用直接改密碼

### 80. 除錯 Log 加強
- **狀態**: ✅ 完成
- **修改檔案**: `AuthRepository.kt`, `AuthViewModel.kt`
- **說明**: `verifyVerificationCode` 加入 Log.d 輸出 `emailKey`、`enteredCode`、`storedCode`、`match` 結果；`sendPasswordReset` 輸出 `targetEmail`、`code`、`emailField`

---

### 81. Bug 修復：MatchingRepository 欄位不一致 + SeekerViewModel 死碼修復
- **狀態**: ✅ 完成
- **修改檔案**: `Experience.kt`, `MatchingRepository.kt`, `ExpertRepository.kt`, `di/SeekerViewModel.kt`
- **說明**:
  1. **`activeState` vs `status` 欄位不一致**：
     - `MatchingRepository` 以 `.orderByChild("activeState").equalTo(true)` 查詢，但 `ExpertRepository` 寫入的是 `"status" to "active"`，兩者不一致導致客戶端配對永遠找不到任何專家
     - 修正：`Experience` 資料類別 `activeState: Boolean` → `status: String`；`MatchingRepository` 改為 `.orderByChild("status").equalTo("active")`
  2. **`isOnline` 未寫入 Firebase**：
     - `MatchingRepository` 篩選 `exp.isOnline`，但 `ExpertRepository.setExpertOnline()` 從未將 `isOnline` 寫入 `active_experiences`，導致所有線上專家都被過濾掉
     - 修正：`publishExperience()` 寫入時加上 `"isOnline" to true`；`setExpertOnline()` 同時更新 `active_experiences/{expId}/isOnline`
  3. **`startAiPreview`、`matchAndAssignExpert`、`startMatchTimeout` 從未被呼叫**：
     - `SeekerViewModel.sendQuestion()` 成功建立問題後，從未啟動 AI preview timer、客戶端配對、或配對超時計時器
     - 修正：在 `onSent` 回呼中加入 `matchAndAssignExpert(questionId, text, userId)`、`startAiPreview(questionId, text)`、`startMatchTimeout(questionId)`、`listenToMyQuestionStatus(questionId)`
- **附帶修復**：
  - 移除 `Experience.kt` 中不再需要的 `@PropertyName` annotation
  - 修復 `AuthViewModelTest` 中因 Auth 功能演進導致的 5 個測試失敗（密碼強度、驗證碼、email domain white list、Log.d not mocked 等問題）
- **編譯通過**: `./gradlew assembleDebug` ✅（0 warnings）
- **測試通過**: `./gradlew testDebugUnitTest` — 19 全綠 ✅

---

### 82. Auth 系統重構：DRY 驗證 + Race Condition 修復 + NavHost 整合 + UI 元件拆分
- **狀態**: ✅ 完成
- **新增檔案**:
  - `ui/common/AuthUtils.kt` — 驗證邏輯集中（validatePassword, validateNickname, isAllowedEmail, ALLOWED_DOMAINS）
  - `ui/common/ToastOverlay.kt` — 浮動 Toast 訊息疊層
  - `ui/common/LoadingOverlay.kt` — 全螢幕載入遮罩
  - `ui/common/CompactTextField.kt` — 統一樣式輸入框
  - `ui/navigation/Route.kt` — 路由常數（AUTH, ROLE_SELECT, ASK, EXPERT, CHAT）
  - `ui/auth/WelcomePanel.kt` — 初始頁面（Google 登入 + 密碼登入 + 註冊）
  - `ui/auth/LoginForm.kt` — 登入/註冊表單面板
  - `ui/auth/ResetPasswordPanel.kt` — 重設密碼面板（ForgotPasswordPanel + NewPasswordForm）
- **修改檔案**: `AuthScreen.kt` (610→155 行), `AuthViewModel.kt`, `AuthRepository.kt`, `AppNavigation.kt` (273→~230 行), `functions/index.js`, `functions/package.json`, `Experience.kt`
- **說明**:
  - **DRY 驗證**：`validatePassword()` / `validateNickname()` / `isAllowedEmail()` / `ALLOWED_DOMAINS` 由 AuthViewModel 抽出至 `AuthUtils.kt`，消除註冊與 NicknameSettingsDialog 間的驗證重複
  - **Race Condition 修復**：三個獨立 `LaunchedEffect` + `delay(3000)` 的 toast 狀態合併為單一 `Channel<String>`（`_toastEvent`），消除多個計時器重疊導致的顯示/隱藏錯亂
  - **NavHost 整合**：`AuthScreen` 從 `if (!isLoggedIn)` 外部區塊移入 NavHost 作為 `Routes.AUTH` route，preserve NavController lifecycle 實現登入/登出平滑轉場
  - **UI 元件拆分**：AuthScreen.kt 從 610 行降至 155 行，提取 5 個獨立元件（WelcomePanel / LoginForm / ResetPasswordPanel / CompactTextField / ToastOverlay / LoadingOverlay），共享元件置於 `ui/common/`
  - **Cloud Functions v2**：`resetPassword` 與 `sendVerificationEmail` 改為 `firebase-functions/v2/https` onCall API，部署至 `warmhelpapp`（Node.js 22, us-central1, 2nd Gen）
  - **resetPassword CF 驗證邏輯**：直接讀取 RTDB `code` 欄位比對，不再依賴 `verified` 旗標，消除 `removeValue()` 與 `setValue(true)` 間的 race condition
  - **移除 email fallback**：`confirmResetPassword` 僅保留 Cloud Function 路徑，不再降級 `sendPasswordResetEmail`
  - **`sendResetVerificationCode` 修復**：補上 `prefix = "reset_"` 參數，修正先前因缺少 prefix 導致錯誤寫入註冊路徑的問題
  - **導覽棧安全**：登入成功時 `navController.navigate(Routes.ROLE_SELECT) { popUpTo(Routes.AUTH) { inclusive = true } }`，確保返回鍵不回登入頁
  - **新增 `_navigateEvent` 通道**：用於 `confirmResetPassword` 成功後觸發 `ShowLoginForm` 導航事件
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 18 全綠 ✅

---

### 83. 重構深化：UiText 密封類別 + AuthRepository 協程化 + 輸入層分離
- **狀態**: ✅ 完成
- **新增檔案**: `ui/common/UiText.kt` — 密封類別（Dynamic / Resource），ViewModel 不再硬編碼字串，i18n ready
- **修改檔案**: `AuthRepository.kt`, `AuthViewModel.kt`, `AuthScreen.kt`, `LoginForm.kt`, `ResetPasswordPanel.kt`, `AuthViewModelTest.kt`
- **說明**:
  - **UiText 密封類別**：`UiText.Dynamic(String)` 儲存動態錯誤訊息，`UiText.Resource(@StringRes)` 支援未來 `stringResource` 國際化，ViewModel 不再直接硬編碼中文
  - **AuthRepository 全部改為 `suspend fun`**：
    - 7 個 callback 方法（login / register / signInWithGoogle / sendPasswordReset / resetPasswordCloudFunction / generateVerificationCode / verifyVerificationCode）全部使用 `suspendCancellableCoroutine` 改為掛起函式
    - ViewModel 不再巢狀 callback，改用 `viewModelScope.launch { try { ... } catch { ... } }` 線性化控制流
    - limit-reached 情況透過 `throw Exception` 傳遞，畀 ViewModel 統一捕獲
  - **移除 ViewModel 輸入狀態**：`AuthUiState` 刪除 email / password / confirmPassword / nickname / verificationCode / newPassword / confirmNewPassword 共 7 個使用者輸入欄位
  - **UI 層持有輸入狀態**：LoginForm（5 個 `rememberSaveable`）、ForgotPasswordPanel（2 個）、NewPasswordForm（2 個）各自管理自己的輸入值，僅在按鈕點擊時一次性傳遞給 ViewModel 方法（如 `submit(email, password, ...)`）
  - **ViewModel 精簡**：移除了 `updateEmail()`、`updatePassword()` 等 7 個 setter 方法；`submit()` / `sendPasswordReset()` / `confirmResetPassword()` 改為接收參數；新增 `setError(message: String)` 供 UI 層錯誤顯示
  - **error 改為 `UiText?`**：ViewModel 的 `error` 欄位型態從 `String?` 改為 `UiText?`，透過 `.asString()` @Composable 轉換為顯示文字
  - **測試更新**：10 個測試改用 `coEvery` + `runTest` + `testDispatcher.scheduler.advanceUntilIdle()` 配合 suspend 函式
- **編譯通過**: `./gradlew assembleDebug` ✅（僅 deprecated GoogleSignIn 警告）
- **測試通過**: `./gradlew testDebugUnitTest` — 18 全綠 ✅（10 Auth + 4 Expert + 4 Seeker）

---

### 84. Bug 修復：AuthViewModel 缺少 import kotlinx.coroutines.flow.update
- **狀態**: ✅ 完成
- **修改檔案**: `AuthViewModel.kt`
- **說明**:
  - `AuthViewModel` 大量使用 `_uiState.update { ... }` 但缺少 `import kotlinx.coroutines.flow.update`
  - 其他 ViewModel 皆有正確 import
  - 補上 import 以修復編譯錯誤

### 85. UI/UX 優化：導航轉場動畫
- **狀態**: ✅ 完成
- **修改檔案**: `AppNavigation.kt`
- **說明**:
  - 所有子路由（ASK / EXPERT / CHAT）加入 slideInHorizontally + fadeIn 入場動畫（350ms）
  - 加入 slideOutHorizontally + fadeOut 出場動畫
  - popEnterTransition / popExitTransition 反向滑入/滑出（1/3 偏移量），形成自然的推入/彈出效果
  - 移除原本的 `tween(0)` 無動畫設定

### 86. UI/UX 優化：ExpertScreen Toast → Snackbar
- **狀態**: ✅ 完成
- **修改檔案**: `ExpertScreen.kt`
- **說明**:
  - 移除 `Toast.makeText()` 系統級提示，改為 Compose SnackbarHost
  - `ExpertScreen` 外層加入 `Scaffold` + `SnackbarHostState`
  - ViewModel 的 `ShowToast` 事件改為顯示 Snackbar，融入 App 視覺風格

### 87. UI/UX 優化：移除未使用 import + 硬編碼修正
- **狀態**: ✅ 完成
- **修改檔案**: `FullSettingsScreen.kt`, `RoleSelectScreen.kt`
- **說明**:
  - `FullSettingsScreen.kt` 移除 3 個未使用的 animation import
  - `RoleSelectScreen.kt` DrawerContent 的 `nickname` 從 `"測試暱稱"` 改為動態傳入的實際暱稱

### 88. UI/UX 優化：MatchingDialog 圖示改為 CircularProgressIndicator
- **狀態**: ✅ 完成
- **修改檔案**: `MatchingDialog.kt`
- **說明**:
  - 標題區的 `🔍` emoji 圖示改為統一的 `CircularProgressIndicator`，與下方的載入 spinner 視覺一致
  - 解決 emoji 在不同裝置/系統上渲染不一致的問題

### 89. UI/UX 優化：AskQuestionScreen Snackbar 送出回饋 + Scaffold 容器
- **狀態**: ✅ 完成
- **修改檔案**: `AskQuestionScreen.kt`
- **說明**:
  - 外層改為 `Scaffold` + `SnackbarHost`，送出問題後顯示「問題已送出，正在為您配對專家」
  - 移除未使用的 `drawBackgroundGlow` import
  - 容器顏色統一由 Scaffold containerColor 管理

---

### 90. FCM 推播通知（Push Notifications）
- **狀態**: ✅ 完成
- **新增檔案**（2 個）:
  - `data/repository/FcmService.kt` — FirebaseMessagingService（處理 token 刷新/儲存 + 前景訊息顯示通知）
  - `functions/index.js` 改寫 — 新增 2 個 database trigger（新訊息推播 / 專家接受推播） + `sendNotification` helper
- **修改檔案**（8 個）:
  - `gradle/libs.versions.toml` — 新增 `firebaseMessaging = "24.1.0"` 版本 + library entry
  - `app/build.gradle.kts` — 新增 `firebase-messaging` 依賴
  - `AndroidManifest.xml` — 註冊 `FcmService`（`MESSAGING_EVENT` intent-filter）+ `POST_NOTIFICATIONS` 權限
  - `App.kt` — 建立 `chat_messages` NotificationChannel（IMPORTANCE_HIGH）
  - `data/repository/AuthRepository.kt` — 新增 `saveFcmToken()` 方法（讀取 FCM token 寫入 `users/{uid}/fcmToken`）
  - `data/repository/UserRepository.kt` — 新增 `getFcmToken()` 方法
  - `ui/auth/AuthViewModel.kt` — 三種登入流程（register / login / Google sign-in）成功後呼叫 `saveFcmToken()`
  - `ui/navigation/AppNavigation.kt` — 新增通知權限請求（Android 13+ POST_NOTIFICATIONS）+ 通知點擊深連結處理（讀取 intent extra `navigate_chatroom_id` 自動導航至聊天室）+ 啟動時呼叫 `saveFcmToken()`
- **說明**:
  - **FcmService**：`onNewToken` 自動儲存 token 至 Firebase；`onMessageReceived` 解析 data payload 顯示通知（點擊自動導航至對應聊天室）
  - **Cloud Functions**：
    - `sendNotificationOnNewMessage`：監聽 `/chatrooms/{chatroomId}/messages/{messageId}` 有新訊息時推播給對方
    - `sendNotificationOnExpertAccept`：監聽 `/questions/{questionId}/status` 變為 `expert_accepted`/`taken` 時推播給提問者
    - 自動處理 token 失效（`messaging/registration-token-not-registered` 時清除）
  - **深連結**：通知點擊 → `MainActivity` intent extra → `AppNavigation` LaunchedEffect 檢查 → 自動 `navController.navigate(Routes.chat(...))`
  - **token 同步**：登入後、App 啟動時、token 刷新時三種路徑同步 token，確保即時性
- **注意事項**：
  - Cloud Function 需 `firebase deploy --only functions` 部署後推播功能才生效
  - 模擬器需安裝 Google Play 服務才能接收推播
  - Android 13+ 首次啟動會顯示通知權限請求對話框
  - FCM token 儲存於 `users/{uid}/fcmToken`，由 Cloud Function 讀取後透過 Admin SDK 發送

---

### 91. Bug 修復：ChatInputBar 缺少 FontWeight import
- **狀態**: ✅ 完成
- **修改檔案**: `ui/chat/components/ChatInputBar.kt`
- **說明**: `OutlinedTextField` 的 leadingIcon 中 `Text(fontWeight = FontWeight.Normal)` 缺少 `import androidx.compose.ui.text.font.FontWeight`，補上後編譯通過

---

### 92. DrawerContent 搜尋欄提示詞位置修正
- **狀態**: ✅ 完成
- **修改檔案**: `DrawerContent.kt`
- **說明**: 移除搜尋欄的固定 `.height(48.dp)` 限制，讓 TextField 使用內建高度，解決 placeholder 提示詞因固定高度導致垂直錯位的問題

---

### 93. DrawerContent 支援 Google 頭像與暱稱同步
- **狀態**: ✅ 完成
- **修改檔案**: `DrawerContent.kt`, `RoleSelectScreen.kt`, `AppNavigation.kt`
- **說明**: 
  - `DrawerContent` 新增 `avatarUrl: String?` 參數，有頭像 URL 時用 Coil `AsyncImage` 顯示 Circle 裁切大頭貼，無則顯示預設首字圓圈
  - `RoleSelectScreen` 新增 `avatarUrl` 參數並傳遞至 DrawerContent
  - `AppNavigation` 讀取 `authRepository.currentUser?.displayName` 與 `photoUrl`，暱稱優先取 RTDB 設定值，空白則 fallback 至 Google displayName

---

### 94. AskQuestionScreen 還原金色背景光暈 + 輸入框居中
- **狀態**: ✅ 完成
- **修改檔案**: `AskQuestionScreen.kt`, `AskQuestionInputBar.kt`
- **說明**:
  - **背景光暈加強**：三個 `radialGradient` — 左下 `#D4A853` 18%、右下 `#D4A853` 18%、底部中央 `#361C0A` 60%，半徑放大至 0.8w~1.0w，金色層次更深
  - **輸入框居中**：`Column` 改為 `Arrangement.Center`，Header + InputBar 整體垂直居中，不再被 `weight(1f)` 推到最底
   - **InputBar padding**：移除 `bottom` padding，改為對稱 `horizontal = 30.dp`

---

### 95. Bug 修復：SeekerViewModel 殘留 TODO() 導致 Runtime 崩潰
- **狀態**: ✅ 完成
- **修改檔案**: `di/SeekerViewModel.kt`
- **說明**: `showSeekerConfirmDialog` 屬性的 getter 遺留 `TODO()` 未實作，外部呼叫將直接拋出 `NotImplementedError`。改為回傳 `_uiState.value.showSeekerConfirmDialog`，正確委託至 UiState

### 96. AskQuestionScreen 加入 BackHandler 返回支援
- **狀態**: ✅ 完成
- **修改檔案**: `ui/seeker/AskQuestionScreen.kt`
- **說明**: `AskQuestionScreen` 雖接收 `onBack` 回呼參數但從未呼叫，導致系統返回鍵無反應。加入 `BackHandler(onBack = onBack)` 處理返回手勢，並將 `onBack` 傳遞給 `AskQuestionHeader.onMenuClick`，使左上角漢堡選單圖示可觸發返回

### 97. 修復 SDK 路徑設定（local.properties）
- **狀態**: ✅ 完成
- **修改檔案**: `local.properties`
- **說明**: `sdk.dir` 原本指向 `C:\Program Files`（無 platforms/build-tools），改為正確的 SDK 路徑 `C:\Users\user\AppData\Local\Android\Sdk`（含 android-37.0 platforms 與 36.0.0 build-tools）。修正後編譯與測試皆可正常執行
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 18 全綠 ✅

---

### 98. 背景光暈完整迭代：琥珀 → 藍色 → 黑色疊層 → 用戶自訂多層漸層
- **狀態**: ✅ 完成
- **修改檔案**: `BackgroundGlow.kt`
- **提交**: `35e5c9d`, `554af9c`, `05c5e49`, `6bec7b8`, `807e7e7`
- **說明**:
  - 初始為琥珀色 `radialGradient`（#D4A853 / #361C0A）
  - 改為純藍色 radial（#133281 → #0055FF），含主光暈 + 副光暈
  - 嘗試黑色疊層方案（黑色半透明圓形蓋在藍色底上）
  - 用戶自行 push 版本（`6bec7b8`）：垂直漸層 `#133281→#08162F→#133281` + 主光暈 `#4DA3FF(alpha=0.28, 半徑=290.dp)` + 副光暈 `#00D4FF(alpha=0.10, 半徑=470.dp)`
  - `807e7e7` 將垂直漸層頂端底端統一改為 `#133281`
  - `drawBackgroundGlow()` 回傳 `this.fillMaxSize().drawBehind{...}` 單一 Modifier

### 99. 系統列黑色條問題：多次嘗試未解 → 最終修復
- **狀態**: ✅ 完成
- **修改檔案**: `MainActivity.kt`, `themes.xml`, `AskQuestionScreen.kt`, `RoleSelectScreen.kt`
- **提交**: `49e8b2a`, `02ce859`, `ec4d271`, `9f38a17`, `bd1f50f`, `45d08bf`, `9f36615`
- **說明**:
  - 先前嘗試（失敗）：
    - 設定 `window.statusBarColor = Color.TRANSPARENT` 及各種深藍色值
    - 啟用 `enableEdgeToEdge()` + `setDecorFitsSystemWindows(false)`
    - 設定 `window.isStatusBarContrastEnforced = false`
    - `themes.xml` 加入 `android:windowBackground="@android:color/transparent"`
    - 最終在 `decorView.setBackgroundColor(Color.parseColor("#133281"))` 強制背景色
    - 移除 `controller.hide()`（`systemUiVisibility` 隱藏系統列）
  - **真正原因**（commit `9f36615`）：`AskQuestionScreen.kt` 的 modifier 順序錯誤。前一 session 在 `fillMaxSize()` 後插了 `.background(Color.Black)`，導致系統列區域被填黑
  - **修復**：移除 `.background(Color.Black)`，恢復 `drawBackgroundGlow()` → `windowInsetsPadding(safeDrawing)` 順序。讓 glow 在 `safeDrawing` 之前繪製，自然延伸到系統列區域
  - `.background(Color(0xFF171717))` 也從 `RoleSelectScreen.kt` 暫時移除（後續 commit `edfb8c1` 加回）

### 100. AppNavigation 重構：Box 外層包裹全螢幕背景光暈
- **狀態**: ✅ 完成
- **修改檔案**: `AppNavigation.kt`, `ChatScreen.kt`
- **提交**: `c79e082`, `80a5ffa`, `a133918`
- **說明**:
  - `a133918`：`Box(drawBackgroundGlow)` 移至 `Scaffold` 外層作為全螢幕背景
  - `NavHost` 改為 `fillMaxSize()` + `padding(innerPadding)` 避免被 system bars 遮擋
  - `c79e082`：嘗試 ChatScreen 內用 Box 包裹全螢幕背景（後續 revert）
  - `ChatScreen` 最終移除 `drawBackgroundGlow()`、`statusBarsPadding()`、`imePadding()`，僅留 `fillMaxSize().clickable{focusManager.clearFocus()}`

### 101. BackgroundGlow 簡化：多層漸層 → 單色 + 黑色 radial 暗角
- **狀態**: ✅ 完成
- **修改檔案**: `BackgroundGlow.kt`, `MainActivity.kt`, `RoleSelectScreen.kt`
- **提交**: `edfb8c1`
- **說明**:
  - `BackgroundGlow.kt` 從 52 行砍到 23 行：移除垂直漸層 base、移除雙 radial glow（主光暈 #4DA3FF + 副光暈 #00D4FF）
  - 新方案：純色 `#2631C9` ＋ 單一黑色 radial 暗角（`Brush.radialGradient(Black → Transparent)`，中心置中、半徑 `width * 4.0f`）
  - 移除了 `fillMaxSize()` 呼叫（改由呼叫端自行決定尺寸）
  - `RoleSelectScreen.kt` 加回 `.background(Color(0xff171717))` 保留 Drawer 灰色背景
  - `MainActivity.kt` 移除過時註解

### 102. 移除提問頁面左上角漢堡選單按鈕
- **狀態**: ✅ 完成
- **修改檔案**: `AskQuestionHeader.kt`, `AskQuestionScreen.kt`
- **說明**:
  - `AskQuestionHeader.kt`：移除 hamburger menu Box（含 `onMenuClick` 參數、import `clickable`/`background`/`clip`/`RoundedCornerShape`）
  - `AskQuestionScreen.kt`：移除傳入的 `onMenuClick = onBack`、移除未使用的 `background` import
  - 返回功能仍由 `BackHandler(onBack = onBack)` 保留（系統返回手勢/按鍵）

### 103. Google 登入卡住修復：加上 20 秒逾時保護
- **狀態**: ✅ 完成
- **修改檔案**: `AuthViewModel.kt`
- **說明**:
  - `signInWithGoogle()` 中 `authRepository.signInWithGoogle(idToken)` 使用 `suspendCancellableCoroutine`，若 Firebase Auth 無回應（網路問題、憑證不匹配）會永久掛起
  - 加上 `withTimeout(20_000L)` 包裹，逾時時拋出 `TimeoutCancellationException`
  - 使用者不再看到無限 loading，20 秒後顯示「登入逾時」提示

---

## Known Issues
- ~~**鍵盤無法收回**：`RoleSelectScreen` 側邊欄搜尋欄焦點後，點主畫面空白處無法收起鍵盤。已嘗試 `focusManager.clearFocus()` 與 `InputMethodManager.hideSoftInputFromWindow()` 皆無效。~~ ✅ **已修復**（#7）：覆蓋層點擊時一併呼叫 `clearFocus` + `hideSoftInputFromWindow`。

---

### 105. 背景光暈最終方案確定 + 黑色條真正原因修復
- **狀態**: ✅ 完成
- **修改檔案**: `BackgroundGlow.kt`, `AskQuestionScreen.kt`, `MainActivity.kt`, `RoleSelectScreen.kt`, `themes.xml`
- **說明**:
  - **黑色條真正原因**：`AskQuestionScreen.kt` 的 modifier 順序錯誤——`.background(Color.Black)` 在 `safeDrawing` 前，導致系統列區域被填黑。修復：移除 `.background(Color.Black)`，恢復 `drawBackgroundGlow()` → `windowInsetsPadding(safeDrawing)` 順序
  - **`BackgroundGlow.kt` 簡化**：從 52 行砍到 23 行，移除垂直漸層 base 與雙 radial glow，改為純色 `#2631C9` ＋ 黑色 radial 暗角（`radialGradient(Black → Transparent)`，半徑 `width*4`），移除 `fillMaxSize()` 改由呼叫端決定尺寸
  - **`MainActivity.kt`**：`enableEdgeToEdge()` + `decorView.setBackgroundColor("#133281")` + `createNotificationChannel()`
  - **`themes.xml`**：`windowBackground transparent`
  - **最終用戶選擇**：`BackgroundGlow.kt` 最終方案為單色 `#2631C9` + 黑色 radial 暗角

### 106. FullSettingsScreen + DrawerContent 搜尋欄
- **狀態**: ✅ 完成
- **修改檔案**: `FullSettingsScreen.kt`, `DrawerContent.kt`, `RoleSelectScreen.kt`
- **說明**:
  - `FullSettingsScreen.kt`：`SettingsItem` 之間加入 4dp 間隔，避免外框黏在一起
  - `DrawerContent.kt`：側邊欄加入搜尋 TextField（已知問題：focus 後點空白處無法收回鍵盤）

### 107. AiRepository.generateExpertTags() + 本地降級斷詞
- **狀態**: ✅ 完成
- **修改檔案**: `AiRepository.kt`
- **說明**:
  - `generateExpertTags(domain, subDomain, problem)` 呼叫 Gemini API 產生專家配對標籤
  - 支援全形逗號 `，` 分隔
  - 空 API Key 時回傳 `emptyList()` 並 log 警告
  - **本地降級機制**：API 配額用盡或斷線時，不再回傳 `emptyList()`，改由 `generateLocalFallbackTags()` 斷詞 domain/subDomain 直接作為標籤 + problem 依分隔符號/介系詞拆分取前 5 個

### 108. ExpertScreen 結構化輸入 + AI 關聯標籤
- **狀態**: ✅ 完成
- **修改檔案**: `ExpertScreen.kt`, `ExpertViewModel.kt`, `AppModule.kt`
- **說明**:
  - `QuickLogCard` 加入結構化輸入欄位（核心領域/技術子項目/具體問題）
  - 點選「自動產生配對標籤」按鈕 → 呼叫 `AiRepository.generateExpertTags()` → 產生關聯標籤 Chip
  - `ExpertViewModel` 新增 `fetchTagsFromAi()` 橋接方法，`viewModelScope.launch` + 錯誤 toast
  - `AppModule.kt` DI 更新：`ExpertViewModel(get(), get())`（加入 `AiRepository` 依賴）

### 109. MatchingRepository Jaccard 相似度升級
- **狀態**: ✅ 完成
- **修改檔案**: `MatchingRepository.kt`
- **說明**:
  - 分數計算從單純的 `overlap > 0` 改為 **Jaccard 相似度**（`intersectSize / unionSize`）
  - 聯集越大分數越低，單一泛用詞（如「大陸」）會被長句子稀釋，杜絕泛用詞誤報
  - 信心閥值 `0.08`：低於此分的直接濾掉
  - 分數型別從 `Int` 改為 `Double`

### 110. 每日提問配額防禦（max 3/day + 防多開）
- **狀態**: ✅ 完成
- **修改檔案**: `QuestionRepository.kt`, `SeekerViewModel.kt`, `AskQuestionScreen.kt`
- **說明**:
  - `QuestionRepository` 新增 `getTodayQuestionCount()`（今日有效提問數）、`hasActiveQuestion()`（是否已有進行中提問）
  - `SeekerViewModel`：`SeekerUiState` 加入 `dailyRemainingQuota` / `quotaError`；`sendQuestion()` 進入協程優先檢查多開 + 配額，超過則設 `quotaError` 不發送
  - `AskQuestionScreen`：進入畫面自動重整額度、`LaunchedEffect` 攔截 `quotaError` 彈 Snackbar、輸入框上方顯示「今日剩餘提問次數：X 次」（歸零變紅）

### 111. AGENTS.md 規則強化

### 112. SeekerViewModel 瘦身：抽離 3 個 UseCase（441→293 行）
- **狀態**: ✅ 完成
- **新增檔案**（3 個）:
  - `domain/seeker/ValidateQuestionQuotaUseCase.kt` — 封裝多開檢查 + 每日配額檢查，回傳 `QuotaResult.Valid / Invalid`
  - `domain/seeker/ObserveQuestionStatusUseCase.kt` — Firebase ValueEventListener 包裝為 `Flow<QuestionStatus>`（callbackFlow），定義 `QuestionStatus` sealed class（Taken / ExpertAccepted / NoExperts / Cancelled / Matching）
  - `domain/seeker/SendQuestionMediaUseCase.kt` — 三種媒體上傳邏輯（語音/影片/圖片）從 ViewModel 抽出，注入 `MediaUploader`；`SendMedia` data class 移至該檔案
- **修改檔案**（5 個）:
  - `di/SeekerViewModel.kt`（441→293 行）：移除 `uploadSelectedMedia`（~77 行）、`listenToMyQuestionStatus`（~58 行）、`removeUserQuestionListener`、`userQuestionRef`/`userQuestionListener` 欄位；改為注入 3 個 UseCase + `observeQuestionStatusUseCase(questionId).collectLatest{}` + `sendQuestionMediaUseCase(...)` + `validateQuestionQuotaUseCase(userId)`
  - `di/AppModule.kt` — 註冊 3 個新 UseCase；SeekerViewModel 建構參數從 5 個增至 8 個
  - `ui/seeker/AskQuestionScreen.kt` — 改用 `domain.seeker.SendMedia` 取代 `SeekerViewModel.SendMedia`
  - `app/src/test/.../SeekerViewModelTest.kt` — 改用 mocking `ObserveQuestionStatusUseCase`（`MutableSharedFlow`）+ `UnconfinedTestDispatcher` 測試 Flow 驅動的狀態轉換
  - `app/src/test/.../ExpertViewModelTest.kt` — 補上 `AiRepository` mock 參數
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 18 全綠 ✅
- `domain/seeker/` 檔案數：1 → 4（+3 UseCase）
- `di/SeekerViewModel.kt` 行數：441 → 293（-148 行）
- **狀態**: ✅ 完成
- **修改檔案**: `AGENTS.md`
- **說明**:
  - 每 5 次修改程式碼後更新 `PROGRESS.md`（同問題 debugging 只算 1 次）
  - 編輯完後需 `git push`，再到另一台 `git pull`，每次修改完都必須 PUSH

---

## 待注意事項（更新）
- `AuthScreen.kt` 使用 `DisposableEffect` 設定 `SOFT_INPUT_ADJUST_NOTHING`，離開時還原
- `AuthViewModel.setError()` 接受 `String`，包裝為 `UiText.Dynamic` 寫入 state + 發送 toast
- `error` 型態為 `UiText?`，UI 透過 `.asString()` @Composable 顯示
- `AuthRepository` 全部 7 個異步方法已改為 `suspend fun`（使用 `suspendCancellableCoroutine`）
- 輸入狀態（email/password/驗證碼等）由 UI 層 `rememberSaveable` 持有，ViewModel 方法以參數接收
- `AuthUiState` 僅保留：isLoading / error / isRegisterMode / isLoggedIn / verificationSent / verificationSentTo / verificationLastSentAt / resetSent / resetLastSentAt / resetVerificationLastSentAt / showNewPasswordForm
- 註冊驗證碼路徑：`email_verification/{emailKey}/code`
- 重設驗證碼路徑：`email_verification/reset_{emailKey}/code`
- 重設驗證碼冷卻：`resetVerificationLastSentAt`（獨立於註冊的 `verificationLastSentAt`）
- Cloud Function `resetPassword` 存放於 `functions/index.js`（v2 API），需手動 `firebase deploy --only functions:resetPassword`
- Cloud Function 直接讀取 RTDB `code` 欄位驗證，不依賴 `verified` 旗標
- Cloud Function `sendVerificationEmail` 使用 nodemailer + Gmail App Password
- `app/build.gradle.kts` 新增 `firebase-functions:21.1.0` 依賴

---

### 104. 架構修正批次：#5 路徑常數、#6 MediaUploader、#7 鍵盤修復、#3 Domain UseCase、#4 MatchCoordinator
- **狀態**: ✅ 完成
- **修改/新增檔案**:
  - `ui/navigation/Route.kt` — 新增 `EXTRA_*` 路徑常數，`FcmService.kt` / `AppNavigation.kt` 改用 `Routes` 常數
  - `data/repository/MediaUploader.kt` — 新增 `StoragePath` 物件管理上傳路徑；影片獨立走 `chat_video/` 路徑
  - `ui/seeker/RoleSelectScreen.kt` — Drawer 覆蓋層 .clickable 加入 `clearFocus` + `hideSoftInputFromWindow`，修復鍵盤無法收回
  - `domain/auth/` — 新增 7 個 UseCase（LoginUseCase / RegisterUseCase / SignInWithGoogleUseCase / GenerateVerificationCodeUseCase / VerifyVerificationCodeUseCase / ResetPasswordUseCase / LogoutUseCase）
  - `ui/auth/AuthViewModel.kt` — 改用 UseCase 取代直接注入 AuthRepository
  - `di/AppModule.kt` — DI 註冊新 UseCase
  - `domain/seeker/MatchCoordinator.kt` — 新增（timer/matching 邏輯從 SeekerViewModel 抽出）
  - `di/SeekerViewModel.kt` — 改用 MatchCoordinator，移除內聯 `matchAndAssignExpert` / `startMatchTimeout` / `startAiPreview` / `cancelMatchTimeout` 及 companion object
  - `app/src/test/.../AuthViewModelTest.kt` — 改用 UseCase mock
- **說明**:
  - #5：路徑常數集中管理，消除散落各處的路徑字串
  - #6：StoragePath 物件統一 Storage 上傳路徑；影片不再共用圖片上傳函式
  - #7：Drawer 點擊覆蓋區域時一併關閉鍵盤（原僅 focusManager.clearFocus 無效）
  - #3：Domain layer 建立 `auth/` UseCase，ViewModel 依賴抽象而非 Repository 實作
  - #4：配對/逾時邏輯抽出至 MatchCoordinator，ViewModel 專注 state 管理
- **編譯通過**: `./gradlew assembleDebug` ✅
- **測試通過**: `./gradlew testDebugUnitTest` — 全數通過 ✅

---

### 113. 全螢幕 MatchingOverlay 取代 Snackbar + 延遲 activeChatRoomId 至 AI 回應完成
- **狀態**: ✅ 完成
- **新增檔案**:
  - `ui/seeker/MatchingOverlay.kt` — 全螢幕配對中覆蓋層（半透明黑底 + 旋轉 spinner +「配對中請稍後」+「取消配對」按鈕）
- **修改檔案**:
  - `ui/seeker/AskQuestionScreen.kt` — 移除 `showSentFeedback` 區域狀態與 Snackbar 回饋；改用 `MatchingOverlay`（條件：`isUserMatching && activeChatRoomId.isBlank() && quotaError == null`）；`showSentFeedback` 參數改為 `seekerUiState.isUserMatching`
  - `di/SeekerViewModel.kt` — `sendQuestion.onSent()` 不再立即設定 `activeChatRoomId`，改為 AI 回應寫入 Firebase 後（成功/例外）才設定，觸發導航至 ChatScreen
- **說明**:
  - Snackbar「問題已送出，正在為您配對專家」改為全螢幕半透明遮罩 MatchingOverlay，視覺更符合配對中等待感
  - `activeChatRoomId` 延遲設定：送出問題後先顯示 overlay，等 Gemini AI 生成回應並寫入 Firebase 後才跳轉 ChatScreen
  - 取消配對按鈕呼叫 `viewModel.cancelUserMatching()`（既有實作）
  - overlay 會在「專家接受配對」（expert_accepted）或「逾期」（expired）時被 `isUserMatching = false` 自動隱藏
- **commit**: `f20269d`

---

### 114. 配對流程修正：不跳轉 + 收起鍵盤 + 預防閃退
- **狀態**: ✅ 完成
- **修改檔案**:
  - `di/SeekerViewModel.kt` — AI 回應不再觸發 `activeChatRoomId`，停留在 AskQuestionScreen 等待真人配對成功才跳轉
  - `di/SeekerViewModel.kt` — `onAiChatroomReady` 改為空 lambda，不再被 startAiPreview 導航
  - `ui/seeker/AskQuestionScreen.kt` — 送出問題時 `focusManager.clearFocus()` 自動收起鍵盤
  - `domain/seeker/MatchCoordinator.kt` — `startAiPreview` 的 `generateResponse()` 補上 try-catch，防止 Gemini API 異常導致 App 崩潰
- **說明**:
  - 3 秒閃退原因：`startAiPreview` 的 `generateResponse()` 無 try-catch，Gemini API 掛掉時未捕捉例外直接炸協程
  - 現在送出問題後停留在原畫面顯示 overlay，直到專家配對成功或取消
- **commits**: `b8d5908`, `5167b38`, `ca41141`

