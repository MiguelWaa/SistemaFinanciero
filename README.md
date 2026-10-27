# SIGSF · Sistema Integral de Gestión de Servicios Financieros

Proyecto del curso **Curso Integrador I Sistemas - Software** (UTP).
Sistema web de gestión para **bancos, casas de cambio y casas de empeño**,
con frontend, backend en Java y persistencia en **PostgreSQL**.

- **Integrantes:** Miguel Ángel Machaca Machaca · Ricardo Gabriel Cervantes Tejada · Cesar Huaripaucar Huaman
- **Docente:** Rene Alonso Nieto Valencia

---

## 🆕 Versión 5.3 · Interfaz animada

Capa visual `web/sigsf-anim.css` + `web/sigsf-anim.js` en todas las páginas: degradados en movimiento, indicadores con ícono y números que cuentan, entradas escalonadas, botones con brillo y onda, gráficos que se dibujan, ticket de pago con escaneo y confeti al confirmar un pago. Respeta «reducir movimiento». Capturas: `evidencias/v53_*.png`.

---

## 🆕 Versión 5.2 · Pagos con QR (Yape, Plin, Tunki), cotizaciones e interfaz Bootstrap 5

| Tema | Qué se implementó | Dónde |
|---|---|---|
| **Pagar con QR desde el portal** | El cliente elige qué paga, ve el QR de cobro de la empresa (Yape, Plin o Tunki), paga en su app y registra el número de operación y, si quiere, la captura de la constancia | `web/portal-inicio.html` › *Pagar con QR* |
| **El pago aparece en el sistema** | Queda **Por verificar** con aviso en el panel; caja lo confirma (baja el saldo del préstamo o empeño y emite el comprobante `RC-…`) o lo rechaza con motivo | `web/pagos.html`, `service/PagoService.java` |
| **Cobro en caja y billeteras** | Registro de pagos en ventanilla; el ADMIN configura número, titular y **sube el QR** de cada billetera | `pagos.html` › *Cobrar en caja* / *Billeteras y QR* |
| **Cotizaciones** | Préstamo (cuota y cronograma), empeño y cambio con número `COT-…`, vigencia de 7 días, impresión/PDF y conversión en solicitud | portal › *Cotizar*, `pagos.html` › *Cotizaciones* |
| **Reportes** | Comprobante, cotización, estado de cuenta y reporte de pagos imprimibles; pagos y cotizaciones en Excel/CSV/JSON | `documento.html`, `portal-documento.html`, `reportes.html` |
| **Interfaz empresarial** | Bootstrap 5.3.3 local (sin CDN), tema propio, tablas con orden y paginación, avisos y confirmaciones; panel con menú agrupado y adaptable a celular | `web/vendor/`, `sigsf-ui.css`, `sigsf-ui.js`, `sigsf-crud.js` |
| **Rendimiento** | Caché y compresión gzip de archivos estáticos, más hilos de atención | `web/ApiServer.java` |
| **Base de datos** | Esquema **5.2.0**: tablas `billetera`, `pago`, `cotizacion` (se crean solas al arrancar o con `db/pagos_qr.sql`) | `db/pagos_qr.sql` |
| **Pruebas** | 126 → **151** (`PagoServiceTest`, `CotizacionServiceTest`, `PagosApiTest`); arreglado el script de CI que no copiaba `semilla_100.tsv` | `test/`, `scripts/compilar_y_probar.*` |

**Probarlo en 1 minuto (sin PostgreSQL):** en `db.properties` poner `persistencia=memoria`, ejecutar y entrar a
<http://localhost:8091/portal.html> con el documento `70000002` (clave `70000002`) → *Pagar con QR*. Luego, en
<http://localhost:8091/login.html> con `lucia.ramos@sigsf.com` / `lucia123` → *Pagos y cobranzas* → *Confirmar pago*.
Detalle en `docs/09_PAGOS_QR_Y_COTIZACIONES.md`.

---

