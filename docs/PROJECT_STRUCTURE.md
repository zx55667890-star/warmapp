# PROJECT_STRUCTURE.md — 專案目錄結構

**總計：125 個 Kotlin 檔，267 次 Git 提交**

## `app/src/main/java/com/example/myapplication/`

| 檔案 | 路徑 |
|------|------|
| **MainActivity** | [`MainActivity.kt`](../app/src/main/java/com/example/myapplication/MainActivity.kt) |

### `data/`
| 檔案 | 路徑 |
|------|------|
| **Constants** | [`Constants.kt`](../app/src/main/java/com/example/myapplication/data/Constants.kt) |

#### `data/model/`
| 檔案 | 路徑 |
|------|------|
| **ChatMessage** | [`ChatMessage.kt`](../app/src/main/java/com/example/myapplication/data/model/ChatMessage.kt) |
| **Experience** | [`Experience.kt`](../app/src/main/java/com/example/myapplication/data/model/Experience.kt) |
| **SolutionItem** | [`SolutionItem.kt`](../app/src/main/java/com/example/myapplication/data/model/SolutionItem.kt) |

#### `data/repository/`
| 檔案 | 路徑 |
|------|------|
| **AiRepository** | [`AiRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/AiRepository.kt) |
| **AuthRepository** | [`AuthRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/AuthRepository.kt) |
| **DataMigrator** | [`DataMigrator.kt`](../app/src/main/java/com/example/myapplication/data/repository/DataMigrator.kt) |
| **ExpertRepository** | [`ExpertRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/ExpertRepository.kt) |
| **FcmService** | [`FcmService.kt`](../app/src/main/java/com/example/myapplication/data/repository/FcmService.kt) |
| **MatchingRepository** | [`MatchingRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/MatchingRepository.kt) |
| **MatchingRepositoryInterface** | [`MatchingRepositoryInterface.kt`](../app/src/main/java/com/example/myapplication/data/repository/MatchingRepositoryInterface.kt) |
| **MediaUploader** | [`MediaUploader.kt`](../app/src/main/java/com/example/myapplication/data/repository/MediaUploader.kt) |
| **MessageRepository** | [`MessageRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/MessageRepository.kt) |
| **MessageRepositoryFactory** | [`MessageRepositoryFactory.kt`](../app/src/main/java/com/example/myapplication/data/repository/MessageRepositoryFactory.kt) |
| **MessageRepositoryInterface** | [`MessageRepositoryInterface.kt`](../app/src/main/java/com/example/myapplication/data/repository/MessageRepositoryInterface.kt) |
| **QuestionRepository** | [`QuestionRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/QuestionRepository.kt) |
| **UserRepository** | [`UserRepository.kt`](../app/src/main/java/com/example/myapplication/data/repository/UserRepository.kt) |

### `di/`
| 檔案 | 路徑 |
|------|------|
| **AppModule** | [`AppModule.kt`](../app/src/main/java/com/example/myapplication/di/AppModule.kt) |
| **ExpertViewModel (DI)** | [`ExpertViewModel.kt`](../app/src/main/java/com/example/myapplication/di/ExpertViewModel.kt) |
| **SeekerViewModel (DI)** | [`SeekerViewModel.kt`](../app/src/main/java/com/example/myapplication/di/SeekerViewModel.kt) |

### `domain/`

#### `domain/auth/`
| 檔案 | 路徑 |
|------|------|
| **GenerateVerificationCodeUseCase** | [`GenerateVerificationCodeUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/GenerateVerificationCodeUseCase.kt) |
| **LoginUseCase** | [`LoginUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/LoginUseCase.kt) |
| **LogoutUseCase** | [`LogoutUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/LogoutUseCase.kt) |
| **RegisterUseCase** | [`RegisterUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/RegisterUseCase.kt) |
| **ResetPasswordUseCase** | [`ResetPasswordUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/ResetPasswordUseCase.kt) |
| **SignInWithGoogleUseCase** | [`SignInWithGoogleUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/SignInWithGoogleUseCase.kt) |
| **VerifyVerificationCodeUseCase** | [`VerifyVerificationCodeUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/auth/VerifyVerificationCodeUseCase.kt) |

#### `domain/chat/`
| 檔案 | 路徑 |
|------|------|
| **FetchOpponentUseCase** | [`FetchOpponentUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/chat/FetchOpponentUseCase.kt) |
| **ObserveMessagesUseCase** | [`ObserveMessagesUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/chat/ObserveMessagesUseCase.kt) |
| **OpponentProfile** | [`OpponentProfile.kt`](../app/src/main/java/com/example/myapplication/domain/chat/OpponentProfile.kt) |
| **RecallMessageUseCase** | [`RecallMessageUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/chat/RecallMessageUseCase.kt) |
| **SendMediaUseCase** | [`SendMediaUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/chat/SendMediaUseCase.kt) |
| **SendTextMessageUseCase** | [`SendTextMessageUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/chat/SendTextMessageUseCase.kt) |

