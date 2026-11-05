#!/bin/sh
# ============================================================
#  SIGSF · Plan de pruebas v5.5 · Ejecuta TODOS los niveles
#    1) Unitarias + integración + sistema + aceptación + seguridad + rendimiento (JUnit 4)
#    2) Pruebas de API (Postman / Newman)
#    3) Pruebas de sistema en navegador (Playwright)
#  Uso:  sh pruebas/ejecutar_plan_pruebas.sh
#  Requisitos: JDK 17+, Node.js 18+ (y una vez: cd pruebas/e2e && npm install && npx playwright install chromium)
#  Resultados en pruebas/reportes/ (no se versiona).
# ============================================================
cd "$(dirname "$0")/.."
RAIZ=$(pwd)
REP="$RAIZ/pruebas/reportes"
PUERTO=${SIGSF_SERVER_PORT:-8091}
rm -rf "$REP" && mkdir -p "$REP/junit" "$REP/newman" "$REP/capturas" "$REP/playwright"
RESUMEN="$REP/RESUMEN_PLAN_PRUEBAS.txt"
echo "SIGSF · Resultado del plan de pruebas · $(date '+%Y-%m-%d %H:%M')" > "$RESUMEN"
echo "Versión: $(cat VERSION) · commit $(git rev-parse --short HEAD 2>/dev/null || echo '-')" >> "$RESUMEN"
echo "Java: $(java -version 2>&1 | grep -i version | head -1)" >> "$RESUMEN"
echo "" >> "$RESUMEN"

echo "== [1/3] JUnit: unitarias, integración, sistema, aceptación, seguridad y rendimiento"
sh scripts/compilar_y_probar.sh > "$REP/junit/salida_junit.txt" 2>&1
J=$?
grep -E "^OK \(|^Tests run|RENDIMIENTO" "$REP/junit/salida_junit.txt" | sed 's/^/  /' | tee -a "$RESUMEN"
[ $J -eq 0 ] && echo "  JUnit: CORRECTO" | tee -a "$RESUMEN" || echo "  JUnit: CON FALLAS (ver junit/salida_junit.txt)" | tee -a "$RESUMEN"

echo "== Levantando el SIGSF en modo memoria (puerto $PUERTO)"
CP="lib/postgresql.jar:lib/slf4j-api-2.0.16.jar:lib/logback-core-1.5.12.jar:lib/logback-classic-1.5.12.jar:build/ci/classes"
SIGSF_PERSISTENCIA=memoria SIGSF_SERVER_PORT=$PUERTO SIGSF_BACKUP_AUTOMATICO_HORAS=0 SIGSF_BACKUP_DIR="$REP/respaldos" \
  java -cp "$CP" com.utp.sistemafinanciero.ServerMain > "$REP/servidor.log" 2>&1 &
PID=$!
trap 'kill $PID 2>/dev/null' EXIT
for i in $(seq 1 60); do
  curl -sf "http://localhost:$PUERTO/api/health" > /dev/null 2>&1 && break
  sleep 1
done

echo "== [2/3] Postman / Newman: pruebas de API"
cd pruebas/e2e
[ -d node_modules ] || npm install --no-audit --no-fund > "$REP/npm_install.txt" 2>&1
npx newman run ../postman/SIGSF_API.postman_collection.json -e ../postman/SIGSF_local.postman_environment.json \
  --env-var "baseUrl=http://localhost:$PUERTO" -r cli,htmlextra,junit \
  --reporter-htmlextra-export "$REP/newman/reporte-api.html" --reporter-htmlextra-title "SIGSF · Pruebas de API" \
  --reporter-junit-export "$REP/newman/junit-api.xml" > "$REP/newman/salida_newman.txt" 2>&1
N=$?
grep -E "requests|assertions" "$REP/newman/salida_newman.txt" | sed 's/^/  /' | tee -a "$RESUMEN"
[ $N -eq 0 ] && echo "  Newman: CORRECTO" | tee -a "$RESUMEN" || echo "  Newman: CON FALLAS" | tee -a "$RESUMEN"

echo "== [3/3] Playwright: pruebas de sistema en el navegador"
SIGSF_URL="http://localhost:$PUERTO" npx playwright test > "$REP/salida_playwright.txt" 2>&1
P=$?
grep -E "passed|failed|flaky|skipped" "$REP/salida_playwright.txt" | tail -3 | sed 's/^/  /' | tee -a "$RESUMEN"
[ $P -eq 0 ] && echo "  Playwright: CORRECTO" | tee -a "$RESUMEN" || echo "  Playwright: CON FALLAS" | tee -a "$RESUMEN"
cd "$RAIZ"

echo ""
echo "Resumen: $RESUMEN"
echo "Reportes: pruebas/reportes/newman/reporte-api.html · pruebas/reportes/playwright/index.html"
[ $J -eq 0 ] && [ $N -eq 0 ] && [ $P -eq 0 ]