## 🆕 Versión 5.1 · Avance 3: 100 registros por módulo

| Tema | Qué se implementó | Dónde |
|---|---|---|
| **Datos de demostración (PostgreSQL)** | 100 registros por módulo en las 32 tablas (300 operaciones); re-ejecutable, sin borrar datos, claves foráneas por clave natural | `db/datos_100_registros.sql` |
| **Datos de demostración (memoria)** | Los mismos datos con `persistencia=memoria`; `memoria.datos.masivos=false` los desactiva | `dao/memoria/DatosMasivos.java`, `semilla_100.tsv` |
| **Generador** | Determinista (semilla 20261025): produce el SQL y el TSV desde una sola fuente | `scripts/datos/generar_datos_100.py` |
| **Pruebas** | 117 → **126** (`DatosMasivosTest`) | `test/.../dao/memoria/` |

**Cómo cargarlos:** en pgAdmin, después de `schema.sql`, `datos_ejemplo.sql`, `portal_cliente.sql` y `seguridad.sql`, ejecutar `db/datos_100_registros.sql`.
Usuarios generados: `<correo>` / `sigsf123`. Clientes del portal generados: documento `71000001` a `71000100` (clave = documento).
Detalle en `docs/08_DATOS_DE_DEMOSTRACION.md`.

---

## 🆕 Versión 5.0 · Sistema de control de versiones (Git)

Tercera entrega del taller. El proyecto ahora es un **repositorio Git** gestionado con **Git Flow**.

| Tema | Qué se implementó | Dónde |
|---|---|---|
| **Repositorio y ramas** | `main` (producción), `develop` (integración), `feature/*`, `release/*`, `hotfix/*`; merges `--no-ff`; etiquetas `v4.0.0`, `v5.0.0`, `v5.0.1` | `docs/07_CONTROL_DE_VERSIONES.md` |
| **Commits** | Conventional Commits validados por hook `commit-msg`; autoría por integrante | `.githooks/` |
| **Conflictos y rollback** | Conflicto real resuelto en `CHANGELOG.md`; `git revert -m 1` de un merge que rompía las pruebas | `docs/07` §6 y §7 |
| **Seguridad del repositorio** | `db.properties` fuera de Git (plantilla `db.properties.ejemplo`); hook `pre-commit` contra credenciales y archivos generados | `.gitignore`, `.githooks/pre-commit` |
| **Versión en la aplicación** | Pantalla **Control de versiones** (versión, commits, red de ramas, etiquetas, esquema BD) y versión en el pie del panel; `GET /api/version`, `GET /api/versiones` (ADMIN) | `web/versiones.html`, paquete `version/` |
| **Versionamiento de la BD** | Tabla `version_esquema` (1.0.0 → 5.0.0) creada por `db/version_esquema.sql` o al arrancar | `db/version_esquema.sql` |
| **Build e integración continua** | `version.properties` con el commit dentro del .jar; GitHub Actions compila y ejecuta **126 pruebas** | `build.xml`, `.github/workflows/ci.yml` |
| **Documentación** | Inventario v5.0 (T-25…T-34 + inventario de artefactos), `CHANGELOG.md`, `CONTRIBUTING.md`, `VERSION` | `docs/02`, raíz |

> **Al clonar:** `scripts\git\instalar_hooks.bat` y `copy db.properties.ejemplo db.properties`.

---

## 🆕 Versión 4.0 · Recursos de código fuente, inventario y seguridad Java

Segunda entrega del taller. Agrega:

