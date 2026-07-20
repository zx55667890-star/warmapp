#!/usr/bin/env pwsh
param([switch]$Quiet)

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

# Count .kt files
$ktCount = (Get-ChildItem -Recurse -Filter "*.kt" |
    Where-Object { $_.FullName -notmatch '\\build\\' } |
    Measure-Object).Count

# Count git commits
$commitCount = git rev-list --count HEAD 2>$null
if (-not $commitCount) { $commitCount = "N/A" }

$srcRoot = "app/src/main/java/com/example/myapplication"
$lines = @()

$lines += "# PROJECT_STRUCTURE.md — 專案目錄結構"
$lines += ""
$lines += "**總計：${ktCount} 個 Kotlin 檔，${commitCount} 次 Git 提交**"
$lines += ""
$lines += "> 本檔案由 generate_project_structure.ps1 自動產生於 ${timestamp}，請勿手動編輯。"
$lines += ""
$lines += '```'
$lines += "warmapp/"

# Collect all relevant files, sorted
$extensions = @(".kt", ".js", ".json", ".toml", ".md")
$excludePaths = @('\build\', '\.git\', '\.gradle\', '\.idea\', '\node_modules\', '\.artifacts\', '\hs_err_pid')

$files = Get-ChildItem -Path "." -Recurse -File |
    Where-Object { $_.Extension -in $extensions } |
    Where-Object {
        $full = $_.FullName
        -not ($excludePaths | Where-Object { $full -match $_ })
    } |
    Sort-Object FullName

$cwd = (Get-Location).Path
foreach ($f in $files) {
    $rel = $f.FullName.Substring($cwd.Length + 1)
    $lines += "  $rel"
}

$lines += '```'
$lines += ""
$lines += "## 快速核對用清單"
$lines += ""

# domain/ use cases
$lines += "### domain/ 底下所有 UseCase"
$domainFiles = Get-ChildItem -Path $srcRoot/domain -Recurse -Filter "*.kt" | Sort-Object FullName
if ($domainFiles.Count -eq 0) {
    $lines += "（無）"
} else {
    foreach ($f in $domainFiles) {
        $rel = $f.FullName -replace [regex]::Escape("$cwd/$srcRoot/"), ""
        $lines += "- $rel"
    }
}
$lines += ""

# di/ modules
$lines += "### di/ 底下所有檔案"
$diFiles = Get-ChildItem -Path $srcRoot/di -Filter "*.kt" | Sort-Object FullName
if ($diFiles.Count -eq 0) {
    $lines += "（無）"
} else {
    foreach ($f in $diFiles) {
        $rel = $f.FullName -replace [regex]::Escape("$cwd/$srcRoot/"), ""
        $lines += "- $rel"
    }
}
$lines += ""

# ViewModels
$lines += "### ui/*/*ViewModel.kt"
$vmFiles = Get-ChildItem -Path $srcRoot/ui -Recurse -Filter "*ViewModel.kt" | Sort-Object FullName
if ($vmFiles.Count -eq 0) {
    $lines += "（無）"
} else {
    foreach ($f in $vmFiles) {
        $rel = $f.FullName -replace [regex]::Escape("$cwd/$srcRoot/"), ""
        $lines += "- $rel"
    }
}
$lines += ""

# test/ files
$lines += "### test/ 底下所有 *Test.kt"
$testPath = "app/src/test/java"
if (Test-Path $testPath) {
    $testFiles = Get-ChildItem -Path $testPath -Recurse -Filter "*.kt" | Sort-Object FullName
    if ($testFiles.Count -eq 0) {
        $lines += "（無 — 已全數清除）"
    } else {
        foreach ($f in $testFiles) {
            $rel = $f.FullName -replace [regex]::Escape("$cwd/$testPath/"), ""
            $lines += "- $rel"
        }
    }
} else {
    $lines += "（無 — 目錄不存在）"
}
$lines += ""

# androidTest/ files
$lines += "### androidTest/ 底下所有檔案"
$androidTestPath = "app/src/androidTest"
if (Test-Path $androidTestPath) {
    $atFiles = Get-ChildItem -Path $androidTestPath -Recurse -Filter "*.kt" | Sort-Object FullName
    if ($atFiles.Count -eq 0) {
        $lines += "（無 — 已全數清除）"
    } else {
        foreach ($f in $atFiles) {
            $rel = $f.FullName -replace [regex]::Escape("$cwd/$androidTestPath/java/"), ""
            $lines += "- $rel"
        }
    }
} else {
    $lines += "（無 — 目錄不存在）"
}

$lines | Set-Content "docs/PROJECT_STRUCTURE.md" -Encoding utf8

if (-not $Quiet) {
    Write-Output "已產生 docs/PROJECT_STRUCTURE.md（共 $ktCount 個 .kt 檔）"
}
