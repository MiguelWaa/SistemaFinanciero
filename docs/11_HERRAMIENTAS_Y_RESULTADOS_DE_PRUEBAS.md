# SIGSF · Herramientas, codificación propuesta y resultados de pruebas (v5.5)

Complementa a `docs/10_PLAN_DE_PRUEBAS.md`. Aquí se documenta **con qué** se prueba, **cómo está codificado** y **qué
resultado se obtuvo**, además del inventario de la documentación de pruebas y las rutas compartidas.

---

## 1. Herramientas elegidas

| Herramienta | Versión | Nivel | Por qué se eligió | Ubicación en el repositorio |
|---|---|---|---|---|
| **JUnit 4 + Hamcrest** | 4.13.2 / 1.3 | Unitario, integración, sistema, aceptación, seguridad, rendimiento | Viene con NetBeans (Alt + F6); no necesita servidor externo | `test/` (36 clases) |
| **NetBeans + Apache Ant** | 20+ | Ejecución local | IDE del curso; `build.xml` compila y prueba | `build.xml`, `nbproject/` |
| **Postman + Newman** | 11 / 6.2.1 | Integración de API (caja negra) | Colección compartible con el equipo; Newman la ejecuta en consola y en la CI con reporte HTML | `pruebas/postman/` |
| **newman-reporter-htmlextra** | 1.23.1 | Reporte | Reporte HTML de cada petición y aserción | `pruebas/e2e/package.json` |
| **Playwright Test** | 1.63.0 | Sistema en navegador (E2E) y responsive | Maneja el navegador real, toma capturas, reporte HTML y traza de fallas | `pruebas/e2e/` |
| **GitHub Actions** | — | Regresión continua | Ejecuta las pruebas en cada push / Pull Request | `.github/workflows/ci.yml`, `plan-pruebas.yml` |
| **Git (Git Flow)** | 2.40+ | Gestión de versiones de pruebas | Rama `feature/plan-de-pruebas`, etiqueta `v5.5.0` | `docs/07_CONTROL_DE_VERSIONES.md` |
| **SLF4J + Logback** | 2.0.16 / 1.5.12 | Diagnóstico | `test/logback-test.xml`: los WARN de seguridad quedan como evidencia | `test/logback-test.xml` |
| **Scripts del plan** | — | Orquestación | Un comando ejecuta los 3 bloques y deja el resumen | `pruebas/ejecutar_plan_pruebas.sh` / `.bat` |

## 2. Codificación propuesta

### 2.1 Unitaria (JUnit · DAO en memoria por inyección de dependencias)

```java
// test/com/utp/sistemafinanciero/service/ExpedienteServiceTest.java
@Before public void preparar() {
    servicio = new ExpedienteService(new ExpedienteAprobacionDaoMemoria());   // DIP: sin PostgreSQL
}
@Test public void resolverRegistraEstadoEnMayusculasYFecha() {
    ExpedienteAprobacion e = servicio.crear(expediente("EXP-T02"));
    ExpedienteAprobacion r = servicio.resolver(e.getIdExpediente(), "aprobado");
    assertEquals("APROBADO", r.getEstado());
    assertNotNull(r.getFechaResolucion());
}
```

### 2.2 Entorno de prueba reutilizable (sistema completo en un puerto libre)

```java
// test/com/utp/sistemafinanciero/plan/ServidorDePrueba.java
Contenedor c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-plan-pruebas"));
ApiServer app = new ApiServer(Paths.get("web"), "", 1024 * 1024);   // frontend + API, mismo origen
Rutas.registrar(app, c);
int puerto = app.iniciar(0);                                         // puerto libre: pruebas en paralelo con la app
```

### 2.3 Sistema (flujo de extremo a extremo)

```java
// SistemaFlujoCompletoTest.cpSis03_cicloCompletoDeUnPrestamoDesdeElPortal
registro en el portal → POST /api/portal/{id}/prestamo (tasa 1 % ignorada → 15 %)
→ PUT /api/solicitudes/{op}/aprobar → POST /api/portal/{id}/pagos (YAPE)
→ PUT /api/pagos/{id}/confirmar → assertEquals(saldoInicial - cuota, saldoFinal, 0.001)
```

### 2.4 Aceptación (BDD: Dado / Cuando / Entonces)

```java
/** HU-05 · Como supervisor quiero rechazar una solicitud y que no se pueda volver a resolver. */
@Test public void hu05_supervisorRechazaSolicitud() throws Exception {
    // Dado una solicitud de préstamo PENDIENTE del cliente 70000001
    // Cuando el supervisor la rechaza
    assertEquals("ANULADA", objeto(s.pedir("PUT", "/api/solicitudes/" + idOp + "/rechazar", admin, "{}")).get("estado"));
    // Entonces ya no se puede aprobar
    assertEquals(400, s.pedir("PUT", "/api/solicitudes/" + idOp + "/aprobar", admin, "{}").statusCode());
}
```

### 2.5 Seguridad y rendimiento