| Tema | Qué se implementó | Dónde |
|---|---|---|
| **Registro (Logback)** | SLF4J 2.0 + Logback 1.5: log general, de acceso, de seguridad, de errores y **un archivo por área** (`logs/areas/MARKETING.log`…). Rotación diaria/10 MB, protección contra *log injection*. | `src/logback.xml`, `test/logback-test.xml`, `web/AreaNegocio` |
| **Exportación de ficheros** | Excel (.xlsx con formato), CSV (UTF-8, RFC 4180) y JSON en **15 recursos de las 9 áreas**. | `GET /api/{recurso}/exportar?formato=xlsx\|csv\|json` |
| **Importación de ficheros** | CSV o Excel con **vista previa**, plantilla descargable, reporte de errores por fila; pasa por las mismas reglas del servicio. Las operaciones de dinero no se importan. | `POST /api/{recurso}/importar`, `GET /api/{recurso}/plantilla` |
| **Seguridad Java** | Lector XLSX sin XXE ni bomba ZIP, neutralización de fórmulas, lista blanca de extensiones, límite de filas y tamaño, auditoría IMPORTAR/EXPORTAR. | paquete `archivos/` |
| **Pruebas** | 77 → **99 pruebas** (nuevas: `LectorCsvTest`, `EscrituraArchivosTest`, `ArchivoServiceTest`, `ArchivosApiTest`, `LogbackConfigTest`). | `test/` |
| **Documentación** | `docs/06_RECURSOS_DE_CODIGO_FUENTE.md` (paquetes, librerías, dependencias, logback, ficheros por área), inventario v4.0 y seguridad Java. | `docs/` |

> **Antes de compilar:** ejecutar `scripts\descargar_librerias.bat` (descarga SLF4J y Logback a `lib/`).

---

## ✨ Versión Taller de Arquitectura (MVC · TDD · DAO · SOLID)

Esta versión reorganiza el backend en una arquitectura por capas con patrones
de diseño, agrega seguridad completa, respaldo de la base de datos y pruebas
automatizadas. Documentación detallada en la carpeta **`docs/`**:

| Documento | Contenido |
|---|---|
| `docs/01_ARQUITECTURA_Y_PATRONES.md` | Capas frontend + backend + persistencia, MVC, DAO, DTO, SOLID |
| `docs/02_INVENTARIO_DE_DOCUMENTACION.md` | Inventario por área (Marketing, Ventas, Logística, Almacén, Producción, Operaciones, Dirección…) |
| `docs/03_CONSIDERACIONES_DE_SEGURIDAD.md` | Controles OWASP transversales y por área |
| `docs/04_RESPALDO_Y_CONFIGURACION_BD.md` | Backup, restauración, rol de mínimo privilegio |
| `docs/05_PRUEBAS_TDD.md` | 99 pruebas JUnit 4 |
| `docs/06_RECURSOS_DE_CODIGO_FUENTE.md` | Paquetes, librerías, dependencias, Logback, importación y exportación de ficheros por área |
| `docs/07_CONTROL_DE_VERSIONES.md` | Git Flow, commits, merge, conflictos, rollback, etiquetas, remoto, CI, versionamiento de la BD |
| `docs/09_PAGOS_QR_Y_COTIZACIONES.md` | Pagos con Yape/Plin/Tunki, verificación, cotizaciones, documentos imprimibles e interfaz Bootstrap 5 |

**Qué cambió (resumen):**

- **MVC:** `Rutas` se dividió en 15 controladores (`controller/`), uno por módulo.
- **DAO:** 19 interfaces (`dao/`) con dos implementaciones: `dao/jdbc` (PostgreSQL) y `dao/memoria` (pruebas / demo), elegidas con `persistencia=` en `db.properties` (Abstract Factory).
- **DTO:** `dto/` (la API ya no devuelve hashes de contraseña).
- **SOLID:** los servicios reciben sus dependencias por constructor; todo se conecta en `config/Contenedor`.
- **Seguridad:** sesión con token (30 min), permisos por rol, PBKDF2 con sal, bloqueo por intentos, cabeceras seguras, auditoría. Ver tabla de permisos en la pantalla **Seguridad**.
- **Respaldo:** pantalla **Respaldos y auditoría**, respaldo automático cada 24 h, `scripts/*.bat`, `db/seguridad.sql`.
- **TDD:** carpeta `test/` (Alt + F6 en NetBeans).

