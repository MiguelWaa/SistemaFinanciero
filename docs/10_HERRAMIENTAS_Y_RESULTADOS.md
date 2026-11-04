# SIGSF · Herramientas, codificación propuesta y resultados de las pruebas

Complementa el plan (`docs/09_PLAN_DE_PRUEBAS.md`). Resultados de la ejecución completa
del **3 de noviembre de 2026** sobre la versión **6.0.0** (base `SistemaFinanciero_test`
recreada desde cero). Registro íntegro: `evidencias/pruebas/reportes/consola_plan_completo.txt`.

## 1. Herramientas elegidas

| Herramienta | Versión | Nivel | Por qué se eligió | Ruta en el repositorio |
|---|---|---|---|---|
| **JUnit 4** + Hamcrest | 4.13.2 / 1.3 | PU, PI | Integrada en NetBeans; ya usada en el TDD de v4–v5; runner `Parameterized` para valores límite | `test/` |
| **Mockito** | 5.14 | PU | Dobles de prueba para verificar la interacción servicio ↔ DAO sin base de datos | `test/.../service/*MockitoTest.java` |
| **JaCoCo** | 0.8.12 | PU, PI, PS | Cobertura de líneas y ramas por paquete; también mide el servidor durante E2E | `scripts/compilar_y_probar.*` (`COBERTURA=1`) |
| **PostgreSQL** (base de pruebas) | 16 | PE, PI | La misma base de producción, aislada (`_test`) | `scripts/pruebas/preparar_bd_pruebas.*` |
| **Postman / Newman** | 11 / 6 | PI | Colección importable en Postman y ejecutable en consola/CI con reporte HTML | `pruebas/postman/` |
| **Playwright** + pytest | 1.63 / 9.1 | PS | Navegador real, esperas automáticas, capturas y descargas; corre en CI sin pantalla | `pruebas/e2e/` |
| **pytest-bdd** (Gherkin en español) | 9.0 | PA | Criterios de aceptación legibles por el usuario y ejecutables | `pruebas/aceptacion/` |
| **Apache JMeter** | 5.6 (compatible 2.13+) | PR | Estándar de la industria para carga HTTP; ejecución sin interfaz | `pruebas/rendimiento/SIGSF_carga.jmx` |
| **GitHub Actions** | — | Regresión | Ejecuta el plan en cada *push* / *Pull Request* con PostgreSQL de servicio | `.github/workflows/ci.yml` |
| Git + GitHub | 2.43 | Versionamiento | Ramas por nivel de prueba, Pull Requests, etiquetas SemVer | `docs/07_CONTROL_DE_VERSIONES.md` |

## 2. Codificación propuesta (extractos)

**Valores límite con JUnit `Parameterized`** — `PrestamoValoresLimiteTest`

```java
@Parameters(name = "{0}: monto={1} tasa={2} plazo={3} -> {4}")
public static Collection<Object[]> casos() {
    return Arrays.asList(new Object[][]{
        {"PU-PRE-02", "99.99",      "2",       12,  false},  // mínimo − 0.01
        {"PU-PRE-19", "100.00",     "2",       12,  true},   // mínimo
        {"PU-PRE-05", "1000000.01", "2",       12,  false},  // máximo + 0.01
        {"PU-PRE-13", "1000",       "1500",    12,  false},  // antes: HTTP 500
        {"PU-PRE-17", "1000",       "2",       361, false},  // plazo máximo + 1
        ...
```

**Interacción con Mockito** — `ClienteServiceMockitoTest`

```java
when(dao.buscarPorDocumento("70000001")).thenReturn(cliente("Ya", "70000001"));
assertThrows(ReglaNegocioException.class, () -> servicio.crear(cliente("Otro", "70000001")));
verify(dao, never()).crear(any());          // un duplicado nunca llega a la base
```

**Contrato del patrón DAO** — la misma clase abstracta se ejecuta en memoria y en PostgreSQL

