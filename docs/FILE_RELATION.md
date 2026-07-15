# FILE_RELATION.md — 檔案依賴關係

## 專家模式 (Expert)

```
ExpertScreen.kt
  ├── ExpertScreenContent.kt (stateless)
  │   ├── QuickLogCard (輸入 + 發布)
  │   ├── KnowledgeItemCard (技能卡片顯示)
  │   └── SkillEditDialog (編輯彈窗)
  │
  └── ExpertViewModel.kt
        ├── ExpertInputValidator.kt (驗證邏輯)
        ├── ExpertRepository.kt (Firebase 資料)
        │     ├── Constants.kt (路徑/欄位/狀態常數)
        │     └── SolutionItem.kt (資料模型)
        └── AppModule.kt (DI)
```

## 聊天模式 (Chat)

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

## 提問者模式 (Seeker)

```
AskQuestionScreen.kt
  ├── AskQuestionHeader.kt
  ├── AskQuestionInputBar.kt
  ├── AttachmentBottomSheet.kt
  └── SelectedMedia.kt

RoleSelectScreen.kt
  └── MatchingOverlay.kt / MatchingDialog.kt

SeekerViewModel.kt
  ├── QuestionRepository.kt
  ├── MatchingRepository.kt
  ├── MatchCoordinator.kt
  ├── ValidateQuestionQuotaUseCase.kt
  └── SendQuestionMediaUseCase.kt
```

## 驗證 (Auth)

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

## 後端 Cloud Function

```
Firebase Scheduler (every 5 min)
  └── batchProcessPendingSkills (index.js)
        ├── Firebase RTDB (read pending_skills)
        ├── Blacklist check (tags_blacklist)
        ├── Whitelist check (tags_whitelist)
        └── Gemini AI (5 model fallback chain)
              └── Google Search grounding (FALLBACK models only)
```
