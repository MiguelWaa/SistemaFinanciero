@echo off
cd /d "%~dp0"
set SIGSF_PERSISTENCIA=memoria
set SIGSF_SERVER_PORT=8091
set SIGSF_BACKUP_AUTOMATICO_HORAS=0
echo Abrir http://localhost:8091/ - Ctrl+C para detener.
java -jar dist\SIGSF.jar
pause
