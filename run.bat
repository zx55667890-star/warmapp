@echo off
:: 切換到當前腳本所在的目錄
cd /d "%~dp0"

echo [1/3] 正在編譯專案...
call gradlew.bat :app:assembleDebug
if %errorlevel% neq 0 (
    echo 編譯失敗！
    pause
    exit /b %errorlevel%
)

echo [2/3] 正在安裝至手機...
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
if %errorlevel% neq 0 (
    echo 安裝失敗！
    pause
    exit /b %errorlevel%
)

echo [3/3] 正在啟動 App...
adb shell am force-stop com.example.myapplication
adb shell am start -n com.example.myapplication/.MainActivity

echo 流程完成！
timeout /t 3