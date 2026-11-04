# SIGSF · Pruebas automatizadas (TDD)

**Herramienta:** JUnit 4 (incluido en NetBeans) + Mockito 5. **Carpeta:** `test/`. **Resultado:** **216 pruebas en 35 clases** (versión 6.0; eran 126 en la 5.1, 117 en la 5.0 y 99 en la 4.0).

> Desde la versión 6.0 este documento es el detalle del nivel unitario/TDD. El plan completo (entorno, integración, sistema, aceptación y rendimiento) está en `docs/09_PLAN_DE_PRUEBAS.md` y los resultados en `docs/10_HERRAMIENTAS_Y_RESULTADOS.md`.

También sin NetBeans: `sh scripts/compilar_y_probar.sh` (o `.bat`); es lo que ejecuta la integración continua en cada push.

## Cómo ejecutarlas en NetBeans

1. Clic derecho en **Test Libraries → Add Library…** → **JUnit 4.13.2** y **Hamcrest 1.3** (si no aparecen ya).
2. Copie antes las librerías con `scripts\descargar_librerias.bat` (SLF4J/Logback).
3. Menú **Run → Test Project** (Alt + F6). Las pruebas **no necesitan PostgreSQL**: usan los DAO en memoria.

## Ciclo TDD aplicado

1. **Rojo:** se escribe la prueba a partir del requisito (p. ej. RF-OPE-02: "total a recibir = monto × tipo de cambio").
2. **Verde:** se implementa lo mínimo en el servicio.
3. **Refactor:** se mejora el diseño (inyección de dependencias, DTO) manteniendo las pruebas en verde.

La inyección de dependencias (DIP) es lo que permite el TDD: el servicio recibe un `PrestamoDao`, y en la prueba se le entrega `PrestamoDaoMemoria`.

## Suites

| Clase de prueba | Pruebas | Requisito que respalda |
|---|---|---|
| PrestamoServiceTest | 5 | RF-OPE-05: cuota con interés simple, vencimiento, redondeo |
| OperacionServiceTest | 4 | RF-OPE-01/02/03, RNF-OPE-01: cálculo en servidor, anti-manipulación |
| ClienteServiceTest | 5 | RF-CLI-03/04/06, RNF-CLI-01: documento único, bloqueo |
| UsuarioServiceTest | 6 | RF-SEG-02, RNF-GEN-03/04: PBKDF2, correo único, política de claves |
| AuthServiceTest | 6 | RF-SEG-01, RNF-SEG-01: login, bloqueo, migración de hash, logout |
| ProduccionServiceTest | 6 | RF-PRD-02/03, RNF-PRD-01/02/03 |
| SolicitudServiceTest | 4 | RF-SOL-01/02 |
| VentaYEmpenoServiceTest | 3 | RF-VNT-02, RNF-VNT-01, RF-OPE-06 |
| Pbkdf2PasswordHasherTest | 5 | RNF-GEN-03 |
| GestorSesionesTest | 5 | RNF-SEG-02 |
| LimitadorIntentosTest | 3 | Fuerza bruta |
| PoliticaAccesoTest | 7 | RBAC e IDOR |
| RespaldoServiceTest | 5 | Respaldo, retención, path traversal |
| RepositorioMemoriaTest | 3 | Contrato DAO |
| ApiIntegracionTest | 10 | Integración Vista↔API↔Servicio↔DAO, 401/403, cabeceras, auditoría |
| DatosMasivosTest | 9 | 100 registros por módulo, 300 operaciones, idempotencia, claves foráneas (versión 5.1) |
| LectorCsvTest | 4 | Importación CSV: separadores, comillas, Windows-1252, límites |
| EscrituraArchivosTest | 4 | Fórmulas neutralizadas, CSV con BOM, Excel ida y vuelta, XXE |
| ArchivoServiceTest | 7 | Importación por área: errores por fila, vista previa, mass assignment, operaciones no importables, exportación de las 9 áreas |
| ArchivosApiTest | 4 | Permisos 401/403 de ficheros, descarga, auditoría IMPORTAR |
| LogbackConfigTest | 3 | Configuración de Logback, anti log injection, área de cada ruta |
| VersionServiceTest | 12 | RF-VER-01…04: versión desde Git / version.properties / VERSION, esquema al día, tipos de commit, CHANGELOG, lista blanca de GitCli |
| GitCliIntegracionTest | 3 | Repositorio Git real temporal: rama, etiqueta, merge y grafo (se omite si no hay git) |
| VersionApiTest | 3 | `/api/version` para todo el personal, `/api/versiones` solo ADMIN, 401 sin sesión |
| PrestamoValoresLimiteTest | 19 | RF-OPE-05 · valores límite de monto, tasa y plazo (versión 6.0) |
| OperacionParticionesTest | 10 | RF-OPE-01/02 · particiones y estado inicial (6.0) |
| EmpenoValoresLimiteTest | 8 | RF-OPE-06 · valores límite (6.0) |
| PortalServiceTest | 9 | RF-POR-01…05 · sin operaciones huérfanas (6.0) |
| CatalogosServiceTest | 7 | Servicios de catálogo y panel (6.0) |
| ClienteServiceMockitoTest / AuthServiceMockitoTest | 7 / 5 | Interacción con Mockito (6.0) |
| ContratoClienteDao (memoria + JDBC) | 5 + 5 | Contrato del patrón DAO (6.0) |
| PostgresIntegracionTest / ApiPostgresIntegracionTest | 8 / 7 | Integración con PostgreSQL real (6.0) |
| **Total** | **216** | |
