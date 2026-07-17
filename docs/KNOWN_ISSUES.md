# KNOWN_ISSUES.md — 已知問題

## 🔴 Critical

### 1. `pending_skills` 孤立風險
Cloud Function crash 在 claim 之後、寫回結果之前，entry 會被標記 `processing` 而擱置最多 5 分鐘。
- **影響**：該筆技能永遠不會被處理
- **現狀**：`PROCESSING_TIMEOUT_MS = 5 min` 會超時釋放
- **建議**：實作異常 cleanup 或手動清除腳本

### 2. saveSkill 原子性喪失
`saveSkill()` 使用個別 `setValue()`（非 `updateChildren()`），若 `solutions` 寫入成功但 `pending_skills` 失敗，產生孤立 solution。
- **影響**：資料不一致，技能永遠 PENDING
- **現狀**：可接受（Cloud Function 只讀 pending_skills）
- **建議**：實作清理機制定時掃描孤立資料

## 🟡 Medium

### 3. `@StringRes` annotation target 警告
Kotlin 2.x 編譯時觸發 KT-73255。
- **解法**：加 `-Xannotation-default-target=param-property` 或改用 `@param:` target

### 4. Cloud Function reset cooldown 期間 PENDING 累積
所有模型 EXHAUSTED 時需等 10 分鐘 cooldown 才會重置，期間 PENDING skills 不處理。
- **建議**：考慮手動觸發機制或逐步縮短 cooldown

### 5. Submission Lock 同批次邊界情況
同一批次中同一使用者有多筆 entry，混雜 ACTIVE 與 REJECTED 時，`hasActive` 會將 rejectedCount 歸零。
- **影響**：低機率場景（ACTIVE 與 REJECTED 同時出現才受影響）

### 11. Gen3 Free Tier 不支援內建 `googleSearch`（429 / RESOURCE_EXHAUSTED）
Free Tier Gen3 模型（如 `gemini-3-flash-preview`）無法搭配 `tools: [{ googleSearch: {} }]`，會回傳 429。
- **解法**：透過 `useWebFetch` 旗標改用 Serper 外部搜尋 API，將搜尋結果注入 prompt 後再送模型
- **現狀**：FALLBACK_1 使用 Serper，FALLBACK_2~3 使用內建 googleSearch（僅 Gen2 模型）

## 🟢 Low

### 6. 前端亂碼檢測無法完美
部分正常中文字組合的無意義句子（如「燒烤是黑子黑吃黑」）仍可能通過前端驗證。
- **現狀**：依賴後端 AI 兜底，可接受

### 7. Android Studio IDE 快取過期
`write` 工具直接覆寫檔案後，AS 的 IDE 快取未自動更新，導致虛假的「Unresolved reference」錯誤。
- **解法**：File → Invalidate Caches and Restart

### 8. 跨檔案命名未同步風險
ViewModel property/method 改名時若未同步對應的 Screen composable，IDE 會報 cascading 錯誤。
- **現狀**：CLI 建構仍成功，僅 IDE 層級錯誤
- **解法**：修改後必須 compile 確認

### 9. chat MediaPlayer 偶爾 crash
- **狀態**：待調查

### 10. Google Sign In 尚未完成
- **狀態**：`SignInWithGoogleUseCase.kt` 存在但尚未完整整合

### 12. `Icons.Outlined.TrendingUp` 已棄用
ExpertScreen.kt:302 使用 `Icons.Outlined.TrendingUp`，應改用 `Icons.AutoMirrored.Outlined.TrendingUp`。
- **影響**：編譯 warning，功能正常

### 13. `GoogleSignIn` 已棄用
AuthScreen.kt:37/95/101 使用 `GoogleSignIn` class，已標記 deprecated。
- **影響**：編譯 warning，功能正常
- **建議**：遷移至 Credential Manager API

### 14. Constants.kt 集中式常數未拆分
`data/Constants.kt` 包含所有 Firebase 路徑、欄位、狀態值。隨專案規模成長建議依 feature 拆分（ChatConstants.kt / ExpertConstants.kt）或內部用 object 命名空間隔離。

### 15. `combine` 三 flow 同步導致 Firebase 刪除不即時反映
`ChatViewModel.observeChatDataStreams()` 使用 `flatMapLatest` 內的 `combine(observeMessages, observeTypingStatus, observeChatStatus)`，僅在三 flow **全部有新值**時才 emit。若只從 Firebase 刪除 messages 資料（typing/status 不變），UI 畫面不會即時更新。需強制關閉 app 重開才會反映。
- **影響**：低（正常使用不會刪 Firebase 資料，僅除錯時受影響）
- **建議**：改用 `merge` 分拆或各自獨立 collect

### 16. Firebase `orderByChild("timestamp").limitToLast(N)` query listener 低機率不觸發
在 `chatrooms/{id}/messages` 節點上使用 `orderByChild("timestamp").limitToLast(100)` 時，`addValueEventListener` 的 `onDataChange` 偶爾完全不呼叫，即使同路徑的 `addValueEventListener`（無 query）正常運作。原因不明。
- **影響**：低（已改用直接 `addMessagesListener` 繞過）
- **建議**：若需分頁查詢，考慮用 `startAfter` / `endBefore` 搭配 direct listener 自行排序
