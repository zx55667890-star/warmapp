# CHAT_FILES_INDEX.md — 本次對話修改/參考的檔案索引

## 第 3 輪：AI 標籤提取遷移至 Backend Cloud Function

### 新增檔案
- `functions/index.js` — Cloud Function `batchProcessPendingSkills`（每 5 分鐘排程批次處理）
- `functions/package.json` — Node 20, @google/generative-ai

### 修改檔案
- `data/model/SolutionItem.kt` — 加入 SkillStatus 列舉 + status 欄位
- `data/repository/ExpertRepository.kt` — 加入黑/白名單查詢、saveSkill（寫入 pending_skills 佇列）、status 讀取
- `di/ExpertViewModel.kt` — 移除 SharedPreferences 依賴、fetchTagsFromAi()、submitSolution()；新增 publishSkill() 流程
- `di/AppModule.kt` — Koin 註冊改為 `viewModel { ExpertViewModel(get()) }`
- `ui/expert/ExpertScreen.kt` — QuickLogCard 簡化為輸入+發布；KnowledgeItemCard 顯示 PENDING spinner / REJECTED 紅字
- `domain/expert/ExpertInputValidator.kt` — 強化解除重複檢測邏輯
- `database.rules.json` — 加入 pending_skills、tags_blacklist、tags_whitelist 路徑規則與 index

### 廢棄（不再使用）
- `domain/expert/ExtractLocalTagsUseCase.kt` — dead code（舊客戶端 AI 標籤提取）
- `di/TagViewModel.kt` — 已移除（職責合併至 ExpertViewModel）

---

## 第 4 輪：Cloud Function 優化（5 模型 fallback + 2nd Gen + SDK 遷移）

### 修改檔案
- `functions/index.js` — 全面改寫：
  - 1st Gen → 2nd Gen (`firebase-functions/v2/scheduler`)
  - `functions.config()` → `defineSecret('GEMINI_API_KEY')`
  - `@google/generative-ai` (deprecated) → `@google/genai`
  - 5 模型 fallback 鏈（model-specific thinkingConfig）
  - 503 retry（2s / 4s backoff）
  - `minInstances: 1`
  - `config/model_status` 追蹤與重置
- `functions/package.json` — `@google/generative-ai:^0.21.0` → `@google/genai:^2.10.0`
- `database.rules.json` — 加入 `config/` 路徑規則
- `docs/AGENTS.md` — 更新技術棧、架構說明、開發指令

### 效能測試結果
| 模型 | 速度 | 備註 |
|------|------|------|
| `gemini-3.1-flash-lite` (PRIMARY) | **~0.7s** ✅ | 最快最穩 |
| `gemini-2.5-flash` | ~2-5s | 穩定 |
| `gemini-2.5-flash-lite` | ~4s | 偶發503 |
| `gemini-3.5-flash` | 失敗 | 頻繁503 |
| `gemini-3-flash-preview` | ~17s | 關thinking仍慢 |

---

## 第 5 輪：Cloud Function 依賴升級（Node 24 + firebase-admin v14 + firebase-functions v7 RC）

### 修改檔案
- `functions/package.json`:
  - engines.node: `"20"` → `"24"`
  - `firebase-admin: ^12.0.0` → `^14.0.0`
  - `firebase-functions: ^5.0.0` → `7.3.0-rc.0`（RC 版本，因 v7.2.x stable 不支援 admin v14）
- `functions/index.js` — `admin.database()` → `getDatabase()`（firebase-admin v14 移除 legacy namespace）
- `docs/AGENTS.md` — 更新技術棧版本、新增 firebase-admin v14 注意事項
- `CHAT_FILES_INDEX.md` — 本次記錄
- `docs/PROGRESS.md` — 本次記錄

### 關鍵發現
| 項目 | 說明 |
|------|------|
| firebase-admin v14 強制 Node.js 22+ | 必須同步升級 engines.node |
| `admin.database()` 移除 | 改用 `const { getDatabase } = require('firebase-admin/database')` |
| firebase-functions v7.2.x 不支援 admin v14 | 需使用 `7.3.0-rc.0`（peer 含 `^14.0.0`） |
| Cloud Functions 最高 runtime | Node.js 24（2026/7 為止尚無 Node.js 26） |
| Firebase CLI 版本 | 15.23.0（與 runtime 無關） |

