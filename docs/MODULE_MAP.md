# MODULE_MAP.md — 功能對應檔案

## 登入 / 驗證
| 檔案 | 角色 |
|------|------|
| `ui/auth/AuthScreen.kt` | 登入首頁 composable |
| `ui/auth/LoginForm.kt` | 登入表單 composable |
| `ui/auth/WelcomePanel.kt` | 歡迎畫面 composable |
| `ui/auth/ResetPasswordPanel.kt` | 重設密碼 composable |
| `ui/auth/NicknameSettingsDialog.kt` | 暱稱設定彈窗 |
| `ui/auth/AuthViewModel.kt` | 驗證流程 ViewModel |
| `domain/auth/LoginUseCase.kt` | 登入 use case |
| `domain/auth/RegisterUseCase.kt` | 註冊 use case |
| `domain/auth/LogoutUseCase.kt` | 登出 use case |
| `domain/auth/ResetPasswordUseCase.kt` | 重設密碼 use case |
| `domain/auth/GenerateVerificationCodeUseCase.kt` | 產 verification code |
| `domain/auth/VerifyVerificationCodeUseCase.kt` | 驗證 verification code |
| `domain/auth/SignInWithGoogleUseCase.kt` | Google 登入 use case |
| `data/repository/AuthRepository.kt` | Firebase Auth 操作 |

**依賴樹：**
```
AuthScreen.kt
  ├── WelcomePanel.kt
  ├── LoginForm.kt
  ├── ResetPasswordPanel.kt
  └── NicknameSettingsDialog.kt

AuthViewModel.kt
  ├── LoginUseCase.kt
  ├── RegisterUseCase.kt
  ├── LogoutUseCase.kt
  ├── ResetPasswordUseCase.kt
  ├── GenerateVerificationCodeUseCase.kt
  ├── VerifyVerificationCodeUseCase.kt
  ├── SignInWithGoogleUseCase.kt
  └── AuthRepository.kt (Firebase Auth)
```

## 聊天
| 檔案 | 角色 |
|------|------|
| `ui/chat/ChatScreen.kt` | 聊天主畫面 |
| `ui/chat/ChatViewModel.kt` | 聊天 ViewModel |
| `ui/chat/ChatEvent.kt` | 聊天事件 sealed class |
| `ui/chat/ChatUiState.kt` | 聊天 UI 狀態 data class |
| `ui/chat/ChatScrollManager.kt` | 聊天滾動管理器 |
| `ui/chat/ChatMediaSender.kt` | 媒體傳送邏輯 |
| `ui/chat/bubble/ChatBubble.kt` | 聊天泡泡容器 |
| `ui/chat/bubble/BubbleContent.kt` | 泡泡內容（文字/圖片/影片） |
| `ui/chat/bubble/BubbleContextMenu.kt` | 泡泡長按選單 |
| `ui/chat/bubble/BubbleStatusMetadata.kt` | 訊息狀態（已讀/時間） |
| `ui/chat/bubble/ImageGrid.kt` | 圖片網格顯示 |
| `ui/chat/bubble/VideoThumbnail.kt` | 影片縮圖 |
| `ui/chat/bubble/VoiceMessageBubble.kt` | 語音訊息泡泡 |
| `ui/chat/components/ChatTopBar.kt` | 頂部工具列 |
| `ui/chat/components/ChatBottomArea.kt` | 底部輸入區域容器 |
| `ui/chat/components/ChatInputBar.kt` | 文字輸入列 |
| `ui/chat/components/MessageList.kt` | 訊息列表 |
| `ui/chat/components/ReplyPreviewBar.kt` | 回覆預覽列 |
| `ui/chat/components/TypingIndicator.kt` | 打字中指示器 |
| `ui/chat/dialog/ChatDialogHost.kt` | 聊天對話框容器 |
| `ui/chat/dialog/EndChatConfirmDialog.kt` | 結束對話確認彈窗 |
| `ui/chat/dialog/OpponentProfileDialog.kt` | 對方資料彈窗 |
| `domain/chat/SendTextMessageUseCase.kt` | 發送文字訊息 |
| `domain/chat/SendMediaUseCase.kt` | 發送媒體訊息 |
| `domain/chat/ObserveMessagesUseCase.kt` | 監聽訊息 |
| `domain/chat/FetchOpponentUseCase.kt` | 取得對方資料 |
| `domain/chat/RecallMessageUseCase.kt` | 收回訊息 |
| `domain/chat/OpponentProfile.kt` | 對方資料模型 |
| `data/repository/MessageRepository.kt` | 訊息資料存取 |
| `data/repository/MessageRepositoryInterface.kt` | 訊息 repository interface |
| `data/repository/MessageRepositoryFactory.kt` | 訊息 repository factory |
| `data/repository/FcmService.kt` | FCM 推播服務（位於 repository 目錄但實為 Service，透過 Service 與 ViewModel 通訊） |
| `data/model/ChatMessage.kt` | 聊天訊息資料模型 |

