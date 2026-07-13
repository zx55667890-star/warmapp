# CHAT_FILES_INDEX.md — 本次對話修改/參考的檔案索引

## 第 3 輪：AI 標籤提取遷移至 Backend Cloud Function（本次）

### 新增檔案
- `functions/index.js` — Cloud Function `batchProcessPendingSkills`（每 5 分鐘排程批次處理）
- `functions/package.json` — Node 20, @google/generative-ai

### 修改檔案
- `data/model/SolutionItem.kt` — 加入 SkillStatus 列舉 + status 欄位
- `data/repository/ExpertRepository.kt` — 加入黑/白名單查詢、saveSkill（寫入 pending_skills 佇列）、status 讀取
- `di/ExpertViewModel.kt` — 移除 SharedPreferences 依賴、fetchTagsFromAi()、submitSolution()；新增 publishSkill() 流程
- `di/AppModule.kt` — Koin 註冊改為 `viewModel { ExpertViewModel(get()) }`
- `ui/expert/ExpertScreen.kt` — QuickLogCard 簡化為輸入+發布；KnowledgeItemCard 顯示 PENDING spinner / REJECTED 紅字
- `domain/expert/ExpertInputValidator.kt` — 強化解除重複檢測邏輯
- `database.rules.json` — 加入 pending_skills、tags_blacklist、tags_whitelist 路徑規則與 index

### 廢棄（不再使用）
- `domain/expert/ExtractLocalTagsUseCase.kt` — dead code（舊客戶端 AI 標籤提取）
- `di/TagViewModel.kt` — 已移除（職責合併至 ExpertViewModel）

## 主要相關檔案結構
```
warmapp/
├── app/src/main/java/com/example/myapplication/
│   ├── data/
│   │   ├── model/SolutionItem.kt
│   │   └── repository/ExpertRepository.kt
│   ├── di/
│   │   ├── AppModule.kt
│   │   └── ExpertViewModel.kt
│   ├── domain/expert/
│   │   ├── ExpertInputValidator.kt
│   │   └── ExtractLocalTagsUseCase.kt  (dead)
│   └── ui/expert/
│       └── ExpertScreen.kt
├── functions/
│   ├── index.js
│   └── package.json
├── database.rules.json
├── AGENTS.md
├── CHAT_FILES_INDEX.md
└── PROGRESS.md
```
