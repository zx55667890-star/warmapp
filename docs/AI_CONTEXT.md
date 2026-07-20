# AI_CONTEXT.md — AI 開發者必讀

## 專案概要
warmapp：知識技能記錄平台 Android App。使用者可以作為 **專家 (Expert)** 發布技能並獲得 AI 標籤，也可以作為 **提問者 (Seeker)** 提出問題並由專家回答。使用 Firebase Realtime Database 作為主要資料庫。

## 目前 Feature 狀態
- ✅ **聊天**：文字/圖片/語音/影片，已讀收回（基礎）
- ✅ **登入**：電話驗證 + Google Sign In（基礎整合）
- ✅ **專家技能發布**：後端 AI 自動標籤提取（6 模型 fallback）
- ✅ **提問者**：發問 + 配對專家
- ✅ **相機**：拍照/錄影
- ✅ **錄音**：語音訊息
- 🚧 **通知**：FCM 基礎架構已建立，待完整整合
- 🚧 **搜尋**：尚未實作

## ⚠️ 不能改的項目
這些項目已經過驗證且穩定，**嚴禁重構或修改**：

### ExpertRepository (callbackFlow)
```
listenToSolutionHistory()  → callbackFlow
observeExpertStatus()      → callbackFlow
```
- 已從 `suspendCancellableCoroutine` 遷移到 `callbackFlow`
- `awaitClose` 自動清除 listener
- 空白 userId guard 已驗證完整
- `saveSkill()` 使用個別 `setValue()`（非 `updateChildren()`）— 這是為了解決 `setPersistenceEnabled(true)` 衝突的刻意決定

### Constants.kt
- Firebase 路徑、欄位名稱、狀態值全部集中於此
- 專案中所有 Firebase 相關字串都依賴此檔
- 不能拆分、不能改名已有常數

### Cloud Function
- **DB triggered**（`onValueWritten`），非排程 — `processSkillsOnWrite` + `processQuestionsOnWrite` 各自監聽 `pending_skills/{id}` / `pending_questions/{id}`
- 排程 `batchProcess`（`every 1 minute`）已於 Round 17 刪除
- 2nd Gen (`firebase-functions/v2/database`)
- Secret via `defineSecret('GEMINI_API_KEY')` + `defineSecret('SERPER_API_KEY')`
- Model 陣列（6 個）：PRIMARY（無搜尋）+ FALLBACK_1（Serper）+ FALLBACK_2~3（googleSearch）+ FALLBACK_4~5（Serper + thinking）
- `useWebFetch` 旗標：先 `searchOnSerper()` 取搜尋結果注入 prompt，再送模型（無 `tools: [googleSearch]`）
- `thinkingConfig`：Gen3 模型支援 `{ thinkingLevel: 'minimal' | 'low' | 'medium' | 'high' }`（FALLBACK_4~5 使用 `minimal`）
- `searchOnSerper()` 調用 `https://google.serper.dev/search`，前 3 筆 organic，5s timeout
- **匹配演算法**：hybrid（tag Jaccard ×0.3 + embedding cosine ×0.7），threshold 0.25；純 tag 路徑 threshold 0.15（MATCH_TAG_THRESHOLD）；tagJ=0 時降級純 embedding，threshold 0.7（同義詞仍可配對）
- **客戶端配對**：`MatchingRepository` bigram Jaccard，threshold 0.08；需由 `matchCoordinator.matchAndAssignExpert()` 手動呼叫

### SkillStatus 列舉
- `SolutionItem.status` 是 `SkillStatus` 型別（ACTIVE / PENDING / REJECTED）
- 不能改成 `String`
- 比對用 `== SkillStatus.XXX` 而非 `.name`

### UI 字串
- 所有字串在 `strings.xml`
- 不能用硬編碼 `"中文"` 在 Kotlin 中

### Modifier 順序
- required → `modifier` → optional

## 🗑️ 已刪除的項目
- `ExtractLocalTagsUseCase.kt`（舊客戶端 AI 標籤提取，dead code）
- `TagViewModel.kt`（已合併至 ExpertViewModel）
- `ExpertScreen.kt` 中的舊 `publishError` 參數（改為 floating overlay）

