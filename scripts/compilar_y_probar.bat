@echo off
REM ============================================================
REM  SIGSF - Compila y ejecuta las pruebas JUnit sin NetBeans (Windows)
REM  Uso: doble clic o  scripts\compilar_y_probar.bat
REM    set COBERTURA=1                          ademas, informe JaCoCo
REM    set BD_PRUEBAS=SistemaFinanciero_test    ademas, integracion con PostgreSQL real
REM  Requiere JDK 17+ en el PATH. Descarga a lib\test\ la primera vez:
REM  JUnit 4.13.2, Hamcrest 1.3, Mockito 5, Byte Buddy, Objenesis y JaCoCo 0.8.12.
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0.."
set MAVEN=https://repo1.maven.org/maven2
if not exist lib\test mkdir lib\test
call :bajar junit-4.13.2.jar      junit/junit/4.13.2/junit-4.13.2.jar
call :bajar hamcrest-core-1.3.jar org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
call :bajar mockito-core.jar      org/mockito/mockito-core/5.14.2/mockito-core-5.14.2.jar
call :bajar byte-buddy.jar        net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar
call :bajar byte-buddy-agent.jar  net/bytebuddy/byte-buddy-agent/1.15.10/byte-buddy-agent-1.15.10.jar
call :bajar objenesis.jar         org/objenesis/objenesis/3.3/objenesis-3.3.jar
if not "%COBERTURA%"=="" (
  call :bajar jacocoagent.jar     org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar
  call :bajar jacococli.jar       org/jacoco/org.jacoco.cli/0.8.12/org.jacoco.cli-0.8.12-nodeps.jar
)

set CP=lib\postgresql.jar;lib\slf4j-api-2.0.16.jar;lib\logback-core-1.5.12.jar;lib\logback-classic-1.5.12.jar
set JUNIT=lib\test\junit-4.13.2.jar;lib\test\hamcrest-core-1.3.jar;lib\test\mockito-core.jar;lib\test\byte-buddy.jar;lib\test\byte-buddy-agent.jar;lib\test\objenesis.jar
if exist build\ci rmdir /s /q build\ci
mkdir build\ci\classes build\ci\test
if not exist build\reportes mkdir build\reportes

dir /s /b src\*.java > build\ci\fuentes.txt
dir /s /b test\*.java > build\ci\pruebas.txt
echo == Compilando...
javac -encoding UTF-8 --release 17 -cp "%CP%" -d build\ci\classes @build\ci\fuentes.txt || goto :error
REM Recursos que no son .java (logback.xml, semilla_100.tsv...). Corrige DEF-001.
robocopy src build\ci\classes /e /xf *.java /njh /njs /nfl /ndl >nul
javac -encoding UTF-8 --release 17 -cp "%CP%;build\ci\classes;%JUNIT%" -d build\ci\test @build\ci\pruebas.txt || goto :error
copy /y test\logback-test.xml build\ci\test\ >nul

set PRUEBAS=
for /r test %%f in (*Test.java) do (
  set "r=%%f"
  set "r=!r:%CD%\test\=!"
  set "r=!r:.java=!"
  set "r=!r:\=.!"
  set PRUEBAS=!PRUEBAS! !r!
)
if "%BD_PRUEBAS%"=="" set BD_PRUEBAS=sin_base_de_pruebas
if "%PGPASSWORD%"=="" set PGPASSWORD=123456
set JVM=-Dstdout.encoding=UTF-8 -Dsigsf.db.name=%BD_PRUEBAS% -Dsigsf.db.password=%PGPASSWORD%
if not "%COBERTURA%"=="" set JVM=%JVM% -javaagent:lib\test\jacocoagent.jar=destfile=build\reportes\jacoco.exec
echo == Ejecutando pruebas...
java %JVM% -cp "%CP%;build\ci\test;build\ci\classes;%JUNIT%" com.utp.sistemafinanciero.EjecutorPlanDePruebas %PRUEBAS%
set RESULTADO=%errorlevel%
if not "%COBERTURA%"=="" (
  java -jar lib\test\jacococli.jar report build\reportes\jacoco.exec --classfiles build\ci\classes --sourcefiles src --html build\reportes\cobertura --csv build\reportes\cobertura.csv
  echo Informe de cobertura: build\reportes\cobertura\index.html
)
if not "%1"=="nopause" pause
exit /b %RESULTADO%

:bajar
if not exist lib\test\%1 curl -sSfL -o lib\test\%1 %MAVEN%/%2
exit /b 0

:error
echo Error de compilacion.
if not "%1"=="nopause" pause
exit /b 1
