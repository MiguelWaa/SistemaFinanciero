
#!/bin/sh
# ============================================================
# SIGSF - Compila y ejecuta las pruebas JUnit sin NetBeans
# Uso: sh scripts/compilar_y_probar.sh
# Requiere JDK 17+ y curl.
# Compatible con Windows Git Bash y GitHub Actions (Ubuntu).
# ============================================================

set -e

cd "$(dirname "$0")/.."

MAVEN="https://repo1.maven.org/maven2"

# Descargar JUnit y Hamcrest si no existen
mkdir -p lib/test

[ -f lib/test/junit-4.13.2.jar ] || \
  curl -sSfL -o lib/test/junit-4.13.2.jar \
  "$MAVEN/junit/junit/4.13.2/junit-4.13.2.jar"

[ -f lib/test/hamcrest-core-1.3.jar ] || \
  curl -sSfL -o lib/test/hamcrest-core-1.3.jar \
  "$MAVEN/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"

# Separador de classpath según el sistema operativo
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SEP=';' ;;
  *) SEP=':' ;;
esac

# Bibliotecas de la aplicación
CP="lib/postgresql.jar${SEP}lib/slf4j-api-2.0.16.jar${SEP}lib/logback-core-1.5.12.jar${SEP}lib/logback-classic-1.5.12.jar"

# Bibliotecas de pruebas
JUNIT="lib/test/junit-4.13.2.jar${SEP}lib/test/hamcrest-core-1.3.jar"

# Preparar las carpetas de compilación
rm -rf build/ci
mkdir -p build/ci/classes build/ci/test

# Compilar las clases de la aplicación
echo "== Compilando clases de la aplicación =="

javac -encoding UTF-8 --release 17 \
  -cp "$CP" \
  -d build/ci/classes \
  $(find src -name '*.java')

# Copiar recursos necesarios de la aplicación
cp src/logback.xml build/ci/classes/

mkdir -p build/ci/classes/com/utp/sistemafinanciero/dao/memoria

cp src/com/utp/sistemafinanciero/dao/memoria/semilla_100.tsv \
  build/ci/classes/com/utp/sistemafinanciero/dao/memoria/

# Compilar las clases de prueba
echo "== Compilando clases de prueba =="

javac -encoding UTF-8 --release 17 \
  -cp "$CP${SEP}build/ci/classes${SEP}$JUNIT" \
  -d build/ci/test \
  $(find test -name '*.java')

# Copiar la configuración de Logback para las pruebas
cp test/logback-test.xml build/ci/test/

# Obtener los nombres completos de las clases de prueba
PRUEBAS=$(cd test && find . -name '*Test.java' |
  sed 's|^\./||;s|\.java$||;s|/|.|g' |
  sort | tr '\n' ' ')

if [ -z "$PRUEBAS" ]; then
  echo "ERROR: No se encontraron clases de prueba." >&2
  exit 1
fi

# Ejecutar JUnit
echo "== Ejecutando pruebas =="

java -cp "$CP${SEP}build/ci/test${SEP}build/ci/classes${SEP}$JUNIT" \
  org.junit.runner.JUnitCore $PRUEBAS

