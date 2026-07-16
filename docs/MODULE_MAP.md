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
| `data/repository/FcmService.kt` | Firebase Cloud Messaging |
| `data/model/ChatMessage.kt` | 聊天訊息資料模型 |

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
| `data/model/SolutionItem.kt` | 技能資料模型 + SkillStatus enum |
| `data/model/Experience.kt` | 專家上線經驗資料模型 |
| `domain/expert/ExpertInputValidator.kt` | 前端輸入驗證邏輯 |
| `domain/expert/PublishSkillUseCase.kt` | 發布技能 UseCase |
| `domain/expert/ObserveSolutionsUseCase.kt` | 監聽技能歷史 UseCase |
| `util/ExpertTitleUtil.kt` | 專家稱號工具 |

## 提問者模式 (seeker)
| 檔案 | 角色 |
|------|------|
| `ui/seeker/RoleSelectScreen.kt` | 角色選擇畫面 |
| `ui/seeker/AskQuestionScreen.kt` | 提問主畫面 |
| `ui/seeker/SelectedMedia.kt` | 已選媒體顯示 |
| `ui/seeker/MatchingOverlay.kt` | 配對中遮罩 |
| `ui/seeker/MatchingDialog.kt` | 配對對話框 |
| `ui/seeker/SeekerConfirmDialog.kt` | 確認彈窗 |
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
| `ui/navigation/AppNavigation.kt` | NavHost + 路由設定 |
| `ui/navigation/Route.kt` | 路由定義 sealed class |

## 主題
| 檔案 | 角色 |
|------|------|
| `ui/theme/Color.kt` | 基礎色板（Purple/Pink 主題色） |
| `ui/theme/AppColors.kt` | AppColors 深色主題色板 |
| `ui/theme/Theme.kt` | Material3 主題設定 |
| `ui/theme/Type.kt` | 字型定義 |

## 資源
| 檔案 | 角色 |
|------|------|
| `res/values/strings.xml` | 所有 UI 字串（50+ 條） |
| `res/values/themes.xml` | 系統主題 |

## 工具
| 檔案 | 角色 |
|------|------|
| `util/ImageUtils.kt` | 圖片處理工具 |
| `util/NetworkUtils.kt` | 網路狀態工具 |

## DI / 依賴注入
| 檔案 | 角色 |
|------|------|
| `di/AppModule.kt` | Koin 模組（所有 singleton + ViewModel） |
| `data/Constants.kt` | FirebasePaths, FirebaseFields, StatusValues 常數 |
| `data/repository/DataMigrator.kt` | 資料遷移工具 |
| `data/repository/AiRepository.kt` | AI 相關 Firebase Functions 呼叫 |
| `data/repository/UserRepository.kt` | 使用者資料 CRUD |

## 後端 (Cloud Function)
| 檔案 | 角色 |
|------|------|
| `functions/index.js` | batchProcessPendingSkills (排程 AI 分析，含 Serper 搜尋、thinkingConfig、4 model fallback) |
| `functions/package.json` | Node.js 24, firebase-admin, @google/genai |
| `database.rules.json` | RTDB Security Rules |

## docs/ 文件
| 檔案 | 角色 |
|------|------|
| `AGENTS.md` | AI 核心規則 |
| `PROGRESS.md` | 開發進度 |
| `CHAT_FILES_INDEX.md` | 每次對話修改檔案索引 |
| `ARCHITECTURE.md` | 系統架構與資料流 |
| `MODULE_MAP.md` | 功能對應檔案 |
| `AI_CONTEXT.md` | AI 開發者必讀（危險區域、不能改的） |
| `DIRECTORY_RULES.md` | 分層規則與 import 限制 |
| `CODING_STYLE.md` | 程式碼風格 |
| `DEPENDENCIES.md` | 版本依賴資訊 |
| `KNOWN_ISSUES.md` | 已知問題 |
| `ROADMAP.md` | 開發藍圖 |
| `FILE_RELATION.md` | 檔案依賴關係 |
| `CHANGELOG.md` | 更新紀錄 |
