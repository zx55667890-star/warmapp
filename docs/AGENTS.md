# AGENTS.md — AI 核心規則

## 強制規則

- **查閱最新文檔：** 回答 API/SDK 語法前**必須**搜尋最新官方文檔。
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
