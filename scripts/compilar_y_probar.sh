#!/bin/sh
# ============================================================
#  SIGSF · Compila y ejecuta las pruebas JUnit sin NetBeans
#  Uso:  sh scripts/compilar_y_probar.sh
#  Lo usa también la integración continua (GitHub Actions).
#  Requiere JDK 17+. Descarga JUnit 4.13.2 y Hamcrest 1.3 a lib/test/
#  la primera vez (carpeta ignorada por Git).
# ============================================================
set -e
cd "$(dirname "$0")/.."
MAVEN=https://repo1.maven.org/maven2
mkdir -p lib/test
[ -f lib/test/junit-4.13.2.jar ]      || curl -sSfL -o lib/test/junit-4.13.2.jar      $MAVEN/junit/junit/4.13.2/junit-4.13.2.jar
[ -f lib/test/hamcrest-core-1.3.jar ] || curl -sSfL -o lib/test/hamcrest-core-1.3.jar $MAVEN/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar

CP="lib/postgresql.jar:lib/slf4j-api-2.0.16.jar:lib/logback-core-1.5.12.jar:lib/logback-classic-1.5.12.jar"
JUNIT="lib/test/junit-4.13.2.jar:lib/test/hamcrest-core-1.3.jar"
rm -rf build/ci && mkdir -p build/ci/classes build/ci/test

echo "== Compilando $(find src -name '*.java' | wc -l) clases..."
javac -encoding UTF-8 --release 17 -cp "$CP" -d build/ci/classes $(find src -name '*.java')
cp src/logback.xml build/ci/classes/

echo "== Compilando $(find test -name '*.java' | wc -l) clases de prueba..."
javac -encoding UTF-8 --release 17 -cp "$CP:build/ci/classes:$JUNIT" -d build/ci/test $(find test -name '*.java')
cp test/logback-test.xml build/ci/test/

PRUEBAS=$(cd test && find . -name '*Test.java' | sed 's|^\./||;s|\.java$||;s|/|.|g' | sort | tr '\n' ' ')
echo "== Ejecutando pruebas..."
java -cp "$CP:build/ci/test:build/ci/classes:$JUNIT" org.junit.runner.JUnitCore $PRUEBAS
