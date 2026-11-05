# SIGSF · Plan de Pruebas de Software (v5.5)

**Proyecto:** Sistema Integral de Gestión de Servicios Financieros (SIGSF) · Curso Integrador I Sistemas - Software (UTP)
**Integrantes:** Miguel Ángel Machaca Machaca · Ricardo Gabriel Cervantes Tejada · Cesar Huaripaucar Huaman
**Docente:** Rene Alonso Nieto Valencia · **Versión del plan:** 1.0 (software 5.5.0) · **Estructura:** ISO/IEC/IEEE 29119-3 (antes IEEE 829)

Documento hermano: `docs/11_HERRAMIENTAS_Y_RESULTADOS_DE_PRUEBAS.md` (herramientas, codificación y resultados obtenidos).

---

## 1. Objetivo

Comprobar, con evidencia repetible, que el SIGSF cumple sus requisitos funcionales (RF) y no funcionales (RNF) en
todas sus capas —frontend, API REST, servicios, DAO y base de datos— antes de liberar la versión, y dejar las pruebas
automatizadas para que cada cambio futuro se valide solo (regresión en la integración continua).

## 2. Alcance

| Dentro del alcance | Fuera del alcance |
|---|---|
| 12 módulos: Seguridad y usuarios, Clientes y servicios, Operaciones (cambio, préstamo, empeño), Solicitudes, Portal del Cliente, Pagos QR (Yape/Plin/Tunki) y cotizaciones, Ventas, Marketing, Logística, Almacén, Producción, Dirección/Gerencia | Pasarelas reales de Yape/Plin/Tunki (el pago se valida manualmente en caja, por diseño) |
| Respaldo y auditoría, importación/exportación de ficheros, control de versiones, reportes | Pruebas de carga a escala de producción (miles de usuarios) |
| Seguridad OWASP Top 10 aplicada al sistema | Pruebas de penetración externas / infraestructura de red |
| Navegadores: Chromium de escritorio y móvil | Navegadores antiguos (Internet Explorer) |

## 3. Elementos de prueba y trazabilidad con lo ya construido

| Lo realizado en avances anteriores | Cómo se prueba en este plan |
|---|---|
| 1. Diagramas de procesos (AS-IS/TO-BE) | Los flujos TO-BE se convierten en pruebas de **sistema** de extremo a extremo (CP-SIS-03/04) |
| 2. Patrones DTO, DAO, MVC, SOLID | DIP permite probar servicios con DAO en memoria (**unitarias**); el DTO se verifica en la API (usuarios sin contraseña, CP-API-21) |
| 3. Diagrama de clases | Cada clase de servicio tiene su clase de prueba `*ServiceTest` |
| 4. Modelo lógico/físico de BD | `DatosMasivosTest` (100 registros, claves foráneas) y `RepositorioMemoriaTest` (contrato DAO); los DAO JDBC se prueban en el entorno QA con PostgreSQL |
| 5. Diccionario de datos | Partición de equivalencia sobre campos obligatorios, únicos y rangos (documento único, monto > 0, máximo por pago) |
| 6. Prototipos UX/UI | Pruebas en navegador con **Playwright** (15 módulos, portal y vista móvil) |
| 7. Modelo de reportes | Exportación Excel/CSV/JSON (HU-07, CP-API-23/24), comprobante RC- y cotización COT- |
| 8. Recursos de código, Logback, ficheros | `LogbackConfigTest`, `LectorCsvTest`, `EscrituraArchivosTest`, `ArchivoServiceTest` |
| 9. Seguridad Java | `SeguridadOwaspApiTest` + casos negativos en Postman + E2E-11/12 |
| 10. Control de versiones (Git) | `VersionServiceTest`, `GitCliIntegracionTest`; la CI ejecuta el plan en cada push |

## 4. Estrategia

Se sigue la **pirámide de pruebas**: muchas pruebas unitarias rápidas, menos de integración y pocas de sistema y
aceptación, todas automatizadas.

