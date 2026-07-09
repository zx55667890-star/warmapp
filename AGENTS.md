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
- 編輯 + 編譯都在 `Documents\warmapp`
- 編譯指令：`./gradlew installDebug`（自動建置 + 安裝到已連線裝置）
- 仍需 **git push** 定期備份上 GitHub，但不用每次修改都 push

## Workflow
1. 修改程式碼
2. 我先用 `./gradlew assembleDebug --daemon --parallel --offline` 編譯確認沒問題
3. 沒問題就 git push
4. 執行 `.\install.bat` 安裝到手機

## Prerequisites
- Android SDK：`C:\Users\user\AppData\Local\Android\Sdk`
- 透過 `adb devices` 確認裝置已連線（支援 USB / 無線偵錯）