```java
public abstract class ContratoClienteDao {
    protected abstract ClienteDao crearDao();          // ClienteDaoMemoria o ClienteDaoJdbc
    @Test public void actualizarPersisteLosCambios() { ... assertEquals(1, dao.actualizar(c)); }
}
```

**Guardia de la base de pruebas** — `BdPruebas.requerir()` omite (no falla) si la base no termina en `_test`.

**Newman (JavaScript de Postman)** — PI-NEW-15

```javascript
pm.test("HTTP 400", () => pm.response.to.have.status(400));
pm.test("Nunca error 500", () => pm.expect(pm.response.code).to.be.below(500));
```

**Playwright** — PS-10 (portal, monto menor al mínimo)

```python
filas_antes = tramites(pagina).count()
pagina.fill("#prMonto", "50"); pagina.click("#prBtn")
expect(pagina.locator("#prMsg")).to_contain_text("mínimo")
assert tramites(pagina).count() == filas_antes, "no debe quedar una operación huérfana"
```

**Gherkin en español** — `HU02_prestamo_desde_el_portal.feature`

```gherkin
Esquema del escenario: Cuota según monto y plazo
  Dado que ingresé al portal con el documento "<documento>"
  Cuando solicito un préstamo de "<monto>" a "<plazo>" meses
  Entonces veo el mensaje "<mensaje>"
  Ejemplos:
    | documento | monto | plazo | mensaje             |
    | 71000008  | 1200  | 6     | 380.00              |
    | 71000010  | 99.99 | 12    | mínimo es S/ 100.00 |
```

**Corrección derivada de las pruebas** — `ReglasMonto` (servicio) replica los CHECK de la
base para que memoria y PostgreSQL se comporten igual, y `ApiServer` traduce el desborde
numérico `22003` a HTTP 400 como segunda barrera.

## 3. Resultados obtenidos

### 3.1 Resumen por nivel

| Nivel | Herramienta | Casos | Aprobados | Fallidos | Evidencia |
|---|---|---|---|---|---|
| PE Preparación del entorno | psql | 6 scripts · 34 tablas | ✅ | 0 | `consola_plan_completo.txt` |
| PU Unitarias | JUnit 4 + Mockito | 171 | 171 | 0 | `junit_resultado.txt`, `resultado_pruebas.csv` |
| PI Integración (JUnit) | JUnit + PostgreSQL | 45 | 45 | 0 | `junit_resultado.txt` |
| PI Integración (API) | Newman | 31 (51 aserciones) | 31 | 0 | `newman_sigsf.html` |
| PS Sistema | Playwright | 13 | 13 | 0 | `reporte_e2e_aceptacion.html`, `e2e/*.png` |
| PA Aceptación | pytest-bdd | 10 | 10 | 0 | `reporte_e2e_aceptacion.html`, `aceptacion/*.png` |
| PR Rendimiento | JMeter | 1 escenario · 2 837 peticiones | 3/3 criterios | 0 | `resumen_rendimiento.csv`, `criterios.txt` |
| **Total** | | **270 casos + 1 escenario** | **270** | **0** | |

Antes de corregir, la primera ejecución de las pruebas nuevas dio **22 fallas** y la
segunda **5** (rojo de TDD): `evidencias/pruebas/01_…txt`, `02_…txt`.

### 3.2 Cobertura de código (JaCoCo, % de líneas)

| Capa / paquete | v5.1.0 (126 pruebas) | v6.0.0 JUnit (216) | v6.0.0 JUnit + sistema |
|---|---|---|---|
| service (reglas de negocio) | 56.5 | 74.7 | 75.4 |
| dao.jdbc (PostgreSQL) | 0.0 | 33.0 | 59.9 |
| controller | 73.2 | 76.8 | 78.9 |
| web (ApiServer, filtros) | 71.1 | 73.0 | 82.2 |
| config | 37.0 | 64.4 | 80.5 |
| security | 86.6 | 86.6 | 86.6 |
| **Total backend – líneas** | **61.2** | **70.3** | **75.4** |
| **Total backend – ramas** | **51.7** | **59.5** | **62.4** |

