# 專案進度紀錄 (封存 #1 ~ #50)

此檔案包含專案初期 #1 到 #50 的開發紀錄，保留以供查閱。當前進度請見 `PROGRESS.md`。

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




## CHANGELOG 封存（Round 15 以前）

以下內容從 CHANGELOG.md 搬移至本封存檔，保留以供查閱。

## 已解決問題一覽（合併自 KNOWN_ISSUES.md）

| # | 項目 | 處理 |
|---|---|---|
| 1 | `pending_skills` 孤立風險 | 新增 `releaseStuckProcessing()` 清除卡住的 processing 標記 |
| 2 | saveSkill 原子性喪失 | 改用 `updateChildren()` 原子寫入 solutions + pending_skills |
| 3 | `@StringRes` 編譯警告 | 加 `-Xannotation-default-target=param-property` |
| 4 | CF cooldown 10min → 5min | 縮短重置間隔 |
| 5 | Submission Lock 邊界 bug | rejectedCount 累積而非歸零，有 ACTIVE 時不鎖 |
| 6 | `pending_questions` 孤立風險 | 同 #1 加入 `releaseStuckProcessing()` |
| 7 | 兩個 CF 合併為一個 | 新 `exports.batchProcess` 依序處理 skills→questions |
| 8 | Firebase query 低機率不觸發 | 已改用 `addMessagesListener` 無 query 版本 |
| 9 | `healOrphanedPending` 報錯 | 修復空 cursor 時 `startAfter('')` 的 query 問題 |
| 10 | 跨檔案命名同步 | 已在 AGENTS.md 中有強制規則 |
| 11 | MediaPlayer crash 風險 | 加入 try-catch + safe `release()` helper |
| 12 | Google Sign In 未完成 | 整合 GoogleSignIn API（加 `@Suppress("DEPRECATION")`），完整上線 |
| 13 | `TrendingUp` icon 棄用 | 改用 `Icons.AutoMirrored.Outlined.TrendingUp` |
| 14 | GoogleSignIn deprecated | 加 `@Suppress("DEPRECATION")` 保留原 API（Credential Manager 相容性不足） |
| 15 | `combine` 三 flow 同步問題 | `observeTypingStatus`/`observeChatStatus` 加 `onStart { emit() }` |
| 16 | 前端亂碼檢測 | 增加 `MAX_CHAR_FREQUENCY` 單字頻率檢查，現有規則已達實用標準 |
| 17 | IDE 快取過期 | 已加入 AGENTS.md 文件，使用 `edit` 工具減少觸發 |
| 18 | 中文亂碼問題（4 檔案） | AuthViewModel/AiRepository/NetworkUtils/MessageList 修復 UTF-8 編碼損毀 |
| 19 | 全形 `＠` 信箱格式 | `AuthUtils.normalizeEmail()` 自動轉半形 |
| 20 | 單機切換配對 | 還原為兩台設備流程，移除 PendingAcceptance/SeekerConfirmDialog 等 |

## 2026-07-19 — Round 15：修復匹配路徑不一致 + 專家經驗發佈 UI

### 修改檔案
- `functions/index.js` — `matchQuestionByTags()` 讀取路徑 `experiences` → `active_experiences`
- `data/repository/ExpertRepository.kt` — `expertId` → `authorId` 欄位名稱修正
- `ui/expert/components/QuickLogCard.kt` — 新增「也設為配對經驗」按鈕
- `ui/expert/ExpertScreen.kt` — 串接 `onPublishExperience`
- `data/repository/MatchingRepository.kt` + `di/CoreModule.kt` — 路徑常數修正
- `database.rules.json` — `active_experiences` 補 `authorId`/`timestamp` 驗證 + `.indexOn: ["status"]`
- `strings.xml` — 新增 `expert_label_experience_sync` + `expert_toast_experience_published`

### 變更
- `matchQuestionByTags()` 讀取路徑對齊客戶端寫入路徑
- QuickLogCard 新增次級按鈕「也設為配對經驗」

