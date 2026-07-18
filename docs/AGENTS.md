# AGENTS.md — AI 核心規則

## 強制規則 (CRITICAL)
- **查閱最新文檔：** 在回答任何關於 API、SDK 或技術實作的語法前，**必須**使用搜尋工具查詢最新的官方文檔。
- **Constants.kt 優先：** 修改 Firebase 路徑、欄位名稱或狀態值時，優先更新 `app/src/main/java/com/example/myapplication/data/Constants.kt`，而非直接寫字串。
- **status 欄位型別：** `SolutionItem.status` 是 `SkillStatus` 列舉型別（非 `String`），比對時用 `solution.status == SkillStatus.PENDING` 而非比對 `.name`。
- **UI 字串：** 所有使用者可見文字必須出自 `strings.xml`；動態 Toast 用 `ShowToastRaw`，固定訊息用 `ShowToast(resId)`。
- **ViewModel / Screen 命名同步：** 修改 `ExpertUiState` 或 ViewModel 的 property/method 名稱時，必須同步更新 `ExpertScreen.kt` 中對應的引用（如 `publishErrorRes` ↔ `publishFeedbackRes`），否則 IDE 會報 cascading Unresolved reference 錯誤。
- **標註來源：** 必須在回答中附上你參考的官方文檔連結。
- **版本對齊：** 請參考 `docs/DEPENDENCIES.md` 中指定的版本，不要提供舊版語法。
- **強制使用 MCP 工具：** 本環境已綁定 Firebase MCP Server。排查資料庫問題（如讀取 `pending_skills`、查閱狀態）時，**嚴禁**要求我手動前往 Firebase Console 查看，你必須主動調用 MCP 工具來獲取或修改資料。
- **MCP 資料庫 URL：** 調用 `firebase_realtimedatabase_get_data` 時**必須**指定 `databaseUrl: "https://warmhelpapp-default-rtdb.firebaseio.com"`，否則 MCP 預設會用 `firebasedatabase.app` 網域（DNS 無法解析導致錯誤）。
- **優先參考 docs/：** 專案架構、已知問題、開發指令等請優先查閱 `docs/` 目錄下的對應文件。
