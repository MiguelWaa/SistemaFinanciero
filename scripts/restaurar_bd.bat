@echo off
REM ============================================================
REM  SIGSF - Restaurar un respaldo SQL en PostgreSQL
REM  Uso:  scripts\restaurar_bd.bat respaldos\sigsf_XXXXXXXX_XXXXXXXXX_pgdump.sql
REM  ATENCION: reemplaza los datos actuales de la base por los del respaldo.
REM  - Respaldo "pgdump": contiene estructura y datos.
REM  - Respaldo "jdbc":   solo datos; ejecute antes db\schema.sql.
REM ============================================================
setlocal
set PGBIN=C:\Program Files\PostgreSQL\16\bin
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=SistemaFinanciero
set PGUSER=postgres

if "%~1"=="" (
  echo Indique el archivo a restaurar. Ejemplo:
  echo   scripts\restaurar_bd.bat respaldos\sigsf_20261010_230000000_pgdump.sql
  exit /b 1
)
if not exist "%~1" (
  echo No existe el archivo %~1
  exit /b 1
)
echo Se restaurara "%~1" sobre la base %PGDATABASE%.
set /p OK=Escriba SI para continuar: 
if /I not "%OK%"=="SI" exit /b 0

"%PGBIN%\psql.exe" -v ON_ERROR_STOP=1 -d %PGDATABASE% -f "%~1"
if errorlevel 1 (
  echo ERROR durante la restauracion.
  exit /b 1
)
echo Restauracion completada.
endlocal