**依賴樹：**
```
ChatScreen.kt
  ├── ChatTopBar.kt
  ├── MessageList.kt
  │     └── ChatBubble.kt
  │           ├── BubbleContent.kt
  │           ├── BubbleContextMenu.kt
  │           └── BubbleStatusMetadata.kt
  ├── ChatBottomArea.kt
  │     ├── ChatInputBar.kt
  │     └── ReplyPreviewBar.kt
  └── ChatDialogHost.kt
        ├── EndChatConfirmDialog.kt
        └── OpponentProfileDialog.kt

ChatViewModel.kt
  ├── ChatEvent.kt (sealed class)
  ├── ChatUiState.kt (data class)
  ├── ChatScrollManager.kt
  ├── ChatMediaSender.kt
  ├── MessageRepository.kt (Firebase RTDB)
  ├── MediaUploader.kt (Firebase Storage)
  └── FcmService.kt (推播)
```

## 專家模式 (skill 技能發布)
| 檔案 | 角色 |
|------|------|
| `ui/expert/ExpertScreen.kt` | ExpertScreen bridge + ExpertScreenContent |
| `ui/expert/components/QuickLogCard.kt` | 技能輸入卡片 + FeedbackBanner |
| `ui/expert/components/KnowledgeItemCard.kt` | 知識庫項目卡片 + EmptyKnowledgeCard |
| `ui/expert/components/SkillEditDialog.kt` | 技能編輯對話框 |
| `ui/expert/ExpertDialogs.kt` | 專家相關對話框 |
| `ui/expert/ExpertViewModel.kt` | 專家 ViewModel（publish/edit/listen） |
| `data/repository/ExpertRepository.kt` | 技能資料 CRUD（Firebase RTDB） |
| `data/repository/AiRepository.kt` | AI 標籤分析（Gemini API） |
| `data/model/SolutionItem.kt` | 技能資料模型 + SkillStatus enum |
| `data/model/Experience.kt` | 專家上線經驗資料模型 |
| `domain/expert/ExpertInputValidator.kt` | 前端輸入驗證邏輯 |
| `domain/expert/PublishSkillUseCase.kt` | 發布技能 UseCase |
| `domain/expert/ObserveSolutionsUseCase.kt` | 監聽技能歷史 UseCase |
| `util/ExpertTitleUtil.kt` | 專家稱號工具 |

**依賴樹：**
```
ExpertScreen.kt
  ├── ExpertScreenContent (stateless)
  │   ├── components/QuickLogCard.kt (輸入 + 發布)
  │   ├── components/KnowledgeItemCard.kt (技能卡片顯示)
  │   └── components/SkillEditDialog.kt (編輯彈窗)
  │
  └── ExpertViewModel.kt
        ├── PublishSkillUseCase.kt (發布技能)
        ├── ObserveSolutionsUseCase.kt (監聽技能歷史)
        ├── ExpertInputValidator.kt (驗證邏輯)
        ├── ExpertRepository.kt (Firebase 資料)
        │     ├── Constants.kt (路徑/欄位/狀態常數)
        │     └── SolutionItem.kt (資料模型)
        └── AppModule.kt (DI - 路徑 ui/expert/)
```