```
                 ▲  Aceptación (HU-01…HU-10, BDD Dado/Cuando/Entonces)        10
                ▲▲▲ Sistema (JUnit E2E 7 + Playwright 13)                     20
              ▲▲▲▲▲▲ Integración (JUnit API 24 + Postman/Newman 26)           50
          ▲▲▲▲▲▲▲▲▲▲▲ Unitarias (servicios, seguridad, ficheros, versiones)   139
          ───────────── No funcionales: seguridad OWASP 10 · rendimiento 2
```

| Técnica | Dónde se aplica |
|---|---|
| Caja blanca (rutas de código, reglas) | Unitarias de servicios: cada `throw ReglaNegocioException` tiene su caso |
| Caja negra · partición de equivalencia y valores límite | Montos (≤ 0, máximo S/ 10 000), clave mínima (6/8 caracteres), plazo > 0 |
| Tabla de decisión | Permisos por rol × módulo (ADMIN, SUPERVISOR, CAJERO, ANALISTA, CLIENTE) |
| Pruebas basadas en escenarios (BDD) | Aceptación por historia de usuario |
| Pruebas negativas / abuso | Seguridad: inyección SQL, path traversal, IDOR, fuerza bruta, XSS, token alterado |
| Regresión automática | GitHub Actions en cada push y Pull Request |

## 5. Preparación del entorno

### 5.1 Ambientes

| Ambiente | Para qué | Persistencia | Cómo se levanta |
|---|---|---|---|
| **DEV** (cada integrante) | Unitarias, integración, sistema | `memoria` (datos de ejemplo + 100 registros) | NetBeans → Run (F6) o `sh pruebas/ejecutar_plan_pruebas.sh` |
| **QA** (laboratorio) | Aceptación con el usuario y prueba de los DAO JDBC | PostgreSQL 16 · base `SistemaFinanciero` | `db/schema.sql` → `datos_ejemplo.sql` → `portal_cliente.sql` → `seguridad.sql` → `version_esquema.sql` → `datos_100_registros.sql` → `pagos_qr.sql` |
| **CI** (GitHub Actions) | Regresión automática | `memoria` | `.github/workflows/ci.yml` (JUnit) y `.github/workflows/plan-pruebas.yml` (plan completo) |

### 5.2 Software y versiones

| Componente | Versión | Uso |
|---|---|---|
| JDK | 17 o superior (probado en 17 y 21) | Compilar y ejecutar |
| Apache NetBeans + Ant | 20+ | IDE, `Run → Test Project` (Alt + F6) |
| JUnit / Hamcrest | 4.13.2 / 1.3 | Pruebas automatizadas Java |
| PostgreSQL + pgAdmin 4 | 16 | Ambiente QA |
| Node.js | 18+ | Ejecutar Newman y Playwright |
| Postman / Newman | 11 / 6.2.1 (+ htmlextra 1.23.1) | Pruebas de API |
| Playwright Test + Chromium | 1.63.0 | Pruebas de sistema en navegador |
| Git + GitHub | 2.40+ | Versionamiento y CI |

### 5.3 Datos de prueba

| Perfil | Usuario | Clave |
|---|---|---|
| ADMIN | `admin@sigsf.com` | `admin123` |
| CAJERO | `lucia.ramos@sigsf.com` | `lucia123` |
| ANALISTA | `jorge.torres@sigsf.com` | `jorge123` |
| Cliente del portal | documento `70000001` … `70000003`, `71000001` … `71000100` | igual al documento |
| Usuarios generados (100) | su correo | `sigsf123` |

Las pruebas que crean datos generan documentos y números de operación únicos (marca de tiempo), así que se pueden repetir
sin limpiar la base.

### 5.4 Pasos de preparación (una sola vez)

1. `scripts\descargar_librerias.bat` (SLF4J/Logback) y `lib\postgresql.jar`.
2. `copy db.properties.ejemplo db.properties` y, para DEV, `persistencia=memoria`.
3. `cd pruebas\e2e` → `npm install` → `npx playwright install chromium`.
4. Verificar: `http://localhost:8091/api/health` responde `{"status":"ok"}`.

## 6. Niveles de prueba