### 部署
- Cloud Function 部署成功（`batchProcessPendingSkills` + `batchProcessPendingQuestions`）
- Database Rules 部署成功（`active_experiences` 規則生效）

---

## 2026-07-19 — Round 13 復原修復

### 修正
- 還原 17 個 `ui/chat/` 檔案到 Round 13 前狀態
- **ChatMediaSender** — `onPendingRemoved` 成功觸發、try-catch、`onScrollToBottom`
- **ChatViewModel** — `onMessageAdded` 實際插入、`filteredMessages` dedup、`isChatActive` Snackbar
- **ChatScrollManager** — 移除 `totalItems > 0` 檢查
- **MessageRepository** — `sendMessageWithFields` 加入 failure listener
- **ChatScreen** — 強制 `isDarkTheme = true`、背景色、statusBarsPadding、imePadding、SnackbarHost
- **ChatTopBar/QuestionBanner** — 硬編碼色碼改 AppColors
- **BubbleContent** — Pending spinner 暗背景移除、置中
- **Firebase query listener bug** — `orderByChild("timestamp").limitToLast(N)` 不觸發，改用直接 `addValueEventListener`
- **`initChat` 總是更新 `_userId`** — ViewModel 重用時 userId 不再卡在舊值
- **文字樂觀更新** — `sendMessage` 直接插入 `optimistic_` 暫存訊息
- **Observer try-catch** — 防止 collect block 崩潰殺死整體觀察協程

---

## 2026-07-18 — Round 14：提問端 AI 標籤生成管線 + 非同步 Tag 配對

### 新增
- **提問端 AI 標籤生成管線** — 仿照專家端 `pending_skills` 模式，新增 `pending_questions` 路徑 + `batchProcessPendingQuestions` Cloud Function
- **Tag 相似度配對** — Cloud Function 以 Jaccard 相似度（門檻 0.15）進行配對
- **`Constants.kt`** — 新增 `PENDING_QUESTIONS` 路徑常數、`FirebaseFields` 擴充

### 修改檔案
- `functions/index.js` — 新增 `batchProcessPendingQuestions` + `matchQuestionByTags()`
- `data/Constants.kt` — `PENDING_QUESTIONS` 路徑 + `FirebaseFields` 新欄位
- `data/repository/QuestionRepository.kt` — `sendQuestion()` 入隊 `pending_questions/{id}`
- `ui/seeker/SeekerViewModel.kt` — 移除 `matchCoordinator.matchAndAssignExpert()`
- `database.rules.json` — 新增 `pending_questions` 路徑 + `.indexOn`
- `ui/chat/ChatScreen.kt` — SnackbarHost 修復
- `ui/chat/components/ChatTopBar.kt` — 硬編碼色碼改 AppColors
- 17 個 `ui/chat/` 檔案 — Round 13 復原 + 選擇性修復

### 變更
- 排程 `5min→1min`，批量上限 `20→50`
- `QuestionRepository.sendQuestion()` 同步寫入 `pending_questions/{id}`

### 已知問題
- `combine` 三 flow 同步問題 — 僅三者皆有新值時才 emit
- `orderByChild("timestamp")` query listener 不觸發 — 改用 direct listener

### 測試
- ✅ 提問「淘寶要怎麼樣從台灣退貨回去？」生成標籤 `["淘寶","退貨","台灣","物流"]`，PRIMARY 751ms

---

## 2026-07-18 — Round 13：全面 AppColors 主題化

