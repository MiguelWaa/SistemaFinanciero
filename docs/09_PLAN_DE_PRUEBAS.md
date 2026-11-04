# SIGSF · Plan de Pruebas de Software

| Campo | Valor |
|---|---|
| Identificador | SIGSF-PP-001 · versión 1.0 |
| Sistema bajo prueba | SIGSF – Sistema Integral de Gestión de Servicios Financieros **v6.0.0** |
| Fecha | 3 de noviembre de 2026 (Avance de Proyecto Final 4) |
| Estructura | IEEE 829-2008 / ISO/IEC/IEEE 29119-3 (plan, diseño, casos, registro y reporte de pruebas) |
| Referencias | Requisitos RF/RNF (Avance 2 §3.1) · `docs/01` arquitectura · `docs/03` seguridad · `docs/05` TDD |
| Equipo | Ricardo Gabriel Cervantes Tejada · Cesar Huaripaucar Huaman · Miguel Ángel Machaca Machaca |
| Resultados | `docs/10_HERRAMIENTAS_Y_RESULTADOS.md` · `evidencias/pruebas/` |

---

## 1. Objetivo

Demostrar, con evidencia reproducible, que el SIGSF cumple sus requisitos funcionales y
no funcionales **en todas las capas** (servicio, base de datos, API, interfaz y
experiencia del usuario) y encontrar los defectos antes de la entrega. El plan define
**qué** se prueba, **cómo**, **con qué herramientas**, **en qué entorno** y **cuándo una
prueba o una versión se considera aprobada**.

## 2. Alcance

### 2.1 Ítems de prueba

Backend Java (16 controladores, 21 servicios, DAO en memoria y JDBC), base de datos
PostgreSQL 16 (34 tablas, restricciones CHECK, disparador de auditoría), API REST (más de 100
rutas), frontend `web/` (23 pantallas) y Portal del Cliente, para los 12 módulos del
sistema: Seguridad y usuarios, Clientes y servicios, Operaciones (cambio, préstamos,
empeños), Ventas, Almacén, Logística, Marketing, Producción, Dirección/Gerencia,
Solicitudes, Portal del Cliente, Archivos y Control de versiones.

### 2.2 Características que SÍ se prueban

| Tipo | Características |
|---|---|
| Funcionales | Cálculos financieros (cambio, cuota, vencimiento, saldo), reglas de negocio y validaciones, CRUD de catálogos, flujo de solicitudes (PENDIENTE → COMPLETADA/ANULADA), portal del cliente, importación/exportación de ficheros, panel de indicadores, control de versiones |
| Seguridad | Autenticación PBKDF2, bloqueo por intentos, sesiones y cierre de sesión, RBAC (403), anti-IDOR, inyección SQL, cabeceras de seguridad, auditoría inmutable, mensajes que no revelan información |
| Datos | Esquema y versión de la base, restricciones CHECK, disparador de auditoría, 100 registros por módulo, contrato DAO memoria ↔ JDBC |
| No funcionales | Rendimiento con 50 usuarios concurrentes, tiempo de respuesta de la API, uso en celular (390 px) |

### 2.3 Características que NO se prueban (y por qué)

| Característica | Motivo |
|---|---|
| Respaldo real con `pg_dump` en Windows | Depende de la instalación local de PostgreSQL; se prueba la estrategia JDBC y la retención (RespaldoServiceTest) |
| Asistente por voz | Requiere micrófono y la API de voz del navegador |
| Integraciones externas (pagos, SUNAT) | Fuera del alcance de la versión 6.0 |
| Navegadores Firefox y Safari | El plan usa Chromium; Playwright permite agregarlos con `--browser firefox` |

## 3. Estrategia (enfoque)

### 3.1 Pirámide de pruebas

```
                 ▲  PA  Aceptación (BDD, Gherkin)         10 escenarios
                ▲▲▲ PS  Sistema extremo a extremo         13 casos
              ▲▲▲▲▲ PR  Rendimiento (JMeter)               1 escenario · 2 837 peticiones
            ▲▲▲▲▲▲▲ PI  Integración (JUnit + Newman)      76 casos
       ▲▲▲▲▲▲▲▲▲▲▲▲ PU  Unitarias (JUnit 4 + Mockito)    171 casos
```

Muchas pruebas rápidas y aisladas en la base, pocas y valiosas en la cima. **Total: 270
casos automatizados + 1 escenario de carga.**

