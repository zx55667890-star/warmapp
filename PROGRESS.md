# PROGRESS.md — 專案進度與狀態

## 已完成

### 基礎架構
- [x] SolutionItem data class（id, questionId, expertise, tags, timestamp）
- [x] ExpertRepository.saveSolution() / listenToSolutionHistory()
- [x] ExpertViewModel.submitSolution() + solutionHistory 型別更新
- [x] KnowledgeItemCard 與 QuickLogCard 實作
- [x] FlowRow 標籤晶片顯示
- [x] QuickLogCard 重複檢測（expertise 比對）

### Gemini API 整合與架構重構
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
- [x] **[NEW]** 優化配額用盡處理，在 ExtractLocalTagsUseCase 中主動拋出 Exception，解決靜默失敗
- [x] **[NEW]** 優化 Token 耗費，在 checks 後才調用 API 避免浪費
- [x] **[NEW]** 重構移除 TagViewModel，將職責合併至 ExpertViewModel，清除了 dead code fetchTagsFromAi
- [x] **[NEW]** 實現 QuickLogCard 的兩步確認與手動新增/編輯標籤流程（UI 降級與微調機制）

### Git
- [x] 約 28 次提交，已全部推送至 main

### 全域代碼優化（第 2 輪）
- [x] Modifier 參數順序檢查 — 全部 21 個 Composable 符合慣例，無需修改
- [x] 未使用資源檢查 — 無 `colors.xml` / `backup_rules.xml`，所有資源皆在使用
- [x] 依賴管理確認 — `libs.versions.toml` 完整，`build.gradle.kts` 全用 `libs.*`
- [x] 修復 `local.properties` PropertyEscape lint error（`C:` → `C\:`）
- [x] 更新 `.gitignore` 加入 `.kotlin/`，移除已追蹤的 compiler session 快取
- [x] `lintDebug` ✅、`testDebugUnitTest` ✅、`assembleDebug` ✅

## 未解決問題

1. **無整合測試** — 只能靠單元測試與手動安裝 APK 測試。

2. **3 Flash Preview 強制 thinking** — 已移除該模型。

3. **AGP 棄用警告** — 已清理。

4. **Lint 棄用警告** — GoogleSignIn 尚待遷移，UI 部分已加上 @Suppress。

5. **未檢查的型別轉換** — 已修復。

6. **UiText annotation target 警告** — 已修復。

## 模型清單與配額

| 模型 | RPM | RPD | 支援 Thinking |
|------|-----|-----|---------------|
| Gemini 3.1 Flash Lite | 15 | 500 | 否 |
| Gemini 2.5 Flash Lite | 10 | 20 | 否 |
| Gemini 3.5 Flash | 5 | 20 | 是（已關閉） |
| Gemini 2.5 Flash | 5 | 20 | 是（已關閉） |
