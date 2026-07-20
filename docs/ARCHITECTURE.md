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
│  DB triggered (onValueWritten) — scheduler removed         │
│    ├─ processSkillsOnWrite:                                │
│    │   └─ pending_skills/{id} write → trigger              │
│    │   └─ Blacklist → Whitelist → Gemini AI + 6 fallback   │
│    │   └─ Submission Lock management                        │
│    │                                                       │
│    └─ processQuestionsOnWrite:                             │
│        └─ pending_questions/{id} write → trigger           │
│        └─ Blacklist → Whitelist → Gemini AI + 6 fallback   │
│        └─ Hybrid matching: tagJ ×0.3 + embed ×0.7          │
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
                      ▼  (DB write 立即觸發)
                processSkillsOnWrite (Cloud Function)
                      │
                      ├─ 1. Blacklist 檢查 (tags_blacklist/{base64(text)})
                      │     └─ 命中 → REJECTED
                      │
                      ├─ 2. Whitelist 檢查 (tags_whitelist/{base64(text)}/tags)
                      │     └─ 命中 → ACTIVE + 快取標籤
                      │
              ├─ 3. AI 分析 (6 模型接力)
              │     PRIMARY: gemini-3.1-flash-lite (無搜尋)
              │       → REJECT 才丟給下一棒
              │     FALLBACK_1: gemini-3.1-flash-lite + Serper (`useWebFetch`)
              │     FALLBACK_2: gemini-2.5-flash-lite + googleSearch
              │     FALLBACK_3: gemini-2.5-flash + googleSearch
              │     FALLBACK_4: gemini-3.5-flash + Serper + minimal thinking
              │     FALLBACK_5: gemini-3-flash-preview + Serper + minimal thinking
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
                ▼  (DB write 立即觸發)
          processQuestionsOnWrite (Cloud Function)
                │
                ├─ 1. Blacklist 檢查 (tags_blacklist/{base64(text)})
                │     └─ 命中 → cancelled
                │
                ├─ 2. Whitelist 檢查 (tags_whitelist/{base64(text)}/tags)
                │     └─ 命中 → 快取標籤 + Tag 配對
                │
                ├─ 3. AI 6 模型降級分析 → 題目標籤
                │     PRIMARY: gemini-3.1-flash-lite (無搜尋)
                │     FALLBACK_1~5: Serper / Google Search
                │
                └─ 4. Hybrid 匹配 (matchQuestionByTags)
                      ├─ 讀取所有 active_experiences
                      ├─ 讀取各專家的 ACTIVE solutions 合併標籤集
                      ├─ 每個 candidate 計算 3 個分數：
                      │   ├─ tagJaccard: 標籤字串交集
                      │   ├─ textJaccard: 文字 bigram 交集
                      │   └─ embedSim: 全文 embedding 餘弦相似度
                      ├─ 混合分數: tagJ ×0.3 + embed ×0.7 (threshold 0.4)
                      ├─ tagJ=0 降級純 embed (threshold 0.7, 同義詞情境)
                      └─ 最佳匹配 → questions/{id}:
                            expertId, status:"pending_acceptance"
                            matchedExpText, matchedExpTimestamp
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