## 提問者模式 (seeker)
| 檔案 | 角色 |
|------|------|
| `ui/seeker/RoleSelectScreen.kt` | 角色選擇畫面 |
| `ui/seeker/AskQuestionScreen.kt` | 提問主畫面 |
| `ui/seeker/SelectedMedia.kt` | 已選媒體顯示 |
| `ui/seeker/MatchingOverlay.kt` | 配對中遮罩 |
| `ui/seeker/components/AskQuestionHeader.kt` | 提問標題列 |
| `ui/seeker/components/AskQuestionInputBar.kt` | 提問輸入列 |
| `ui/seeker/components/AttachmentBottomSheet.kt` | 附件選擇 bottom sheet |
| `ui/seeker/components/BackgroundGlow.kt` | 背景光暈 |
| `ui/seeker/components/DrawerContent.kt` | 側邊欄內容 |
| `ui/seeker/components/FullSettingsScreen.kt` | 設定畫面 |
| `ui/seeker/SeekerViewModel.kt` | 提問者 ViewModel |
| `domain/seeker/MatchCoordinator.kt` | 配對協調器 |
| `domain/seeker/ObserveQuestionStatusUseCase.kt` | 問題狀態監聽 |
| `domain/seeker/SendQuestionMediaUseCase.kt` | 傳送問題媒體 |
| `domain/seeker/ValidateQuestionQuotaUseCase.kt` | 發問額度驗證 |
| `data/repository/QuestionRepository.kt` | 問題資料存取 |
| `data/repository/MatchingRepository.kt` | 配對資料存取 |
| `data/repository/MatchingRepositoryInterface.kt` | 配對 repository interface |

**依賴樹：**
```
AskQuestionScreen.kt
  ├── AskQuestionHeader.kt
  ├── AskQuestionInputBar.kt
  ├── AttachmentBottomSheet.kt
  └── SelectedMedia.kt

RoleSelectScreen.kt
  └── MatchingOverlay.kt

SeekerViewModel.kt (ui/seeker/)
  ├── QuestionRepository.kt
  ├── MatchingRepository.kt
  ├── MatchCoordinator.kt
  ├── ValidateQuestionQuotaUseCase.kt
  └── SendQuestionMediaUseCase.kt
```

## 相機 / 媒體
| 檔案 | 角色 |
|------|------|
| `ui/camera/CameraCaptureScreen.kt` | 相機拍攝畫面 |
| `ui/camera/CameraControlButtons.kt` | 相機控制按鈕 |
| `ui/camera/CameraPreviewActions.kt` | 預覽操作 |
| `ui/camera/CameraViewModel.kt` | 相機 ViewModel |
| `ui/camera/ImagePreviewScreen.kt` | 圖片預覽 |
| `ui/camera/VideoPreviewPlayer.kt` | 影片播放器 |
| `data/repository/MediaUploader.kt` | 媒體上傳（Storage） |
| `util/MediaMetadataHelper.kt` | 媒體詮釋資料 helper |
| `util/VideoCacheManager.kt` | 影片快取管理 |
| `util/VideoThumbnailCache.kt` | 影片縮圖快取 |

## 錄音
| 檔案 | 角色 |
|------|------|
| `ui/voice/VoiceRecordingScreen.kt` | 錄音畫面 |
| `ui/voice/VoiceRecordingViewModel.kt` | 錄音 ViewModel |

## 共用 UI
| 檔案 | 角色 |
|------|------|
| `ui/common/AuthUtils.kt` | 驗證相關工具 |
| `ui/common/CompactTextField.kt` | 緊湊文字輸入框 |
| `ui/common/LoadingOverlay.kt` | 載入遮罩 |
| `ui/common/OfflineBanner.kt` | 離線通知橫幅 |
| `ui/common/ToastOverlay.kt` | Toast 疊層 |
| `ui/common/UiText.kt` | UI 文字 sealed class |
| `ui/components/FullScreenImageDialog.kt` | 全螢幕圖片 |
| `ui/components/RatingDialog.kt` | 評分對話框 |
| `ui/components/ScrollToBottomButton.kt` | 滾到底部按鈕 |
| `ui/components/VideoPlayerDialog.kt` | 影片播放對話框 |