#### `domain/expert/`
| 檔案 | 路徑 |
|------|------|
| **ExpertInputValidator** | [`ExpertInputValidator.kt`](../app/src/main/java/com/example/myapplication/domain/expert/ExpertInputValidator.kt) |

#### `domain/seeker/`
| 檔案 | 路徑 |
|------|------|
| **MatchCoordinator** | [`MatchCoordinator.kt`](../app/src/main/java/com/example/myapplication/domain/seeker/MatchCoordinator.kt) |
| **ObserveQuestionStatusUseCase** | [`ObserveQuestionStatusUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/seeker/ObserveQuestionStatusUseCase.kt) |
| **SendQuestionMediaUseCase** | [`SendQuestionMediaUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/seeker/SendQuestionMediaUseCase.kt) |
| **ValidateQuestionQuotaUseCase** | [`ValidateQuestionQuotaUseCase.kt`](../app/src/main/java/com/example/myapplication/domain/seeker/ValidateQuestionQuotaUseCase.kt) |

### `ui/`

#### `ui/auth/`
| 檔案 | 路徑 |
|------|------|
| **AuthScreen** | [`AuthScreen.kt`](../app/src/main/java/com/example/myapplication/ui/auth/AuthScreen.kt) |
| **AuthViewModel** | [`AuthViewModel.kt`](../app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt) |
| **LoginForm** | [`LoginForm.kt`](../app/src/main/java/com/example/myapplication/ui/auth/LoginForm.kt) |
| **NicknameSettingsDialog** | [`NicknameSettingsDialog.kt`](../app/src/main/java/com/example/myapplication/ui/auth/NicknameSettingsDialog.kt) |
| **ResetPasswordPanel** | [`ResetPasswordPanel.kt`](../app/src/main/java/com/example/myapplication/ui/auth/ResetPasswordPanel.kt) |
| **WelcomePanel** | [`WelcomePanel.kt`](../app/src/main/java/com/example/myapplication/ui/auth/WelcomePanel.kt) |

#### `ui/camera/`
| 檔案 | 路徑 |
|------|------|
| **CameraCaptureScreen** | [`CameraCaptureScreen.kt`](../app/src/main/java/com/example/myapplication/ui/camera/CameraCaptureScreen.kt) |
| **CameraControlButtons** | [`CameraControlButtons.kt`](../app/src/main/java/com/example/myapplication/ui/camera/CameraControlButtons.kt) |
| **CameraPreviewActions** | [`CameraPreviewActions.kt`](../app/src/main/java/com/example/myapplication/ui/camera/CameraPreviewActions.kt) |
| **CameraViewModel** | [`CameraViewModel.kt`](../app/src/main/java/com/example/myapplication/ui/camera/CameraViewModel.kt) |
| **ImagePreviewScreen** | [`ImagePreviewScreen.kt`](../app/src/main/java/com/example/myapplication/ui/camera/ImagePreviewScreen.kt) |
| **VideoPreviewPlayer** | [`VideoPreviewPlayer.kt`](../app/src/main/java/com/example/myapplication/ui/camera/VideoPreviewPlayer.kt) |

