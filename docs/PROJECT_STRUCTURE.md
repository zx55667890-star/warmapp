# PROJECT_STRUCTURE.md — 專案目錄結構

**總計：115 個 Kotlin 檔，215 次 Git 提交**

```
warmapp/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/example/myapplication/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── Constants.kt
│       │   │   │   ├── model/
│       │   │   │   │   ├── ChatMessage.kt
│       │   │   │   │   ├── Experience.kt
│       │   │   │   │   └── SolutionItem.kt
│       │   │   │   └── repository/
│       │   │   │       ├── AiRepository.kt
│       │   │   │       ├── AuthRepository.kt
│       │   │   │       ├── DataMigrator.kt
│       │   │   │       ├── ExpertRepository.kt
│       │   │   │       ├── FcmService.kt
│       │   │   │       ├── MatchingRepository.kt
│       │   │   │       ├── MatchingRepositoryInterface.kt
│       │   │   │       ├── MediaUploader.kt
│       │   │   │       ├── MessageRepository.kt
│       │   │   │       ├── MessageRepositoryFactory.kt
│       │   │   │       ├── MessageRepositoryInterface.kt
│       │   │   │       ├── QuestionRepository.kt
│       │   │   │       └── UserRepository.kt
│       │   │   ├── di/
│       │   │   │   ├── AppModule.kt
│       │   │   │   ├── ExpertViewModel.kt
│       │   │   │   └── SeekerViewModel.kt
│       │   │   ├── domain/
│       │   │   │   ├── auth/
│       │   │   │   │   ├── GenerateVerificationCodeUseCase.kt
│       │   │   │   │   ├── LoginUseCase.kt
│       │   │   │   │   ├── LogoutUseCase.kt
│       │   │   │   │   ├── RegisterUseCase.kt
│       │   │   │   │   ├── ResetPasswordUseCase.kt
│       │   │   │   │   ├── SignInWithGoogleUseCase.kt
│       │   │   │   │   └── VerifyVerificationCodeUseCase.kt
│       │   │   │   ├── chat/
│       │   │   │   │   ├── FetchOpponentUseCase.kt
│       │   │   │   │   ├── ObserveMessagesUseCase.kt
│       │   │   │   │   ├── OpponentProfile.kt
│       │   │   │   │   ├── RecallMessageUseCase.kt
│       │   │   │   │   ├── SendMediaUseCase.kt
│       │   │   │   │   └── SendTextMessageUseCase.kt
│       │   │   │   ├── expert/
│       │   │   │   │   └── ExpertInputValidator.kt
│       │   │   │   └── seeker/
│       │   │   │       ├── MatchCoordinator.kt
│       │   │   │       ├── ObserveQuestionStatusUseCase.kt
│       │   │   │       ├── SendQuestionMediaUseCase.kt
│       │   │   │       └── ValidateQuestionQuotaUseCase.kt
│       │   │   ├── ui/
│       │   │   │   ├── auth/
│       │   │   │   │   ├── AuthScreen.kt
│       │   │   │   │   ├── AuthViewModel.kt
│       │   │   │   │   ├── LoginForm.kt
│       │   │   │   │   ├── NicknameSettingsDialog.kt
│       │   │   │   │   ├── ResetPasswordPanel.kt
│       │   │   │   │   └── WelcomePanel.kt
│       │   │   │   ├── camera/
│       │   │   │   │   ├── CameraCaptureScreen.kt
│       │   │   │   │   ├── CameraControlButtons.kt
│       │   │   │   │   ├── CameraPreviewActions.kt
│       │   │   │   │   ├── CameraViewModel.kt
│       │   │   │   │   ├── ImagePreviewScreen.kt
│       │   │   │   │   └── VideoPreviewPlayer.kt
│       │   │   │   ├── chat/
│       │   │   │   │   ├── ChatEvent.kt
│       │   │   │   │   ├── ChatMediaSender.kt
│       │   │   │   │   ├── ChatScreen.kt
│       │   │   │   │   ├── ChatScrollManager.kt
│       │   │   │   │   ├── ChatUiState.kt
│       │   │   │   │   ├── ChatViewModel.kt
│       │   │   │   │   ├── bubble/
│       │   │   │   │   │   ├── BubbleContent.kt
│       │   │   │   │   │   ├── BubbleContextMenu.kt
│       │   │   │   │   │   ├── BubbleStatusMetadata.kt
│       │   │   │   │   │   ├── ChatBubble.kt
│       │   │   │   │   │   ├── ImageGrid.kt
│       │   │   │   │   │   ├── VideoThumbnail.kt
│       │   │   │   │   │   └── VoiceMessageBubble.kt
│       │   │   │   │   ├── components/
│       │   │   │   │   │   ├── ChatBottomArea.kt
│       │   │   │   │   │   ├── ChatInputBar.kt
│       │   │   │   │   │   ├── ChatTopBar.kt
│       │   │   │   │   │   ├── MessageList.kt
│       │   │   │   │   │   ├── ReplyPreviewBar.kt
│       │   │   │   │   │   └── TypingIndicator.kt
│       │   │   │   │   └── dialog/
│       │   │   │   │       ├── ChatDialogHost.kt
│       │   │   │   │       ├── EndChatConfirmDialog.kt
│       │   │   │   │       └── OpponentProfileDialog.kt
│       │   │   │   ├── common/
│       │   │   │   │   ├── AuthUtils.kt
│       │   │   │   │   ├── CompactTextField.kt
│       │   │   │   │   ├── LoadingOverlay.kt
│       │   │   │   │   ├── OfflineBanner.kt
│       │   │   │   │   ├── ToastOverlay.kt
│       │   │   │   │   └── UiText.kt
│       │   │   │   ├── components/
│       │   │   │   │   ├── FullScreenImageDialog.kt
│       │   │   │   │   ├── RatingDialog.kt
│       │   │   │   │   ├── ScrollToBottomButton.kt
│       │   │   │   │   └── VideoPlayerDialog.kt
│       │   │   │   ├── expert/
│       │   │   │   │   ├── ExpertDialogs.kt
│       │   │   │   │   └── ExpertScreen.kt
│       │   │   │   ├── navigation/
│       │   │   │   │   ├── AppNavigation.kt
│       │   │   │   │   └── Route.kt
│       │   │   │   ├── seeker/
│       │   │   │   │   ├── AskQuestionScreen.kt
│       │   │   │   │   ├── MatchingDialog.kt
│       │   │   │   │   ├── MatchingOverlay.kt
│       │   │   │   │   ├── RoleSelectScreen.kt
│       │   │   │   │   ├── SeekerConfirmDialog.kt
│       │   │   │   │   ├── SelectedMedia.kt
│       │   │   │   │   ├── components/
│       │   │   │   │   │   ├── AskQuestionHeader.kt
│       │   │   │   │   │   ├── AskQuestionInputBar.kt
│       │   │   │   │   │   ├── AttachmentBottomSheet.kt
│       │   │   │   │   │   ├── BackgroundGlow.kt
│       │   │   │   │   │   ├── DrawerContent.kt
│       │   │   │   │   │   └── FullSettingsScreen.kt
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   ├── Theme.kt
│       │   │   │   │   └── Type.kt
│       │   │   │   └── voice/
│       │   │   │       ├── VoiceRecordingScreen.kt
│       │   │   │       └── VoiceRecordingViewModel.kt
│       │   │   └── util/
│       │   │       ├── ExpertTitleUtil.kt
│       │   │       ├── ImageUtils.kt
│       │   │       ├── MediaMetadataHelper.kt
│       │   │       ├── NetworkUtils.kt
│       │   │       ├── VideoCacheManager.kt
│       │   │       └── VideoThumbnailCache.kt
│       │   ├── res/
│       │   │   └── values/
│       │   │       ├── strings.xml
│       │   │       └── themes.xml
│       │   ├── AndroidManifest.xml
│       │   └── ...
│       └── test/java/com/example/myapplication/
│           └── di/
│               └── ExpertViewModelTest.kt (3 test files total)
├── functions/
│   ├── index.js
│   ├── package.json
│   └── package-lock.json
├── gradle/
│   ├── libs.versions.toml
│   └── ...
├── database.rules.json
├── AGENTS.md
├── CHAT_FILES_INDEX.md
├── PROGRESS.md
└── docs/
    ├── PROJECT_STRUCTURE.md
    ├── ARCHITECTURE.md
    ├── MODULE_MAP.md
    ├── AI_CONTEXT.md
    ├── DIRECTORY_RULES.md
    ├── CODING_STYLE.md
    ├── DEPENDENCIES.md
    ├── KNOWN_ISSUES.md
    ├── ROADMAP.md
    ├── FILE_RELATION.md
    └── CHANGELOG.md
```