### 6.1 Pruebas unitarias
- **Objeto:** una clase aislada (servicio, seguridad, lectores/escritores de ficheros, versión).
- **Cómo:** el servicio recibe por constructor un DAO en memoria (DIP), sin servidor ni base de datos.
- **Responsable:** el autor del módulo (ver §11). **Criterio:** 100 % de las pruebas en verde antes del commit.
- **Clases:** 28 clases · 139 pruebas (nuevas en v5.5: `ProductoServiceTest`, `TipoCambioServiceTest`, `ExpedienteServiceTest`).

### 6.2 Pruebas de integración
- **Objeto:** Vista ↔ API ↔ Filtro de seguridad ↔ Controlador ↔ Servicio ↔ DAO.
- **Cómo:** se levanta `ApiServer` en un puerto libre y se llama por HTTP (`ApiIntegracionTest`, `PagosApiTest`,
  `ArchivosApiTest`, `VersionApiTest`, `GitCliIntegracionTest`) y, desde fuera del código, la colección Postman
  `pruebas/postman/SIGSF_API.postman_collection.json` (26 peticiones, 63 aserciones) ejecutada con Newman.

### 6.3 Pruebas de sistema
- **Objeto:** el sistema completo como lo usa una persona, recorriendo varios módulos.
- **Cómo:** `test/.../plan/SistemaFlujoCompletoTest` (7 flujos por HTTP, con el frontend servido por el mismo servidor)
  y `pruebas/e2e/tests/*.spec.js` con Playwright (13 casos en Chromium de escritorio y móvil, con capturas).
- **Flujos clave:** registro en el portal → solicitud de préstamo → aprobación → pago con Yape → confirmación en caja →
  baja del saldo → estado de cuenta → comprobante RC-; atención en ventanilla → cambio de moneda → panel → Excel.

### 6.4 Pruebas de aceptación

Automatizadas en `test/.../plan/AceptacionHistoriasUsuarioTest` (formato Dado / Cuando / Entonces) y validadas con el
usuario en el ambiente QA mediante esta matriz:

| HU | Historia de usuario | Criterio de aceptación | Prueba | Resultado |
|---|---|---|---|---|
| HU-01 | Como cliente quiero crear mi cuenta en el portal | Queda ACTIVO, entra con su documento y no se duplica | `hu01_…` | ✅ Aprobada |
| HU-02 | Como cliente quiero cotizar y convertir en solicitud | Número COT-, cuota, vigencia; pasa a CONVERTIDA una sola vez | `hu02_…` | ✅ Aprobada |
| HU-03 | Como cliente quiero pagar con Yape/Plin/Tunki | Queda POR_VERIFICAR; tras confirmar tiene comprobante RC- | `hu03_…` · E2E-09/10 | ✅ Aprobada |
| HU-04 | Como cajero quiero registrar un cambio de moneda | Usa el TC vigente del servidor; monto 0 rechazado | `hu04_…` | ✅ Aprobada |
| HU-05 | Como supervisor quiero rechazar solicitudes | Pasa a ANULADA y no se puede volver a resolver | `hu05_…` | ✅ Aprobada |
| HU-06 | Como administrador quiero permisos por rol | CAJERO sin Usuarios/Respaldos; ANALISTA no confirma pagos | `hu06_…` · E2E-07 | ✅ Aprobada |
| HU-07 | Como analista quiero exportar datos | Excel, CSV y JSON descargables | `hu07_…` | ✅ Aprobada |
| HU-08 | Como gerencia quiero resolver expedientes | Estado OBSERVADO/APROBADO con fecha de resolución | `hu08_…` | ✅ Aprobada |
| HU-09 | Como gerente quiero indicadores en el panel | 7 indicadores clave presentes | `hu09_…` · E2E-01 | ✅ Aprobada |
| HU-10 | Como cliente quiero ver mi estado de cuenta privado | Ve el suyo (200) y no el de otro (403) | `hu10_…` | ✅ Aprobada |

### 6.5 Pruebas no funcionales

