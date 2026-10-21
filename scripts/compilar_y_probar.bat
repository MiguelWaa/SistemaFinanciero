@echo off
REM ============================================================
REM  SIGSF - Compila y ejecuta las pruebas JUnit sin NetBeans (Windows)
REM  Uso: doble clic o  scripts\compilar_y_probar.bat
REM  Requiere JDK 17+ en el PATH. Descarga JUnit a lib\test\ la primera vez.
REM ============================================================
setlocal enabledelayedexpansion
cd /d "%~dp0.."
set MAVEN=https://repo1.maven.org/maven2
if not exist lib\test mkdir lib\test
if not exist lib\test\junit-4.13.2.jar curl -sSfL -o lib\test\junit-4.13.2.jar %MAVEN%/junit/junit/4.13.2/junit-4.13.2.jar
if not exist lib\test\hamcrest-core-1.3.jar curl -sSfL -o lib\test\hamcrest-core-1.3.jar %MAVEN%/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar

set CP=lib\postgresql.jar;lib\slf4j-api-2.0.16.jar;lib\logback-core-1.5.12.jar;lib\logback-classic-1.5.12.jar
set JUNIT=lib\test\junit-4.13.2.jar;lib\test\hamcrest-core-1.3.jar
if exist build\ci rmdir /s /q build\ci
mkdir build\ci\classes build\ci\test

dir /s /b src\*.java > build\ci\fuentes.txt
dir /s /b test\*.java > build\ci\pruebas.txt
echo == Compilando...
javac -encoding UTF-8 --release 17 -cp "%CP%" -d build\ci\classes @build\ci\fuentes.txt || goto :error
copy /y src\logback.xml build\ci\classes\ >nul
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
echo == Ejecutando pruebas...
java -cp "%CP%;build\ci\test;build\ci\classes;%JUNIT%" org.junit.runner.JUnitCore %PRUEBAS%
pause
exit /b 0
:error
echo Error de compilacion.
pause
exit /b 1
