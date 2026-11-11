# Plan de pruebas de seguridad 5.6.0

Objetivo: verificar controles del SIGSF mediante ensayos repetibles y evidencia trazable.

Alcance ejecutado: HTTP local, datos de demostración y DAO en memoria. La validación con PostgreSQL, navegador y herramientas externas conserva su estado pendiente.

| ID | Área | Entrada / preparación | Criterio | Código / herramienta | Resultado |
|---|---|---|---|---|---|
| PS-01 | Autenticación / sesión | Token inválido y cierre de sesión | 401 y sesión revocada | SeguridadOwaspApiTest / Newman | Aprobado en suites actuales |
| PS-02 | Autorización / IDOR | Cambiar rol o id de cliente | 403 o rechazo de regla | SeguridadOwaspApiTest / Newman | Aprobado |
| PS-03 | Fuerza bruta | 5 claves erróneas | Bloqueo temporal | SeguridadOwaspApiTest | Aprobado |
| PS-04 | SQL injection | Carga SQL en login y consultas parametrizadas | Sin acceso y sin ejecutar SQL de entrada | JUnit memoria / PostgresqlSeguridadIT | Memoria aprobada; JDBC pendiente |
| PS-05 | XSS | HTML en nombre y DOM sin ejecución | Texto literal | Playwright | Histórico; repetir en 5.6.0 |
| PS-06 | Path traversal | Rutas ../ y codificadas | No exponer archivos | SeguridadOwaspApiTest | Aprobado |
| PS-07 | Operaciones financieras | Monto negativo, exceso y tasa manipulada | Rechazo o tasa del servidor | JUnit / Newman | Aprobado en memoria |
| PS-08 | Límites | Cuerpo > 1 MiB y JSON > 64 niveles | Respuesta controlada 400 | JUnit | Aprobado |
| PS-09 | Fuzzing acotado | 20 entradas de sintaxis, tipo y valor | 4xx sin token ni caída del servicio | comprobar_entradas.py | 20/20 aprobadas |
| PS-10 | JSON completo | JSON válido con texto sobrante | 400 sin token | JsonEstrictoTest / API / Python | Corregido y aprobado |
| PS-11 | Deserialización Java | Bytes AC ED 00 05 70 con tipo Java | 415; no deserializar objetos | SeguridadEntradasApiTest / Python | Aprobado; prueba acotada |
| PS-12 | Metadatos de tipo | Campo @class | Solo Map/List, sin instanciación por metadatos | JsonEstrictoTest / revisión | Aprobado dentro del alcance |
| PS-13 | Content-Type / UTF-8 | text/plain, charset no permitido y bytes inválidos | 415 para tipo/charset, 400 para UTF-8 inválido | SeguridadEntradasApiTest | Corregido y aprobado |
| PS-14 | CSP / cabeceras | Scripts externos, eventos y cabeceras | Scripts solo del propio origen | FrontendCspTest / API | Estructura aprobada; navegador pendiente |
| PS-15 | Importación / exportación | XLSX/DTD, ZIP y fórmulas CSV | Rechazar o neutralizar según control | Suites archivos/ | Incluido en JUnit aprobado |
| PS-16 | Vulnerabilidades de componentes | CVE aplicables en JAR y npm | Informe actualizado y triage | Dependency-Check / npm audit | npm: 21 paquetes señalados; corrección y JAR pendientes |
| PS-17 | Penetración ampliada | ZAP pasivo y activo autenticado | Informe, cobertura, hallazgos y retest | escanear_zap.py | Implementado; Docker no disponible |
| PS-18 | Configuración y persistencia | FK, CHECK, unicidad, rollback y permisos | Restricciones y mínimo privilegio | PostgresqlSeguridadIT | 7 casos compilados; ejecución pendiente |

Entrada: versión identificada, datos sintéticos y dependencias disponibles.
Salida: controles críticos aprobados, hallazgos triados y retest, informes DAST/SCA/BD y aceptación documentada.
Responsabilidades propuestas: Miguel, integración; Ricardo, backend/API; Cesar, interfaz/evidencia. Estas asignaciones requieren validación del equipo.

La deserialización probada es acotada: el parser construye Map/List y no interpreta @class. No es una prueba universal de ausencia de ejecución remota.
El tipo de contenido Java se rechaza con 415 desde esta versión; antes llegaba al parser y daba 400.
La prueba SQLi en memoria no demuestra resistencia del motor PostgreSQL.
CSP mantiene estilos inline para la interfaz existente; elimina la autorización de scripts inline.
TLS, restauración de respaldos y concurrencia real de pagos requieren pruebas adicionales de aceptación antes de producción.
