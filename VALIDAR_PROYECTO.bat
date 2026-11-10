@echo off
cd /d "%~dp0"
python scripts\validar_proyecto.py %*
pause