| Tipo | Requisito | Prueba | Criterio |
|---|---|---|---|
| Seguridad | RNF-SEG-01/02, OWASP A01–A07 | `SeguridadOwaspApiTest` (10), Postman carpeta 05 (5), E2E-11/12 | 0 vulnerabilidades explotables |
| Rendimiento / concurrencia | RNF-GEN-01 | `RendimientoApiTest`: 20 usuarios × 25 peticiones | 0 % errores · p95 < 1 s |
| Compatibilidad / responsive | RNF-UX | E2E-13 en Pixel 7 (Chromium móvil) | Sin desplazamiento horizontal |
| Usabilidad / robustez de la UI | RNF-UX | E2E-05: 15 módulos sin errores de JavaScript | 0 errores en consola |
| Regresión | — | GitHub Actions en cada push / PR | Todo en verde para fusionar |

## 7. Catálogo de casos de prueba (resumen)

| Rango | Nivel | Herramienta | Cantidad |
|---|---|---|---|
| Suites `*ServiceTest`, `security/*`, `archivos/*`, `version/*`, `dao/*`, `config/*`, `backup/*` | Unitario | JUnit 4 | 139 |
| `web/*ApiTest`, `GitCliIntegracionTest` | Integración | JUnit 4 | 24 |
| CP-API-01 … CP-API-26 | Integración (API) | Postman / Newman | 26 (63 aserciones) |
| CP-SIS-01 … CP-SIS-07 | Sistema | JUnit 4 (HTTP) | 7 |
| E2E-01 … E2E-13 | Sistema (navegador) | Playwright | 13 |
| HU-01 … HU-10 | Aceptación | JUnit 4 (BDD) | 10 |
| CP-SEG-01 … CP-SEG-10 | Seguridad | JUnit 4 | 10 |
| CP-REN-01 … CP-REN-02 | Rendimiento | JUnit 4 (hilos concurrentes) | 2 |
| **Total** | | | **231 casos** |

Plantilla de cada caso: **ID · Requisito · Precondición · Datos · Pasos · Resultado esperado · Resultado obtenido · Estado**.
En el código, el ID está en el nombre del método o en su Javadoc (p. ej. `cpSis03_cicloCompletoDeUnPrestamoDesdeElPortal`).

## 8. Matriz de trazabilidad (requisito → casos)

| Requisito | Unitaria | Integración | Sistema / E2E | Aceptación |
|---|---|---|---|---|
| RF-SEG-01 Login y sesión | `AuthServiceTest` | CP-API-02…05, 25/26 | CP-SIS-05 · E2E-01…04 | HU-06 |
| RF-SEG-02 Usuarios y claves | `UsuarioServiceTest`, `Pbkdf2PasswordHasherTest` | CP-API-21 | — | HU-06 |
| RF-CLI-03/04 Clientes | `ClienteServiceTest` | CP-API-06…08 | CP-SIS-04 · E2E-06 | HU-01 |
| RF-OPE-02 Cambio de moneda | `OperacionServiceTest`, `TipoCambioServiceTest` | CP-API-09/10 | CP-SIS-04 | HU-04 |
| RF-OPE-05 Préstamos | `PrestamoServiceTest` | CP-API-12/13 | CP-SIS-03 | HU-05 |
| RF-SOL Solicitudes | `SolicitudServiceTest` | CP-API-13 | CP-SIS-03 | HU-02, HU-05 |
| RF-PAG Pagos QR | `PagoServiceTest` | `PagosApiTest` · CP-API-14…16 | CP-SIS-03 · E2E-08…10 | HU-03 |
| RF-COT Cotizaciones | `CotizacionServiceTest` | `PagosApiTest` | — | HU-02 |
| RF-ALM Almacén | `ProductoServiceTest` | `ArchivosApiTest` | E2E-05 | — |
| RF-DIR Expedientes | `ExpedienteServiceTest` | — | E2E-05 | HU-08 |
| RF-REP Reportes / ficheros | `ArchivoServiceTest`, `EscrituraArchivosTest` | CP-API-22…24 | CP-SIS-04 | HU-07, HU-09 |
| RF-VER Versiones | `VersionServiceTest` | `VersionApiTest`, `GitCliIntegracionTest` | E2E-05 | — |
| RNF-SEG (OWASP) | `PoliticaAccesoTest`, `LimitadorIntentosTest` | CP-API-17…21 | CP-SEG-01…10 · E2E-11/12 | HU-10 |
| RNF-GEN-01 Rendimiento | — | tiempo < 1 s en cada petición Postman | CP-REN-01/02 | — |