### 變更
- **~120 處硬編碼色碼移除，~60 處 `isSystemInDarkTheme()` 砍掉，~10 處 emoji 改 Material Icon，~15 處新增動畫**
- 主題基底（3 檔案）— AppColors 色值調整、Theme 純深色 darkColorScheme、Type Nunito Sans
- 共用元件（7 檔案）— LoadingOverlay/ToastOverlay/CompactTextField/OfflineBanner 等全 AppColors
- 登入模組（5 檔案）— AuthScreen/WelcomePanel/LoginForm/ResetPasswordPanel/NicknameSettingsDialog
- 導航（2 檔案）— AppNavigation Scaffold Transparent
- 專家模組（6 檔案）— ExpertScreen/ExpertDialogs/QuickLogCard
- 提問者（11 檔案）— AskQuestionScreen/RoleSelectScreen/MatchingOverlay 等
- 聊天（12 檔案）— ChatTopBar/ChatBubble/BubbleContent 等
- 相機（6 檔案）+ 錄音（1 檔案）

### 修改檔案（36+ UI 檔案）

### Git
- 約 280+ 次提交
- 第 13 輪變更：36+ 個 UI 檔案

---

## 2026-07-17 — Round 12：主題系統重構 + DI 拆分

### 新增
- **Nunito Sans 字體** — `nunito_sans_regular.ttf` + `nunito_sans_bold.ttf`
- **6 個 DI module** — CoreModule, AuthModule, ChatModule, ExpertModule, SeekerModule, MediaModule

### 修改檔案
- `ui/theme/AppColors.kt` — 從 Color.kt 遷出獨立檔
- `ui/theme/Color.kt` — 合併進 AppColors.kt 後刪除
- `ui/theme/Theme.kt` — 純深色 darkColorScheme
- `ui/theme/Type.kt` — Nunito Sans 完整 14 級 Typography
- `di/CoreModule.kt`, `di/AuthModule.kt`, `di/ChatModule.kt`, `di/ExpertModule.kt`, `di/SeekerModule.kt`, `di/MediaModule.kt` — 新增
- `di/AppModule.kt` — 刪除（94 行 → 6 modules 共 158 行）
- `ui/MainActivity.kt` — KoinApplication 載入 6 modules
- `docs/MODULE_MAP.md` — DI 區塊更新
- 根目錄 `CHANGELOG_OLD.md`、`get_sha1.md` 搬入 `docs/`

### Git
- 約 53+ 次提交

---

## 2026-07-17 — Round 11：Serper 外部搜尋（避開 Gen3 Free Tier 限制）

### 新增
- `searchOnSerper()` 函式 — 調用 `https://google.serper.dev/search`
- `useWebFetch` 自訂旗標
- `SERPER_API_KEY` Firebase secret
- Gen3 thinking 語法支援 — `thinkingLevel`
- 批次測試按鈕（ExpertScreen 底部，20 筆冷門技能）
- Per-skill logging

### 修改檔案
- `functions/index.js` — Serper 搜尋整合、Model 陣列縮減為 4 個
- `ui/expert/ExpertScreen.kt` — 批次測試按鈕

### 模型測試結果（2026/7/17）
- `gemini-3.1-flash-lite` + Serper：731ms，2/2 接受 ✅
- `gemini-3-flash-preview` + Serper + thinkingLevel `low`：待測試

### Git
- 約 42+ 次提交

---

## 2026-07-16 — Round 10：AGP 升級 + ExpertViewModel 命名同步修復

### 修改檔案
- `gradle/libs.versions.toml` — AGP 9.2.1 → 9.3.0
- `ui/expert/ExpertViewModel.kt` — `publishErrorRes` → `publishFeedbackRes`
- `docs/` — API 文件更新

### 修正
- ExpertViewModel 命名未同步導致 IDE cascading 錯誤

---

## 2026-07-15 — Round 9：發布反饋改為 floating overlay

### 修改檔案
- `ui/expert/ExpertScreen.kt` — `onClearPublishError` → `onClearPublishFeedback`；Scaffold 包裹 Box；新增 floating overlay Card
- `ui/expert/components/QuickLogCard.kt` — 移除 inline error Text，改為 `onClearFeedback`
- `ui/expert/ExpertViewModel.kt` — `publishFeedbackRes` 命名同步、3 秒自動消失

