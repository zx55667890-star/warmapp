# PROJECT_STRUCTURE.md — 專案目錄結構

**總計：117 個 Kotlin 檔，341 次 Git 提交**

> 本檔案由 generate_project_structure.sh 自動產生於 2026-07-19 18:54:57，請勿手動編輯。

```
warmapp/
（未安裝 tree，改用 find 列出所有 .kt / 設定檔路徑，排版較陽春，建議 apt/brew install tree 後重跑）
app/google-services.json
app/src/main/java/com/example/myapplication/MainActivity.kt
app/src/main/java/com/example/myapplication/data/Constants.kt
app/src/main/java/com/example/myapplication/data/model/ChatMessage.kt
app/src/main/java/com/example/myapplication/data/model/Experience.kt
app/src/main/java/com/example/myapplication/data/model/SolutionItem.kt
app/src/main/java/com/example/myapplication/data/repository/AiRepository.kt
app/src/main/java/com/example/myapplication/data/repository/AuthRepository.kt
app/src/main/java/com/example/myapplication/data/repository/DataMigrator.kt
app/src/main/java/com/example/myapplication/data/repository/ExpertRepository.kt
app/src/main/java/com/example/myapplication/data/repository/FcmService.kt
app/src/main/java/com/example/myapplication/data/repository/MatchingRepository.kt
app/src/main/java/com/example/myapplication/data/repository/MatchingRepositoryInterface.kt
app/src/main/java/com/example/myapplication/data/repository/MediaUploader.kt
app/src/main/java/com/example/myapplication/data/repository/MessageRepository.kt
app/src/main/java/com/example/myapplication/data/repository/MessageRepositoryFactory.kt
app/src/main/java/com/example/myapplication/data/repository/MessageRepositoryInterface.kt
app/src/main/java/com/example/myapplication/data/repository/QuestionRepository.kt
app/src/main/java/com/example/myapplication/data/repository/UserRepository.kt
app/src/main/java/com/example/myapplication/di/AuthModule.kt
app/src/main/java/com/example/myapplication/di/ChatModule.kt
app/src/main/java/com/example/myapplication/di/CoreModule.kt
app/src/main/java/com/example/myapplication/di/ExpertModule.kt
app/src/main/java/com/example/myapplication/di/MediaModule.kt
app/src/main/java/com/example/myapplication/di/SeekerModule.kt
app/src/main/java/com/example/myapplication/domain/auth/GenerateVerificationCodeUseCase.kt
app/src/main/java/com/example/myapplication/domain/auth/LoginUseCase.kt
app/src/main/java/com/example/myapplication/domain/auth/LogoutUseCase.kt
app/src/main/java/com/example/myapplication/domain/auth/RegisterUseCase.kt
app/src/main/java/com/example/myapplication/domain/auth/ResetPasswordUseCase.kt
app/src/main/java/com/example/myapplication/domain/auth/SignInWithGoogleUseCase.kt
app/src/main/java/com/example/myapplication/domain/auth/VerifyVerificationCodeUseCase.kt
app/src/main/java/com/example/myapplication/domain/chat/FetchOpponentUseCase.kt
app/src/main/java/com/example/myapplication/domain/chat/ObserveMessagesUseCase.kt
app/src/main/java/com/example/myapplication/domain/chat/OpponentProfile.kt
app/src/main/java/com/example/myapplication/domain/chat/RecallMessageUseCase.kt
app/src/main/java/com/example/myapplication/domain/chat/SendMediaUseCase.kt
app/src/main/java/com/example/myapplication/domain/chat/SendTextMessageUseCase.kt
app/src/main/java/com/example/myapplication/domain/expert/ExpertInputValidator.kt
app/src/main/java/com/example/myapplication/domain/expert/ObserveSolutionsUseCase.kt
app/src/main/java/com/example/myapplication/domain/expert/PublishSkillUseCase.kt
app/src/main/java/com/example/myapplication/domain/seeker/MatchCoordinator.kt
app/src/main/java/com/example/myapplication/domain/seeker/ObserveQuestionStatusUseCase.kt
app/src/main/java/com/example/myapplication/domain/seeker/SendQuestionMediaUseCase.kt
app/src/main/java/com/example/myapplication/domain/seeker/ValidateQuestionQuotaUseCase.kt
app/src/main/java/com/example/myapplication/ui/auth/AuthScreen.kt
app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt
app/src/main/java/com/example/myapplication/ui/auth/LoginForm.kt
app/src/main/java/com/example/myapplication/ui/auth/NicknameSettingsDialog.kt
app/src/main/java/com/example/myapplication/ui/auth/ResetPasswordPanel.kt
app/src/main/java/com/example/myapplication/ui/auth/WelcomePanel.kt
app/src/main/java/com/example/myapplication/ui/camera/CameraCaptureScreen.kt
app/src/main/java/com/example/myapplication/ui/camera/CameraControlButtons.kt
app/src/main/java/com/example/myapplication/ui/camera/CameraPreviewActions.kt
app/src/main/java/com/example/myapplication/ui/camera/CameraViewModel.kt
app/src/main/java/com/example/myapplication/ui/camera/ImagePreviewScreen.kt
app/src/main/java/com/example/myapplication/ui/camera/VideoPreviewPlayer.kt
app/src/main/java/com/example/myapplication/ui/chat/ChatEvent.kt
app/src/main/java/com/example/myapplication/ui/chat/ChatMediaSender.kt
app/src/main/java/com/example/myapplication/ui/chat/ChatScreen.kt
app/src/main/java/com/example/myapplication/ui/chat/ChatScrollManager.kt
app/src/main/java/com/example/myapplication/ui/chat/ChatUiState.kt
app/src/main/java/com/example/myapplication/ui/chat/ChatViewModel.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleContent.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleContextMenu.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleStatusMetadata.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/ChatBubble.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/ImageGrid.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/VideoThumbnail.kt
app/src/main/java/com/example/myapplication/ui/chat/bubble/VoiceMessageBubble.kt
app/src/main/java/com/example/myapplication/ui/chat/components/ChatBottomArea.kt
app/src/main/java/com/example/myapplication/ui/chat/components/ChatInputBar.kt
app/src/main/java/com/example/myapplication/ui/chat/components/ChatTopBar.kt
app/src/main/java/com/example/myapplication/ui/chat/components/MessageList.kt
app/src/main/java/com/example/myapplication/ui/chat/components/ReplyPreviewBar.kt
app/src/main/java/com/example/myapplication/ui/chat/components/TypingIndicator.kt
app/src/main/java/com/example/myapplication/ui/chat/dialog/ChatDialogHost.kt
app/src/main/java/com/example/myapplication/ui/chat/dialog/EndChatConfirmDialog.kt
app/src/main/java/com/example/myapplication/ui/chat/dialog/OpponentProfileDialog.kt
app/src/main/java/com/example/myapplication/ui/common/AuthUtils.kt
app/src/main/java/com/example/myapplication/ui/common/CompactTextField.kt
app/src/main/java/com/example/myapplication/ui/common/LoadingOverlay.kt
app/src/main/java/com/example/myapplication/ui/common/OfflineBanner.kt
app/src/main/java/com/example/myapplication/ui/common/ToastOverlay.kt
app/src/main/java/com/example/myapplication/ui/common/UiText.kt
app/src/main/java/com/example/myapplication/ui/components/FullScreenImageDialog.kt
app/src/main/java/com/example/myapplication/ui/components/RatingDialog.kt
app/src/main/java/com/example/myapplication/ui/components/ScrollToBottomButton.kt
app/src/main/java/com/example/myapplication/ui/components/VideoPlayerDialog.kt
app/src/main/java/com/example/myapplication/ui/expert/ExpertDialogs.kt
app/src/main/java/com/example/myapplication/ui/expert/ExpertScreen.kt
app/src/main/java/com/example/myapplication/ui/expert/ExpertViewModel.kt
app/src/main/java/com/example/myapplication/ui/expert/components/KnowledgeItemCard.kt
app/src/main/java/com/example/myapplication/ui/expert/components/QuickLogCard.kt
app/src/main/java/com/example/myapplication/ui/expert/components/SkillEditDialog.kt
app/src/main/java/com/example/myapplication/ui/navigation/AppNavigation.kt
app/src/main/java/com/example/myapplication/ui/navigation/Route.kt
app/src/main/java/com/example/myapplication/ui/seeker/AskQuestionScreen.kt
app/src/main/java/com/example/myapplication/ui/seeker/MatchingOverlay.kt
app/src/main/java/com/example/myapplication/ui/seeker/RoleSelectScreen.kt
app/src/main/java/com/example/myapplication/ui/seeker/SeekerViewModel.kt
app/src/main/java/com/example/myapplication/ui/seeker/SelectedMedia.kt
app/src/main/java/com/example/myapplication/ui/seeker/components/AskQuestionHeader.kt
app/src/main/java/com/example/myapplication/ui/seeker/components/AskQuestionInputBar.kt
app/src/main/java/com/example/myapplication/ui/seeker/components/AttachmentBottomSheet.kt
app/src/main/java/com/example/myapplication/ui/seeker/components/BackgroundGlow.kt
app/src/main/java/com/example/myapplication/ui/seeker/components/DrawerContent.kt
app/src/main/java/com/example/myapplication/ui/seeker/components/FullSettingsScreen.kt
app/src/main/java/com/example/myapplication/ui/theme/AppColors.kt
app/src/main/java/com/example/myapplication/ui/theme/Theme.kt
app/src/main/java/com/example/myapplication/ui/theme/Type.kt
app/src/main/java/com/example/myapplication/ui/voice/VoiceRecordingScreen.kt
app/src/main/java/com/example/myapplication/ui/voice/VoiceRecordingViewModel.kt
app/src/main/java/com/example/myapplication/util/ExpertTitleUtil.kt
app/src/main/java/com/example/myapplication/util/ImageUtils.kt
app/src/main/java/com/example/myapplication/util/MediaMetadataHelper.kt
app/src/main/java/com/example/myapplication/util/NetworkUtils.kt
app/src/main/java/com/example/myapplication/util/VideoCacheManager.kt
app/src/main/java/com/example/myapplication/util/VideoThumbnailCache.kt
docs/AGENTS.md
docs/AI_CONTEXT.md
docs/ARCHITECTURE.md
docs/CHANGELOG.md
docs/CHANGELOG_OLD.md
docs/CODING_STYLE.md
docs/DEPENDENCIES.md
docs/MODULE_MAP.md
docs/PROJECT_STRUCTURE.md
docs/ROADMAP.md
docs/index.md
functions/index.js
functions/package-lock.json
functions/package.json
gradle/libs.versions.toml
```

