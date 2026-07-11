# CHAT_FILES_INDEX.md — 本次對話修改/參考的檔案索引

## 新增檔案
- `data/model/SolutionItem.kt` — SolutionItem data class

## 修改檔案
- `app/build.gradle.kts` — SDK 依賴 + packaging 排除設定
- `data/repository/AiRepository.kt` — 遷移至新 SDK Client API
- `data/repository/ExpertRepository.kt` — 結構化 SolutionItem 儲存/監聽
- `di/ExpertViewModel.kt` — submitSolution()、solutionHistory 型別更新
- `di/TagViewModel.kt` — 加入 SharedPreferences + FirebaseDatabase 依賴
- `di/AppModule.kt` — Koin 註冊更新
- `domain/expert/ExtractLocalTagsUseCase.kt` — 核心：配額管理、時間校正、ban、模型輪換、thinking config、3 秒 timeout、變數遮蔽修正、429 攔截
- `ui/expert/ExpertScreen.kt` — KnowledgeItemCard、QuickLogCard（重複檢測）、FlowRow chip
- `ui/navigation/AppNavigation.kt` — koinViewModel()

## 主要資料夾結構
```
warmapp/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/example/myapplication/
│       ├── data/
│       │   ├── model/SolutionItem.kt
│       │   └── repository/
│       │       ├── AiRepository.kt
│       │       └── ExpertRepository.kt
│       ├── di/
│       │   ├── AppModule.kt
│       │   ├── ExpertViewModel.kt
│       │   └── TagViewModel.kt
│       ├── domain/expert/ExtractLocalTagsUseCase.kt
│       └── ui/
│           ├── expert/ExpertScreen.kt
│           └── navigation/AppNavigation.kt
├── AGENTS.md
├── CHAT_FILES_INDEX.md
└── PROGRESS.md
```