```java
@Test public void cpSeg01_inyeccionSqlEnLoginNoDaAcceso() {          // OWASP A03
    pedir("POST", "/api/auth/login", null, "{\"email\":\"admin@sigsf.com' OR '1'='1\",...}") → 400, sin token
}
@Test public void cpRen01_veinteUsuariosConcurrentesSinErroresYP95MenorAUnSegundo() {
    20 hilos × 25 GET (/clientes, /operaciones, /dashboard, /prestamos, /pagos) → errores == 0 && p95 < 1000 ms
}
```

### 2.6 Postman (script de prueba de cada petición)

```javascript
// CP-API-15 Caja confirma el pago
pm.test("HTTP 200", () => pm.response.to.have.status(200));
const p = pm.response.json();
pm.test("CONFIRMADO con comprobante RC-", () => {
  pm.expect(p.estado).to.eql("CONFIRMADO");
  pm.expect(p.numeroComprobante).to.match(/^RC-\d{6}$/);
});
```

### 2.7 Playwright (navegador real)

```javascript
// pruebas/e2e/tests/03-portal-pago.spec.js · E2E-10
await ingresar(page, 'cajero');
await page.click('[data-nav="pagos.html"]');
const tarjeta = page.frameLocator('#moduleFrame').locator('article', { hasText: numero });
await tarjeta.getByRole('button', { name: /Confirmar pago/i }).click();
await frame.getByRole('button', { name: /Sí, confirmar/i }).click();
expect((await respuesta.json()).numeroComprobante).toMatch(/^RC-\d{6}$/);
```

### 2.8 Cómo se ejecuta

| Qué | Comando |
|---|---|
| Todo el plan (Windows) | `pruebas\ejecutar_plan_pruebas.bat` |
| Todo el plan (Linux/Mac/Git Bash) | `sh pruebas/ejecutar_plan_pruebas.sh` |
| Solo JUnit en NetBeans | `Run → Test Project` (Alt + F6) |
| Solo JUnit en consola | `scripts\compilar_y_probar.bat` |
| Solo API | `cd pruebas/e2e && npm run test:api` (servidor encendido) |
| Solo navegador | `cd pruebas/e2e && npm run test:e2e` (servidor encendido) |
| En GitHub | pestaña **Actions → Plan de pruebas SIGSF → Run workflow** |

## 3. Resultados obtenidos

Ejecución del 04/11/2026 sobre la versión 5.5.0, ambiente DEV (`persistencia=memoria`, datos de ejemplo + 100 registros
por módulo), JDK 21, Chromium 1.63. El plan completo se ejecutó tres veces seguidas con el mismo resultado (sin pruebas
inestables).

### 3.1 Resumen por nivel

| Nivel | Herramienta | Casos | Aprobados | Fallidos | % |
|---|---|---|---|---|---|
| Unitarias | JUnit 4 | 139 | 139 | 0 | 100 % |
| Integración (código) | JUnit 4 | 24 | 24 | 0 | 100 % |
| Integración (API) | Postman / Newman | 26 (63 aserciones) | 26 | 0 | 100 % |
| Sistema (HTTP) | JUnit 4 | 7 | 7 | 0 | 100 % |
| Sistema (navegador) | Playwright | 13 | 13 | 0 | 100 % |
| Aceptación | JUnit 4 (BDD) | 10 | 10 | 0 | 100 % |
| Seguridad OWASP | JUnit 4 | 10 | 10 | 0 | 100 % |
| Rendimiento | JUnit 4 | 2 | 2 | 0 | 100 % |
| **Total** | | **231** | **231** | **0** | **100 %** |

JUnit pasó de **151 a 192 pruebas** (36 clases): +12 unitarias, +7 sistema, +10 aceptación, +10 seguridad, +2 rendimiento.

### 3.2 Rendimiento (CP-REN-01)

| Métrica | Resultado | Criterio | Estado |
|---|---|---|---|
| Peticiones concurrentes | 500 (20 usuarios × 25) | — | — |
| Errores | 0 | 0 % | ✅ |
| Percentil 50 | 44 ms | — | ✅ |
| Percentil 95 | 52 ms | < 1000 ms | ✅ |
| Máximo | 69 ms | — | ✅ |
| Rendimiento | ≈ 410 peticiones/s | — | ✅ |
| Tiempo medio por petición en Newman | ≈ 50 ms | < 1000 ms por petición | ✅ |

### 3.3 Seguridad (OWASP Top 10 · 2021)

