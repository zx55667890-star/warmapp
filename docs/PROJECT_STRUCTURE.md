# PROJECT_STRUCTURE.md — 專案目錄結構

**總計：125 個 Kotlin 檔，267 次 Git 提交**
warmapp/
├── app/
│   ├── [build.gradle.kts](../app/build.gradle.kts)
│   └── src/
│       ├── main/
│       │   ├── java/com/example/myapplication/
│       │   │   ├── [MainActivity.kt](../app/src/main/java/com/example/myapplication/MainActivity.kt)
│       │   │   ├── data/
│       │   │   │   ├── [Constants.kt](../app/src/main/java/com/example/myapplication/data/Constants.kt)
│       │   │   │   ├── model/
│       │   │   │   │   ├── [ChatMessage.kt](../app/src/main/java/com/example/myapplication/data/model/ChatMessage.kt)
│       │   │   │   │   ├── [Experience.kt](../app/src/main/java/com/example/myapplication/data/model/Experience.kt)
│       │   │   │   │   └── [SolutionItem.kt](../app/src/main/java/com/example/myapplication/data/model/SolutionItem.kt)
│       │   │   │   └── repository/
│       │   │   │       ├── [AiRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/AiRepository.kt)
│       │   │   │       ├── [AuthRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/AuthRepository.kt)
│       │   │   │       ├── [DataMigrator.kt](../app/src/main/java/com/example/myapplication/data/repository/DataMigrator.kt)
│       │   │   │       ├── [ExpertRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/ExpertRepository.kt)
│       │   │   │       ├── [FcmService.kt](../app/src/main/java/com/example/myapplication/data/repository/FcmService.kt)
│       │   │   │       ├── [MatchingRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/MatchingRepository.kt)
│       │   │   │       ├── [MatchingRepositoryInterface.kt](../app/src/main/java/com/example/myapplication/data/repository/MatchingRepositoryInterface.kt)
│       │   │   │       ├── [MediaUploader.kt](../app/src/main/java/com/example/myapplication/data/repository/MediaUploader.kt)
│       │   │   │       ├── [MessageRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/MessageRepository.kt)
│       │   │   │       ├── [MessageRepositoryFactory.kt](../app/src/main/java/com/example/myapplication/data/repository/MessageRepositoryFactory.kt)
│       │   │   │       ├── [MessageRepositoryInterface.kt](../app/src/main/java/com/example/myapplication/data/repository/MessageRepositoryInterface.kt)
│       │   │   │       ├── [QuestionRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/QuestionRepository.kt)
│       │   │   │       └── [UserRepository.kt](../app/src/main/java/com/example/myapplication/data/repository/UserRepository.kt)
│       │   │   ├── di/
│       │   │   │   ├── [AppModule.kt](../app/src/main/java/com/example/myapplication/di/AppModule.kt)
│       │   │   │   ├── [ExpertViewModel.kt](../app/src/main/java/com/example/myapplication/di/ExpertViewModel.kt)
│       │   │   │   └── [SeekerViewModel.kt](../app/src/main/java/com/example/myapplication/di/SeekerViewModel.kt)
│       │   │   ├── domain/
│       │   │   │   ├── auth/
│       │   │   │   │   ├── [GenerateVerificationCodeUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/GenerateVerificationCodeUseCase.kt)
│       │   │   │   │   ├── [LoginUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/LoginUseCase.kt)
│       │   │   │   │   ├── [LogoutUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/LogoutUseCase.kt)
│       │   │   │   │   ├── [RegisterUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/RegisterUseCase.kt)
│       │   │   │   │   ├── [ResetPasswordUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/ResetPasswordUseCase.kt)
│       │   │   │   │   ├── [SignInWithGoogleUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/SignInWithGoogleUseCase.kt)
│       │   │   │   │   └── [VerifyVerificationCodeUseCase.kt](../app/src/main/java/com/example/myapplication/domain/auth/VerifyVerificationCodeUseCase.kt)
│       │   │   │   ├── chat/
│       │   │   │   │   ├── [FetchOpponentUseCase.kt](../app/src/main/java/com/example/myapplication/domain/chat/FetchOpponentUseCase.kt)
│       │   │   │   │   ├── [ObserveMessagesUseCase.kt](../app/src/main/java/com/example/myapplication/domain/chat/ObserveMessagesUseCase.kt)
│       │   │   │   │   ├── [OpponentProfile.kt](../app/src/main/java/com/example/myapplication/domain/chat/OpponentProfile.kt)
│       │   │   │   │   ├── [RecallMessageUseCase.kt](../app/src/main/java/com/example/myapplication/domain/chat/RecallMessageUseCase.kt)
│       │   │   │   │   ├── [SendMediaUseCase.kt](../app/src/main/java/com/example/myapplication/domain/chat/SendMediaUseCase.kt)
│       │   │   │   │   └── [SendTextMessageUseCase.kt](../app/src/main/java/com/example/myapplication/domain/chat/SendTextMessageUseCase.kt)
│       │   │   │   ├── expert/
│       │   │   │   │   └── [ExpertInputValidator.kt](../app/src/main/java/com/example/myapplication/domain/expert/ExpertInputValidator.kt)
│       │   │   │   └── seeker/
│       │   │   │       ├── [MatchCoordinator.kt](../app/src/main/java/com/example/myapplication/domain/seeker/MatchCoordinator.kt)
│       │   │   │       ├── [ObserveQuestionStatusUseCase.kt](../app/src/main/java/com/example/myapplication/domain/seeker/ObserveQuestionStatusUseCase.kt)
│       │   │   │       ├── [SendQuestionMediaUseCase.kt](../app/src/main/java/com/example/myapplication/domain/seeker/SendQuestionMediaUseCase.kt)
│       │   │   │       └── [ValidateQuestionQuotaUseCase.kt](../app/src/main/java/com/example/myapplication/domain/seeker/ValidateQuestionQuotaUseCase.kt)
│       │   │   ├── ui/
│       │   │   │   ├── auth/
│       │   │   │   │   ├── [AuthScreen.kt](../app/src/main/java/com/example/myapplication/ui/auth/AuthScreen.kt)
│       │   │   │   │   ├── [AuthViewModel.kt](../app/src/main/java/com/example/myapplication/ui/auth/AuthViewModel.kt)
│       │   │   │   │   ├── [LoginForm.kt](../app/src/main/java/com/example/myapplication/ui/auth/LoginForm.kt)
│       │   │   │   │   ├── [NicknameSettingsDialog.kt](../app/src/main/java/com/example/myapplication/ui/auth/NicknameSettingsDialog.kt)
│       │   │   │   │   ├── [ResetPasswordPanel.kt](../app/src/main/java/com/example/myapplication/ui/auth/ResetPasswordPanel.kt)
│       │   │   │   │   └── [WelcomePanel.kt](../app/src/main/java/com/example/myapplication/ui/auth/WelcomePanel.kt)
│       │   │   │   ├── camera/
│       │   │   │   │   ├── [CameraCaptureScreen.kt](../app/src/main/java/com/example/myapplication/ui/camera/CameraCaptureScreen.kt)
│       │   │   │   │   ├── [CameraControlButtons.kt](../app/src/main/java/com/example/myapplication/ui/camera/CameraControlButtons.kt)
│       │   │   │   │   ├── [CameraPreviewActions.kt](../app/src/main/java/com/example/myapplication/ui/camera/CameraPreviewActions.kt)
│       │   │   │   │   ├── [CameraViewModel.kt](../app/src/main/java/com/example/myapplication/ui/camera/CameraViewModel.kt)
│       │   │   │   │   ├── [ImagePreviewScreen.kt](../app/src/main/java/com/example/myapplication/ui/camera/ImagePreviewScreen.kt)
│       │   │   │   │   └── [VideoPreviewPlayer.kt](../app/src/main/java/com/example/myapplication/ui/camera/VideoPreviewPlayer.kt)
│       │   │   │   ├── chat/
│       │   │   │   │   ├── [ChatEvent.kt](../app/src/main/java/com/example/myapplication/ui/chat/ChatEvent.kt)
│       │   │   │   │   ├── [ChatMediaSender.kt](../app/src/main/java/com/example/myapplication/ui/chat/ChatMediaSender.kt)
│       │   │   │   │   ├── [ChatScreen.kt](../app/src/main/java/com/example/myapplication/ui/chat/ChatScreen.kt)
│       │   │   │   │   ├── [ChatScrollManager.kt](../app/src/main/java/com/example/myapplication/ui/chat/ChatScrollManager.kt)
│       │   │   │   │   ├── [ChatUiState.kt](../app/src/main/java/com/example/myapplication/ui/chat/ChatUiState.kt)
│       │   │   │   │   ├── [ChatViewModel.kt](../app/src/main/java/com/example/myapplication/ui/chat/ChatViewModel.kt)
│       │   │   │   │   ├── bubble/
│       │   │   │   │   │   ├── [BubbleContent.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleContent.kt)
│       │   │   │   │   │   ├── [BubbleContextMenu.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleContextMenu.kt)
│       │   │   │   │   │   ├── [BubbleStatusMetadata.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/BubbleStatusMetadata.kt)
│       │   │   │   │   │   ├── [ChatBubble.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/ChatBubble.kt)
│       │   │   │   │   │   ├── [ImageGrid.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/ImageGrid.kt)
│       │   │   │   │   │   ├── [VideoThumbnail.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/VideoThumbnail.kt)
│       │   │   │   │   │   └── [VoiceMessageBubble.kt](../app/src/main/java/com/example/myapplication/ui/chat/bubble/VoiceMessageBubble.kt)
│       │   │   │   │   ├── components/
│       │   │   │   │   │   ├── [ChatBottomArea.kt](../app/src/main/java/com/example/myapplication/ui/chat/components/ChatBottomArea.kt)
│       │   │   │   │   │   ├── [ChatInputBar.kt](../app/src/main/java/com/example/myapplication/ui/chat/components/ChatInputBar.kt)
│       │   │   │   │   │   ├── [ChatTopBar.kt](../app/src/main/java/com/example/myapplication/ui/chat/components/ChatTopBar.kt)
│       │   │   │   │   │   ├── [MessageList.kt](../app/src/main/java/com/example/myapplication/ui/chat/components/MessageList.kt)
│       │   │   │   │   │   ├── [ReplyPreviewBar.kt](../app/src/main/java/com/example/myapplication/ui/chat/components/ReplyPreviewBar.kt)
│       │   │   │   │   │   └── [TypingIndicator.kt](../app/src/main/java/com/example/myapplication/ui/chat/components/TypingIndicator.kt)
│       │   │   │   │   └── dialog/
│       │   │   │   │       ├── [ChatDialogHost.kt](../app/src/main/java/com/example/myapplication/ui/chat/dialog/ChatDialogHost.kt)
│       │   │   │   │       ├── [EndChatConfirmDialog.kt](../app/src/main/java/com/example/myapplication/ui/chat/dialog/EndChatConfirmDialog.kt)
│       │   │   │   │       └── [OpponentProfileDialog.kt](../app/src/main/java/com/example/myapplication/ui/chat/dialog/OpponentProfileDialog.kt)
│       │   │   │   ├── common/
│       │   │   │   │   ├── [AuthUtils.kt](../app/src/main/java/com/example/myapplication/ui/common/AuthUtils.kt)
│       │   │   │   │   ├── [CompactTextField.kt](../app/src/main/java/com/example/myapplication/ui/common/CompactTextField.kt)
│       │   │   │   │   ├── [LoadingOverlay.kt](../app/src/main/java/com/example/myapplication/ui/common/LoadingOverlay.kt)
│       │   │   │   │   ├── [OfflineBanner.kt](../app/src/main/java/com/example/myapplication/ui/common/OfflineBanner.kt)
│       │   │   │   │   ├── [ToastOverlay.kt](../app/src/main/java/com/example/myapplication/ui/common/ToastOverlay.kt)
│       │   │   │   │   └── [UiText.kt](../app/src/main/java/com/example/myapplication/ui/common/UiText.kt)
│       │   │   │   ├── components/
│       │   │   │   │   ├── [FullScreenImageDialog.kt](../app/src/main/java/com/example/myapplication/ui/components/FullScreenImageDialog.kt)
│       │   │   │   │   ├── [RatingDialog.kt](../app/src/main/java/com/example/myapplication/ui/components/RatingDialog.kt)
│       │   │   │   │   ├── [ScrollToBottomButton.kt](../app/src/main/java/com/example/myapplication/ui/components/ScrollToBottomButton.kt)
│       │   │   │   │   └── [VideoPlayerDialog.kt](../app/src/main/java/com/example/myapplication/ui/components/VideoPlayerDialog.kt)
│       │   │   │   ├── expert/
│       │   │   │   │   ├── [ExpertDialogs.kt](../app/src/main/java/com/example/myapplication/ui/expert/ExpertDialogs.kt)
│       │   │   │   │   ├── [ExpertScreen.kt](../app/src/main/java/com/example/myapplication/ui/expert/ExpertScreen.kt)
│       │   │   │   │   └── components/
│       │   │   │   │       ├── [KnowledgeItemCard.kt](../app/src/main/java/com/example/myapplication/ui/expert/components/KnowledgeItemCard.kt)
│       │   │   │   │       ├── [QuickLogCard.kt](../app/src/main/java/com/example/myapplication/ui/expert/components/QuickLogCard.kt)
│       │   │   │   │       └── [SkillEditDialog.kt](../app/src/main/java/com/example/myapplication/ui/expert/components/SkillEditDialog.kt)
│       │   │   │   ├── navigation/
│       │   │   │   │   ├── [AppNavigation.kt](../app/src/main/java/com/example/myapplication/ui/navigation/AppNavigation.kt)
│       │   │   │   │   └── [Route.kt](../app/src/main/java/com/example/myapplication/ui/navigation/Route.kt)
│       │   │   │   ├── seeker/
│       │   │   │   │   ├── [AskQuestionScreen.kt](../app/src/main/java/com/example/myapplication/ui/seeker/AskQuestionScreen.kt)
│       │   │   │   │   ├── [MatchingDialog.kt](../app/src/main/java/com/example/myapplication/ui/seeker/MatchingDialog.kt)
│       │   │   │   │   ├── [MatchingOverlay.kt](../app/src/main/java/com/example/myapplication/ui/seeker/MatchingOverlay.kt)
│       │   │   │   │   ├── [RoleSelectScreen.kt](../app/src/main/java/com/example/myapplication/ui/seeker/RoleSelectScreen.kt)
│       │   │   │   │   ├── [SeekerConfirmDialog.kt](../app/src/main/java/com/example/myapplication/ui/seeker/SeekerConfirmDialog.kt)
│       │   │   │   │   ├── [SelectedMedia.kt](../app/src/main/java/com/example/myapplication/ui/seeker/SelectedMedia.kt)
│       │   │   │   │   ├── components/
│       │   │   │   │   │   ├── [AskQuestionHeader.kt](../app/src/main/java/com/example/myapplication/ui/seeker/components/AskQuestionHeader.kt)
│       │   │   │   │   │   ├── [AskQuestionInputBar.kt](../app/src/main/java/com/example/myapplication/ui/seeker/components/AskQuestionInputBar.kt)
│       │   │   │   │   │   ├── [AttachmentBottomSheet.kt](../app/src/main/java/com/example/myapplication/ui/seeker/components/AttachmentBottomSheet.kt)
│       │   │   │   │   │   ├── [BackgroundGlow.kt](../app/src/main/java/com/example/myapplication/ui/seeker/components/BackgroundGlow.kt)
│       │   │   │   │   │   ├── [DrawerContent.kt](../app/src/main/java/com/example/myapplication/ui/seeker/components/DrawerContent.kt)
│       │   │   │   │   │   └── [FullSettingsScreen.kt](../app/src/main/java/com/example/myapplication/ui/seeker/components/FullSettingsScreen.kt)
│   │   │   │   ├── theme/
│   │   │   │   │   ├── [AppColors.kt](../app/src/main/java/com/example/myapplication/ui/theme/AppColors.kt)
│   │   │   │   │   ├── [Theme.kt](../app/src/main/java/com/example/myapplication/ui/theme/Theme.kt)
│   │   │   │   │   └── [Type.kt](../app/src/main/java/com/example/myapplication/ui/theme/Type.kt)
│   │   │   │   └── voice/
│   │   │   │       ├── [VoiceRecordingScreen.kt](../app/src/main/java/com/example/myapplication/ui/voice/VoiceRecordingScreen.kt)
│   │   │   │       └── [VoiceRecordingViewModel.kt](../app/src/main/java/com/example/myapplication/ui/voice/VoiceRecordingViewModel.kt)
│       │   │   └── util/
│       │   │       ├── [ExpertTitleUtil.kt](../app/src/main/java/com/example/myapplication/util/ExpertTitleUtil.kt)
│       │   │       ├── [ImageUtils.kt](../app/src/main/java/com/example/myapplication/util/ImageUtils.kt)
│       │   │       ├── [MediaMetadataHelper.kt](../app/src/main/java/com/example/myapplication/util/MediaMetadataHelper.kt)
│       │   │       ├── [NetworkUtils.kt](../app/src/main/java/com/example/myapplication/util/NetworkUtils.kt)
│       │   │       ├── [VideoCacheManager.kt](../app/src/main/java/com/example/myapplication/util/VideoCacheManager.kt)
│       │   │       └── [VideoThumbnailCache.kt](../app/src/main/java/com/example/myapplication/util/VideoThumbnailCache.kt)
│       │   ├── res/
│       │   │   └── values/
│       │   │       ├── [strings.xml](../app/src/main/res/values/strings.xml)
│       │   │       └── [themes.xml](../app/src/main/res/values/themes.xml)
│       │   ├── [AndroidManifest.xml](../app/src/main/AndroidManifest.xml)
│       │   └── ...
│       └── test/java/com/example/myapplication/
│           └── di/
│               └── [ExpertViewModelTest.kt](../app/src/test/java/com/example/myapplication/di/ExpertViewModelTest.kt) (3 test files total)
├── functions/
│   ├── [index.js](../functions/index.js)
│   ├── [package.json](../functions/package.json)
│   └── [package-lock.json](../functions/package-lock.json)
├── gradle/
│   ├── [libs.versions.toml](../gradle/libs.versions.toml)
│   └── ...
├── [database.rules.json](../database.rules.json)
├── [AGENTS.md](../AGENTS.md)
├── [CHAT_FILES_INDEX.md](../CHAT_FILES_INDEX.md)
├── [PROGRESS.md](../PROGRESS.md)
└── docs/
    ├── [PROJECT_STRUCTURE.md](../docs/PROJECT_STRUCTURE.md)
    ├── [ARCHITECTURE.md](../docs/ARCHITECTURE.md)
    ├── [MODULE_MAP.md](../docs/MODULE_MAP.md)
    ├── [AI_CONTEXT.md](../docs/AI_CONTEXT.md)
    ├── [DIRECTORY_RULES.md](../docs/DIRECTORY_RULES.md)
    ├── [CODING_STYLE.md](../docs/CODING_STYLE.md)
    ├── [DEPENDENCIES.md](../docs/DEPENDENCIES.md)
    ├── [KNOWN_ISSUES.md](../docs/KNOWN_ISSUES.md)
    ├── [ROADMAP.md](../docs/ROADMAP.md)
    ├── [FILE_RELATION.md](../docs/FILE_RELATION.md)
    └── [CHANGELOG.md](../docs/CHANGELOG.md)