### 部署結果
- `batchProcessPendingSkills` 成功更新至 Node.js 24（revision 00015）
- Runtime ID: `nodejs24`
- 新 instance 啟動正常，HTTP 503 等錯誤觀察無異常

---

---

## 第 6 輪：全面修復未解決問題（編輯流程、效能優化、排程縮短、dead code 清理、測試強化）

### 修改檔案
- `domain/expert/ExtractLocalTagsUseCase.kt` — **已刪除**（dead code，無任何依賴）
- `data/repository/ExpertRepository.kt`:
  - `suspendCancellableCoroutine` → `kotlinx.coroutines.tasks.await()`（checkBlacklist, checkWhitelist, saveSkill, publishExperience, editExperience）
  - 新增 `editSkill()` — 更新 solutions + 重新寫入 pending_skills
- `di/ExpertViewModel.kt`:
  - `publishSkill()` 中的 blacklist/whitelist 查詢改為 `async {}` 並行執行
  - 新增 `SkillEditDialog` 狀態（`skillEditTarget`, `editText`, `editError`）
  - 新增 `startSkillEdit()`, `cancelSkillEdit()`, `updateSkillEditText()`, `submitSkillEdit()`
- `ui/expert/ExpertScreen.kt`:
  - `KnowledgeItemCard` 編輯按鈕改為 `viewModel.startSkillEdit(solution)`
  - PENDING 狀態隱藏編輯按鈕
  - 新增 `SkillEditDialog` composable
- `functions/index.js` — `schedule: 'every 5 minutes'` → `'every 1 minutes'`
- `app/src/test/.../di/ExpertViewModelTest.kt` — 修正建構子、新增 edit skill 測試案例
- `docs/PROGRESS.md` — 新增第 6 輪條目，更新未解決問題清單
- `CHAT_FILES_INDEX.md` — 本次記錄

---

## 第 7 輪：大型重構 — callbackFlow、狀態提升、資安加固（2026/7/14 code review）

### 修改檔案
### 新增檔案
- **`data/Constants.kt`** — 統一路徑/欄位/狀態值常數：`FirebasePaths`、`FirebaseFields`、`StatusValues`

### 修改檔案
- **`data/repository/ExpertRepository.kt`**:
  - `listenToSolutionHistory()` → 回傳 `Flow<List<SolutionItem>>` via `callbackFlow`
  - `initializeExpertStatus()` → `observeExpertStatus(userId)` 回傳 `Flow<Pair<Double, Long>>`
  - 移除 `statusListener`、`currentUserId` 全域變數（Repository 變為無狀態）
  - `saveSkill()` 改為 `updateChildren()` 原子寫入（取代兩個獨立 `setValue()`）
  - `setExpertOnline()` 改為接收 `userId` 參數
  - `cleanup()` 簡化（僅處理 experience）
  - `suspendCancellableCoroutine` → `kotlinx.coroutines.tasks.await()`（全方法遷移）
  - 移除 `checkBlacklist()` / `checkWhitelist()`（移至 Cloud Function 伺服端處理）
  - 新增 `editSkill()` — 更新 solutions + 重新寫入 pending_skills
  - 所有硬編碼路徑/欄位/狀態值替換為 `FirebasePaths`/`FirebaseFields`/`StatusValues` 常數

- **`di/ExpertViewModel.kt`**:
  - `listenToSolutions()` / `initializeExpertStatus()` 改用 `viewModelScope.launch { flow.collect }`
  - `setExpertOnline(online, userId)` 改為接收 userId
  - `ExpertUiEvent.ShowToast` 改用 `@StringRes resId: Int`（固定訊息）+ `ShowToastRaw`（動態訊息）
  - 移除所有硬編碼 Toast 字串
  - 新增 `startSkillEdit()` / `cancelSkillEdit()` / `updateSkillEditText()` / `submitSkillEdit()` 技能編輯流程
  - `skillEditTarget` / `editErrorRes` 狀態欄位
  - `ExpertInputValidator.ValidationError.toResourceId()` 映射 enum → string resource
  - `_uiEvent` Channel 改為 `Channel.BUFFERED`