#### `ui/chat/`
| 檔案 | 路徑 |
|------|------|
| **ChatEvent** | [`ChatEvent.kt`](../app/src/main/java/com/example/myapplication/ui/chat/ChatEvent.kt) |
| **ChatMediaSender** | [`ChatMediaSender.kt`](../app/src/main/java/com/example/myapplication/ui/chat/ChatMediaSender.kt) |
| **ChatScreen** | [`ChatScreen.kt`](../app/src/main/java/com/example/myapplication/ui/chat/ChatScreen.kt) |
| **ChatScrollManager** | [`ChatScrollManager.kt`](../app/src/main/java/com/example/myapplication/ui/chat/ChatScrollManager.kt) |
| **ChatUiState** | [`ChatUiState.kt`](../app/src/main/java/com/example/myapplication/ui/chat/ChatUiState.kt) |
| **ChatViewModel** | [`ChatViewModel.kt`](../app/src/main/java/com/example/myapplication/ui/chat/ChatViewModel.kt) |

##### `ui/chat/bubble/`
| 檔案 | 路徑 |
|------|------|
| **BubbleContent** | [`BubbleContent.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleContent.kt) |
| **BubbleContextMenu** | [`BubbleContextMenu.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleContextMenu.kt) |
| **BubbleStatusMetadata** | [`BubbleStatusMetadata.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleStatusMetadata.kt) |
| **ChatBubble** | [`ChatBubble.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/ChatBubble.kt) |
| **ImageGrid** | [`ImageGrid.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/ImageGrid.kt) |
| **VideoThumbnail** | [`VideoThumbnail.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/VideoThumbnail.kt) |
| **VoiceMessageBubble** | [`VoiceMessageBubble.kt`](../app/src/main/java/com/example/myapplication/ui/chat/bubble/VoiceMessageBubble.kt) |

##### `ui/chat/components/`
| 檔案 | 路徑 |
|------|------|
| **ChatBottomArea** | [`ChatBottomArea.kt`](../app/src/main/java/com/example/myapplication/ui/chat/components/ChatBottomArea.kt) |
| **ChatInputBar** | [`ChatInputBar.kt`](../app/src/main/java/com/example/myapplication/ui/chat/components/ChatInputBar.kt) |
| **ChatTopBar** | [`ChatTopBar.kt`](../app/src/main/java/com/example/myapplication/ui/chat/components/ChatTopBar.kt) |
| **MessageList** | [`MessageList.kt`](../app/src/main/java/com/example/myapplication/ui/chat/components/MessageList.kt) |
| **ReplyPreviewBar** | [`ReplyPreviewBar.kt`](../app/src/main/java/com/example/myapplication/ui/chat/components/ReplyPreviewBar.kt) |
| **TypingIndicator** | [`TypingIndicator.kt`](../app/src/main/java/com/example/myapplication/ui/chat/components/TypingIndicator.kt) |

##### `ui/chat/dialog/`
| 檔案 | 路徑 |
|------|------|
| **ChatDialogHost** | [`ChatDialogHost.kt`](../app/src/main/java/com/example/myapplication/ui/chat/dialog/ChatDialogHost.kt) |
| **EndChatConfirmDialog** | [`EndChatConfirmDialog.kt`](../app/src/main/java/com/example/myapplication/ui/chat/dialog/EndChatConfirmDialog.kt) |
| **OpponentProfileDialog** | [`OpponentProfileDialog.kt`](../app/src/main/java/com/example/myapplication/ui/chat/dialog/OpponentProfileDialog.kt) |

#### `ui/common/`
| 檔案 | 路徑 |
|------|------|
| **AuthUtils** | [`AuthUtils.kt`](../app/src/main/java/com/example/myapplication/ui/common/AuthUtils.kt) |
| **CompactTextField** | [`CompactTextField.kt`](../app/src/main/java/com/example/myapplication/ui/common/CompactTextField.kt) |
| **LoadingOverlay** | [`LoadingOverlay.kt`](../app/src/main/java/com/example/myapplication/ui/common/LoadingOverlay.kt) |
| **OfflineBanner** | [`OfflineBanner.kt`](../app/src/main/java/com/example/myapplication/ui/common/OfflineBanner.kt) |
| **ToastOverlay** | [`ToastOverlay.kt`](../app/src/main/java/com/example/myapplication/ui/common/ToastOverlay.kt) |
| **UiText** | [`UiText.kt`](../app/src/main/java/com/example/myapplication/ui/common/UiText.kt) |