### 3.2 Técnicas de diseño de casos

| Técnica | Dónde se aplica | Ejemplo |
|---|---|---|
| Partición de equivalencia | Montos, estados, documentos | `OperacionParticionesTest` (PU-OPE-01…10) |
| Análisis de valores límite | Monto, tasa y plazo de préstamos y empeños | `PrestamoValoresLimiteTest` (PU-PRE-01…19): 99.99 / 100.00 / 1 000 000.00 / 1 000 000.01 |
| Tabla de decisión | Permisos por rol (RBAC) | `PoliticaAccesoTest`, PI-NEW-21/22/29 |
| Transición de estados | Solicitudes, cuentas bloqueadas | `SolicitudServiceTest`, CA-03.2, CA-04.1 |
| Dobles de prueba (mocks) | Interacción servicio ↔ DAO | `ClienteServiceMockitoTest`, `AuthServiceMockitoTest` |
| Prueba de contrato | Patrón DAO (Liskov) | `ContratoClienteDao` en memoria y en PostgreSQL |
| Caja blanca (cobertura) | Todo el backend | JaCoCo: líneas y ramas por paquete |
| BDD (Dado/Cuando/Entonces) | Criterios de aceptación | `pruebas/aceptacion/features/*.feature` |
| Pruebas exploratorias guiadas | Interfaz | Hallazgos DEF-009, DEF-010, DEF-012 |

### 3.3 TDD y regresión

Cada defecto se reproduce **primero con una prueba que falla (rojo)**, luego se corrige
(verde) y la prueba queda como regresión permanente. Evidencia del rojo:
`evidencias/pruebas/01_tdd_rojo_antes_de_corregir.txt` (22 fallas) y
`02_tdd_rojo_portal_y_catalogos.txt` (5 fallas). La integración continua ejecuta toda la
suite en cada *push* y *Pull Request*.

## 4. Entorno de pruebas (PE · Preparación del entorno)

| Elemento | Desarrollo (equipo) | Pruebas (CI / laboratorio) |
|---|---|---|
| Sistema operativo | Windows 10/11 | Ubuntu 24.04 (GitHub Actions `ubuntu-latest`) |
| Java | JDK 17+ · NetBeans 20+ | Temurin JDK 17 |
| Base de datos | PostgreSQL 16 · `SistemaFinanciero` | PostgreSQL 16 · **`SistemaFinanciero_test`** (contenedor de servicio en CI) |
| Servidor SIGSF | puerto 8091 | puerto **8092** |
| Navegador | Chrome | Chromium (Playwright 1.63) |
| Herramientas | NetBeans, pgAdmin, Postman | JUnit, Mockito, JaCoCo, Newman, pytest, JMeter |

**Pasos de preparación**

| ID | Paso | Comando / archivo |
|---|---|---|
| PE-01 | Crear la base de pruebas **desde cero** y cargar los 6 scripts en orden (re-ejecutable) | `scripts/pruebas/preparar_bd_pruebas.sh` / `.bat` |
| PE-02 | Descargar librerías de prueba (JUnit, Mockito, Byte Buddy, Objenesis, JaCoCo) | `scripts/descargar_librerias.*` · `lib/test/` |
| PE-03 | Instalar herramientas de sistema/aceptación | `pip install -r pruebas/requirements.txt` · `playwright install chromium` |
| PE-04 | Instalar Newman | `npm install -g newman newman-reporter-htmlextra` |
| PE-05 | Instalar JMeter 5.6 (opcional) | https://jmeter.apache.org/download_jmeter.cgi |
| PE-06 | Ejecutar todo el plan | `scripts/pruebas/ejecutar_plan_de_pruebas.sh` / `.bat` |

**Reglas del entorno.** (1) Las pruebas de PostgreSQL solo corren contra una base cuyo
nombre termina en `_test` (`BdPruebas`); nunca tocan la base de desarrollo. (2) Sin base
de pruebas, esas pruebas se **omiten** y el resto de la suite sigue funcionando.
(3) Los datos son **deterministas** (semilla 20261025): 100 registros por módulo y 300
operaciones, idénticos en cada equipo. (4) Credenciales de prueba: `admin@sigsf.com /
admin123` (ADMIN), `lucia.ramos@sigsf.com / lucia123` (CAJERO), 42 cajeros generados
`… / sigsf123`, clientes del portal `71000001…` (clave = documento).

