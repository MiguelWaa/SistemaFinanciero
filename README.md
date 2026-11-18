# SIGSF 5.11.0 - Proyecto integrado

Sistema Integral de Gestión de Servicios Financieros. Curso Integrador I Sistemas - Software, UTP.

Integrantes: Miguel Ángel Machaca Machaca; Ricardo Gabriel Cervantes Tejada; Cesar Huaripaucar Huaman.
Docente: Rene Alonso Nieto Valencia.

## Despliegue 5.11.0

Plan completo: [docs/19_PLAN_DE_DESPLIEGUE.md](docs/19_PLAN_DE_DESPLIEGUE.md). Evidencias: `evidencias/v5.11.0/`.

```sh
python scripts/pipeline_local.py                 # mismas etapas que el CD: JUnit, paquete, Docker, staging
docker compose --env-file deploy/sigsf.env up -d # producción en contenedores (app + PostgreSQL)
sh scripts/desplegar.sh release/sigsf-5.11.0.tar.gz --destino /opt/sigsf --systemd
sh scripts/rollback.sh --destino /opt/sigsf      # volver a la versión anterior
```

CD: `git push origin v5.11.0` ejecuta `.github/workflows/cd.yml` (JAR, imagen GHCR, Release y despliegue SSH).
Resultados 18-11-2026: 229 JUnit, 7 JDBC en PostgreSQL 16, smoke 9/9 en contenedor y staging, reversión automática y manual comprobadas.
Presentación del taller: `docs/presentacion/SIGSF_Despliegue_5min.pptx` y `.pdf`.

## Entrada automática 5.10.0

El video de SIGSF ocupa toda la ventana, sin recuadro ni botones permanentes. Al terminar aparece la portada bancaria.

En Windows, cierre la consola anterior y haga doble clic en **INICIAR_DEMO.bat**: inicia Java y abre Chrome a pantalla completa con sonido automático habilitado en un perfil separado de demostración. Requiere Java 17+ y Google Chrome. **F11** sale de pantalla completa; al terminar, cierre esa ventana y pulse Enter en la consola.

Guía: [docs/18_ENTRADA_AUTOMATICA.md](docs/18_ENTRADA_AUTOMATICA.md). La ejecución del iniciador en Windows y la escucha real están pendientes de validación en ese equipo; aquí se verificaron el flujo simulado y los recursos servidos.

## Abrir primero

- `DOCUMENTACION_INTEGRAL.html`: índice y desarrollo de todos los requisitos.
- `docs/presentacion/SIGSF_Integrado_5min.pptx` y `.pdf`: 7 diapositivas para 5 minutos y 12 anexos.
- `INICIAR_DEMO.bat` (Windows) o `sh INICIAR_DEMO.sh` (Linux/macOS): aplicación con datos en memoria.

## Ejecutar la aplicación

Instale Java 17 o superior. Descomprima todo el proyecto y ejecute el iniciador desde esa carpeta.
El iniciador Windows abre Chrome automáticamente; la dirección es http://localhost:8091/. Cuenta de demostración: admin@sigsf.com / admin123.
Los datos de la demostración se pierden al cerrar. Para desarrollar, abra el proyecto en NetBeans.
La configuración PostgreSQL usa `db.properties.ejemplo` y `db.properties`; el iniciador demo fuerza memoria sin editarlos.

## Pruebas reproducibles

Requisitos adicionales: JDK completo 17+, Python 3.10+ y Node.js 20+.
Los JAR están incluidos en el ZIP. En un clon sin JAR: `python scripts/preparar_dependencias.py`.

```sh
cd pruebas/e2e
npm ci --no-audit --no-fund
npx playwright install chromium
cd ../..
python scripts/validar_proyecto.py
```

Solo Java: `python scripts/validar_proyecto.py --solo-junit`.
Sin navegador: `python scripts/validar_proyecto.py --omitir-e2e` (queda NO EJECUTADO).
Cada ejecución genera una carpeta fechada en `pruebas/reportes/`, con registros y resultado.json.
Empaquetar después de validar: `python scripts/empaquetar.py`.

## Implementación de seguridad

- JSON completo, claves únicas, números válidos, controles escapados y profundidad máxima de 64 niveles.
- Content-Type application/json, UTF-8 estricto y respuesta 415 para tipos no admitidos.
- CSP con script-src 'self', script-src-attr 'none' y object-src 'none'.
- 26 scripts trasladados a `web/pages/`; ayuda utiliza eventos explícitos.
- 22 pruebas JUnit nuevas y una nueva prueba E2E, junto a las suites recibidas.
- Ejecutores para PostgreSQL, ZAP e inventario/SCA en `scripts/`.

## Resultados de esta entrega

5.10.0: 31/31 pruebas del flujo con DOM simulado, 11/11 JUnit selectivas de CSP y recursos multimedia, y sintaxis de scripts aprobadas. Informes: `evidencias/v5.10.0/`. Iniciador Windows y escucha en navegador real pendientes de ejecución en Windows.

Se conserva como antecedente la regresión completa de 5.9.0: 224 JUnit, 63 aserciones API y 24 comprobaciones adicionales. El backend no cambió. Sus clases fueron compiladas con Java 21.0.13 y -source 17 -target 17; el modo normal y CI usan --release 17 con JDK completo.

Pendientes: 14 E2E en Chromium, 7 pruebas JDBC, ZAP, análisis de JAR/frontend y corrección de las alertas npm.
No se atribuyen los 13 E2E históricos a esta versión. El inventario local de dependencias no calcula CVE.
Auditoría npm autorizada y ejecutada: **21 paquetes señalados (1 crítico, 13 altos y 7 moderados)**.
Umbral high: NO CUMPLE. Son dependencias de herramientas de pruebas; la explotación en SIGSF no está demostrada.
Informe y plan de corrección: `docs/14_AUDITORIA_NPM.md`. No se modificó el lock.

## Base de datos

Use una BD nueva para instalar. `db/schema.sql` contiene DROP TABLE y reinicializa tablas.
Orden: schema.sql, datos_ejemplo.sql, portal_cliente.sql, seguridad.sql, version_esquema.sql, pagos_qr.sql.
Para pruebas aisladas, configure SIGSF_DB_PASSWORD y ejecute `python scripts/probar_postgresql.py`:
crea una BD nueva sigsf_test_ y conserva el resultado para inspección.
No ejecuta cambios sobre una base existente.

## Versionamiento

La copia conserva el historial, ramas y remoto declarado. Integración local en
feature/integracion-seguridad-5.6.0, merge en release/5.6.0 y etiqueta v5.6.0.
Actualización de interfaz en feature/interfaz-grafito-audio, integrada en release/5.7.0 y etiqueta v5.7.0.
Portada bancaria integrada en release/5.8.0 y etiqueta v5.8.0.
Presentación audiovisual integrada en release/5.9.0 y etiqueta v5.9.0.
Entrada automática integrada en release/5.10.0 y etiqueta v5.10.0.
No se publica en GitHub. Para activar los hooks tras clonar: scripts/git/instalar_hooks.sh o .bat.
Los documentos originales se conservan como histórico donde corresponde.
