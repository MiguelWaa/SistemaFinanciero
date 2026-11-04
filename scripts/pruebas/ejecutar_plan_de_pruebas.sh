#!/bin/sh
# ============================================================
#  SIGSF · Ejecuta el PLAN DE PRUEBAS completo (Avance 4, v6.0.0)
#
#    PE  Preparación del entorno   base SistemaFinanciero_test desde cero
#    PU  Unitarias                 JUnit 4 + Mockito (+ JaCoCo con COBERTURA=1)
#    PI  Integración               JUnit + PostgreSQL real · Postman/Newman
#    PS  Sistema (E2E)             Playwright + pytest
#    PA  Aceptación (BDD)          Gherkin en español + pytest-bdd
#    PR  Rendimiento               Apache JMeter (si está instalado)
#
#  Uso:   PGPASSWORD=123456 sh scripts/pruebas/ejecutar_plan_de_pruebas.sh
#  Requisitos: JDK 17+, PostgreSQL 16 (psql en el PATH), Node.js (newman),
#              Python 3.11+ (pip install -r pruebas/requirements.txt;
#              playwright install chromium) y JMeter 5.6 (opcional).
#  Variables:  COBERTURA=1  PUERTO=8092  JMETER=jmeter  USUARIOS=50  DURACION=60
#  Reportes:   build/reportes/   ·   Capturas: evidencias/pruebas/
# ============================================================
set -e
cd "$(dirname "$0")/../.."
PUERTO="${PUERTO:-8092}"
export PGPASSWORD="${PGPASSWORD:-123456}" PGOPTIONS="--client-min-messages=warning"
export SIGSF_DB_NAME=SistemaFinanciero_test SIGSF_DB_PASSWORD="$PGPASSWORD"
REP=build/reportes
mkdir -p "$REP/newman" "$REP/jmeter" "$REP/pytest"
ESTADO=0

echo "==================== PE · Preparación del entorno ===================="
sh scripts/pruebas/preparar_bd_pruebas.sh

echo "==================== PU + PI · JUnit (unitarias e integración) ===================="
BD_PRUEBAS=SistemaFinanciero_test sh scripts/compilar_y_probar.sh || ESTADO=1

echo "==================== Servidor de pruebas en el puerto $PUERTO ===================="
CP="lib/postgresql.jar:lib/slf4j-api-2.0.16.jar:lib/logback-core-1.5.12.jar:lib/logback-classic-1.5.12.jar:build/ci/classes"
AGENTE=""
[ -n "$COBERTURA" ] && AGENTE="-javaagent:lib/test/jacocoagent.jar=destfile=$REP/jacoco-sistema.exec,dumponexit=true"
SIGSF_SERVER_PORT=$PUERTO SIGSF_BACKUP_AUTOMATICO_HORAS=0 java $AGENTE -cp "$CP" \
    com.utp.sistemafinanciero.ServerMain > "$REP/servidor_pruebas.log" 2>&1 &
SERVIDOR=$!
trap 'kill $SERVIDOR 2>/dev/null || true' EXIT
for i in $(seq 1 30); do
  curl -sf "http://localhost:$PUERTO/api/health" > /dev/null && break
  sleep 1
done
curl -sf "http://localhost:$PUERTO/api/health" || { echo "El servidor no arrancó"; exit 1; }
echo

echo "==================== PI · API con Postman / Newman ===================="
npx --yes -p newman -p newman-reporter-htmlextra newman run pruebas/postman/SIGSF_API.postman_collection.json \
    -e pruebas/postman/SIGSF_local.postman_environment.json --env-var "base=http://localhost:$PUERTO" \
    -r cli,htmlextra,junit --reporter-htmlextra-export "$REP/newman/newman_sigsf.html" \
    --reporter-junit-export "$REP/newman/newman_junit.xml" --color off | tee "$REP/newman/consola_newman.txt" || ESTADO=1

echo "==================== PS + PA · Playwright (sistema y aceptación) ===================="
(cd pruebas && SIGSF_URL="http://localhost:$PUERTO" python -m pytest -v \
    --junitxml="../$REP/pytest/pytest_junit.xml" --html="../$REP/pytest/reporte_e2e_aceptacion.html" \
    --self-contained-html) | tee "$REP/pytest/consola_pytest.txt" || ESTADO=1

echo "==================== PR · Rendimiento con JMeter ===================="
JMETER="${JMETER:-jmeter}"
if command -v "$JMETER" > /dev/null 2>&1; then
  rm -f "$REP/jmeter/resultados.jtl"
  (cd pruebas/rendimiento && "$JMETER" -n -t SIGSF_carga.jmx -l "../../$REP/jmeter/resultados.jtl" \
      -j "../../$REP/jmeter/jmeter.log" -Jport="$PUERTO" -Jusuarios="${USUARIOS:-50}" -Jduracion="${DURACION:-60}" \
      -Jjmeter.save.saveservice.output_format=csv -Jjmeter.save.saveservice.print_field_names=true) \
      | tee "$REP/jmeter/consola_jmeter.txt"
  python pruebas/rendimiento/analizar_jtl.py "$REP/jmeter/resultados.jtl" | tee "$REP/jmeter/criterios.txt" || ESTADO=1
else
  echo "JMeter no está instalado: se omite PR-01 (descárguelo de https://jmeter.apache.org)."
fi

kill $SERVIDOR 2>/dev/null || true
wait $SERVIDOR 2>/dev/null || true

if [ -n "$COBERTURA" ]; then
  echo "==================== Cobertura combinada (JUnit + sistema) ===================="
  java -jar lib/test/jacococli.jar merge "$REP/jacoco.exec" "$REP/jacoco-sistema.exec" --destfile "$REP/jacoco-total.exec"
  java -jar lib/test/jacococli.jar report "$REP/jacoco-total.exec" --classfiles build/ci/classes --sourcefiles src \
       --html "$REP/cobertura-total" --csv "$REP/cobertura-total.csv"
fi

echo
echo "Reportes en $REP/  ·  capturas en evidencias/pruebas/"
[ $ESTADO -eq 0 ] && echo "RESULTADO: PLAN DE PRUEBAS APROBADO" || echo "RESULTADO: HAY PRUEBAS FALLIDAS"
exit $ESTADO