## 5. Niveles de prueba

### 5.1 PU · Pruebas unitarias — 171 casos

* **Objetivo:** verificar cada regla de negocio aislada de la base de datos.
* **Herramientas:** JUnit 4.13.2 + Hamcrest, Mockito 5, JaCoCo 0.8.12. Carpeta `test/`.
* **Entrada:** el código compila. **Salida:** 100 % de casos aprobados y cobertura de
  líneas del backend ≥ 70 % (servicios ≥ 70 %).

| Clase (nuevas en v6.0 en **negrita**) | Casos | Requisito |
|---|---|---|
| **PrestamoValoresLimiteTest** (parametrizada) | 19 | RF-OPE-05 · límites de monto, tasa y plazo |
| **OperacionParticionesTest** (parametrizada) | 10 | RF-OPE-01/02 · particiones y estado inicial |
| **EmpenoValoresLimiteTest** (parametrizada) | 8 | RF-OPE-06 |
| **PortalServiceTest** | 9 | RF-POR-01…05 · sin operaciones huérfanas |
| **CatalogosServiceTest** | 7 | Roles, tipo de cambio, productos, proveedores, marketing, expedientes, panel |
| **ClienteServiceMockitoTest** (Mockito) | 7 | RF-CLI-03/04/06 · interacción con el DAO |
| **AuthServiceMockitoTest** (Mockito) | 5 | RF-SEG-01, RNF-SEG-01 |
| Pruebas de v5.1 (servicios, seguridad, ficheros, versiones, datos) | 106 | ver `docs/05_PRUEBAS_TDD.md` |

### 5.2 PI · Pruebas de integración — 76 casos

| Grupo | Herramienta | Casos | Qué integra |
|---|---|---|---|
| **ContratoClienteDao** (memoria y JDBC) | JUnit | 10 | Mismo contrato en las 2 implementaciones del DAO |
| **PostgresIntegracionTest** (PI-BD-01…08) | JUnit + PostgreSQL | 8 | Esquema y versión, disparador de auditoría, CHECK, reglas servicio = base, 100 registros |
| **ApiPostgresIntegracionTest** (PI-API-01…07) | JUnit + HttpClient + PostgreSQL | 7 | HTTP → filtro → controlador → servicio → JDBC → tabla y auditoría |
| ApiIntegracionTest, ArchivosApiTest, VersionApiTest, GitCliIntegracionTest | JUnit | 20 | API completa en memoria, ficheros, Git real |
| **Colección Postman** (PI-NEW-01…31) | Postman / Newman | 31 (51 aserciones) | API REST sobre el servidor real con PostgreSQL |

* **Entrada:** PE-01 completado y unitarias en verde. **Salida:** 100 % aprobado, ninguna
  respuesta HTTP 500, tiempo de cada petición < 800 ms (aserción de Newman).

### 5.3 PS · Pruebas de sistema (extremo a extremo) — 13 casos

Herramienta: **Playwright + pytest** (Chromium real). Carpeta `pruebas/e2e/`.
Capturas en `evidencias/pruebas/e2e/`.

| ID | Caso | Resultado esperado |
|---|---|---|
| PS-01 | Login con clave errónea | Mensaje genérico, permanece en login |
| PS-02 | Login ADMIN | Panel con indicadores y rol |
| PS-03 | Abrir el panel sin sesión | Redirige al login |
| PS-04 | Registrar cliente desde el módulo | Aparece en la tabla al buscarlo |
| PS-05 | Cambio de moneda (CAJERO) | Total = monto × venta, 2 decimales |
| PS-06 | Títulos de tabla de 3 módulos | Sin texto duplicado (DEF-009) |
| PS-07 | Aprobar solicitud en la bandeja | Mensaje "Solicitud N° … aprobada" |
| PS-08 | CAJERO abre Usuarios | Aviso de permiso, ningún correo visible |
| PS-09 | Portal: préstamo S/ 3 000 a 12 meses | Cuota S/ 700.00 y trámite en historial |
| PS-10 | Portal: préstamo S/ 50 | Mensaje de mínimo; historial sin cambios (DEF-007) |
| PS-11 | Exportar operaciones a Excel | Descarga `.xlsx` válida (OOXML) |
| PS-12 | Cerrar sesión | El token deja de servir (HTTP 401) |
| PS-13 | Portal en celular (390 px) | Sin desplazamiento horizontal (DEF-010) |

