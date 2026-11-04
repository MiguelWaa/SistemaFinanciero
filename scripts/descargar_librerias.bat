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

echo Descargando librerias de prueba (JUnit, Mockito, JaCoCo) en lib\test ...
if not exist lib\test mkdir lib\test
curl -fL -o lib\test\junit-4.13.2.jar %MAVEN%/junit/junit/4.13.2/junit-4.13.2.jar || goto error
curl -fL -o lib\test\hamcrest-core-1.3.jar %MAVEN%/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar || goto error
curl -fL -o lib\test\mockito-core.jar %MAVEN%/org/mockito/mockito-core/5.14.2/mockito-core-5.14.2.jar || goto error
curl -fL -o lib\test\byte-buddy.jar %MAVEN%/net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar || goto error
curl -fL -o lib\test\byte-buddy-agent.jar %MAVEN%/net/bytebuddy/byte-buddy-agent/1.15.10/byte-buddy-agent-1.15.10.jar || goto error
curl -fL -o lib\test\objenesis.jar %MAVEN%/org/objenesis/objenesis/3.3/objenesis-3.3.jar || goto error
curl -fL -o lib\test\jacocoagent.jar %MAVEN%/org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar || goto error
curl -fL -o lib\test\jacococli.jar %MAVEN%/org/jacoco/org.jacoco.cli/0.8.12/org.jacoco.cli-0.8.12-nodeps.jar || goto error

echo.
echo Listo. Librerias en lib\ y lib\test\:
dir /b lib\*.jar lib\test\*.jar
echo En NetBeans: Clean and Build (Shift+F11) y luego Run (F6).
pause
exit /b 0

:error
echo.
echo [ERROR] No se pudo descargar. Revise su conexion o descargue manualmente
echo         los .jar indicados en lib\LEEME.txt
pause
exit /b 1