Informe navegable: `evidencias/pruebas/reportes/cobertura_html/index.html`.

### 3.3 Rendimiento (PR-01: 50 usuarios, 60 s)

| Petición | Muestras | Promedio | p90 | p95 | Máx. | Error |
|---|---|---|---|---|---|---|
| 01 Login cajero | 50 | 292 ms | 572 ms | 617 ms | 910 ms | 0 % |
| 02 Panel (indicadores) | 578 | 678 ms | 926 ms | 983 ms | 1 206 ms | 0 % |
| 03 Listar clientes | 563 | 294 ms | 482 ms | 554 ms | 765 ms | 0 % |
| 04 Listar operaciones | 556 | 296 ms | 482 ms | 558 ms | 766 ms | 0 % |
| 05 Registrar cambio | 552 | 343 ms | 548 ms | 614 ms | 804 ms | 0 % |
| 06 Bandeja de solicitudes | 538 | 291 ms | 468 ms | 525 ms | 747 ms | 0 % |
| **Total** | **2 837** | **382 ms** | **707 ms** | **820 ms** | **1 206 ms** | **0 %** |

Criterios: p90 707 ms < 1 000 ms ✅ · errores 0 % < 1 % ✅ · 46.9 peticiones/s ≥ 20 ✅.
El panel es la petición más lenta (calcula 10 indicadores en cada llamada): ver OBS-01.

## 4. Registro de defectos

| ID | Defecto | Severidad | Detectado por | Estado |
|---|---|---|---|---|
| DEF-001 | La integración continua no copiaba los recursos (`semilla_100.tsv`): 9 pruebas fallaban en GitHub Actions desde v5.1.0 | Alta | PE (ejecución por consola) | ✅ Corregido (hotfix 5.1.1) |
| DEF-002 | Tasa de interés negativa: aceptada en modo memoria y rechazada por PostgreSQL (comportamiento distinto) | Media | PU-PRE-08, PI-BD-04, PU-EMP-04 | ✅ Corregido |
| DEF-003 | Tasa 1500 %, montos de 15 dígitos o `1e20` devolvían **HTTP 500** (desborde de NUMERIC) | Alta | PU-PRE-06/13, PU-OPE-07, PI-API-04, PI-NEW-15/18 | ✅ Corregido |
| DEF-004 | Plazo sin máximo: 100 000 meses → vencimiento en el año 10360 | Media | PU-PRE-17, PU-EMP-08 | ✅ Corregido (360 meses / 365 días) |
| DEF-005 | Estado inicial sin validar: se guardaban operaciones "HACKEADO" y clientes "VIP_SIN_CONTROL" | Alta | PU-OPE-10, ClienteServiceMockitoTest, PI-NEW-10/14 | ✅ Corregido |
| DEF-006 | Préstamo de S/ 0.01 generaba cuota S/ 0.00 | Baja | PU-PRE-02 | ✅ Corregido (mínimo S/ 100) |
| DEF-007 | Portal: una solicitud rechazada dejaba una **operación PENDIENTE huérfana** (y el artículo, en empeños) | Alta | PU-POR-06/07, PS-10, PI-NEW-28 | ✅ Corregido (validar antes de grabar) |
| DEF-008 | Mensajes engañosos: "Identificador inválido" para montos y "hay registros relacionados" (409) con cliente inexistente | Baja | PI-API-05, PI-NEW-16 | ✅ Corregido |
| DEF-009 | Títulos "Registros de registrar …" en 13 módulos | Baja | PS-06 | ✅ Corregido |
| DEF-010 | Portal en celular: el historial se desbordaba 41 px en horizontal | Media | PS-13 | ✅ Corregido |
| DEF-011 | Reglas de la base no replicadas en el servicio: tipo de cambio 0 y stock negativo aceptados en memoria | Media | PU-CAT-02/03 | ✅ Corregido |
| DEF-012 | La pantalla Usuarios muestra el formulario de alta a un CAJERO (la API sí lo bloquea con 403) | Baja | PS-08 (exploratoria) | 🕒 Diferido a v6.1 |
| DEF-013 | Si el navegador cancela una petición, el servidor registra "Error interno" en `sigsf-errores.log` (29 registros en la corrida E2E) | Baja | PS (log del servidor) | 🕒 Diferido a v6.1 |
| OBS-01 | `/api/dashboard` es la petición más lenta (p90 926 ms con 50 usuarios). Mejora propuesta: caché de 30 s | Mejora | PR-01 | 🕒 Planificado v6.1 |

