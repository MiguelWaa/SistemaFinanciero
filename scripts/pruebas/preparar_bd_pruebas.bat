@echo off
REM ============================================================
REM  SIGSF - Plan de Pruebas - Preparacion del entorno (PE-01)
REM  Crea desde cero la base de PRUEBAS SistemaFinanciero_test
REM  (no toca la base de desarrollo) y ejecuta los scripts en orden.
REM  Requiere psql en el PATH (C:\Program Files\PostgreSQL\16\bin).
REM  Uso:  set PGPASSWORD=123456  y  scripts\pruebas\preparar_bd_pruebas.bat
REM ============================================================
setlocal
cd /d "%~dp0..\.."
if "%PGHOST%"=="" set PGHOST=localhost
if "%PGPORT%"=="" set PGPORT=5432
if "%PGUSER%"=="" set PGUSER=postgres
set BD=SistemaFinanciero_test
echo == Recreando la base %BD%
psql -q -v ON_ERROR_STOP=1 -d postgres -c "DROP DATABASE IF EXISTS \"%BD%\" WITH (FORCE);" || goto :error
psql -q -v ON_ERROR_STOP=1 -d postgres -c "CREATE DATABASE \"%BD%\" ENCODING 'UTF8' TEMPLATE template0;" || goto :error
for %%f in (schema.sql datos_ejemplo.sql portal_cliente.sql seguridad.sql version_esquema.sql datos_100_registros.sql pagos_qr.sql) do (
  echo    - db\%%f
  psql -q -v ON_ERROR_STOP=1 -d %BD% -f db\%%f >nul || goto :error
)
psql -d %BD% -At -c "SELECT 'clientes=' || count(*) FROM cliente;"
echo == Base de pruebas lista.
exit /b 0
:error
echo Error preparando la base de pruebas.
exit /b 1
