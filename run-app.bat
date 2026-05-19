@echo off
cd /d "%~dp0"
set "MVN=%~dp0.tools\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MVN%" (
    echo Maven not found in this project. Running setup first...
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-maven.ps1"
    if errorlevel 1 (
        echo Setup failed. See instructions in BEGINNER_GUIDE.md
        pause
        exit /b 1
    )
)
echo Starting Blood Donation Management System (JavaFX)...
"%MVN%" javafx:run
pause