- **`ui/expert/ExpertScreen.kt`**:
  - 抽離 `ExpertScreenContent` — stateless composable，接收 `ExpertUiState` + 6 個 lambda
  - `ExpertScreen` 作為 thin bridge（event 解析 + state/lambda 傳遞）
  - `context.getString()` 在 `LaunchedEffect` 中解析資源 ID
  - `contentDescription` 改為 `stringResource(R.string.expert_edit_content_desc)`
  - `.collectAsState()` → `.collectAsStateWithLifecycle()`
  - Event 收集改為 `lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED)`
  - 新增 `SkillEditDialog` composable（技能編輯彈窗）
  - PENDING 狀態隱藏編輯按鈕
  - `SkillStatus` 枚舉直接比較（不再用 `.name`）
  - 所有硬編碼中文字串替換為 `stringResource()`

- **`domain/expert/ExpertInputValidator.kt`**:
  - 回傳值從 `String?` 改為 `ValidationError?` 列舉
  - 魔法數字常數化：`MIN_SKILL_LENGTH(4)`, `UNIQUE_CHAR_RATIO_THRESHOLD(0.4)`, `MAX_CONSECUTIVE_DUPLICATES(3)`, `MAX_ADJACENT_PAIRS(3)`, `SINGLETONS_THRESHOLD(3)`, `PURE_ENGLISH_MIN_LENGTH(6)`
  - 中文字元範圍改為 `0x4E00..0x9FFF`（取代 regex）
  - 疊代改為 `zipWithNext()`（取代 indexed for-loop）
  - 公開 `MAX_CHAR_LIMIT` 供 UI 端使用

- **`res/values/strings.xml`** — 新增 33 條字串資源（toast、error、ui 文字、contentDescription、expert_input_* 驗證錯誤）

- **`ui/navigation/AppNavigation.kt`** — `expertViewModel.setExpertOnline(true)` → `setExpertOnline(true, userId)`

- **`app/build.gradle.kts`** + **`gradle/libs.versions.toml`** — 新增 `lifecycle-runtime-compose` 依賴

- **`data/model/SolutionItem.kt`** — `SkillStatus.fromName()` 工廠方法；`status` 欄位從 `String` 改為 `SkillStatus`

- **`di/AppModule.kt`** — `getReference` 路徑字串改為 `FirebasePaths` 常數

- **`di/SeekerViewModel.kt`** — 狀態字串改為 `StatusValues` 常數；路徑改為 `FirebasePaths` 常數

- **`database.rules.json`**:
  - 移除 `$other` 萬用字元規則
  - `tags` 驗證從 `".validate": "true"` 改為逐項字串 + 50 字限制
  - `pending_skills` 寫入權限收緊至僅限資料擁有者
  - `tags_blacklist` / `tags_whitelist` 設為 admin-only（read/write = false）
  - 新增 `config/` 路徑規則

- **`functions/index.js`**:
  - 1st Gen (`firebase-functions`) → 2nd Gen (`firebase-functions/v2/scheduler`)
  - `functions.config()` → `defineSecret('GEMINI_API_KEY')`
  - `@google/generative-ai` (SDK) → `@google/genai`
  - `admin.database()` → `getDatabase()`（firebase-admin v14）
  - 5 模型 fallback 鏈及 model-specific `thinkingConfig`
  - 503 自動 retry（與 429 相同 backoff）
  - `minInstances: 1`
  - `responseMimeType: 'application/json'` 強制 JSON 輸出
  - `processing` flag 併發保護機制（atomic transaction claim，5 分鐘 timeout）
  - 伺服端 blacklist → whitelist → AI 三步驟處理
  - 全數模型 EXHAUSTED 時加入 reset cooldown（10 分鐘）

- **`functions/package.json`**:
  - engines.node: `"20"` → `"24"`
  - `firebase-admin: ^12.0.0` → `^14.0.0`
  - `firebase-functions: ^5.0.0` → `7.3.0-rc.0`
  - `@google/generative-ai: ^0.21.0` → `@google/genai: ^2.10.0`

- **`app/src/test/.../di/ExpertViewModelTest.kt`** — `Dispatchers.setMain(testDispatcher)` + `runTest` + `advanceUntilIdle()`（支援 Flow-based API）。新增多個測試案例（initial state、listenToSolutions、publishSkill 驗證）