### 廢棄
- QuickLogCard 的 `publishError` 參數

---

## 2026-07-15 — Cloud Function 修正

### 修正
- `model_status` 路徑編碼 — 模型名含 `.` 導致 RTDB crash
- Gemini 2.5 系列不支援 `responseMimeType: 'application/json'` + `tools: [googleSearch]` 並用
- FALLBACK 順序重排

---

## 2026-07-14 — Round 8：Crash 修復 + saveSkill 改寫 + 亂碼檢測 + Submission Lock

### 修改檔案
- `ui/expert/ExpertViewModel.kt` — userId.isBlank guard、submissionLock 監聽、isLoading 防連點
- `data/repository/ExpertRepository.kt` — callbackFlow guard、`updateChildren()` → 個別 `setValue()`
- `domain/expert/ExpertInputValidator.kt` — bigram 重複檢測、`SKILL_UNLIKELY_CHARS`
- `ui/expert/ExpertScreen.kt` — PENDING 隱藏卡片、ACTIVE 顯示標籤
- `ui/navigation/AppNavigation.kt` — LaunchedEffect guard
- `functions/index.js` — AI prompt 強化、Submission Lock 機制
- `strings.xml` — 新增 2 條字串

### 修正
- 啟動閃退 crash
- `updateChildren()` + `setPersistenceEnabled(true)` 衝突
- 亂碼檢測不足
- 惡意連續發布無阻擋機制

---

## 2026-07-14 — Round 7：大型重構（callbackFlow、狀態提升、資安加固）

### 新增檔案
- `data/Constants.kt` — FirebasePaths / FirebaseFields / StatusValues 常數

### 修改檔案（已列於 PROGRESS 第 7 輪）

### 關鍵決策
- Repository 無狀態化 — callbackFlow `awaitClose` 自動清除 listener
- `saveSkill()` 用 `updateChildren()` 原子寫入（後因 persistence 衝突在第 8 輪回退為個別 `setValue()`）
- State Hoisting — ExpertScreenContent 可獨立 Preview/測試
- 字串外部化 — ShowToast 使用 `@StringRes`
- 移除 `$other` 萬用字元規則

---

## 2026-07-13 — Round 6：全面修復（編輯流程、效能優化、測試強化）

### 修改檔案
- `domain/expert/ExtractLocalTagsUseCase.kt` — 刪除（dead code）
- `data/repository/ExpertRepository.kt` — `.await()` 寫法、`editSkill()`
- `ui/expert/ExpertViewModel.kt` — SkillEditDialog 狀態
- `ui/expert/ExpertScreen.kt` — 編輯按鈕、SkillEditDialog
- `functions/index.js` — 排程 5→1 分鐘
- `app/.../ExpertViewModelTest.kt` — 測試擴充

---

## 2026-07-13 — Cloud Function 初版

### 新增
- Cloud Function `batchProcessPendingSkills`（首版）
- 5 模型 fallback 鏈
- `config/model_status` 追蹤與重置
- `minInstances: 1`
- `responseMimeType: 'application/json'`

### 變更
- 客戶端 AI → 後端 Cloud Function
- SDK `@google/generative-ai` → `@google/genai`
- `admin.database()` → `getDatabase()`
- Node.js 20 → 24

---

## 2026-07-12 之前
- 初始專案建置
- 聊天功能基礎架構
- 登入/註冊流程
- CameraX 整合
- Media3 播放器
- Koin DI 整合
- Compose Navigation 路由

## Git 備註
- 約 280+ 次提交（至 Round 13），已全部推送至 main
- 第 7 輪：16 個 source 檔案
- 第 8 輪：9 個 source 檔案
- 第 9 輪：1 個檔案
- 第 10 輪：2 個檔案
- 第 11 輪：1 個檔案（`functions/index.js`）
- 第 12 輪：13+ 個檔案
- 第 13 輪：36+ 個 UI 檔案
- Round 14+：記載於對應區段