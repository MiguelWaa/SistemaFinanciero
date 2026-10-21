@echo off
REM ==========================================================================
REM  SIGSF - Descarga de librerias (una sola vez, requiere internet)
REM  Baja SLF4J y Logback desde Maven Central a la carpeta lib\
REM  Uso: doble clic, o desde la carpeta del proyecto: scripts\descargar_librerias.bat
REM ==========================================================================
setlocal
cd /d "%~dp0.."
set MAVEN=https://repo1.maven.org/maven2

echo Descargando slf4j-api 2.0.16 ...
curl -fL -o lib\slf4j-api-2.0.16.jar %MAVEN%/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar || goto error
echo Descargando logback-core 1.5.12 ...
curl -fL -o lib\logback-core-1.5.12.jar %MAVEN%/ch/qos/logback/logback-core/1.5.12/logback-core-1.5.12.jar || goto error
echo Descargando logback-classic 1.5.12 ...
curl -fL -o lib\logback-classic-1.5.12.jar %MAVEN%/ch/qos/logback/logback-classic/1.5.12/logback-classic-1.5.12.jar || goto error

if not exist lib\postgresql.jar (
  echo Descargando driver PostgreSQL 42.7.4 ...
  curl -fL -o lib\postgresql.jar %MAVEN%/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar || goto error
)

echo.
echo Listo. Librerias en lib\:
dir /b lib\*.jar
echo En NetBeans: Clean and Build (Shift+F11) y luego Run (F6).
pause
exit /b 0

:error
echo.
echo [ERROR] No se pudo descargar. Revise su conexion o descargue manualmente
echo         los .jar indicados en lib\LEEME.txt
pause
exit /b 1