**Modo demostración (sin PostgreSQL):** en `db.properties` poner `persistencia=memoria`
y ejecutar. Carga los datos de ejemplo en memoria (se pierden al cerrar).

### Permisos por rol

| Módulo | ADMIN | SUPERVISOR | CAJERO | ANALISTA |
|---|---|---|---|---|
| Usuarios, respaldos, auditoría | ✔ | — | — | — |
| Clientes, operaciones, préstamos, empeños, solicitudes | ✔ | ✔ | ✔ | consulta |
| Ventas | ✔ | consulta | ✔ | consulta |
| Marketing, logística, producción, almacén | ✔ | consulta | consulta (almacén) | ✔ |
| Expedientes (Dirección / Gerencia) | ✔ | ✔ | — | ✔ |
| Tipos de cambio, servicios | ✔ | consulta | consulta | consulta |

Todas las rutas `/api/**` exigen la cabecera `Authorization: Bearer <token>`
(salvo `/api/health`, `/api/auth/login`, `/api/portal/login` y `/api/portal/registro`).

---

## Arquitectura

El sistema está organizado en capas, sin frameworks externos (solo el JDK,
el driver JDBC de PostgreSQL y SLF4J/Logback para el registro):

```
Navegador (frontend HTML/CSS/JS en carpeta web/)
        │  fetch → JSON
        ▼
ApiServer  (servidor HTTP del JDK: com.sun.net.httpserver)
        │
Rutas      (define los endpoints /api/**)
        │
service/   (reglas de negocio: validaciones, cálculos de préstamos, etc.)
        │
dao/       (acceso a datos con JDBC y SQL explícito)
        │
util/Db    (conexión a la base de datos)
        ▼
PostgreSQL (base SistemaFinanciero · 32 tablas)
```

- `model/` — 31 clases del dominio (POJOs) agrupadas por módulo.
- `dao/` — un DAO por entidad principal, con SQL escrito a mano (fines didácticos).
- `service/` — lógica de negocio y validaciones (`ReglaNegocioException` → HTTP 400).
- `web/` (paquete) — `ApiServer`, `Rutas`, `Ctx`, `Handler`: API REST + servidor de archivos.
- `web/` (carpeta) — el frontend completo, servido por el mismo servidor (mismo origen).
- `config/`, `util/` — configuración y utilidades (JSON, contraseñas, JDBC).

El frontend se sirve desde el **mismo** servidor que la API, así que no hay
problemas de CORS y todo funciona abriendo una sola URL.

---

## Requisitos

- **JDK 17** o superior (el proyecto está configurado para NetBeans + Ant).
- **PostgreSQL** con **pgAdmin 4**.
- **Driver JDBC de PostgreSQL** (`postgresql-42.x.jar`) — ver `lib/LEEME.txt`.
- **SLF4J 2.0.16 + Logback 1.5.12** (3 .jar) — `scripts\descargar_librerias.bat`.

---

## Puesta en marcha (3 pasos)

### 1) Base de datos (pgAdmin)
1. Crea una base de datos llamada **`SistemaFinanciero`** (con S y F mayúsculas).
2. En el **Query Tool**, ejecuta **`db/schema.sql`** (crea 31 tablas).
3. Luego ejecuta **`db/datos_ejemplo.sql`** (carga datos de prueba).
4. (Opcional) **`db/portal_cliente.sql`**, **`db/seguridad.sql`** y **`db/datos_100_registros.sql`** (100 registros por módulo).

Detalle paso a paso en **`db/LEEME.txt`**.

### 2) Driver JDBC
Descarga `postgresql-42.x.jar` desde <https://jdbc.postgresql.org/download/>,
cópialo en la carpeta **`lib/`** y renómbralo a **`postgresql.jar`**.
(También puedes agregarlo desde *Properties → Libraries* en NetBeans.)
Detalle en **`lib/LEEME.txt`**.

