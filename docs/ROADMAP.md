# ROADMAP.md — 未來規劃

## ✅ v0.9 (Current — 已完成)
- 聊天功能（文字/圖片/語音/影片）
- 登入/註冊（電話驗證 + Google Sign In）
- 技能發布（Expert Mode） + AI 標籤提取
- 提問者模式（Seeker）
- 相機拍攝
- 錄音功能
- 配對系統
- Submission Lock（防濫用）
- Cloud Function AI 分析（6 模型 fallback + Serper）

## 🚧 v1.0 (進行中)
- [ ] 聊天室完整功能（已讀/收回）
- [ ] 搜尋功能
- [ ] 通知系統（FCM）
- [ ] 完整評分機制
- [ ] 使用者個人檔案
- [x] 設定頁面（`FullSettingsScreen.kt` + `DrawerContent.kt`）
- [ ] Google Sign In 完整整合
- [ ] Crash 穩定性修復（MediaPlayer 等）

## 🔮 v2.0 (規劃中)
- [ ] AI 對話助理
- [ ] Plugin 系統
- [ ] 記憶/上下文管理
- [ ] 語音通話
- [ ] 進階通知
- [ ] 效能優化（分頁載入、圖片快取）

## 📌 技術債務
- [ ] `@StringRes` annotation target 警告修復
- [ ] 孤立 pending_skills 清理機制
- [ ] 前端亂碼檢測強化（bigram 等）
- [ ] 跨檔案命名自動同步檢查