## 🧠 語意快取（Semantic Whitelist Cache）

### 流程
```
新問題進來
  ↓
1. 精確比對 whitelist key（現有）
  → 命中 → 直接用快取 tags（source: "llm"）✅
  ↓ miss
2. 計算 embedding，與 whitelist 所有條目比對 cosine 相似度
  → ≥ 0.75（SEMANTIC_CACHE_THRESHOLD）→ 回傳 tags，不寫入新 entry ✅
  ↓ miss
3. 呼叫 LLM 生成 tags
  → 寫入 whitelist（tags + embedding + source: "llm"）
```

### 注意
- `findSemanticCachedTags()` 共用於 questions 與 skills 兩條 pipeline
- 語意快取命中後仍會呼叫 `matchQuestionByTags()`（已修復 early return bug）
- Whitelist 只保留 LLM 原始計算的 entry，語意快取命中不增生新資料
- `source` 欄位可區分 `"llm"`（花 token）與 `"semantic_cache"`（沒花 token）

## ⚠️ 未解決問題

### Q1 文字缺字問題
- `tags_whitelist` 中 Q1「月底了...」的 key 偶爾遺失首字元「月」
- 疑似 Android App 寫入 `pending_questions` 時文字被截斷
- 待確認：是否為 UI 欄位限制或 API 傳送問題

### Embedding 模型名稱
- 目前 CF 使用 `gemini-embedding-exp-03-07`，但公開 API 只列出 `gemini-embedding-2`
- 若部署環境的模型不可用，需改為 `gemini-embedding-2`

## ⚠️ 這些地方很危險

### pending_skills / pending_questions 孤立（DB triggered）
- Cloud Function 由 DB write 觸發，crash 在 claim 之後、寫回之前，entry 會被標記 `processing`
- 5 分鐘後自動 timeout 釋放（`PROCESSING_TIMEOUT_MS`）
- 必要時可以手動清除：`DELETE /pending_skills/{id}` 或 `DELETE /pending_questions/{id}`
- 高頻連續觸發可能導致 entry 被多個並發 invocation 搶佔而卡住

### saveSkill 非原子性
- 兩個獨立 `setValue()`：先寫 `solutions/` 再寫 `pending_skills/`
- 若第二步失敗 = 孤立 solution（永遠 PENDING）
- 這是已知風險，目前可接受

### 跨檔案命名同步
- 修改 ViewModel 的 property/method 名稱 → 必須同步改 Screen 中的引用
- CLI 建構成功不代表 IDE 無紅字
- 例如第 9 輪 `publishErrorRes` → `publishFeedbackRes` 未同步導致 cascading error

### AI Response Job 殘留（取消後 chatroom 被重建）
- `SeekerViewModel.sendQuestion()` 會啟動一個 `aiResponseJob`（第 132 行）在背景產生 AI 回答並寫入 `chatrooms/ai_$id`
- 若使用者在 AI 回答產生完成之前取消配對，cancelMatching 刪除 chatroom 後，`aiResponseJob` 仍可能繼續執行並**重新建立 chatroom**
- 修復方式：`aiResponseJob` 存為 Job 欄位，在 `cleanupListeners()` 中 cancel
- 若未來新增類似背景寫入 job，必須加入 cleanup 機制

## 🔧 開發指令
- Build：`.\gradlew.bat assembleDebug --daemon --parallel`
- 清理 Build：`.\gradlew.bat clean`
- Deploy Cloud Function：`firebase deploy --only functions --force`
- 查看 Log：`firebase functions:log --only processSkillsOnWrite` 或 `firebase functions:log --only processQuestionsOnWrite`
- RTDB 操作：使用 Firebase MCP 工具
- Git：只 stage 預期檔案，不提交 `node_modules/`

### 取得 debug SHA-1 fingerprint
在模擬器執行以下指令，輸出中找 `SHA1:`，加到 Firebase Console → Project Settings → Android app → SHA certificate fingerprints：

Android Studio 內建 JDK：
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool" -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

系統 Java：
```powershell
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
