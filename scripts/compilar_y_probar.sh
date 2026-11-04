#!/bin/sh
# ============================================================
#  SIGSF · Compila y ejecuta las pruebas JUnit sin NetBeans
#  Uso:  sh scripts/compilar_y_probar.sh            (unitarias + integración en memoria)
#        COBERTURA=1 sh scripts/compilar_y_probar.sh (además, informe JaCoCo)
#        BD_PRUEBAS=SistemaFinanciero_test sh scripts/compilar_y_probar.sh
#              (además, pruebas de integración con PostgreSQL real; ver
#               scripts/pruebas/preparar_bd_pruebas.sh)
#  Lo usa también la integración continua (GitHub Actions).
#  Requiere JDK 17+. Descarga a lib/test/ (carpeta ignorada por Git) la
#  primera vez: JUnit 4.13.2, Hamcrest 1.3, Mockito 5, Byte Buddy,
#  Objenesis y JaCoCo 0.8.12.
# ============================================================
set -e
cd "$(dirname "$0")/.."
MAVEN=https://repo1.maven.org/maven2
mkdir -p lib/test
bajar() { [ -f "lib/test/$1" ] || curl -sSfL -o "lib/test/$1" "$MAVEN/$2"; }
bajar junit-4.13.2.jar       junit/junit/4.13.2/junit-4.13.2.jar
bajar hamcrest-core-1.3.jar  org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
bajar mockito-core.jar       org/mockito/mockito-core/5.14.2/mockito-core-5.14.2.jar
bajar byte-buddy.jar         net/bytebuddy/byte-buddy/1.15.10/byte-buddy-1.15.10.jar
bajar byte-buddy-agent.jar   net/bytebuddy/byte-buddy-agent/1.15.10/byte-buddy-agent-1.15.10.jar
bajar objenesis.jar          org/objenesis/objenesis/3.3/objenesis-3.3.jar
if [ -n "$COBERTURA" ]; then
  bajar jacocoagent.jar      org/jacoco/org.jacoco.agent/0.8.12/org.jacoco.agent-0.8.12-runtime.jar
  bajar jacococli.jar        org/jacoco/org.jacoco.cli/0.8.12/org.jacoco.cli-0.8.12-nodeps.jar
fi

CP="lib/postgresql.jar:lib/slf4j-api-2.0.16.jar:lib/logback-core-1.5.12.jar:lib/logback-classic-1.5.12.jar"
JUNIT="lib/test/junit-4.13.2.jar:lib/test/hamcrest-core-1.3.jar:lib/test/mockito-core.jar:lib/test/byte-buddy.jar:lib/test/byte-buddy-agent.jar:lib/test/objenesis.jar"
rm -rf build/ci && mkdir -p build/ci/classes build/ci/test build/reportes

echo "== Compilando $(find src -name '*.java' | wc -l) clases..."
javac -encoding UTF-8 --release 17 -cp "$CP" -d build/ci/classes $(find src -name '*.java')
# Recursos que no son .java (logback.xml, semilla_100.tsv, ...). Antes solo se copiaba
# logback.xml y DatosMasivosTest fallaba en la integración continua (defecto DEF-001).
(cd src && find . -type f ! -name '*.java' -exec cp --parents {} ../build/ci/classes/ \;)

echo "== Compilando $(find test -name '*.java' | wc -l) clases de prueba..."
javac -encoding UTF-8 --release 17 -cp "$CP:build/ci/classes:$JUNIT" -d build/ci/test $(find test -name '*.java')
cp test/logback-test.xml build/ci/test/

PRUEBAS=$(cd test && find . -name '*Test.java' | sed 's|^\./||;s|\.java$||;s|/|.|g' | sort | tr '\n' ' ')
JVM="-Dstdout.encoding=UTF-8 -Dsigsf.db.name=${BD_PRUEBAS:-sin_base_de_pruebas} -Dsigsf.db.password=${PGPASSWORD:-123456}"
[ -n "$COBERTURA" ] && JVM="$JVM -javaagent:lib/test/jacocoagent.jar=destfile=build/reportes/jacoco.exec"

echo "== Ejecutando pruebas..."
set +e
# EjecutorPlanDePruebas = JUnitCore + resumen por nivel y CSV por caso (build/reportes/)
java $JVM -cp "$CP:build/ci/test:build/ci/classes:$JUNIT" com.utp.sistemafinanciero.EjecutorPlanDePruebas $PRUEBAS > build/reportes/junit.txt 2>&1
RESULTADO=$?
grep -v "^Picked up" build/reportes/junit.txt
set -e

if [ -n "$COBERTURA" ]; then
  echo "== Informe de cobertura JaCoCo en build/reportes/cobertura/index.html"
  java -jar lib/test/jacococli.jar report build/reportes/jacoco.exec \
       --classfiles build/ci/classes --sourcefiles src \
       --html build/reportes/cobertura --csv build/reportes/cobertura.csv --xml build/reportes/cobertura.xml
fi
exit $RESULTADO
