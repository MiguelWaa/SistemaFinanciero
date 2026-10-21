# Registro de cambios · SIGSF

Todos los cambios importantes del proyecto se anotan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y numeración [SemVer](https://semver.org/lang/es/): `MAYOR.MENOR.PARCHE`.

- **MAYOR**: cambios que rompen compatibilidad (API, esquema de BD).
- **MENOR**: funcionalidad nueva compatible.
- **PARCHE**: correcciones sin funcionalidad nueva.

## [Sin publicar]

### Agregado
- Plantilla `db.properties.ejemplo`; `db.properties` sale del repositorio (credenciales).
- Hooks Git `pre-commit` (bloquea credenciales y archivos generados) y `commit-msg`.
- Servicio de versión (`VersionService`) y API `GET /api/version`, `GET /api/versiones` (solo ADMIN).
- Versionamiento del esquema de la base: tabla `version_esquema` y `db/version_esquema.sql`.
- `version.properties` generado por Ant al compilar (commit, rama, etiqueta).
- Pantalla `versiones.html` (resumen, historial, red de ramas, etiquetas, esquema BD) y versión en el pie del panel.
- Integración continua (GitHub Actions) y `scripts/compilar_y_probar.(sh|bat)`.

### Corregido
- Ramas/etiquetas vacías en la vista de versiones (`for-each-ref` usa `%1f`) y referencias mal detectadas en el grafo.

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
