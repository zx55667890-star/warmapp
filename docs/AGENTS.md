# AGENTS.md — AI 核心規則

## 🔒 高風險提醒（絕不可違反，即使沒看過 docs/ 也要遵守）

1. **ExpertRepository 的 `callbackFlow`**（`listenToSolutionHistory()` / `observeExpertStatus()`）架構已驗證穩定，**嚴禁重構**成其他寫法。
2. **Constants.kt** 的既有常數（`FirebasePaths` / `FirebaseFields` / `StatusValues`）**不能拆分、不能改名**；Firebase 路徑/欄位一律引用此檔，不寫死字串。
3. **`SolutionItem.status` 是 `SkillStatus` 列舉**，比對用 `== SkillStatus.XXX`，**不用 `.name`**。
4. **Cloud Function** 現況：兩支獨立 DB triggered functions（`processSkillsOnWrite` + `processQuestionsOnWrite`），各監聽 `pending_skills/{id}` / `pending_questions/{id}`，寫入即觸發。**6 模型 fallback**（PRIMARY + 5 FALLBACK）。改動前先讀 `docs/AI_CONTEXT.md` 的 Cloud Function 段。
5. **跨檔案命名同步**：改 ViewModel 的 property/method 名稱時，**必須同步改** Screen 中對應的引用，否則會有 cascading Unresolved reference。
6. **UI 字串一律 `strings.xml`**，禁止在 Kotlin 中硬編碼中文。
7. **Compose Modifier 順序**：required → `modifier` → optional。

> 以上是壓縮／摘要後最容易遺失、但改錯代價最高的規則。詳細說明見 `docs/AI_CONTEXT.md` 與 `docs/CODING_STYLE.md`。

## 強制規則

- **查閱最新文檔：** 回答 API/SDK 語法前**必須**搜尋最新官方文檔。
- **標註來源：** 回答時必須附上參考的官方文檔連結。
- **強制使用 MCP 工具：** 排查資料庫問題時**嚴禁**要求手動前往 Firebase Console，必須主動調用 MCP 工具。
- **MCP 資料庫 URL：** 調用 `firebase_realtimedatabase_get_data` 時**必須**指定 `databaseUrl: "https://warmhelpapp-default-rtdb.firebaseio.com"`。
- **優先參考 `docs/`：** 先查閱 `docs/` 目錄下的對應文件，再自行推理。
- **版本對齊：** 參考 `docs/DEPENDENCIES.md`，不提供舊版語法。

## 詳細規則請見

| 主題 | 文件 |
|------|------|
| 程式碼風格 / 分層規則 / 常數 / Status 型別 | `docs/CODING_STYLE.md` |
| 系統架構與資料流 | `docs/ARCHITECTURE.md` |
| 模組地圖（功能↔檔案） | `docs/MODULE_MAP.md` |
| 危險區域、不能改的項目 | `docs/AI_CONTEXT.md` |
| 更新紀錄 | `docs/CHANGELOG.md` |