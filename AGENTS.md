# Project Rules for AI Agents

## Critical Rules
- 每 5 次修改程式碼後，更新 `PROGRESS.md`（新增 entry 記錄修改檔案、原因、commit hash）
  - 規則：針對同一個問題的 debugging（如黑條問題的多種嘗試）**只算 1 次修改**，不算入 5 次計數
  - 只有確定完成、進入下一個任務或修復不同問題的改動才算入次數
- 背景風格最終方案：`BackgroundGlow.kt` 使用單色 `#2631C9` + 黑色 radial 暗角
- NAVIGATION EVENT: 使用 `ChatEvent` sealed class + LaunchedEffect collect

## Deprecated / Don't Use
- `AppTabRow.kt` — 已刪除，導航由 RoleSelectScreen 取代
- `ChatScreen.kt` 內不要加 `drawBackgroundGlow()` / `statusBarsPadding()` / `imePadding()`

## Development Environment
- 編輯：`Documents\warmapp`
- 模擬器編譯：`AndroidStudioProjects\warmapp`（編輯完後需 git push，再到另一台 pull）
