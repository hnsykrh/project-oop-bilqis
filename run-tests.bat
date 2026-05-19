@echo off
cd /d "%~dp0"
set "MVN=%~dp0.tools\apache-maven-3.9.6\bin\mvn.cmd"
if not exist "%MVN%" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-maven.ps1"
)
"%MVN%" test
pause