#### `ui/components/`
| 檔案 | 路徑 |
|------|------|
| **FullScreenImageDialog** | [`FullScreenImageDialog.kt`](../app/src/main/java/com/example/myapplication/ui/components/FullScreenImageDialog.kt) |
| **RatingDialog** | [`RatingDialog.kt`](../app/src/main/java/com/example/myapplication/ui/components/RatingDialog.kt) |
| **ScrollToBottomButton** | [`ScrollToBottomButton.kt`](../app/src/main/java/com/example/myapplication/ui/components/ScrollToBottomButton.kt) |
| **VideoPlayerDialog** | [`VideoPlayerDialog.kt`](../app/src/main/java/com/example/myapplication/ui/components/VideoPlayerDialog.kt) |

#### `ui/expert/`
| 檔案 | 路徑 |
|------|------|
| **ExpertDialogs** | [`ExpertDialogs.kt`](../app/src/main/java/com/example/myapplication/ui/expert/ExpertDialogs.kt) |
| **ExpertScreen** | [`ExpertScreen.kt`](../app/src/main/java/com/example/myapplication/ui/expert/ExpertScreen.kt) |

##### `ui/expert/components/`
| 檔案 | 路徑 |
|------|------|
| **KnowledgeItemCard** | [`KnowledgeItemCard.kt`](../app/src/main/java/com/example/myapplication/ui/expert/components/KnowledgeItemCard.kt) |
| **QuickLogCard** | [`QuickLogCard.kt`](../app/src/main/java/com/example/myapplication/ui/expert/components/QuickLogCard.kt) |
| **SkillEditDialog** | [`SkillEditDialog.kt`](../app/src/main/java/com/example/myapplication/ui/expert/components/SkillEditDialog.kt) |

#### `ui/navigation/`
| 檔案 | 路徑 |
|------|------|
| **AppNavigation** | [`AppNavigation.kt`](../app/src/main/java/com/example/myapplication/ui/navigation/AppNavigation.kt) |
| **Route** | [`Route.kt`](../app/src/main/java/com/example/myapplication/ui/navigation/Route.kt) |

#### `ui/seeker/`
| 檔案 | 路徑 |
|------|------|
| **AskQuestionScreen** | [`AskQuestionScreen.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/AskQuestionScreen.kt) |
| **MatchingDialog** | [`MatchingDialog.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/MatchingDialog.kt) |
| **MatchingOverlay** | [`MatchingOverlay.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/MatchingOverlay.kt) |
| **RoleSelectScreen** | [`RoleSelectScreen.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/RoleSelectScreen.kt) |
| **SeekerConfirmDialog** | [`SeekerConfirmDialog.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/SeekerConfirmDialog.kt) |
| **SelectedMedia** | [`SelectedMedia.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/SelectedMedia.kt) |

