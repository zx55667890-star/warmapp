@echo off
chcp 65001 > nul
cd /d "%~dp0"

set PACKAGE_NAME=com.example.myapplication

echo ==========================================
echo ⚡ VS Code 極速部署流 (守護進程 + 增量編譯)
echo ==========================================

:: 1. 呼叫 Gradle 的極速編譯組合拳 (這行是跟 Android Studio 一樣快的關鍵)
:: --daemon: 讓編譯引擎常駐記憶體，下次編譯不用重新熱機 (省下 5-10 秒)
:: --parallel: 啟動 CPU 多核心平行編譯
:: --offline: 如果你沒有新增第三方套件，開啟離線模式可以完全跳過網路檢查 (大加速)
call .\gradlew assembleDebug --daemon --parallel --offline
if %ERRORLEVEL% neq 0 goto ERROR_BUILD

echo.
echo [1/2] 正在極速閃傳 APK 到手機...
:: -r: 覆蓋安裝, -t: 允許測試版, -d: 允許降級
adb install -r -t -d "app\build\outputs\apk\debug\app-debug.apk"

echo.
echo [2/2] 正在自動啟動 App...
adb shell monkey -p %PACKAGE_NAME% -c android.intent.category.LAUNCHER 1 > nul 2>&1

goto EOF

:ERROR_BUILD
echo.
echo 【錯誤】編譯失敗！如果你剛剛有新增第三方套件，請把指令中的 --offline 刪除再試一次。
pause
goto EOF

:EOF