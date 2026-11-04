# Registro de cambios · SIGSF

Todos los cambios importantes del proyecto se anotan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y numeración [SemVer](https://semver.org/lang/es/): `MAYOR.MENOR.PARCHE`.

- **MAYOR**: cambios que rompen compatibilidad (API, esquema de BD).
- **MENOR**: funcionalidad nueva compatible.
- **PARCHE**: correcciones sin funcionalidad nueva.

## [Sin publicar]

## [6.0.0] - 2026-11-03 · Avance 4: Plan de Pruebas de Software

Versión MAYOR: cambian reglas de negocio visibles para el usuario (límites de monto,
tasa y plazo; estados iniciales validados) y el contrato de la API (códigos 400 donde
antes había 500 o 409).

### Agregado
- **Plan de pruebas** `docs/09_PLAN_DE_PRUEBAS.md` (IEEE 829 / ISO 29119-3) y **resultados** `docs/10_HERRAMIENTAS_Y_RESULTADOS.md`.
- Pruebas unitarias parametrizadas (valores límite y particiones): `PrestamoValoresLimiteTest`, `OperacionParticionesTest`, `EmpenoValoresLimiteTest`; `PortalServiceTest`, `CatalogosServiceTest`.
- Pruebas con **Mockito**: `ClienteServiceMockitoTest`, `AuthServiceMockitoTest`.
- Integración con **PostgreSQL real**: `PostgresIntegracionTest`, `ApiPostgresIntegracionTest`, contrato DAO memoria ↔ JDBC (`ContratoClienteDao`); guardia `BdPruebas` (solo bases `_test`).
- Colección **Postman/Newman** (31 casos), pruebas de sistema con **Playwright** (13), aceptación **BDD en español** (4 historias, 10 escenarios) y plan de carga **JMeter**.
- `EjecutorPlanDePruebas` (resumen por nivel y CSV por caso), cobertura **JaCoCo** (`COBERTURA=1`).
- `scripts/pruebas/preparar_bd_pruebas.*` y `scripts/pruebas/ejecutar_plan_de_pruebas.*`.
- CI con dos trabajos: JUnit + PostgreSQL de servicio, y API/sistema/aceptación/carga.
- `Configuracion` acepta propiedades `-Dsigsf.<clave>` (NetBeans: `test-sys-prop.sigsf.db.name`).

### Corregido
- DEF-002/011: reglas de la base (tasa ≥ 0, compra/venta > 0, stock ≥ 0) replicadas en los servicios (`ReglasMonto`).
- DEF-003: montos y tasas fuera de rango ya no producen HTTP 500; `22003` → 400.
- DEF-004/006: límites de préstamo (S/ 100 a S/ 1 000 000, tasa 0–100 %, 1–360 meses) y de empeño (1–365 días).
- DEF-005: estado inicial validado en operaciones y clientes.
- DEF-007: el Portal valida el préstamo/empeño **antes** de grabar la operación (no más operaciones huérfanas).
- DEF-008: mensajes "Valor numérico inválido" y "registro que no existe" (400) en lugar de mensajes engañosos.
- DEF-009: títulos de tabla "Registros de registrar …"; DEF-010: historial del portal desbordado en celulares.

### Pruebas
- JUnit 126 → **216**; total del plan **270 casos**; cobertura de líneas 61.2 % → 70.3 % (JUnit) y 75.4 % (con sistema).

## [5.1.1] - 2026-11-03 · Parche

### Corregido
- DEF-001: `scripts/compilar_y_probar.*` no copiaba los recursos (`semilla_100.tsv`) y 9 pruebas fallaban en la integración continua.

## [5.1.0] - 2026-10-25 · Avance 3: datos de demostración

### Agregado
- `db/datos_100_registros.sql`: completa **100 registros por módulo** (300 operaciones = 100 cambios + 100 préstamos + 100 empeños) en las 32 tablas. Es re-ejecutable (no duplica), no borra datos y resuelve las claves foráneas por clave natural.
- `DatosMasivos` + `semilla_100.tsv`: los mismos datos en el modo demostración (`persistencia=memoria`); se desactiva con `memoria.datos.masivos=false`.
- `scripts/datos/generar_datos_100.py`: generador determinista (semilla fija) que produce el SQL y el TSV.
- `DatosMasivosTest` (9 pruebas): 100 por módulo, 300 operaciones, idempotencia, claves foráneas, avance 100 % y acceso al portal.
- `docs/08_DATOS_DE_DEMOSTRACION.md` y capturas de los módulos con 100 registros en `evidencias/`.

### Cambiado
- Guías de instalación (`db/LEEME.txt`, `INSTALAR_LEEME.txt`) con el paso opcional de los 100 registros. Pruebas: 117 → 126.

## [5.0.1] - 2026-10-21 · Parche

### Corregido
- Nombre de la base inconsistente: README y `db/LEEME.txt` decían `sistema_financiero`; el sistema usa `SistemaFinanciero`.
- Puerto por defecto 8080 en código y README; el sistema usa 8091 (`server.port`).

## [5.0.0] - 2026-10-21 · Taller de control de versiones

Versión MAYOR: cambia el contrato `DaoFactory` (nuevo `versionesEsquema()`), el
esquema de la base (tabla `version_esquema`) y la forma de configurar la conexión
(`db.properties` ya no viene en el repositorio).

### Agregado
- Plantilla `db.properties.ejemplo`; `db.properties` sale del repositorio (credenciales).
- Hooks Git `pre-commit` (bloquea credenciales y archivos generados) y `commit-msg`.
- Servicio de versión (`VersionService`) y API `GET /api/version`, `GET /api/versiones` (solo ADMIN).
- Versionamiento del esquema de la base: tabla `version_esquema` y `db/version_esquema.sql`.
- `version.properties` generado por Ant al compilar (commit, rama, etiqueta).
- Pantalla `versiones.html` (resumen, historial, red de ramas, etiquetas, esquema BD) y versión en el pie del panel.
- Integración continua (GitHub Actions) y `scripts/compilar_y_probar.(sh|bat)`.
- `docs/07_CONTROL_DE_VERSIONES.md`, inventario de documentación v5.0 y `CONTRIBUTING.md`.

### Corregido
- Ramas/etiquetas vacías en la vista de versiones (`for-each-ref` usa `%1f`) y referencias mal detectadas en el grafo.

### Revertido
- Truncamiento de la cuota mensual de préstamos (`RoundingMode.DOWN`): rompía `PrestamoServiceTest` y descuadraba el saldo. Se revirtió el merge con `git revert -m 1`.

## [4.0.0] - Taller de recursos de código fuente

### Agregado
- Registro con SLF4J + Logback (log general, acceso, seguridad, errores y uno por área).
- Exportación Excel/CSV/JSON en 15 recursos e importación CSV/Excel con vista previa.
- Seguridad Java en ficheros: lector XLSX sin XXE ni bomba ZIP, neutralización de fórmulas.
- 99 pruebas JUnit 4 y `docs/06_RECURSOS_DE_CODIGO_FUENTE.md`.

## [3.0.0] - Documentación del proyecto
- Requisitos y estado por área (PDF del proyecto); corrección del nombre oficial SIGSF.

## [2.0.0] - Taller de arquitectura
- Arquitectura MVC + DAO + DTO + SOLID, seguridad (PBKDF2, tokens, roles), respaldos y TDD.

## [1.0.0] - Avance 2
- Requisitos, casos de uso, prototipos UX/UI y modelo de datos (31 tablas).