Si tu usuario/contraseña de PostgreSQL no son los de por defecto, edítalos en
**`db.properties`** (raíz del proyecto).

### 3) Ejecutar
Abre el proyecto en **NetBeans** y presiona **Run (F6)**.
Se ejecuta `com.utp.sistemafinanciero.ServerMain`, que:

- comprueba la conexión a PostgreSQL,
- levanta el servidor en **<http://localhost:8091>** (puerto de `server.port`).

Abre esa URL en el navegador: verás la pantalla de inicio (splash) → login.

---

## Usuarios de acceso

| Correo                   | Contraseña | Rol       |
|--------------------------|------------|-----------|
| `admin@sigsf.com`        | `admin123` | ADMIN     |
| `lucia.ramos@sigsf.com`  | `lucia123` | CAJERO    |
| `jorge.torres@sigsf.com` | `jorge123` | ANALISTA  |

Las contraseñas se guardan con **PBKDF2-HMAC-SHA256 y sal** (los hashes SHA-256 antiguos de `datos_ejemplo.sql` se convierten solos en el primer inicio de sesión).

---

## Endpoints principales de la API

Base: `http://localhost:8091/api`

| Método | Ruta                     | Descripción                          |
|--------|--------------------------|--------------------------------------|
| GET    | `/health`                | Estado del servidor                  |
| POST   | `/auth/login`            | Iniciar sesión                       |
| GET    | `/dashboard`             | Indicadores (KPIs) del panel         |
| GET/POST/PUT/DELETE | `/usuarios`   | Gestión de usuarios                  |
| GET/POST/PUT/DELETE | `/clientes`   | Clientes                             |
| GET/POST/PUT/DELETE | `/servicios`  | Servicios financieros                |
| GET/POST/PUT/DELETE | `/tipos-cambio` | Tipos de cambio                    |
| GET/POST/DELETE     | `/operaciones`| Operaciones (cambio, etc.)           |
| GET/POST/DELETE     | `/prestamos`  | Préstamos (calcula cuota e interés)  |
| GET/POST/DELETE     | `/empenos`    | Empeños                              |
| GET/POST/PUT/DELETE | `/productos`  | Almacén                              |
| GET/POST/PUT/DELETE | `/proveedores`| Logística                            |
| GET/POST/PUT/DELETE | `/campanias` · `/prospectos` | Marketing                |
| GET/POST/DELETE     | `/ventas`     | Ventas                               |
| GET/POST/PUT/DELETE | `/expedientes`| Dirección / Gerencia                 |
| GET/POST/PUT        | `/pagos` · `/pagos/{id}/confirmar` · `/pagos/{id}/rechazar` | Pagos con QR y caja (v5.2) |
| GET/PUT             | `/billeteras` | Yape, Plin y Tunki de la empresa (v5.2) |
| GET/POST            | `/cotizaciones` | Cotizaciones (v5.2)                 |

Ejemplo de login con `curl`:

```bash
curl -X POST http://localhost:8091/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sigsf.com","password":"admin123"}'
# respuesta: {"token":"...", "rol":"ADMIN", ...}
curl http://localhost:8091/api/clientes -H "Authorization: Bearer <token>"
```

---

## Notas

- El panel de inicio (`index.html`) muestra indicadores **reales** calculados
  desde la base de datos (ingresos del mes, clientes activos, operaciones de
  hoy, préstamos colocados y expedientes pendientes).
- La pantalla **Usuarios** está totalmente conectada (listar, crear y eliminar)
  y sirve de **plantilla** para conectar los demás módulos con el mismo patrón
  (usando `web/api.js`).
- Si el servidor inicia pero la base de datos no está disponible, el frontend
  se sigue mostrando y las llamadas a la API informan el error con claridad.

---

## Módulos operativos y Excel de ventas (nuevo)

Ya son funcionales, conectados a la base de datos, estos módulos (cada uno
tiene un panel de registro + tabla en vivo al abrirlo desde el panel):

