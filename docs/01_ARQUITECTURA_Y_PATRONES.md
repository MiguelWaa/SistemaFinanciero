# SIGSF · Arquitectura y patrones de software

**Proyecto:** Sistema Integral de Gestión de Servicios Financieros (bancos, casas de cambio y casas de empeño)
**Curso:** Integrador I Sistemas – Software (UTP) · **Taller:** MVC, TDD, DAO, SOLID

## 1. Vista general: frontend + backend + persistencia

```
┌──────────────────────────── FRONTEND (Vista) ────────────────────────────┐
│ web/*.html · sigsf-crud.js (motor de mantenimientos) · api.js (token)    │
│ login · panel · 13 módulos · Portal del Cliente · Respaldos y Auditoría  │
└───────────────────────────────┬──────────────────────────────────────────┘
                 HTTP + JSON    │  Authorization: Bearer <token>
┌───────────────────────────────▼──────────────── BACKEND (Java 17) ───────┐
│ web/ApiServer  (Front Controller · cabeceras seguras · manejo de errores)│
│   └─ FiltroSeguridad  (autenticación + autorización RBAC)               │
│ controller/*Controller  (Controladores MVC, uno por módulo)             │
│   └─ MapeadorEntrada (JSON → entidad)          DTO (entidad → JSON)     │
│ service/*Service  (Modelo: reglas de negocio, cálculos, validaciones)   │
│   └─ AuditoriaService (Observer)  · RespaldoService (Strategy)          │
│ dao/*Dao  (interfaces)  ← DaoFactory (Abstract Factory)                 │
└───────────────┬───────────────────────────────────────┬──────────────────┘
                │ dao/jdbc  (producción)                │ dao/memoria (TDD / demo)
┌───────────────▼──────────── PERSISTENCIA ─────────────▼──────────────────┐
│ PostgreSQL · base SistemaFinanciero · 32 tablas (+auditoría)            │
│ db/schema.sql · datos_ejemplo.sql · portal_cliente.sql · seguridad.sql  │
└──────────────────────────────────────────────────────────────────────────┘
```

## 2. Recorrido de una petición (ejemplo: registrar un cambio de moneda)

1. **Vista** — `operaciones financieras.html` envía `POST /api/operaciones` con `api.js`, que agrega el token.
2. **Front Controller** — `ApiServer` aplica cabeceras de seguridad y busca la ruta.
3. **Filtro** — `FiltroSeguridad` valida el token (`GestorSesiones`) y el permiso del rol (`PoliticaAcceso`). Sin token → 401; sin permiso → 403.
4. **Controlador** — `OperacionController` convierte el JSON en `Operacion` (`MapeadorEntrada`, que ignora `totalRecibir`).
5. **Modelo** — `OperacionService` valida cliente y monto, toma la cotización del `TipoCambioDao` y calcula `totalRecibir = monto × venta` con `BigDecimal`.
6. **DAO** — `OperacionDaoJdbc` inserta con `PreparedStatement` (sin concatenar datos → sin inyección SQL).
7. **Respuesta** — JSON 200; los errores de negocio vuelven como 400 con mensaje claro.
8. **Auditoría** — `AuditoriaService` (Observer) registra `CREAR · usuario · IP · recurso · HTTP`.

## 3. Patrones implementados (dónde está cada uno)

| Patrón | Clases | Para qué sirve en SIGSF |
|---|---|---|
| **MVC** | Vista: `web/*.html`; Controlador: `controller/*`; Modelo: `service/*` + `model/*` | Separar pantalla, rutas y reglas de negocio |
| **Front Controller** | `web/ApiServer` | Un único punto de entrada para todas las peticiones |
| **DAO** | 19 interfaces en `dao/`, implementaciones en `dao/jdbc` y `dao/memoria` | Aislar el SQL; los servicios no conocen la base de datos |
| **DTO** | `dto/UsuarioDTO`, `SesionDTO`, `LoginDTO`, `ClientePublicoDTO`, `SolicitudDTO`, `VentaReporteDTO`, `IndicadoresDTO`, `RespaldoDTO` | Enviar solo lo necesario (nunca la contraseña); armar resultados de JOIN |
| **Abstract Factory** | `dao/DaoFactory`, `JdbcDaoFactory`, `MemoriaDaoFactory` | Cambiar toda la persistencia con `persistencia=` en `db.properties` |
| **Template Method / genéricos** | `RepositorioMemoria<T>`, `ControladorCrud<E,S>`, `ServicioCrud<E,S>` | Reutilizar el CRUD; cada módulo solo agrega lo propio |
| **Strategy** | `PasswordHasher` → `Pbkdf2PasswordHasher`; `EstrategiaRespaldo` → `RespaldoPgDump`, `RespaldoJdbcSql`, `RespaldoMemoriaJson` | Intercambiar algoritmos de cifrado y de respaldo |
| **Chain of Responsibility / Intercepting Filter** | `web/Filtro`, `security/FiltroSeguridad` | Seguridad aplicada a toda la API antes de los controladores |
| **Observer** | `web/ObservadorPeticion`, `service/AuditoriaService` | Auditar sin mezclar código en los controladores |
| **Composition Root / Inyección de dependencias** | `config/Contenedor` | Único lugar donde se crean y conectan los objetos |
| **Singleton (por composición)** | Servicios y DAO creados una sola vez en `Contenedor` | Objetos sin estado compartidos por todos los hilos |

