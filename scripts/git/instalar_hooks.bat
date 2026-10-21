@echo off
REM Activa los hooks versionados en .githooks\ (una vez por clon).
cd /d "%~dp0..\.."
git config core.hooksPath .githooks
echo Hooks activados: .githooks (pre-commit, commit-msg)
pause