| Ataque probado | Categoría | Resultado |
|---|---|---|
| Inyección SQL en el login | A03 | Rechazado (400), sin token |
| Path traversal (`../db.properties.ejemplo`, `%2e%2e`) | A01 | 404, no sale de `web/` |
| Fuerza bruta (5 intentos) | A07 | Cuenta bloqueada 5 min, ni la clave correcta entra |
| IDOR: pagar/consultar trámites de otro cliente | A01 | 400 / 403 |
| Manipular la tasa del préstamo desde el portal | A04 | Ignorada (15 % fijado por la entidad) |
| XSS almacenado en el nombre de un cliente | A03 | API responde JSON + `nosniff`; la interfaz lo muestra como texto (E2E-11) |
| Cabeceras: CSP, X-Frame-Options, Referrer-Policy | A05 | Presentes en todas las páginas |
| Cuerpo mayor a 1 MB | A04 (DoS) | Rechazado (400) |
| Token alterado / `alg:none` | A02 / A07 | 401 |
| Montos negativos o sobre S/ 10 000 | A04 | 400 con mensaje claro |

### 3.4 Defectos y observaciones

| ID | Tipo | Descripción | Severidad | Estado |
|---|---|---|---|---|
| — | Defecto | No se encontraron defectos funcionales en esta ejecución | — | — |
| OBS-01 | Mejora | El API guarda texto con etiquetas HTML tal cual (la interfaz lo escapa). Se recomienda además validar/limpiar en el servidor (defensa en profundidad) | Baja | Abierta (v5.6) |
| OBS-02 | Mejora | Latencia base ≈ 45 ms incluso en endpoints simples; viene de la capa HTTP, no de la lógica. Revisar `TCP_NODELAY`/keep-alive si se requiere menos | Baja | Abierta (v5.6) |
| OBS-03 | Entorno | Los DAO JDBC se validan en QA con PostgreSQL; las pruebas automáticas usan memoria por velocidad | Info | Aceptada |

### 3.5 Evidencias

| Archivo | Contenido |
|---|---|
| `evidencias/v55_01_junit_192_pruebas.png` | Consola: OK (192 tests) y resumen por nivel |
| `evidencias/v55_02_newman_api.png` | Newman: 26 peticiones, 63 aserciones, 0 fallas |
| `evidencias/v55_03_playwright_reporte.png` | Reporte HTML de Playwright: 13/13 |
| `evidencias/v55_04 … v55_11` | Capturas tomadas por Playwright: panel, login fallido, QR de Yape, pago registrado, bandeja por verificar, XSS como texto, portal en celular, cajero sin Usuarios |
| `evidencias/resultado_plan_pruebas_v55.txt` | Resumen en texto de la ejecución |
| `pruebas/reportes/` (se genera al ejecutar) | `newman/reporte-api.html`, `playwright/index.html`, JUnit XML de cada herramienta |

## 4. Inventario de documentación de pruebas

| N° | Documento / artefacto | Ubicación | Estado |
|---|---|---|---|
| T-35 | Plan de pruebas de software | `docs/10_PLAN_DE_PRUEBAS.md` | ✅ |
| T-36 | Herramientas, codificación y resultados | `docs/11_HERRAMIENTAS_Y_RESULTADOS_DE_PRUEBAS.md` | ✅ |
| T-37 | Pruebas JUnit del plan (sistema, aceptación, seguridad, rendimiento) | `test/com/utp/sistemafinanciero/plan/` | ✅ |
| T-38 | Colección y entorno Postman | `pruebas/postman/` | ✅ |
| T-39 | Pruebas E2E Playwright | `pruebas/e2e/` | ✅ |
| T-40 | Scripts del plan de pruebas | `pruebas/ejecutar_plan_pruebas.sh` · `.bat` | ✅ |
| T-41 | Flujo de CI del plan completo | `.github/workflows/plan-pruebas.yml` | ✅ |
| T-42 | Evidencias del plan de pruebas | `evidencias/v55_*` | ✅ |

El inventario general del proyecto (T-01 … T-42) está en `docs/02_INVENTARIO_DE_DOCUMENTACION.md`.

## 5. Rutas compartidas de las herramientas

Repositorio: **https://github.com/MiguelWaa/SistemaFinanciero** (rama `main`, etiqueta `v5.5.0`).

| Herramienta | Ruta compartida |
|---|---|
| Repositorio y ramas | https://github.com/MiguelWaa/SistemaFinanciero · `/branches` · `/network` |
| Plan de pruebas | https://github.com/MiguelWaa/SistemaFinanciero/blob/main/docs/10_PLAN_DE_PRUEBAS.md |
| Resultados | https://github.com/MiguelWaa/SistemaFinanciero/blob/main/docs/11_HERRAMIENTAS_Y_RESULTADOS_DE_PRUEBAS.md |
| Pruebas JUnit | https://github.com/MiguelWaa/SistemaFinanciero/tree/main/test |
| Colección Postman (importar en Postman) | https://github.com/MiguelWaa/SistemaFinanciero/tree/main/pruebas/postman |
| Pruebas Playwright | https://github.com/MiguelWaa/SistemaFinanciero/tree/main/pruebas/e2e |
| Integración continua (GitHub Actions) | https://github.com/MiguelWaa/SistemaFinanciero/actions |
| Evidencias | https://github.com/MiguelWaa/SistemaFinanciero/tree/main/evidencias |

> Las rutas quedan activas al publicar la versión (`git push --all origin` y `git push origin --tags`).