## 9. Criterios

| Criterio | Regla |
|---|---|
| **Entrada** | Código compila; la rama `feature/*` está actualizada con `develop`; entorno preparado (§5); datos de prueba cargados |
| **Salida (liberar)** | 100 % de casos críticos y altos aprobados; ≥ 95 % del total; 0 defectos críticos/altos abiertos; CI en verde; matriz de aceptación firmada |
| **Suspensión** | El servidor no arranca, la base no responde o falla > 20 % de un nivel: se detiene el nivel y se registra un defecto bloqueante |
| **Reanudación** | Defecto bloqueante corregido en `hotfix/*` o `feature/*` y CI en verde |

## 10. Gestión de defectos

Ciclo: **Nuevo → Asignado → En corrección → Corregido → Verificado → Cerrado** (o Rechazado). Se registran como *Issues*
de GitHub con la etiqueta `bug` y la severidad; la corrección va en una rama `fix/*` o `hotfix/*` y el commit cita el
issue (`fix(pagos): … (#12)`).

| Severidad | Definición | Tiempo de atención |
|---|---|---|
| Crítica | Pérdida de dinero o datos, acceso indebido, el sistema no arranca | Inmediato (hotfix) |
| Alta | Un flujo principal no se puede completar | Antes de la liberación |
| Media | Funciona con un rodeo o un mensaje es incorrecto | Siguiente versión menor |
| Baja | Estético u ortográfico | Planificado |

## 11. Roles y responsabilidades

| Rol | Responsable | Tareas |
|---|---|---|
| Líder de pruebas / integración | Miguel Ángel Machaca Machaca | Plan, entorno, CI, consolidación de resultados |
| Analista de pruebas funcionales | Ricardo Gabriel Cervantes Tejada | Casos de sistema y aceptación, Playwright, matriz HU |
| Analista de pruebas técnicas | Cesar Huaripaucar Huaman | Unitarias, Postman/Newman, seguridad y rendimiento |
| Usuario de negocio (aceptación) | Docente / usuario clave | Revisión y firma de la matriz de aceptación |

## 12. Cronograma

| Semana | Actividad | Entregable |
|---|---|---|
| 1 | Preparación del entorno y datos; revisión de requisitos | Entorno DEV/QA listo |
| 1 | Unitarias e integración (JUnit + Postman) | 163 + 26 casos |
| 2 | Sistema (JUnit E2E + Playwright) y no funcionales | 20 + 12 casos, capturas |
| 2 | Aceptación con el usuario, corrección de defectos | Matriz HU firmada |
| 2 | Informe de resultados y liberación `v5.5.0` | `docs/11`, etiqueta Git |

## 13. Riesgos

| Riesgo | Mitigación |
|---|---|
| Diferencias entre memoria y PostgreSQL | Repetir aceptación en QA con PostgreSQL y los 100 registros |
| Pruebas inestables por animaciones | Playwright con `reducedMotion: 'reduce'` y esperas por estado, no por tiempo |
| Tiempo de la CI | Pruebas en memoria (≈ 12 s JUnit, ≈ 45 s Playwright) |
| Datos repetidos al relanzar pruebas | Documentos y números de operación únicos por ejecución |

## 14. Entregables de prueba

`docs/10_PLAN_DE_PRUEBAS.md` (este documento) · `docs/11_HERRAMIENTAS_Y_RESULTADOS_DE_PRUEBAS.md` ·
`test/` (36 clases JUnit) · `pruebas/postman/` · `pruebas/e2e/` · `pruebas/ejecutar_plan_pruebas.(sh|bat)` ·
`.github/workflows/plan-pruebas.yml` · `evidencias/v55_*.png` · `evidencias/resultado_plan_pruebas_v55.txt`.