**13 defectos: 11 corregidos y verificados, 2 de severidad baja diferidos; 0 abiertos de
severidad Alta o Crítica** → se cumple el criterio de aprobación de la versión.

## 5. Integración demostrada

| Capa | Cómo se evidencia |
|---|---|
| Base de datos | PI-BD-01…08 consultan PostgreSQL directamente (esquema, CHECK, disparador, conteos) |
| Backend | PU (servicios), PI-API (servidor HTTP real) y cobertura JaCoCo del servidor durante E2E |
| Frontend | PS-01…13 y PA en Chromium real, con capturas |
| Versionamiento | Ramas `feature/pruebas-*`, `hotfix/5.1.1`, `release/6.0.0`, etiqueta `v6.0.0` y CI |

## 6. Rutas compartidas

Repositorio: **https://github.com/MiguelWaa/SistemaFinanciero** (publicar con
`git push --all origin && git push origin --tags`).

| Recurso | Ruta |
|---|---|
| Plan de pruebas | `https://github.com/MiguelWaa/SistemaFinanciero/blob/main/docs/09_PLAN_DE_PRUEBAS.md` |
| Herramientas y resultados | `https://github.com/MiguelWaa/SistemaFinanciero/blob/main/docs/10_HERRAMIENTAS_Y_RESULTADOS.md` |
| Pruebas JUnit | `https://github.com/MiguelWaa/SistemaFinanciero/tree/main/test` |
| Colección Postman (importar en Postman) | `https://github.com/MiguelWaa/SistemaFinanciero/blob/main/pruebas/postman/SIGSF_API.postman_collection.json` |
| Pruebas E2E (Playwright) | `https://github.com/MiguelWaa/SistemaFinanciero/tree/main/pruebas/e2e` |
| Criterios de aceptación (Gherkin) | `https://github.com/MiguelWaa/SistemaFinanciero/tree/main/pruebas/aceptacion/features` |
| Plan de carga JMeter | `https://github.com/MiguelWaa/SistemaFinanciero/blob/main/pruebas/rendimiento/SIGSF_carga.jmx` |
| Evidencias y reportes | `https://github.com/MiguelWaa/SistemaFinanciero/tree/main/evidencias/pruebas` |
| Ejecuciones de CI | `https://github.com/MiguelWaa/SistemaFinanciero/actions` |
| Descarga de herramientas | junit.org · site.mockito.org · jacoco.org · postman.com/downloads · playwright.dev/python · pytest-bdd.readthedocs.io · jmeter.apache.org |

## 7. Cómo reproducir

```bash
PGPASSWORD=123456 sh scripts/pruebas/ejecutar_plan_de_pruebas.sh          # Linux / macOS / Git Bash
set PGPASSWORD=123456 && scripts\pruebas\ejecutar_plan_de_pruebas.bat    # Windows
COBERTURA=1 ...                                                           # además, JaCoCo
```

En NetBeans: **Run → Test Project (Alt+F6)** ejecuta las 216 pruebas JUnit; las de
PostgreSQL usan la base `SistemaFinanciero_test` (propiedad `test-sys-prop.sigsf.db.name`
en `nbproject/project.properties`).
