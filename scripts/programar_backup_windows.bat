@echo off
REM ============================================================
REM  SIGSF - Programa un respaldo diario a las 23:00 con el
REM  Programador de tareas de Windows (ejecutar como administrador).
REM  Ademas del respaldo automatico que hace el propio servidor.
REM ============================================================
set RUTA=%~dp0backup_bd.bat
schtasks /Create /SC DAILY /ST 23:00 /TN "SIGSF Respaldo diario" /TR "\"%RUTA%\"" /F
echo Tarea creada. Puede verla en "Programador de tareas" de Windows.
