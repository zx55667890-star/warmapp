# FILE_RELATION.md — 檔案依賴關係

## 專家模式 (Expert)

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

SeekerViewModel.kt (ui/seeker/)
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
        ├── [Self-Heal] healOrphanedPending()
        │     掃描 solutions PENDING > 10min 且無對應 pending_skills entry
        ├── Firebase RTDB (read pending_skills)
        ├── Blacklist check (tags_blacklist)
        ├── Whitelist check (tags_whitelist)
        └── Gemini AI (4 model fallback chain)
              ├── Serper external search (FALLBACK_1, `useWebFetch`)
              └── Google Search grounding (FALLBACK_2~3 only)
```