## 快速核對用清單

### domain/ 底下所有 UseCase
- com/example/myapplication/domain/auth/GenerateVerificationCodeUseCase.kt
- com/example/myapplication/domain/auth/LoginUseCase.kt
- com/example/myapplication/domain/auth/LogoutUseCase.kt
- com/example/myapplication/domain/auth/RegisterUseCase.kt
- com/example/myapplication/domain/auth/ResetPasswordUseCase.kt
- com/example/myapplication/domain/auth/SignInWithGoogleUseCase.kt
- com/example/myapplication/domain/auth/VerifyVerificationCodeUseCase.kt
- com/example/myapplication/domain/chat/FetchOpponentUseCase.kt
- com/example/myapplication/domain/chat/ObserveMessagesUseCase.kt
- com/example/myapplication/domain/chat/OpponentProfile.kt
- com/example/myapplication/domain/chat/RecallMessageUseCase.kt
- com/example/myapplication/domain/chat/SendMediaUseCase.kt
- com/example/myapplication/domain/chat/SendTextMessageUseCase.kt
- com/example/myapplication/domain/expert/ExpertInputValidator.kt
- com/example/myapplication/domain/expert/ObserveSolutionsUseCase.kt
- com/example/myapplication/domain/expert/PublishSkillUseCase.kt
- com/example/myapplication/domain/seeker/MatchCoordinator.kt
- com/example/myapplication/domain/seeker/ObserveQuestionStatusUseCase.kt
- com/example/myapplication/domain/seeker/SendQuestionMediaUseCase.kt
- com/example/myapplication/domain/seeker/ValidateQuestionQuotaUseCase.kt

### di/ 底下所有檔案
- com/example/myapplication/di/AuthModule.kt
- com/example/myapplication/di/ChatModule.kt
- com/example/myapplication/di/CoreModule.kt
- com/example/myapplication/di/ExpertModule.kt
- com/example/myapplication/di/MediaModule.kt
- com/example/myapplication/di/SeekerModule.kt

### ui/*/*ViewModel.kt
- com/example/myapplication/ui/auth/AuthViewModel.kt
- com/example/myapplication/ui/camera/CameraViewModel.kt
- com/example/myapplication/ui/chat/ChatViewModel.kt
- com/example/myapplication/ui/expert/ExpertViewModel.kt
- com/example/myapplication/ui/seeker/SeekerViewModel.kt
- com/example/myapplication/ui/voice/VoiceRecordingViewModel.kt

### test/ 底下所有 *Test.kt（核對是否跟主程式路徑對齊）
（無 — 已全數清除）

### androidTest/ 底下所有檔案
（無 — 已全數清除）