- **Clientes y Servicios Financieros** → registrar y listar clientes.
- **Operaciones Financieras** → registrar cambios de moneda (calcula el total
  a recibir automáticamente) y listarlos.
- **Ventas** → registrar ventas y listarlas.
- **Usuarios** → crear, listar y eliminar usuarios.

**Excel de ventas:** cada vez que se registra una venta, el sistema actualiza
el archivo **`reportes/ventas.xlsx`** (se crea solo en la carpeta del proyecto).
Además, en el módulo Ventas hay un botón **"Descargar Excel"** que baja el
archivo al instante. También puede abrirse directo en el navegador:
`http://localhost:PUERTO/api/ventas/excel`.

El Excel se genera con un componente propio en Java puro (sin librerías
externas), en `util/ExcelExporter.java`.

---

## Portal del Cliente (parte pública)

Además del sistema interno (para el personal), el proyecto incluye un
**Portal del Cliente**: una web aparte donde la propia persona entra con su
cuenta y realiza sus trámites en línea.

**Cómo se entra:** desde la pantalla de bienvenida, botón *"Soy cliente ·
Portal del Cliente"*, o directamente en `http://localhost:PUERTO/portal.html`.

**Qué puede hacer el cliente:**

- **Crear su cuenta** (registro con documento, nombres y contraseña) o
  **iniciar sesión** con su número de documento.
- **Solicitar cambio de moneda** → se calcula cuánto recibirá.
- **Solicitar un préstamo** → se calcula la cuota mensual al instante.
- **Solicitar un empeño** → registra el artículo que deja en garantía.
- **Ver su historial** de trámites con el estado de cada uno.
- **Actualizar sus datos** de contacto.

Todas las solicitudes se guardan con estado **PENDIENTE**, de modo que el
personal las ve desde el sistema interno (módulo Operaciones) y puede
atenderlas. Así se conectan las dos caras del sistema.

**Antes de usarlo por primera vez** hay que ejecutar en pgAdmin el script
**`db/portal_cliente.sql`** (crea la tabla de accesos; no borra nada de lo
que ya tengas). A los clientes que ya existían se les habilita el acceso con
su **número de documento como contraseña inicial**.

Archivos: `web/portal.html` (ingreso/registro), `web/portal-inicio.html`
(panel del cliente), `service/PortalService.java` (lógica) y las rutas
`/api/portal/**`.

---

## Módulos rediseñados y funcionales (versión final)

Todas las páginas de gestión fueron **rehechas** sobre un motor común
(`web/sigsf-modulo.css` + `web/sigsf-crud.js`). Cada módulo ahora **registra,
lista, edita, elimina y busca** de verdad contra PostgreSQL, con validación de
campos obligatorios y mensajes de error claros.

| Página | Qué gestiona |
|---|---|
| Clientes y Servicios Financieros | Clientes · Servicios (2 pestañas) |
| Operaciones Financieras | Operaciones (aprobar/anular) · Tipos de cambio · Préstamos · Empeños |
| Ventas | Ventas + Excel automático |
| Marketing | Prospectos · Campañas |
| Almacén | Productos con alerta de stock bajo |
| Logística | Proveedores |
| Producción | Líneas · Órdenes (avance 100% = terminada) |
| Dirección | Expedientes con aprobar / rechazar |
| Gerencia General | Decisiones: aprobar / observar / rechazar |
| Usuarios | Usuarios · Roles |
| Seguridad | Roles y permisos |
| Reportes | Indicadores en vivo + 4 descargas Excel |
| Configuración | Tipos de cambio + estado del servidor |

Cada módulo incluye: **indicadores (KPIs) calculados en vivo**, buscador,
botones de editar/eliminar por fila y, donde aplica, exportación a Excel.

**Exportaciones disponibles:** `/api/ventas/excel`, `/api/clientes/excel`,
`/api/operaciones/excel`, `/api/productos/excel`.