## 導航
| 檔案 | 角色 |
|------|------|
| `MainActivity.kt` | App 入口 Activity |
| `ui/navigation/AppNavigation.kt` | NavHost + 路由設定 |
| `ui/navigation/Route.kt` | 路由定義 sealed class |

## 主題
| 檔案 | 角色 |
|------|------|
| `ui/theme/AppColors.kt` | 深色主題色板（背景層級、強調色、文字、邊框、玻璃效果、狀態色） |
| `ui/theme/Theme.kt` | 純深色 Material3 主題（darkColorScheme） |
| `ui/theme/Type.kt` | Nunito Sans 字型 + 14 級 Typography |

## 資源
| 檔案 | 角色 |
|------|------|
| `res/values/strings.xml` | 所有 UI 字串（~41 條） |
| `res/values/themes.xml` | 系統主題 |

## 工具
| 檔案 | 角色 |
|------|------|
| `util/ImageUtils.kt` | 圖片處理工具 |
| `util/NetworkUtils.kt` | 網路狀態工具 |

## DI / 依賴注入
| 檔案 | 角色 |
|------|------|
| `di/CoreModule.kt` | 核心基礎設施（Firebase + 跨功能共用單例） |
| `di/AuthModule.kt` | 登入驗證模組 |
| `di/ChatModule.kt` | 聊天模組 |
| `di/ExpertModule.kt` | 專家模組 |
| `di/SeekerModule.kt` | 提問者模組 |
| `di/MediaModule.kt` | 媒體模組 |

## 資料層 (Data)
| 檔案 | 角色 |
|------|------|
| `data/Constants.kt` | FirebasePaths, FirebaseFields, StatusValues 常數 |
| `data/repository/UserRepository.kt` | 使用者資料 CRUD |
| `data/repository/DataMigrator.kt` | 資料遷移工具 |

## 後端 (Cloud Function)
| 檔案 | 角色 |
|------|------|
| `functions/index.js` | processSkillsOnWrite / processQuestionsOnWrite (DB triggered AI 分析，含語意快取、Serper 搜尋、6 模型 fallback) |
| `functions/package.json` | Node.js 24, firebase-admin, @google/genai |
| `database.rules.json` | RTDB Security Rules |

**依賴樹：**
```
DB write → pending_skills/{id} (onValueWritten)
  └── processSkillsOnWrite (index.js)
        ├── Firebase RTDB (read pending_skills)
        ├── Whitelist exact match (tags_whitelist)
        ├── Semantic cache (embedding cosine similarity, threshold 0.75)
        ├── LLM tag generation (6 model fallback chain)
        │     ├── Serper external search (FALLBACK_1, 4, 5)
        │     └── Google Search grounding (FALLBACK_2~3)
        └── matchSkillByTags()

DB write → pending_questions/{id} (onValueWritten)
  └── processQuestionsOnWrite (index.js)
        ├── Firebase RTDB (read pending_questions)
        ├── Whitelist exact match (tags_whitelist)
        ├── Semantic cache (embedding cosine similarity, threshold 0.75)
        ├── LLM tag generation (6 model fallback chain)
        │     ├── Serper external search (FALLBACK_1, 4, 5)
        │     └── Google Search grounding (FALLBACK_2~3)
        └── matchQuestionByTags()
```

## docs/ 文件
| 檔案 | 角色 |
|------|------|
| `AGENTS.md` | AI 核心規則 |
| `ARCHITECTURE.md` | 系統架構與資料流 |
| `MODULE_MAP.md` | 模組地圖（表格 + 依賴樹） |
| `AI_CONTEXT.md` | AI 開發者必讀（危險區域、指令） |
| `CODING_STYLE.md` | 程式碼風格與分層規則 |
| `DEPENDENCIES.md` | 版本依賴資訊 |
| `ROADMAP.md` | 開發藍圖 |
| `CHANGELOG.md` | 更新紀錄（含進度、檔案索引、已解決問題） |
| `CHANGELOG_OLD.md` | 更新紀錄封存 (Round 16~17) |
| `PROJECT_STRUCTURE.md` | 專案目錄結構 |
| `index.md` | 專案文件入口 |
| `R8_SETUP.md` | R8 混淆設定指南 |
| `gradle/libs.versions.toml` | 版本目錄（Version Catalog） |