### 5.4 PA · Pruebas de aceptación — 4 historias, 10 escenarios

Criterios de aceptación escritos en **Gherkin en español** (para que el usuario los lea
y firme) y automatizados con **pytest-bdd + Playwright**. Carpeta `pruebas/aceptacion/`.

| Historia | Criterios | Escenarios |
|---|---|---|
| HU-01 Cambio de moneda en caja | CA-01.1 total calculado · CA-01.2 total manipulado se ignora | 2 |
| HU-02 Préstamo desde el portal | CA-02.1 cuota · CA-02.2 límites · CA-02.3 sin huérfanas | 4 (esquema con 4 ejemplos) |
| HU-03 Control de acceso por rol | CA-03.1 cajero sin usuarios · CA-03.2 bloqueo · CA-03.3 anti-IDOR | 3 |
| HU-04 Bandeja de solicitudes | CA-04.1 aprobar → COMPLETADA | 1 |

**Acta de aceptación (UAT).** El usuario representante revisa cada escenario en
`features/*.feature`, ve su ejecución (`reporte_e2e_aceptacion.html`) y firma:

| Historia | Resultado automático | Aceptado por | Fecha | Firma |
|---|---|---|---|---|
| HU-01 | ✅ 2/2 | | | |
| HU-02 | ✅ 4/4 | | | |
| HU-03 | ✅ 3/3 | | | |
| HU-04 | ✅ 1/1 | | | |

### 5.5 PR · Pruebas de rendimiento (carga)

* **Herramienta:** Apache JMeter (`pruebas/rendimiento/SIGSF_carga.jmx`), análisis con
  `analizar_jtl.py` (equivalente al *Aggregate Report*).
* **Escenario PR-01 "hora punta en caja":** 50 usuarios virtuales con las 43 cuentas de
  cajero, rampa de 15 s, 60 s de duración, pausa 0.3–0.8 s. Cada usuario inicia sesión
  una vez y repite: panel → clientes → operaciones → registrar cambio → bandeja.
* **Criterios (requisito no funcional de rendimiento):** p90 global < 1 000 ms · errores < 1 % · rendimiento ≥ 20
  peticiones/s.

### 5.6 Pruebas de seguridad (transversales)

| Amenaza (OWASP Top 10 2021) | Casos |
|---|---|
| A01 Control de acceso roto | PoliticaAccesoTest, PI-API-06, PI-NEW-21/27/29, PS-08, CA-03.1, CA-03.3 |
| A02 Fallas criptográficas | Pbkdf2PasswordHasherTest, AuthServiceMockitoTest (migración de hash) |
| A03 Inyección | PI-API-07, PI-NEW-06 (SQL en el login), EscrituraArchivosTest (fórmulas, XXE) |
| A04 Diseño inseguro | PI-NEW-13 / CA-01.2 (total calculado en servidor), DEF-005 (estado inicial) |
| A05 Configuración | Cabeceras de seguridad (ApiIntegracionTest), CORS cerrado |
| A07 Autenticación | LimitadorIntentosTest, CA-03.2, PI-NEW-03, PS-12 |
| A09 Registro y monitoreo | PI-BD-02 (auditoría inmutable), PI-API-02, PI-NEW-23 |

## 6. Criterios

| Criterio | Definición |
|---|---|
| **Aprobación de un caso** | Todas sus aserciones se cumplen |
| **Aprobación de la versión** | 100 % de casos aprobados en PU, PI, PS y PA; criterios de PR cumplidos; cero defectos abiertos de severidad Alta o Crítica; cobertura de líneas ≥ 70 % |
| **Suspensión** | El entorno no arranca (BD o servidor caído) o > 20 % de casos fallan por la misma causa |
| **Reanudación** | Causa corregida y PE-01 re-ejecutado |

## 7. Gestión de defectos

Ciclo: **Nuevo → Confirmado (prueba en rojo) → Corregido → Verificado (verde) → Cerrado**
o **Diferido** a una versión planificada. Severidad: **Crítica** (pérdida de datos o de
seguridad) · **Alta** (función principal incorrecta o error 500) · **Media** (regla
incumplida con alternativa) · **Baja** (visual, mensajes, registros). El registro
completo está en `docs/10` §4.

