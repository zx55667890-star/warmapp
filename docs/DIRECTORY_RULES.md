# DIRECTORY_RULES.md — 分層規則

## 強制規則

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

## 檔案規則

### 常數使用
- Firebase 路徑 → `Constants.kt.FirebasePaths`
- Firebase 欄位名稱 → `Constants.kt.FirebaseFields`
- 狀態值 → `Constants.kt.StatusValues`
- UI 字串 → `strings.xml`（`@StringRes`）

### Compose
- Modifier 順序：required → `modifier` → optional
- `collectAsState()` → 改用 `collectAsStateWithLifecycle()`
- Snackbar / Toast → 固定訊息用 `ShowToast(resId)`，動態用 `ShowToastRaw`
- State Hoisting：Screen composable 接收 state + lambda，不直接建立 ViewModel

### Status 型別
- `SolutionItem.status` 是 `SkillStatus` 列舉，不是 `String`
- 比對用 `solution.status == SkillStatus.PENDING`，不用 `.name`

### 測試
- ViewModel 測試使用 `Dispatchers.setMain(testDispatcher)` + `runTest`
- Flow-based API 使用 `advanceUntilIdle()`
