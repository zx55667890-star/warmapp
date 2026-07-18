# AGENTS.md — WarmApp 開發備忘

## 已知限制

### Android Studio IDE 快取
`write` 工具直接覆寫檔案後，Android Studio 的 IDE 快取未自動更新，可能導致虛假的「Unresolved reference」錯誤。
**解法**：File → Invalidate Caches and Restart，或改用 `edit` 工具減少觸發。