### 關鍵決策
| 項目 | 說明 |
|------|------|
| Repository 無狀態化 | callbackFlow 的 `awaitClose` 自動清除 listener，不需手動管理 |
| 原子寫入（第 7 輪） | `saveSkill()` 單一 `updateChildren()` 一次發布 solutions + pending_skills |
| **個別 setValue（第 8 輪）** | **`updateChildren()` 與 `setPersistenceEnabled(true)` 衝突 → 回歸個別 `setValue()`，犧牲原子性換取穩定性** |
| State Hoisting | ExpertScreenContent 可獨立 Preview/測試，ViewModel 只注入在 bridge 層 |
| 字串外部化 | ShowToast 使用 `@StringRes` + `ShowToastRaw`，ViewModel 不再硬編碼 Toast |
| Concurrency | processing timestamp 防止 1 分鐘排程重疊時重複處理同一筆資料 |
| 移除 $other | 任何登入用戶不再能寫入未定義路徑（安全性） |

---

## 第 8 輪：Crash 修復 + saveSkill 改寫 + 亂碼檢測強化 + Submission Lock

### 問題描述
1. **啟動閃退 crash** — `ExpertViewModel.initializeExpertStatus()` / `listenToSolutions()` 中 `userId` 空白時，`callbackFlow` 內 `close(error)` 導致未捕捉異常傳播，app 啟動即 crash
2. **Permission denied on saveSkill** — `updateChildren()` 搭配 `setPersistenceEnabled(true)` 引發多路徑寫入本地快取衝突，即使規則正確仍觸發 permission denied
3. **亂碼檢測不足** — 部分無意義中文句子（如「燒烤是黑子黑吃黑」）繞過前端驗證器
4. **需 Submission Lock** — 惡意使用者連續發布被拒絕的無效技能，無任何阻擋機制

### 修改檔案
- **`di/ExpertViewModel.kt`**:
  - `initializeExpertStatus()`: 加入 `userId.isBlank()` guard + try-catch
  - `listenToSolutions()`: 加入 `userId.isBlank()` guard + try-catch
  - `publishSkill()`: 加入 `userId.isBlank()` 及 `isSubmissionLocked` 檢查
  - `publishExperience()`: 加入 `userId.isBlank()` 檢查
  - 新增 `observeSubmissionLock()` — 監聽 `users/{uid}/submissionLock` 節點
  - 新增 `isSubmissionLocked` 欄位於 `ExpertUiState`
  - `cleanup()` 擴充清除 lock listener
  - 錯誤日誌增加 exception type name

- **`ui/navigation/AppNavigation.kt`**:
  - `LaunchedEffect` 中加入 `userId.isNotBlank()` 條件，避免空白時觸發 Firebase 操作

- **`data/repository/ExpertRepository.kt`**:
  - `listenToSolutionHistory()`: 加入 `userId.isBlank()` guard（空白時 `SendResult()` + close）
  - `observeExpertStatus()`: 加入 `userId.isBlank()` guard
  - `saveSkill()`: `updateChildren()` → 兩個個別 `setValue()`（避免 persistence 衝突）
  - `editSkill()`: `updateChildren()` → 兩個個別 `setValue()`

- **`domain/expert/ExpertInputValidator.kt`**:
  - 新增 `SKILL_UNLIKELY_CHARS`：哦呢嗎吧額喔誒欸啦嘛呀喲嘅誰該
  - 新增二元組（bigram）重複檢測 `windowed(2)`，大於 1 次觸發

- **`ui/expert/ExpertScreen.kt`**:
  - KnowledgeItemCard：`SkillStatus.PENDING` 隱藏整張卡片（不顯示）
  - KnowledgeItemCard：`SkillStatus.ACTIVE` 顯示 AI 標籤（`SuggestionChip`）
  - KnowledgeItemCard：移除 PENDING 狀態 spinner +「AI 分析中...」文字
  - 修正 `visibleHistory` 變數位置（out of `item {}` block）

- **`res/values/strings.xml`**:
  - 新增 `expert_toast_login_required`（「請先登入」）
  - 新增 `expert_toast_submission_locked`（「您的技能多次無法通過驗證，請於24小時後再試」）