El diseño anterior de las páginas ya no se guarda como copia manual: queda en
el historial de Git (etiqueta `v4.0.0`). Para consultarlo:
`git show v4.0.0:web/_respaldo_diseno_anterior/login.html`.

> Nota: el módulo de Producción usa dos tablas nuevas del backend
> (`linea_produccion` y `orden_produccion`) que ya venían en `db/schema.sql`,
> por lo que **no hay que ejecutar ningún script adicional**.

---

## Bandeja de solicitudes, Excel corregido y accesos animados

**1. Solicitudes de clientes (nuevo módulo del administrador).**
Todo lo que un cliente pide desde el Portal aparece en la tarjeta roja
**"Solicitudes de clientes"** del panel, con un contador de pendientes que se
actualiza solo. Dentro hay dos pestañas:

- **Solicitudes:** cada pedido con el nombre del cliente, su documento, el
  detalle del trámite (cuota y plazo si es préstamo, artículo si es empeño) y
  los botones **Aprobar** / **Rechazar**. Al aprobar, el préstamo o empeño
  asociado queda **ACTIVO** automáticamente. La lista se refresca sola.
- **Cuentas de clientes:** aquí se dan los permisos — **Activar** o
  **Bloquear** la cuenta con la que cada cliente entra al Portal.

Endpoints: `/api/solicitudes`, `/api/solicitudes/pendientes`,
`/api/solicitudes/{id}/aprobar`, `/api/solicitudes/{id}/rechazar`,
`/api/clientes/{id}/estado`.

**2. Excel de Almacén corregido.** El enrutador confundía la palabra "excel"
con un número de registro, por lo que `/api/productos/excel` (y los de
clientes y operaciones) devolvían error. Ahora el servidor resuelve primero
las rutas exactas y después las que llevan parámetros, así que **las cuatro
exportaciones funcionan**.

**3. Pantallas de ingreso rediseñadas.** `login.html` y `portal.html` ahora
tienen fondo degradado en movimiento, burbujas flotantes, logo con animación,
campos que se elevan al enfocarlos, botón con brillo, aviso de error con
vibración, mostrar/ocultar contraseña y opción "Recordarme".

---

## Panel con clientes en vivo y portal del cliente mejorado

**Panel del administrador (`index.html`):**

- Los indicadores ya no se caen: cada KPI se calcula por separado, así que si
  una consulta falla el resto sigue mostrándose (antes bastaba un error para
  dejar todo en "—").
- Se quitaron los porcentajes inventados del diseño original; ahora cada
  tarjeta muestra datos reales: ventas registradas, total de clientes y
  solicitudes pendientes.
- Nueva sección **"Últimos clientes registrados"**: muestra los 6 clientes más
  recientes con avatar, documento, contacto, estado y fecha, más el total en
  una etiqueta. Se actualiza sola cada 30 segundos, así que todo cliente que
  registres (o que se registre desde el Portal) aparece ahí.

**Portal del Cliente (`portal-inicio.html`):** encabezado con degradado y
luces, logo con movimiento, tarjetas de trámite que se elevan al pasar el
mouse, campos que reaccionan al foco, botones con destello y un **resumen
personal** (trámites, en revisión, aprobados y monto solicitado) que se
calcula con los datos reales del cliente.

---

### Corrección: indicadores del panel en cero

Las consultas de conteo (`SELECT COUNT(*)`) devuelven en PostgreSQL un valor
de tipo `BIGINT`, pero el código lo leía como `Integer`, y el driver JDBC
rechazaba esa conversión. Por eso los KPIs de clientes activos, operaciones
del día, stock bajo, expedientes pendientes y usuarios salían vacíos.

Solución: esos conteos se leen ahora con `rs.getInt("n")` en los DAOs
correspondientes (ClienteDao, OperacionDao, ProductoDao, UsuarioDao y
ExpedienteAprobacionDao). Las sumas (`SUM`) no se vieron afectadas porque
devuelven `numeric`, que sí corresponde a `BigDecimal`.