## 4. Principios SOLID

| Principio | Evidencia en el código |
|---|---|
| **S – Responsabilidad única** | Controlador = HTTP; Servicio = reglas; DAO = SQL; `MapeadorEntrada` = JSON→entidad; `AuditoriaService` = bitácora |
| **O – Abierto/cerrado** | Nueva persistencia = nueva `DaoFactory`; nuevo mantenimiento = `new ControladorCrud<>(...)` sin tocar `ApiServer`; nuevo algoritmo de respaldo = nueva `EstrategiaRespaldo` |
| **L – Sustitución de Liskov** | `ClienteDaoJdbc` y `ClienteDaoMemoria` son intercambiables: las 77 pruebas usan la de memoria y el sistema real la de JDBC |
| **I – Segregación de interfaces** | `LecturaDao`, `EscrituraDao`, `ActualizableDao`: `OperacionDao` y `ArticuloDao` no implementan `actualizar` porque una operación no se edita |
| **D – Inversión de dependencias** | Los servicios reciben interfaces por constructor: `new OperacionService(OperacionDao, TipoCambioDao)`; nunca hacen `new ...DaoJdbc()` |

## 5. Mapa de paquetes (`src/com/utp/sistemafinanciero`)

| Paquete | Archivos | Contenido |
|---|---|---|
| `model/*` | 32 | Entidades (POJO) por módulo: seguridad, clientes, operaciones, ventas, almacén, marketing, logística, producción, dirección, gerencia |
| `dto` | 8 | Objetos de transferencia |
| `dao` | 24 | Interfaces DAO + interfaces genéricas + `DaoFactory` |
| `dao/jdbc` | 20 | Implementación PostgreSQL |
| `dao/memoria` | 22 | Implementación en memoria + datos semilla |
| `service` | 22 | Reglas de negocio |
| `controller` | 18 | Controladores REST por módulo |
| `security` | 9 | Cifrado, sesiones, RBAC, limitador de intentos, filtro |
| `backup` | 5 | Respaldo con estrategias |
| `web` | 7 | Servidor HTTP, contexto, filtros, rutas |
| `config` | 2 | Configuración y contenedor de dependencias |
| `util` | 3 | JSON, Excel, JDBC |

**Total:** 174 clases Java (~11 400 líneas) + 16 clases de prueba (77 pruebas).

## 6. Módulos del negocio y su controlador

| Área | Vista (web/) | Controlador | Servicio(s) | Endpoints principales |
|---|---|---|---|---|
| Marketing | Marketing.html | MarketingController | CampaniaService, ProspectoService | `/api/campanias`, `/api/prospectos` |
| Ventas | Ventas.html | VentaController | VentaService | `/api/ventas`, `/api/ventas/excel` |
| Logística | Logistica.html | LogisticaController | ProveedorService | `/api/proveedores` |
| Almacén | almacen.html | AlmacenController | ProductoService | `/api/productos`, `/api/productos/excel` |
| Producción | produccion.html | ProduccionController | ProduccionService | `/api/lineas`, `/api/ordenes` |
| Operaciones | operaciones financieras.html | OperacionController | Operacion/TipoCambio/Prestamo/EmpenoService | `/api/operaciones`, `/api/tipos-cambio`, `/api/prestamos`, `/api/empenos` |
| Dirección y Gerencia | direccion gerencia.html, gerencia general… | DireccionController, SistemaController | ExpedienteService, DashboardService | `/api/expedientes`, `/api/dashboard` |
| Clientes y servicios | clientes y servicios financieros.html | ClienteController | ClienteService, ServicioFinancieroService | `/api/clientes`, `/api/servicios` |
| Seguridad y usuarios | usuarios.html, seguridad.html | AuthController, SeguridadController | AuthService, UsuarioService, RolService | `/api/auth/*`, `/api/usuarios`, `/api/roles`, `/api/auditoria` |
| Portal del cliente | portal.html, portal-inicio.html | PortalController | PortalService | `/api/portal/**` |
| Solicitudes | solicitudes.html | SolicitudController | SolicitudService | `/api/solicitudes/**` |
| Respaldos | respaldos.html | RespaldoController | RespaldoService | `/api/respaldos/**` |