##### `ui/seeker/components/`
| 檔案 | 路徑 |
|------|------|
| **AskQuestionHeader** | [`AskQuestionHeader.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/components/AskQuestionHeader.kt) |
| **AskQuestionInputBar** | [`AskQuestionInputBar.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/components/AskQuestionInputBar.kt) |
| **AttachmentBottomSheet** | [`AttachmentBottomSheet.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/components/AttachmentBottomSheet.kt) |
| **BackgroundGlow** | [`BackgroundGlow.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/components/BackgroundGlow.kt) |
| **DrawerContent** | [`DrawerContent.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/components/DrawerContent.kt) |
| **FullSettingsScreen** | [`FullSettingsScreen.kt`](../app/src/main/java/com/example/myapplication/ui/seeker/components/FullSettingsScreen.kt) |

#### `ui/theme/`
| 檔案 | 路徑 |
|------|------|
| **AppColors** | [`AppColors.kt`](../app/src/main/java/com/example/myapplication/ui/theme/AppColors.kt) |
| **Theme** | [`Theme.kt`](../app/src/main/java/com/example/myapplication/ui/theme/Theme.kt) |
| **Type** | [`Type.kt`](../app/src/main/java/com/example/myapplication/ui/theme/Type.kt) |

#### `ui/voice/`
| 檔案 | 路徑 |
|------|------|
| **VoiceRecordingScreen** | [`VoiceRecordingScreen.kt`](../app/src/main/java/com/example/myapplication/ui/voice/VoiceRecordingScreen.kt) |
| **VoiceRecordingViewModel** | [`VoiceRecordingViewModel.kt`](../app/src/main/java/com/example/myapplication/ui/voice/VoiceRecordingViewModel.kt) |

### `util/`
| 檔案 | 路徑 |
|------|------|
| **ExpertTitleUtil** | [`ExpertTitleUtil.kt`](../app/src/main/java/com/example/myapplication/util/ExpertTitleUtil.kt) |
| **ImageUtils** | [`ImageUtils.kt`](../app/src/main/java/com/example/myapplication/util/ImageUtils.kt) |
| **MediaMetadataHelper** | [`MediaMetadataHelper.kt`](../app/src/main/java/com/example/myapplication/util/MediaMetadataHelper.kt) |
| **NetworkUtils** | [`NetworkUtils.kt`](../app/src/main/java/com/example/myapplication/util/NetworkUtils.kt) |
| **VideoCacheManager** | [`VideoCacheManager.kt`](../app/src/main/java/com/example/myapplication/util/VideoCacheManager.kt) |
| **VideoThumbnailCache** | [`VideoThumbnailCache.kt`](../app/src/main/java/com/example/myapplication/util/VideoThumbnailCache.kt) |

## 資源
| 檔案 | 路徑 |
|------|------|
| **strings.xml** | [`strings.xml`](../app/src/main/res/values/strings.xml) |
| **themes.xml** | [`themes.xml`](../app/src/main/res/values/themes.xml) |
| **AndroidManifest** | [`AndroidManifest.xml`](../app/src/main/AndroidManifest.xml) |

## 測試
| 檔案 | 路徑 |
|------|------|
| **ExpertViewModelTest** | [`ExpertViewModelTest.kt`](../app/src/test/java/com/example/myapplication/di/ExpertViewModelTest.kt) |

## 後端
| 檔案 | 路徑 |
|------|------|
| **Cloud Function** | [`index.js`](../functions/index.js) |
| **package.json** | [`package.json`](../functions/package.json) |
| **package-lock.json** | [`package-lock.json`](../functions/package-lock.json) |
| **Security Rules** | [`database.rules.json`](../database.rules.json) |

## Gradle
| 檔案 | 路徑 |
|------|------|
| **Version Catalog** | [`libs.versions.toml`](../gradle/libs.versions.toml) |
| **App build** | [`build.gradle.kts`](../app/build.gradle.kts) |

## 根目錄文件
| 檔案 | 路徑 |
|------|------|
| **AI 核心規則** | [`AGENTS.md`](../AGENTS.md) |
| **修改索引** | [`CHAT_FILES_INDEX.md`](../CHAT_FILES_INDEX.md) |
| **開發進度** | [`PROGRESS.md`](../PROGRESS.md) |

## `docs/`
| 檔案 | 路徑 |
|------|------|
| **PROJECT_STRUCTURE** | [`PROJECT_STRUCTURE.md`](../docs/PROJECT_STRUCTURE.md) |
| **ARCHITECTURE** | [`ARCHITECTURE.md`](../docs/ARCHITECTURE.md) |
| **MODULE_MAP** | [`MODULE_MAP.md`](../docs/MODULE_MAP.md) |
| **AI_CONTEXT** | [`AI_CONTEXT.md`](../docs/AI_CONTEXT.md) |
| **DIRECTORY_RULES** | [`DIRECTORY_RULES.md`](../docs/DIRECTORY_RULES.md) |
| **CODING_STYLE** | [`CODING_STYLE.md`](../docs/CODING_STYLE.md) |
| **DEPENDENCIES** | [`DEPENDENCIES.md`](../docs/DEPENDENCIES.md) |
| **KNOWN_ISSUES** | [`KNOWN_ISSUES.md`](../docs/KNOWN_ISSUES.md) |
| **ROADMAP** | [`ROADMAP.md`](../docs/ROADMAP.md) |
| **FILE_RELATION** | [`FILE_RELATION.md`](../docs/FILE_RELATION.md) |
| **CHANGELOG** | [`CHANGELOG.md`](../docs/CHANGELOG.md) |
