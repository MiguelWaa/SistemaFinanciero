@echo off
REM ============================================================
REM  SIGSF - Ejecuta el PLAN DE PRUEBAS completo (Avance 4, v6.0.0) en Windows
REM    PE entorno · PU/PI JUnit · PI Newman · PS/PA Playwright · PR JMeter
REM  Requisitos: JDK 17+, PostgreSQL 16 (psql en el PATH), Node.js, Python 3.11+
REM              (pip install -r pruebas\requirements.txt y playwright install chromium)
REM              y JMeter 5.6 en el PATH (opcional).
REM  Uso:  set PGPASSWORD=123456  y  scripts\pruebas\ejecutar_plan_de_pruebas.bat
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0..\.."
if "%PGPASSWORD%"=="" set PGPASSWORD=123456
set SIGSF_DB_NAME=SistemaFinanciero_test
set SIGSF_DB_PASSWORD=%PGPASSWORD%
set PUERTO=8092
set REP=build\reportes
if not exist %REP%\newman mkdir %REP%\newman
if not exist %REP%\jmeter mkdir %REP%\jmeter
if not exist %REP%\pytest mkdir %REP%\pytest
set ESTADO=0

echo ==================== PE - Preparacion del entorno ====================
call scripts\pruebas\preparar_bd_pruebas.bat || goto :error

echo ==================== PU + PI - JUnit ====================
set BD_PRUEBAS=SistemaFinanciero_test
call scripts\compilar_y_probar.bat nopause || set ESTADO=1

echo ==================== Servidor de pruebas (puerto %PUERTO%) ====================
set SIGSF_SERVER_PORT=%PUERTO%
set SIGSF_BACKUP_AUTOMATICO_HORAS=0
start "SIGSF-pruebas" /min java -cp "lib\postgresql.jar;lib\slf4j-api-2.0.16.jar;lib\logback-core-1.5.12.jar;lib\logback-classic-1.5.12.jar;build\ci\classes" com.utp.sistemafinanciero.ServerMain
timeout /t 8 /nobreak >nul
curl -sf http://localhost:%PUERTO%/api/health || goto :error

echo ==================== PI - Newman ====================
call npx --yes -p newman -p newman-reporter-htmlextra newman run pruebas\postman\SIGSF_API.postman_collection.json -e pruebas\postman\SIGSF_local.postman_environment.json -r cli,htmlextra,junit --reporter-htmlextra-export %REP%\newman\newman_sigsf.html --reporter-junit-export %REP%\newman\newman_junit.xml || set ESTADO=1

echo ==================== PS + PA - Playwright ====================
pushd pruebas
python -m pytest -v --junitxml=..\%REP%\pytest\pytest_junit.xml --html=..\%REP%\pytest\reporte_e2e_aceptacion.html --self-contained-html || set ESTADO=1
popd

echo ==================== PR - JMeter ====================
where jmeter >nul 2>nul
if %errorlevel%==0 (
  pushd pruebas\rendimiento
  if exist ..\..\%REP%\jmeter\resultados.jtl del ..\..\%REP%\jmeter\resultados.jtl
  call jmeter -n -t SIGSF_carga.jmx -l ..\..\%REP%\jmeter\resultados.jtl -Jport=%PUERTO% -Jjmeter.save.saveservice.output_format=csv -Jjmeter.save.saveservice.print_field_names=true -e -o ..\..\%REP%\jmeter\html
  popd
  python pruebas\rendimiento\analizar_jtl.py %REP%\jmeter\resultados.jtl || set ESTADO=1
) else (
  echo JMeter no esta en el PATH: se omite PR-01.
)

taskkill /fi "WINDOWTITLE eq SIGSF-pruebas*" /f >nul 2>nul
echo.
echo Reportes en %REP%\   Capturas en evidencias\pruebas\
if %ESTADO%==0 (echo RESULTADO: PLAN DE PRUEBAS APROBADO) else (echo RESULTADO: HAY PRUEBAS FALLIDAS)
pause
exit /b %ESTADO%
:error
taskkill /fi "WINDOWTITLE eq SIGSF-pruebas*" /f >nul 2>nul
echo Error preparando o ejecutando el plan de pruebas.
pause
exit /b 1
