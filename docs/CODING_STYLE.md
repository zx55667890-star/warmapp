# CODING_STYLE.md — 程式碼風格與分層規則

## 一、強制規則（程式碼風格）

### 資源
- ✅ 所有 UI 字串 → `strings.xml`
- ✅ 顏色 → `Color.kt`（`AppColors` 物件）
- ❌ 沒有 `colors.xml` 或 `backup_rules.xml`
- ❌ 沒有硬編碼中文字串在 Kotlin 中

### 狀態管理
- ✅ StateFlow（不排隊） / SharedFlow（排隊事件）
- ❌ LiveData（不使用）
- ✅ `StateFlow.collectAsStateWithLifecycle()`
- ❌ `StateFlow.collectAsState()`（應避免）
- ✅ UI 事件走 `Channel(Channel.BUFFERED)` → `SharedFlow`
- ✅ Flow collect 使用 `viewModelScope.launch { flow.collect { } }`

### 資料層
- ✅ Repository 無狀態（不持有 listener 或 userId 全域變數）
- ✅ Firebase 即時監聽用 `callbackFlow`
- ✅ `callbackFlow` 的 `awaitClose` 自動清除 listener
- ✅ Firebase 路徑必須用 `Constants.kt`（`FirebasePaths` / `FirebaseFields` / `StatusValues`）

### Compose
- ✅ Modifier 順序：required → `modifier` → optional
- ✅ State Hoisting：Screen composable 接收 state + lambda
- ❌ Screen composable 內不能直接 new ViewModel
- ✅ Scaffold 內用 `Box` 疊層處理 overlay
- ✅ `collectAsState()` 已全部改用 `collectAsStateWithLifecycle()`
- ✅ `LaunchedEffect` 搭配 `lifecycle.repeatOnLifecycle` 收集事件

### ViewModel
- ✅ 使用者輸入驗證委派給 UseCase 或 Validator（不在 ViewModel 內部處理）
- ✅ Toast/Snackbar 透過 Channel 事件觸發
- ✅ 固定訊息用 `ShowToast(resId: Int)` + `@StringRes`
- ✅ 動態訊息用 `ShowToastRaw(message: String)`

### 資料模型
- ✅ `SolutionItem.status` 是 `SkillStatus` 列舉
- ❌ 不能比對 `status.name`，必須用 `status == SkillStatus.ACTIVE`
- ✅ 資料類別使用 data class

### 依賴注入
- ✅ 統一使用 Koin（`libs.versions.toml` 版本管理）
- ❌ 沒有手動依賴注入
- ✅ ViewModel 路徑：專家在 `ui/expert/`，提問者在 `ui/seeker/`
- ✅ ViewModel 註冊用 `viewModel { ExpertViewModel(get(), get(), get(), get()) }`

### 後端 (Cloud Function)
- ✅ Node.js 24 runtime
- ✅ Secret 使用 `defineSecret('GEMINI_API_KEY')` 而非 `functions.config()`
- ✅ 每 1 分鐘排程（`* * * * *`）
- ✅ `minInstances: 1`
- ✅ 6 模型 fallback 鏈（PRIMARY + 5 FALLBACK）
- ✅ 503 retry（2s / 4s backoff）
- ✅ 429/RESOURCE_EXHAUSTED → EXHAUSTED
- ✅ 有搜尋的模型不使用 `responseMimeType: 'application/json'`（Gemini 2.5 不支援同時使用 tools + JSON mode）
- ✅ 自我修復掃描孤立 PENDING（`healOrphanedPending`，每次 5 個 user，隨 scheduler 每分鐘執行）

### 測試
- ✅ ViewModel 測試用 `Dispatchers.setMain(testDispatcher)` + `runTest`
- ✅ Flow-based API 用 `advanceUntilIdle()`
- ✅ Mockk 進行 mock

### 其他
- ✅ `git push` 前必須確認只 stage 預期檔案
- ✅ 不提交 `node_modules/` 或 `build/` 目錄
- ✅ Dependency 管理統一在 `libs.versions.toml`

## 二、分層規則（DIRECTORY_RULES）

### `ui/` 層
- ❌ 不能直接 import Firebase (`com.google.firebase.*`)
- ❌ 不能直接 new Repository（必須透過 DI/ViewModel）
- ❌ 不能持有 Context 靜態參考（Composable 透過 `LocalContext` 即取即用）
- ✅ 只透過 ViewModel 的 lambda / StateFlow 互動
- ✅ Stateless composable 不直接依賴 ViewModel

### `di/` 層 (ViewModel)
- ✅ 可以呼叫 Repository
- ❌ 不能 import Compose UI 元件（`androidx.compose.*`、`androidx.compose.material3.*`）
- ❌ 不能持有 `Context`（透過 `AndroidViewModel` 或 DI 注入）
- ✅ 使用 `viewModelScope.launch { collect }` 管理 Flow

### `data/repository/` 層
- ❌ 不能 import Compose (`androidx.compose.*`)
- ❌ 不能 import ViewModel (`androidx.lifecycle.ViewModel`)
- ✅ 只操作 Firebase SDK / 資料來源
- ✅ Firebase 路徑必須使用 `Constants.kt`（`FirebasePaths` / `FirebaseFields` / `StatusValues`）

### `domain/` 層
- ❌ 不能 import Android framework（`android.*`）
- ❌ 不能 import Firebase
- ✅ 純 Kotlin 邏輯，可測試
- ✅ 回傳值使用 `ValidationError` 列舉而非 `String`

## 三、常數使用
- Firebase 路徑 → `Constants.kt.FirebasePaths`
- Firebase 欄位名稱 → `Constants.kt.FirebaseFields`
- 狀態值 → `Constants.kt.StatusValues`
- UI 字串 → `strings.xml`（`@StringRes`）

## 四、Status 型別
- `SolutionItem.status` 是 `SkillStatus` 列舉，不是 `String`
- 比對用 `solution.status == SkillStatus.PENDING`，不用 `.name`
