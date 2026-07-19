#!/usr/bin/env bash
#
# generate_project_structure.sh
#
# 掃描專案實際目錄，重新產生 docs/PROJECT_STRUCTURE.md，
# 避免因為手動（或請 LLM）更新文件時漏掉某些檔案。
#
# 用法：在專案根目錄（warmapp/）執行
#   bash generate_project_structure.sh
#
# 會輸出到 docs/PROJECT_STRUCTURE.md（若目錄不存在會自動建立）

set -euo pipefail

PROJECT_ROOT="$(pwd)"
OUT_FILE="docs/PROJECT_STRUCTURE.md"
SRC_ROOT="app/src/main/java"
TEST_ROOT="app/src/test/java"

if [ ! -d "$SRC_ROOT" ]; then
  echo "錯誤：找不到 $SRC_ROOT，請確認你是在專案根目錄執行此腳本。" >&2
  exit 1
fi

mkdir -p docs

KT_COUNT=$(find "$PROJECT_ROOT" -name "*.kt" -not -path "*/build/*" | wc -l | tr -d ' ')
COMMIT_COUNT="N/A"
if [ -d .git ]; then
  COMMIT_COUNT=$(git rev-list --count HEAD 2>/dev/null || echo "N/A")
fi

{
  echo "# PROJECT_STRUCTURE.md — 專案目錄結構"
  echo ""
  echo "**總計：${KT_COUNT} 個 Kotlin 檔，${COMMIT_COUNT} 次 Git 提交**"
  echo ""
  echo "> 本檔案由 generate_project_structure.sh 自動產生於 $(date '+%Y-%m-%d %H:%M:%S')，請勿手動編輯。"
  echo ""
  echo '```'
  echo "warmapp/"
} > "$OUT_FILE"

# 用 find 建立樹狀結構（排除 build/.git/.gradle 等雜訊目錄）
# 這裡用簡單的 tree 指令；若沒有 tree，退回用 find 手刻縮排版本
if command -v tree >/dev/null 2>&1; then
  tree -I "build|.git|.gradle|.idea|node_modules" -F --noreport \
    app functions gradle 2>/dev/null >> "$OUT_FILE" || true
  for f in database.rules.json AGENTS.md CHAT_FILES_INDEX.md PROGRESS.md; do
    [ -f "$f" ] && echo "$f" >> "$OUT_FILE"
  done
  [ -d docs ] && tree -F --noreport docs >> "$OUT_FILE"
else
  echo "（未安裝 tree，改用 find 列出所有 .kt / 設定檔路徑，排版較陽春，建議 apt/brew install tree 後重跑）" >> "$OUT_FILE"
  find app functions gradle docs -type f \
    \( -name "*.kt" -o -name "*.js" -o -name "*.json" -o -name "*.toml" -o -name "*.md" \) \
    -not -path "*/build/*" \
    -not -path "*/node_modules/*" \
    -not -path "*/.git/*" \
    -not -path "*/.gradle/*" \
    -not -path "*/.idea/*" \
    2>/dev/null | sort >> "$OUT_FILE"
fi

echo '```' >> "$OUT_FILE"

# 額外附上：實際存在但可能容易被忽略的清單，方便跟 FILE_RELATION.md / MODULE_MAP.md 核對
{
  echo ""
  echo "## 快速核對用清單"
  echo ""
  echo "### domain/ 底下所有 UseCase"
  find "$SRC_ROOT" -path "*/domain/*" -name "*.kt" -not -path "*/build/*" 2>/dev/null | sed "s|$SRC_ROOT/|- |" | sort
  echo ""
  echo "### di/ 底下所有檔案"
  find "$SRC_ROOT" -path "*/di/*" -name "*.kt" -not -path "*/build/*" 2>/dev/null | sed "s|$SRC_ROOT/|- |" | sort
  echo ""
  echo "### ui/*/*ViewModel.kt"
  find "$SRC_ROOT" -name "*ViewModel.kt" -not -path "*/build/*" 2>/dev/null | sed "s|$SRC_ROOT/|- |" | sort
  echo ""
  echo "### test/ 底下所有 *Test.kt（核對是否跟主程式路徑對齊）"
  [ -d "$TEST_ROOT" ] && find "$TEST_ROOT" -name "*.kt" -not -path "*/build/*" 2>/dev/null | sed "s|$TEST_ROOT/|- |" | sort
  echo ""
  echo "### androidTest/ 底下所有檔案"
  find app/src/androidTest -name "*.kt" -not -path "*/build/*" 2>/dev/null | sed "s|app/src/androidTest/java/||" | sed 's/^/- /' | sort
} >> "$OUT_FILE"

echo "已產生 $OUT_FILE（共 $KT_COUNT 個 .kt 檔）"
echo ""
echo "接下來建議："
echo "1. 打開 $OUT_FILE，比對「domain/ 底下所有 UseCase」區塊，確認 PublishSkillUseCase.kt / ObserveSolutionsUseCase.kt 是否存在、放在哪裡"
echo "2. 比對「di/ 底下所有檔案」跟「ui/*/*ViewModel.kt」，確認 ExpertViewModel.kt / SeekerViewModel.kt 實際放在 di/ 還是 ui/expert, ui/seeker"
echo "3. 若發現位置跟 MODULE_MAP.md / FILE_RELATION.md 說的不一樣，決定要「搬檔案」還是「改文件」，兩者擇一，不要留著不一致"