- **`functions/index.js`**:
  - 強化 AI prompt：明確要求 REJECT「看似有詞但整體無意義」內容，加入具體範例
  - 新增 Submission Lock 機制：
    - 使用者維度追蹤 rejectedCount
    - 任一 ACTIVE → rejectedCount 歸零
    - REJECTED → rejectedCount + 1
    - 達 3 次 → `users/{uid}/submissionLock.lockedUntil = Date.now() + 86400000`

### 關鍵發現
| 項目 | 說明 |
|------|------|
| callbackFlow + isBlank crash | `close(error)` 在空白 userId 時觸發未處理的 exception，必須在 Repository 層就 guard |
| updateChildren + persistence | `setPersistenceEnabled(true)` 下多路徑 updateChildren 會與本地快取衝突，無法通過規則驗證 |
| Bigram repeat 檢查 | `windowed(2)` 搭配 `distinct().count() < bigrams.size` 可檢測「吃黑吃黑」型繞過 |
| 前端仍無法完美檢測 | 部分正常中文字組合的無意義句子（「燒烤是黑子黑吃黑」）仍可能通過，依賴後端 AI 兜底 |
| 同批次 edge case | 同一批次中若有 ACTIVE + REJECTED 混雜，`hasActive` 會使鎖定計數歸零而不觸發鎖 |

---


---

## 第 9 輪：發布反饋改為 floating overlay + QuickLogCard 清理

### 修改檔案
- **`ui/expert/ExpertScreen.kt`**:
  - `ExpertScreen` bridge: `onClearPublishError` → `onClearPublishFeedback`
  - `ExpertScreenContent` 參數: `onClearPublishError` → `onClearPublishFeedback`
  - Scaffold 內容包裹 `Box(modifier = Modifier.fillMaxSize().padding(innerPadding))` 以實現疊層效果
  - `innerPadding` 從 LazyColumn 移至 Box
  - 新增 floating overlay Card（`publishFeedbackRes` / `publishFeedbackIsError` 驅動），顯示在 Scaffold 最上方
  - `QuickLogCard` 呼叫端移除 `publishError` / `onClearError`，改用 `onClearFeedback`
  - `QuickLogCard` composable 移除 `publishError` / `onClearError` 參數，改為 `onClearFeedback`
  - 移除 `QuickLogCard` 內建的 inline error `Text`（改由外層 overlay 顯示）

### 廢棄
  - `QuickLogCard` 的 `publishError` 參數（不再 inline 顯示）

---

---

## 第 10 輪：AGP 升級 + ExpertViewModel 命名未同步修復（2026/7/15）

### 問題描述
1. **AGP 9.2.1 → 9.3.0** — Android Studio 提示可升級，經分析為 minor release 無 breaking changes
2. **ExpertViewModel 命名未同步** — 第 9 輪 `ExpertScreen.kt` 已將 `publishErrorRes` / `clearPublishError()` 改為 `publishFeedbackRes` / `clearPublishFeedback()`，但 `ExpertViewModel.kt` 未同步更新，導致 IDE 顯示 Unresolved reference 及 cascading type inference 錯誤（「Cannot infer type for type parameter 'R'/'T'」、`Text` composable type mismatch）

### 修改檔案
- **`gradle/libs.versions.toml`** — `agp = "9.2.1"` → `"9.3.0"`
- **`di/ExpertViewModel.kt`**:
  - `ExpertUiState.publishErrorRes` → `publishFeedbackRes`
  - 新增 `ExpertUiState.publishFeedbackIsError: Boolean = true`
  - `clearPublishError()` → `clearPublishFeedback()`
  - `publishSkill()` 成功時改用 `publishFeedbackRes` 顯示 3 秒 overlay（原為 `ShowToast`）
  - `publishSkill()` catch 區塊加入 `publishFeedbackRes = null` 清理

### 關鍵發現
| 項目 | 說明 |
|------|------|
| CLI 建構仍成功 | `.\gradlew.bat assembleDebug` 無錯誤，僅 IDE 層級出現虛假錯誤 |
| Invalidate Caches 不足 | 刪除 `.idea` / `.gradle` / `app/build` 後重開 AS 才解決 |
| 命名同步風險 | ViewModel 的 property/method 改名時若未同步對應的 Screen composable，IDE 會報 cascading 錯誤 |

---

