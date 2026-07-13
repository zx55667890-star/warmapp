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
- [x] 模型輪換（Gemini 3.5 Flash, 3.1 Flash Lite, 2.5 系列）
- [x] 已移除 3 Flash Preview 等舊款模型
- [x] RPM 滑動 60 秒窗口（in-memory）
- [x] RPD 太平洋午夜重置（SharedPreferences 持久化）
- [x] 修正 RPD 儲存漏洞：加上隨機後綴防止同毫秒請求被 `StringSet` 過濾
- [x] 優化亂碼拒絕邏輯：當模型識別胡言亂語回傳空字串時，視為「成功識別」並立即停止輪詢
- [x] 增加詳細配額監控 Log：在 Logcat 顯示 RPD/RPM 即時計數與 Ban 狀態
- [x] 伺服器時間校正（Firebase .info/serverTimeOffset）
- [x] withTimeoutOrNull(3000) 防死鎖
- [x] 配額封鎖（Quota exceeded / HTTP 429）
- [x] 舊 ban 遷移（migrateOldBans）
- [x] Non-Lite 模型 thinkingBudget(0) 關閉
- [x] **[FIX]** 修正 3.x thinkingLevel 非法值 "off"，改為 "minimal"
- [x] SDK 遷移至 google-genai:1.61.0
- [x] 打包 META-INF 衝突排除
- [x] 優化配額用盡處理，在 ExtractLocalTagsUseCase 中主動拋出 Exception
- [x] 重構移除 TagViewModel，合併至 ExpertViewModel

### 全域代碼優化與修復
- [x] Modifier 參數順序檢查 — 全部 21 個 Composable 符合慣例
- [x] 未使用資源檢查 — 無冗餘資源
- [x] 依賴管理確認 — 使用 `libs.versions.toml`
- [x] 修復 `local.properties` PropertyEscape lint error
- [x] 更新 `.gitignore` 加入 `.kotlin/`
- [x] **[FIX]** 修復 AGP 棄用警告
- [x] **[FIX]** 修復未檢查的型別轉換 (Unchecked Cast)
- [x] **[FIX]** 修復 UiText annotation target 警告
- [x] `lintDebug` ✅、`testDebugUnitTest` ✅、`assembleDebug` ✅
- [x] **[NEW]** 基礎整合測試實作

## 未解決問題

1. **Lint 棄用警告** — GoogleSignIn 尚待遷移至 Credential Manager API（目前 UI 部分已加上 @Suppress 暫避）。

## 模型清單與配額 (2026 最新版)

 模型 | RPM | RPD | 狀態 | 支援 Thinking |
------|-----|-----|------|---------------|
 Gemini 3.5 Flash | 5 | 20 | GA | 是 (已設為 minimal) |
 Gemini 3.1 Flash Lite | 15 | 500 | GA | 否 |
 Gemini 3 Flash Preview | 5 | 20 | Experimental | 是 (已設為 minimal) |
 Gemini 2.5 Flash | 5 | 20 | GA | 是 (已設為 0 budget) |
 Gemini 2.5 Flash Lite | 10 | 20 | GA | 否 |
