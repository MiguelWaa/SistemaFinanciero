# SIGSF · Pruebas automatizadas (TDD)

**Herramienta:** JUnit 4 (incluido en NetBeans). **Carpeta:** `test/`. **Resultado:** 117 pruebas en 24 clases (versión 5.0; eran 99 en 21 clases en la 4.0).

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
| LectorCsvTest | 4 | Importación CSV: separadores, comillas, Windows-1252, límites |
| EscrituraArchivosTest | 4 | Fórmulas neutralizadas, CSV con BOM, Excel ida y vuelta, XXE |
| ArchivoServiceTest | 7 | Importación por área: errores por fila, vista previa, mass assignment, operaciones no importables, exportación de las 9 áreas |
| ArchivosApiTest | 4 | Permisos 401/403 de ficheros, descarga, auditoría IMPORTAR |
| LogbackConfigTest | 3 | Configuración de Logback, anti log injection, área de cada ruta |
| VersionServiceTest | 12 | RF-VER-01…04: versión desde Git / version.properties / VERSION, esquema al día, tipos de commit, CHANGELOG, lista blanca de GitCli |
| GitCliIntegracionTest | 3 | Repositorio Git real temporal: rama, etiqueta, merge y grafo (se omite si no hay git) |
| VersionApiTest | 3 | `/api/version` para todo el personal, `/api/versiones` solo ADMIN, 401 sin sesión |
| **Total** | **117** | |
