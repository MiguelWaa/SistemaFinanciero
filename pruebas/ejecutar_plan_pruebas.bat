@echo off
REM ============================================================
REM  SIGSF - Plan de pruebas v5.5 - Ejecuta TODOS los niveles (Windows)
REM    1) JUnit: unitarias, integracion, sistema, aceptacion, seguridad, rendimiento
REM    2) Postman / Newman: pruebas de API
REM    3) Playwright: pruebas de sistema en el navegador
REM  Uso: doble clic o  pruebas\ejecutar_plan_pruebas.bat
REM  Requisitos: JDK 17+, Node.js 18+ y, una sola vez:
REM     cd pruebas\e2e  y  npm install  y  npx playwright install chromium
REM  Resultados en pruebas\reportes\ (no se versiona).
REM ============================================================
setlocal
cd /d "%~dp0.."
set RAIZ=%CD%
set REP=%RAIZ%\pruebas\reportes
if "%SIGSF_SERVER_PORT%"=="" set SIGSF_SERVER_PORT=8091
if exist "%REP%" rmdir /s /q "%REP%"
mkdir "%REP%\junit" "%REP%\newman" "%REP%\capturas" "%REP%\playwright"

echo == [1/3] JUnit (unitarias, integracion, sistema, aceptacion, seguridad, rendimiento)
call scripts\compilar_y_probar.bat < nul > "%REP%\junit\salida_junit.txt" 2>&1
findstr /b /c:"OK (" /c:"Tests run" /c:"[RENDIMIENTO]" "%REP%\junit\salida_junit.txt"

echo == Levantando el SIGSF en modo memoria (puerto %SIGSF_SERVER_PORT%)
set CP=lib\postgresql.jar;lib\slf4j-api-2.0.16.jar;lib\logback-core-1.5.12.jar;lib\logback-classic-1.5.12.jar;build\ci\classes
set SIGSF_PERSISTENCIA=memoria
set SIGSF_BACKUP_AUTOMATICO_HORAS=0
set SIGSF_BACKUP_DIR=%REP%\respaldos
start "SIGSF-pruebas" /min cmd /c "java -cp %CP% com.utp.sistemafinanciero.ServerMain > pruebas\reportes\servidor.log 2>&1"
set /a INTENTOS=0
:esperar
set /a INTENTOS+=1
curl -sf http://localhost:%SIGSF_SERVER_PORT%/api/health >nul 2>&1 && goto :listo
if %INTENTOS% GEQ 60 goto :listo
timeout /t 1 /nobreak >nul
goto :esperar
:listo

echo == [2/3] Postman / Newman
cd pruebas\e2e
if not exist node_modules call npm install --no-audit --no-fund
call npx newman run ..\postman\SIGSF_API.postman_collection.json -e ..\postman\SIGSF_local.postman_environment.json --env-var "baseUrl=http://localhost:%SIGSF_SERVER_PORT%" -r cli,htmlextra,junit --reporter-htmlextra-export "%REP%\newman\reporte-api.html" --reporter-htmlextra-title "SIGSF - Pruebas de API" --reporter-junit-export "%REP%\newman\junit-api.xml"

echo == [3/3] Playwright
set SIGSF_URL=http://localhost:%SIGSF_SERVER_PORT%
call npx playwright test
cd /d "%RAIZ%"

echo == Deteniendo el servidor de pruebas
taskkill /fi "WINDOWTITLE eq SIGSF-pruebas*" /t /f >nul 2>&1
echo.
echo Reportes: pruebas\reportes\newman\reporte-api.html  y  pruebas\reportes\playwright\index.html
pause
