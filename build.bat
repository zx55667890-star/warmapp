@echo off
cd /d "%~dp0"
echo Building and installing...
call ./gradlew installDebug
if %errorlevel% neq 0 (
    echo.
    echo FAILED - check errors above
    pause
    exit /b %errorlevel%
)
echo.
echo SUCCESS! Launching app...
adb shell monkey -p com.example.myapplication 1 >nul 2>&1
timeout /t 3 /nobreak >nul
