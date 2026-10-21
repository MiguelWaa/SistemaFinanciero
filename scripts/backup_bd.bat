@echo off
REM ============================================================
REM  SIGSF - Respaldo manual de la base de datos con pg_dump
REM  Uso:  scripts\backup_bd.bat
REM  Genera respaldos\sigsf_manual_AAAAMMDD_HHMM.sql (estructura + datos)
REM ============================================================
setlocal
REM --- Ajuste estos valores a su instalacion ---
set PGBIN=C:\Program Files\PostgreSQL\16\bin
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=SistemaFinanciero
set PGUSER=postgres
REM La contrasena NO se escribe aqui: pg_dump la pedira, o use el archivo
REM %APPDATA%\postgresql\pgpass.conf  (formato host:puerto:base:usuario:clave)

if not exist "%PGBIN%\pg_dump.exe" (
  echo No se encontro pg_dump en "%PGBIN%". Edite la variable PGBIN de este archivo.
  exit /b 1
)
if not exist respaldos mkdir respaldos
for /f "tokens=1-3 delims=/- " %%a in ("%date%") do set FECHA=%%c%%b%%a
for /f "tokens=1-2 delims=:." %%a in ("%time: =0%") do set HORA=%%a%%b
set ARCHIVO=respaldos\sigsf_manual_%FECHA%_%HORA%.sql

"%PGBIN%\pg_dump.exe" --format=plain --no-owner --no-privileges --clean --if-exists --encoding=UTF8 -f "%ARCHIVO%" %PGDATABASE%
if errorlevel 1 (
  echo ERROR: no se pudo generar el respaldo.
  exit /b 1
)
echo Respaldo generado correctamente: %ARCHIVO%
endlocal
