# PROGRESS.md — 專案進度與狀態

## 已完成

### 第 1、2 輪：客戶端 AI 基礎架構與優化
- [x] SolutionItem data class（id, questionId, expertise, tags, timestamp）
- [x] ExpertRepository.saveSolution() / listenToSolutionHistory()
- [x] ExpertViewModel.submitSolution() + solutionHistory 型別更新
- [x] KnowledgeItemCard 與 QuickLogCard 實作
- [x] FlowRow 標籤晶片顯示
- [x] QuickLogCard 重複檢測（expertise 比對）
- [x] 5 模型輪換、RPM/RPD 配額管理、時間校正
- [x] SDK 遷移至 google-genai:1.61.0
- [x] 重構移除 TagViewModel、dead code fetchTagsFromAi
- [x] QuickLogCard 兩步確認與手動標籤編輯流程
- [x] 全域代碼優化（Modifier 順序、依賴管理、lint/test/build）

### 第 3 輪：AI 標籤提取遷移至 Backend Cloud Function（本次）
- [x] **SolutionItem.kt** — 加入 `SkillStatus` 列舉（ACTIVE/PENDING/REJECTED）及 `status` 欄位
- [x] **ExpertRepository.kt** — 加入 `checkBlacklist()` / `checkWhitelist()` / `saveSkill()`（pending queue 寫入）
- [x] **ExpertViewModel.kt** — 移除 `SharedPreferences`、`fetchTagsFromAi()`、`submitSolution()`；加入 `publishSkill()` 完整流程
- [x] **AppModule.kt** — Koin 註冊改為 `viewModel { ExpertViewModel(get()) }`
- [x] **ExpertScreen.kt** — QuickLogCard 簡化為輸入+發布；KnowledgeItemCard 狀態顯示（PENDING spinner / REJECTED 紅字）
- [x] **ExpertInputValidator.kt** — 強化重複檢測規則（連續字元、相鄰配對等）
- [x] **functions/index.js** — 建立 `batchProcessPendingSkills` Cloud Function（每 5 分鐘批次處理，最多 20 筆）
- [x] **functions/package.json** — Node 20 + `@google/generative-ai`
- [x] **database.rules.json** — 新增 `pending_skills`、`tags_blacklist`、`tags_whitelist` 路徑規則與 `.indexOn`
- [x] **Cloud Function 部署成功** — 已設定 Gemini API key，排程正常運作

### Git
- [x] 約 28+ 次提交，已全部推送至 main

## 未解決問題

1. **無整合測試** — 只能靠單元測試與手動安裝 APK 測試。

2. **PENDING 等待最長 5 分鐘** — Cloud Function 每 5 分鐘排程執行，使用者體驗上會有延遲。可考慮縮短排程間隔或改為 `onWrite` trigger。

3. **`tags_whitelist/{text}` 的讀取權限** — Android 客戶端在 `checkWhitelist()` 中讀取 `tags_whitelist/{text}`，需確保規則允許 authenticated user 讀取該路徑（已在規則中設定）。

4. **`pending_skills` 尚缺 `.indexOn`** — 已在 `database.rules.json` 中補上 `".indexOn": ["timestamp"]`，但現有資料需要手動建 index 或等 Firebaser 自動生成。Log 已無 warning。

5. **`functions.config()` 棄用** — Firebase 將在 2027 年 3 月移除 Runtime Config 服務。需遷移至環境變數或 `firebase-functions/params` package。

6. **`ExtractLocalTagsUseCase.kt` 仍存在但已為 dead code** — 舊客戶端 AI 標籤提取不再被呼叫，可考慮刪除。

7. **KnowledgeItemCard 編輯按鈕無實際功能** — `onEditClick` 目前是 `// TODO: 觸發編輯 ViewModel 邏輯`，尚無實作。

8. **卡片編輯 TODO** — 缺少編輯已發布技能的流程（修改 expertise / tags / 重新提交 AI 分析）。

## 模型清單（僅供 Cloud Function 參考）
Cloud Function 目前固定使用 `gemini-3.1-flash-lite`，無模型輪換機制。
