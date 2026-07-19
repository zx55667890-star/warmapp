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
│              Cloud Function (Node.js 22)                  │
│  batchProcessPendingSkills (scheduler, every 1 min)      │
│    └─ Blacklist check → Whitelist check → Gemini AI      │
│    └─ 5 model fallback chain (Serper/Google Search)      │
│    └─ Submission Lock management                          │
│                                                          │
│  batchProcessPendingQuestions (scheduler, every 1 min)    │
│    └─ Blacklist check → Whitelist check → Gemini AI      │
│    └─ 5 model fallback chain (same as skills)            │
│    └─ Tag-based matching → expert assignment              │
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
              ├─ 3. AI 分析 (4 模型接力)
              │     PRIMARY: gemini-3.1-flash-lite (無搜尋)
              │       → REJECT 才丟給下一棒
              │     FALLBACK_1: Serper 外部搜尋 (`useWebFetch`，避開 Gen3 429)
              │     FALLBACK_2~3: 內建 Google Search 能力
              │       → 最終 REJECT 才寫入黑名單
                      │
                      └─ 結果寫回：
                            solutions/{uid}/{id}/status = ACTIVE/REJECTED
                            solutions/{uid}/{id}/tags = [...]
                            pending_skills/{id} = null (刪除)
                            tags_whitelist or tags_blacklist (快取)
                            users/{uid}/submissionLock (連續拒絕計數)
```

## 資料流 — 提問標籤生成與配對

```
AskQuestionInputBar (AskQuestionScreen.kt)
    │ onSendClick
    ▼
SeekerViewModel.sendQuestion(text, userId, media)
    │
    ├─ [Guard] ValidateQuestionQuotaUseCase (hasActive + 每日上限)
    │
    └─ QuestionRepository.sendQuestion()
          │
          ├─ Firebase: questions/{pushId}
          │     { text, status:"matching", timestamp, authorId, expertId:"" }
          │
          └─ Firebase: pending_questions/{pushId}  ← 🆕
                { userId, text, timestamp }
                │
                ▼  (排程，最長等 1 分鐘)
          batchProcessPendingQuestions (Cloud Function)
                │
                ├─ 1. Blacklist 檢查 (tags_blacklist/{base64(text)})
                │     └─ 命中 → cancelled
                │
                ├─ 2. Whitelist 檢查 (tags_whitelist/{base64(text)}/tags)
                │     └─ 命中 → 快取標籤 + Tag 配對
                │
                ├─ 3. AI 5 模型降級分析 → 題目標籤
                │     PRIMARY: gemini-3.1-flash-lite (無搜尋)
                │     FALLBACK_1~5: Serper / Google Search
                │
                └─ 4. Tag 相似度配對 (matchQuestionByTags)
                      ├─ 讀取所有 active experiences (`experiences` 路徑)
                      ├─ 讀取各專家的 ACTIVE solutions 合併標籤集
                      ├─ Jaccard 相似度 (門檻 0.15)
                      └─ 最佳匹配 → questions/{id}:
                            expertId, status:"pending_acceptance"
                            matchedExpText, matchedExpTimestamp

> ⚠️ **注意**：`matchQuestionByTags()` 讀取 `/experiences`，但專家端上線寫入 `/active_experiences`
> （`FirebasePaths.ACTIVE_EXPERIENCES`）。兩路徑不一致需釐清。
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
```

> UI 元件階層已移至 `docs/MODULE_MAP.md`（各功能區塊的依賴樹）
