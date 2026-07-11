# PROGRESS.md — 專案進度與狀態

## 已完成

### 基礎架構
- [x] SolutionItem data class（id, questionId, expertise, tags, timestamp）
- [x] ExpertRepository.saveSolution() / listenToSolutionHistory()
- [x] ExpertViewModel.submitSolution() + solutionHistory 型別更新
- [x] KnowledgeItemCard 與 QuickLogCard 實作
- [x] FlowRow 標籤晶片顯示
- [x] QuickLogCard 重複檢測（expertise 比對）

### Gemini API 整合
- [x] 5 模型輪換（已移除 3 Flash Preview）
- [x] RPM 滑動 60 秒窗口（in-memory）
- [x] RPD 太平洋午夜重置（SharedPreferences 持久化）
- [x] 伺服器時間校正（Firebase .info/serverTimeOffset）
- [x] withTimeoutOrNull(3000) 防死鎖
- [x] 配額封鎖（Quota exceeded / HTTP 429）
- [x] 舊 ban 遷移（migrateOldBans）
- [x] Non-Lite 模型 thinkingBudget(0)
- [x] SDK 遷移至 google-genai:1.61.0
- [x] 打包 META-INF 衝突排除

### Git
- [x] 約 28 次提交，已全部推送至 main

## 未解決問題

1. **配額用盡時靜默失敗** — 當所有模型 RPM/RPD 滿時，使用者會看到「成功記錄」但實際送出空標籤。無任何錯誤提示或重試 UI。

2. **無手動標籤輸入備案** — QuickLogCard 沒有任何手動輸入/編輯標籤的欄位。AI 提取失敗時使用者無法自行補上標籤。

3. **無整合測試** — 只能靠手動安裝 APK 測試。

4. **Token 耗費** — 每次提取都傳送完整 prompt 和原文，即使只是 RPD 滿了也會浪費 token（記錄在 RPM 前就已發出請求）。應在 `canUseModel()` 檢查後再決定是否請求。

5. **`fetchTagsFromAi` 未使用** — ExpertViewModel 有一個 `fetchTagsFromAi()` 函式包含 Toast 提示，但 QuickLogCard 沒用到它，變成 dead code。

6. **3 Flash Preview 強制 thinking** — 已移除該模型，但若未來要加回來需注意 `thinkingBudget(0)` 對它無效。

## 模型清單與配額

| 模型 | RPM | RPD | 支援 Thinking |
|------|-----|-----|---------------|
| Gemini 3.1 Flash Lite | 15 | 500 | 否 |
| Gemini 2.5 Flash Lite | 10 | 20 | 否 |
| Gemini 3.5 Flash | 5 | 20 | 是（已關閉） |
| Gemini 2.5 Flash | 5 | 20 | 是（已關閉） |