## 8. Entregables de prueba

Plan (este documento) · casos automatizados (`test/`, `pruebas/`) · datos y entorno
(`scripts/pruebas/`, `db/`) · registros de ejecución y reportes HTML
(`evidencias/pruebas/reportes/`) · capturas (`evidencias/pruebas/e2e`, `/aceptacion`) ·
registro de defectos y resumen de resultados (`docs/10`).

## 9. Responsabilidades y cronograma

| Integrante | Responsabilidad |
|---|---|
| Miguel Ángel Machaca Machaca | Líder de pruebas: plan, entorno (PE), integración continua, versionamiento y reporte |
| Ricardo Gabriel Cervantes Tejada | Pruebas unitarias e integración (JUnit, Mockito, JaCoCo, PostgreSQL) |
| Cesar Huaripaucar Huaman | Pruebas de API, sistema, aceptación y rendimiento (Newman, Playwright, pytest-bdd, JMeter) |

| Semana | Actividad |
|---|---|
| 1 | Plan, preparación del entorno, técnicas de diseño, casos unitarios |
| 2 | Integración con PostgreSQL, colección Postman, corrección de defectos (TDD) |
| 3 | Sistema E2E, aceptación BDD, carga con JMeter, CI y reporte final |

## 10. Riesgos y contingencias

| Riesgo | Contingencia |
|---|---|
| Diferencias entre la base del equipo y la de pruebas | Base de pruebas recreada desde cero en cada ejecución (PE-01) |
| Pruebas E2E frágiles por tiempos de carga | Esperas por condición (Playwright *auto-wait*), nunca `sleep` fijo |
| Datos que cambian entre ejecuciones | Documentos únicos generados y limpieza en `@After` |
| Sin internet en el laboratorio | Íconos servidos localmente (`SIGSF_FA_LOCAL`); librerías en `lib/test/` |
| Ejecutar por error contra la base real | Guardia `_test` en `BdPruebas` y en `preparar_bd_pruebas` |

## 11. Matriz de trazabilidad (requisito → casos)

| Requisito | Unitarias | Integración | Sistema / Aceptación |
|---|---|---|---|
| RF-SEG-01 Inicio de sesión | AuthServiceTest, AuthServiceMockitoTest | PI-API-01, PI-NEW-03/04 | PS-01, PS-02 |
| RF-SEG-03 / RNF-SEG-02 Roles y sesiones | PoliticaAccesoTest, GestorSesionesTest | PI-API-06, PI-NEW-21/22/30/31 | PS-08, PS-12, CA-03.1 |
| RNF-SEG-01 Fuerza bruta | LimitadorIntentosTest | — | CA-03.2 |
| RF-CLI-03/04/06 Clientes | ClienteServiceTest, ClienteServiceMockitoTest | Contrato DAO, PI-API-02/03, PI-NEW-07…12 | PS-04 |
| RF-OPE-01/02 Cambio de moneda | OperacionServiceTest, OperacionParticionesTest | PI-API-04/05, PI-NEW-13…16 | PS-05, CA-01.1, CA-01.2 |
| RF-OPE-05 Préstamos | PrestamoServiceTest, PrestamoValoresLimiteTest | PI-BD-04/05/06, PI-NEW-17…19 | PS-09, CA-02.x |
| RF-OPE-06 Empeños | VentaYEmpenoServiceTest, EmpenoValoresLimiteTest | PI-BD-07 | — |
| RF-POR-01…05 Portal | PortalServiceTest | ApiIntegracionTest, PI-NEW-25…29 | PS-09, PS-10, PS-13, CA-02, CA-03.3 |
| RF-SOL-01/02 Solicitudes | SolicitudServiceTest | — | PS-07, CA-04.1 |
| RF-ALM / RF-LOG / RF-MKT / RF-DIR | CatalogosServiceTest, ProduccionServiceTest | ArchivosApiTest, PI-BD-03 | PS-06 |
| Reportes y ficheros | ArchivoServiceTest, EscrituraArchivosTest, LectorCsvTest | ArchivosApiTest | PS-11 |
| Auditoría | — | PI-BD-02, PI-API-02, PI-NEW-23 | — |
| RF-VER Control de versiones | VersionServiceTest | GitCliIntegracionTest, VersionApiTest | — |
| RNF de rendimiento (tiempo de respuesta) | — | — | PR-01 |
