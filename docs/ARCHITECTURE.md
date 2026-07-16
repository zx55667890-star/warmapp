# ARCHITECTURE.md — 系統架構

## 分層架構 (Clean Architecture)

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                     │
│  Screen (Stateless)  ←  ViewModel (StateHolder)         │
│  Components / Widgets / Dialogs                          │
├─────────────────────────────────────────────────────────┤
│                 Domain Layer (Use Cases)                  │
│  ExpertInputValidator / PublishSkillUseCase              │
│  ObserveSolutionsUseCase                                 │
│  LoginUseCase / RegisterUseCase / ...                    │
│  SendTextMessageUseCase / ObserveMessagesUseCase / ...   │
├─────────────────────────────────────────────────────────┤
│                Data Layer (Repository)                    │
│  ExpertRepository  ←  callbackFlow → Firebase RTDB       │
│  AuthRepository    ←  Firebase Auth                      │
│  MessageRepository ←  Firebase RTDB                      │
│  UserRepository    ←  Firebase RTDB / Auth               │
│  MediaUploader     ←  Firebase Storage                   │
│  AiRepository      ←  Firebase Functions                 │
├─────────────────────────────────────────────────────────┤
│                    Firebase (Backend)                     │
│  Realtime Database  │  Auth  │  Storage  │  Functions    │
│  Cloud Messaging    │  Hosting                           │
├─────────────────────────────────────────────────────────┤
│              Cloud Function (Node.js 24)                  │
│  batchProcessPendingSkills (scheduler, every 5 min)      │
│    └─ Blacklist check → Whitelist check → Gemini AI      │
│    └─ 5 model fallback chain with Google Search          │
│    └─ Submission Lock management                         │
└─────────────────────────────────────────────────────────┘
```

## 資料流 — 技能發布 (publishSkill)

```
QuickLogCard (ExpertScreen.kt)
    │ onPublish(text)
    ▼
ExpertViewModel.publishSkill(userId, text)
    │
    ├─ [Guard] userId.isBlank() → ShowToast("請先登入")
    ├─ [Guard] isSubmissionLocked → ShowToast("24小時後再試")
    ├─ [Guard] duplicate check → ShowToast("已存在")
    ├─ [Validate] ExpertInputValidator.validate(text)
    │     └─ 失敗 → publishFeedbackRes = error string
    │
    └─ [Success] PublishSkillUseCase(userId, trimmed)
          └─ ExpertRepository.saveSkill(userId, text)
                │
                ├─ Firebase: solutions/{userId}/{pushId}
                │     { expertise, tags:[], status:"PENDING", timestamp }
                │
                └─ Firebase: pending_skills/{pushId}
                      { userId, text, timestamp }
                      │
                      ▼  (排程，最長等 5 分鐘)
                batchProcessPendingSkills (Cloud Function)
                      │
                      ├─ [Self-Heal] healOrphanedPending()
                      │    掃描 solutions 中 PENDING > 10 分鐘且
                      │    pending_skills 無對應 entry 者，補回佇列
                      │
                      ├─ 1. Blacklist 檢查 (tags_blacklist/{base64(text)})
                      │     └─ 命中 → REJECTED
                      │
                      ├─ 2. Whitelist 檢查 (tags_whitelist/{base64(text)}/tags)
                      │     └─ 命中 → ACTIVE + 快取標籤
                      │
                      ├─ 3. AI 分析 (5 模型接力)
                      │     PRIMARY: gemini-3.1-flash-lite (無搜尋)
                      │       → REJECT 才丟給下一棒
                      │     FALLBACK_1~4: 有 Google Search 能力
                      │       → 最終 REJECT 才寫入黑名單
                      │
                      └─ 結果寫回：
                            solutions/{uid}/{id}/status = ACTIVE/REJECTED
                            solutions/{uid}/{id}/tags = [...]
                            pending_skills/{id} = null (刪除)
                            tags_whitelist or tags_blacklist (快取)
                            users/{uid}/submissionLock (連續拒絕計數)
```

## 資料流 — 聊天

```
ChatScreen (ui/chat/)
    │
    ▼
ChatViewModel
    │
    ├─ observeMessages() ← MessageRepository (callbackFlow)
    ├─ sendTextMessage() ← MessageRepository
    ├─ sendMedia()       ← MediaUploader (Storage) + MessageRepository
    └─ recallMessage()   ← MessageRepository
```

## UI 元件階層 (Compose)

```
MainActivity
  └─ AppNavigation (NavHost)
       ├─ RoleSelectScreen
       ├─ AuthScreen / LoginForm / RegisterForm
       ├─ Seeker (提問者)
       │   ├─ AskQuestionScreen
       │   │   ├─ AskQuestionHeader
       │   │   ├─ AskQuestionInputBar
       │   │   └─ AttachmentBottomSheet
       │   ├─ MatchingOverlay / MatchingDialog
       │   └─ FullSettingsScreen (DrawerContent)
       │
       ├─ Expert (專家)
       │   └─ ExpertScreen
       │       ├─ ExpertScreenContent (stateless)
       │       │   ├─ QuickLogCard (輸入 + 發布)
       │       │   ├─ KnowledgeItemCard (技能卡片)
       │       │   ├─ SkillEditDialog (編輯彈窗)
       │       │   └─ Floating feedback overlay
       │       └─ ExpertDialogs
       │
       ├─ ChatScreen
       │   ├─ ChatTopBar
       │   ├─ MessageList
       │   │   └─ ChatBubble
       │   │       ├─ BubbleContent
       │   │       ├─ BubbleContextMenu
       │   │       └─ BubbleStatusMetadata
       │   ├─ ChatBottomArea
       │   │   ├─ ChatInputBar
       │   │   └─ ReplyPreviewBar
       │   └─ ChatDialogHost
       │       ├─ EndChatConfirmDialog
       │       └─ OpponentProfileDialog
       │
       ├─ CameraCaptureScreen
       │   ├─ CameraControlButtons
       │   ├─ CameraPreviewActions
       │   └─ ImagePreviewScreen / VideoPreviewPlayer
       │
       └─ VoiceRecordingScreen
```
