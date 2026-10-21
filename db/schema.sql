-- ============================================================
--  SIGSF · Sistema Integral de Gestion de Servicios Financieros
--  Esquema de base de datos para PostgreSQL
--  Curso Integrador I Sistemas - Software · UTP
--
--  Uso en pgAdmin:
--    1) Crea una base de datos llamada  sistema_financiero
--    2) Abre el Query Tool sobre esa base de datos
--    3) Ejecuta este archivo (schema.sql)
--    4) Luego ejecuta datos_ejemplo.sql para cargar datos de prueba
-- ============================================================

-- Se eliminan las tablas si existen (para poder re-ejecutar el script).
DROP TABLE IF EXISTS resolucion_gerencial   CASCADE;
DROP TABLE IF EXISTS decision_gerencia       CASCADE;
DROP TABLE IF EXISTS decision_direccion      CASCADE;
DROP TABLE IF EXISTS reporte_direccion       CASCADE;
DROP TABLE IF EXISTS expediente_aprobacion   CASCADE;
DROP TABLE IF EXISTS orden_produccion        CASCADE;
DROP TABLE IF EXISTS linea_produccion        CASCADE;
DROP TABLE IF EXISTS movimiento_logistica    CASCADE;
DROP TABLE IF EXISTS proveedor               CASCADE;
DROP TABLE IF EXISTS actividad_marketing     CASCADE;
DROP TABLE IF EXISTS campania_prospecto      CASCADE;
DROP TABLE IF EXISTS campania                CASCADE;
DROP TABLE IF EXISTS prospecto               CASCADE;
DROP TABLE IF EXISTS movimiento_inventario   CASCADE;
DROP TABLE IF EXISTS inventario              CASCADE;
DROP TABLE IF EXISTS producto                CASCADE;
DROP TABLE IF EXISTS detalle_venta           CASCADE;
DROP TABLE IF EXISTS venta                   CASCADE;
DROP TABLE IF EXISTS comprobante             CASCADE;
DROP TABLE IF EXISTS empeno                  CASCADE;
DROP TABLE IF EXISTS articulo                CASCADE;
DROP TABLE IF EXISTS prestamo                CASCADE;
DROP TABLE IF EXISTS operacion               CASCADE;
DROP TABLE IF EXISTS tipo_cambio             CASCADE;
DROP TABLE IF EXISTS cliente_servicio        CASCADE;
DROP TABLE IF EXISTS cliente                 CASCADE;
DROP TABLE IF EXISTS servicio_financiero     CASCADE;
DROP TABLE IF EXISTS usuario                 CASCADE;
DROP TABLE IF EXISTS rol_permiso             CASCADE;
DROP TABLE IF EXISTS permiso                 CASCADE;
DROP TABLE IF EXISTS rol                     CASCADE;

DROP TABLE IF EXISTS auditoria               CASCADE;

-- =============================== SEGURIDAD ===============================

CREATE TABLE rol (
    id_rol       SERIAL PRIMARY KEY,
    nombre       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(200)
);

CREATE TABLE permiso (
    id_permiso   SERIAL PRIMARY KEY,
    nombre       VARCHAR(80)  NOT NULL,
    descripcion  VARCHAR(200)
);

CREATE TABLE rol_permiso (
    id_rol      INTEGER NOT NULL REFERENCES rol(id_rol) ON DELETE CASCADE,
    id_permiso  INTEGER NOT NULL REFERENCES permiso(id_permiso) ON DELETE CASCADE,
    PRIMARY KEY (id_rol, id_permiso)
);

CREATE TABLE usuario (
    id_usuario      SERIAL PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    apellido        VARCHAR(80),
    email           VARCHAR(120) NOT NULL UNIQUE,
    password        VARCHAR(200) NOT NULL,
    id_rol          INTEGER REFERENCES rol(id_rol),
    estado          VARCHAR(20)  DEFAULT 'ACTIVO',
    fecha_registro  TIMESTAMP    DEFAULT now()
);

-- =============================== CLIENTES ===============================

CREATE TABLE servicio_financiero (
    id_servicio   SERIAL PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    tipo_servicio VARCHAR(60),
    descripcion   TEXT,
    estado        VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE cliente (
    id_cliente       SERIAL PRIMARY KEY,
    tipo_documento   VARCHAR(20),
    documento        VARCHAR(20) UNIQUE,
    nombres          VARCHAR(120) NOT NULL,
    apellidos        VARCHAR(120),
    fecha_nacimiento DATE,
    telefono         VARCHAR(30),
    correo           VARCHAR(120),
    direccion        VARCHAR(200),
    estado           VARCHAR(20) DEFAULT 'ACTIVO',
    fecha_registro   TIMESTAMP DEFAULT now()
);

CREATE TABLE cliente_servicio (
    id_cliente_servicio SERIAL PRIMARY KEY,
    id_cliente   INTEGER NOT NULL REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    id_servicio  INTEGER NOT NULL REFERENCES servicio_financiero(id_servicio),
    fecha_inicio DATE,
    fecha_fin    DATE,
    estado       VARCHAR(20) DEFAULT 'ACTIVO'
);

-- ============================= OPERACIONES =============================

CREATE TABLE tipo_cambio (
    id_tipo_cambio SERIAL PRIMARY KEY,
    moneda_origen  VARCHAR(10) NOT NULL,
    moneda_destino VARCHAR(10) NOT NULL,
    compra         NUMERIC(12,4) NOT NULL,
    venta          NUMERIC(12,4) NOT NULL,
    fecha_inicio   DATE DEFAULT CURRENT_DATE,
    estado         VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE operacion (
    id_operacion   SERIAL PRIMARY KEY,
    id_cliente     INTEGER REFERENCES cliente(id_cliente),
    id_tipo_cambio INTEGER REFERENCES tipo_cambio(id_tipo_cambio),
    tipo_operacion VARCHAR(30),
    moneda_origen  VARCHAR(10),
    moneda_destino VARCHAR(10),
    monto          NUMERIC(14,2),
    tipo_cambio    NUMERIC(12,4),
    total_entregar NUMERIC(14,2),
    total_recibir  NUMERIC(14,2),
    estado         VARCHAR(20) DEFAULT 'COMPLETADA',
    fecha_operacion TIMESTAMP DEFAULT now()
);

CREATE TABLE prestamo (
    id_prestamo       SERIAL PRIMARY KEY,
    id_operacion      INTEGER REFERENCES operacion(id_operacion) ON DELETE CASCADE,
    monto_prestamo    NUMERIC(14,2) NOT NULL,
    tasa_interes      NUMERIC(6,3),
    plazo_meses       INTEGER,
    cuota_mensual     NUMERIC(14,2),
    fecha_inicio      DATE,
    fecha_vencimiento DATE,
    saldo_pendiente   NUMERIC(14,2),
    estado            VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE articulo (
    id_articulo    SERIAL PRIMARY KEY,
    id_cliente     INTEGER REFERENCES cliente(id_cliente),
    descripcion    VARCHAR(200),
    categoria      VARCHAR(60),
    marca          VARCHAR(60),
    modelo         VARCHAR(60),
    numero_serie   VARCHAR(80),
    estado_fisico  VARCHAR(60),
    valor_estimado NUMERIC(14,2),
    fecha_registro DATE DEFAULT CURRENT_DATE,
    estado         VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE empeno (
    id_empeno         SERIAL PRIMARY KEY,
    id_operacion      INTEGER REFERENCES operacion(id_operacion) ON DELETE CASCADE,
    id_articulo       INTEGER REFERENCES articulo(id_articulo),
    monto_prestamo    NUMERIC(14,2) NOT NULL,
    tasa_interes      NUMERIC(6,3),
    plazo_dias        INTEGER,
    fecha_empeno      DATE,
    fecha_vencimiento DATE,
    saldo_pendiente   NUMERIC(14,2),
    estado            VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE comprobante (
    id_comprobante     SERIAL PRIMARY KEY,
    id_operacion       INTEGER REFERENCES operacion(id_operacion) ON DELETE CASCADE,
    numero_comprobante VARCHAR(40),
    tipo_comprobante   VARCHAR(30),
    fecha_emision      TIMESTAMP DEFAULT now(),
    monto_total        NUMERIC(14,2),
    estado             VARCHAR(20) DEFAULT 'EMITIDO'
);

-- ================================ VENTAS ================================

CREATE TABLE venta (
    id_venta    SERIAL PRIMARY KEY,
    id_cliente  INTEGER REFERENCES cliente(id_cliente),
    id_servicio INTEGER REFERENCES servicio_financiero(id_servicio),
    fecha_venta TIMESTAMP DEFAULT now(),
    monto       NUMERIC(14,2),
    importe     NUMERIC(14,2),
    estado      VARCHAR(20) DEFAULT 'REGISTRADA'
);

CREATE TABLE detalle_venta (
    id_detalle      SERIAL PRIMARY KEY,
    id_venta        INTEGER NOT NULL REFERENCES venta(id_venta) ON DELETE CASCADE,
    descripcion     VARCHAR(200),
    cantidad        INTEGER,
    precio_unitario NUMERIC(14,2),
    subtotal        NUMERIC(14,2)
);

-- =============================== ALMACEN ===============================

CREATE TABLE producto (
    id_producto   SERIAL PRIMARY KEY,
    codigo        VARCHAR(40),
    nombre        VARCHAR(120) NOT NULL,
    descripcion   TEXT,
    categoria     VARCHAR(60),
    unidad_medida VARCHAR(30),
    stock_actual  INTEGER DEFAULT 0,
    stock_minimo  INTEGER DEFAULT 0,
    estado        VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE inventario (
    id_inventario       SERIAL PRIMARY KEY,
    id_producto         INTEGER NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    ubicacion           VARCHAR(80),
    cantidad            INTEGER,
    fecha_actualizacion TIMESTAMP DEFAULT now()
);

CREATE TABLE movimiento_inventario (
    id_movimiento    SERIAL PRIMARY KEY,
    id_producto      INTEGER NOT NULL REFERENCES producto(id_producto) ON DELETE CASCADE,
    tipo_movimiento  VARCHAR(20),
    cantidad         INTEGER,
    motivo           VARCHAR(200),
    fecha_movimiento TIMESTAMP DEFAULT now()
);

-- ============================== MARKETING ==============================

CREATE TABLE prospecto (
    id_prospecto   SERIAL PRIMARY KEY,
    nombres        VARCHAR(120) NOT NULL,
    apellidos      VARCHAR(120),
    documento      VARCHAR(20),
    telefono       VARCHAR(30),
    correo         VARCHAR(120),
    origen         VARCHAR(60),
    estado         VARCHAR(20) DEFAULT 'NUEVO',
    fecha_registro TIMESTAMP DEFAULT now()
);

CREATE TABLE campania (
    id_campania  SERIAL PRIMARY KEY,
    nombre       VARCHAR(120) NOT NULL,
    descripcion  TEXT,
    fecha_inicio DATE,
    fecha_fin    DATE,
    presupuesto  NUMERIC(14,2),
    canal        VARCHAR(60),
    estado       VARCHAR(20) DEFAULT 'ACTIVA'
);

CREATE TABLE campania_prospecto (
    id_campania_prospecto SERIAL PRIMARY KEY,
    id_campania    INTEGER NOT NULL REFERENCES campania(id_campania) ON DELETE CASCADE,
    id_prospecto   INTEGER NOT NULL REFERENCES prospecto(id_prospecto) ON DELETE CASCADE,
    fecha_contacto DATE,
    resultado      VARCHAR(120),
    estado         VARCHAR(20) DEFAULT 'PENDIENTE'
);

CREATE TABLE actividad_marketing (
    id_actividad    SERIAL PRIMARY KEY,
    id_prospecto    INTEGER REFERENCES prospecto(id_prospecto) ON DELETE CASCADE,
    id_campania     INTEGER REFERENCES campania(id_campania) ON DELETE CASCADE,
    tipo_actividad  VARCHAR(60),
    descripcion     TEXT,
    fecha_actividad TIMESTAMP DEFAULT now()
);

-- ============================== LOGISTICA ==============================

CREATE TABLE proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    ruc          VARCHAR(20),
    contacto     VARCHAR(120),
    telefono     VARCHAR(30),
    correo       VARCHAR(120),
    direccion    VARCHAR(200),
    estado       VARCHAR(20) DEFAULT 'ACTIVO'
);

CREATE TABLE movimiento_logistica (
    id_movimiento_logistica SERIAL PRIMARY KEY,
    id_proveedor     INTEGER REFERENCES proveedor(id_proveedor) ON DELETE CASCADE,
    tipo_movimiento  VARCHAR(30),
    descripcion      TEXT,
    cantidad         INTEGER,
    fecha_movimiento TIMESTAMP DEFAULT now(),
    estado           VARCHAR(20) DEFAULT 'REGISTRADO'
);

-- ============================= PRODUCCION =============================

CREATE TABLE linea_produccion (
    id_linea    SERIAL PRIMARY KEY,
    codigo      VARCHAR(40),
    nombre      VARCHAR(120) NOT NULL,
    descripcion TEXT,
    estado      VARCHAR(20) DEFAULT 'ACTIVA'
);

CREATE TABLE orden_produccion (
    id_orden           SERIAL PRIMARY KEY,
    id_producto        INTEGER REFERENCES producto(id_producto),
    id_linea           INTEGER REFERENCES linea_produccion(id_linea),
    cantidad           INTEGER,
    fecha_inicio       DATE,
    fecha_fin_estimada DATE,
    fecha_fin_real     DATE,
    avance             NUMERIC(5,2) DEFAULT 0,
    estado             VARCHAR(20) DEFAULT 'PENDIENTE',
    observaciones      TEXT
);

-- ============================== DIRECCION ==============================

CREATE TABLE expediente_aprobacion (
    id_expediente    SERIAL PRIMARY KEY,
    codigo           VARCHAR(40),
    area             VARCHAR(80),
    solicitante      VARCHAR(120),
    monto            NUMERIC(14,2),
    impacto          VARCHAR(60),
    descripcion      TEXT,
    estado           VARCHAR(20) DEFAULT 'PENDIENTE',
    fecha_solicitud  TIMESTAMP DEFAULT now(),
    fecha_resolucion TIMESTAMP
);

CREATE TABLE decision_direccion (
    id_decision    SERIAL PRIMARY KEY,
    id_expediente  INTEGER REFERENCES expediente_aprobacion(id_expediente) ON DELETE CASCADE,
    tipo_decision  VARCHAR(40),
    descripcion    TEXT,
    fecha_decision TIMESTAMP DEFAULT now(),
    estado         VARCHAR(20) DEFAULT 'VIGENTE'
);

CREATE TABLE reporte_direccion (
    id_reporte       SERIAL PRIMARY KEY,
    tipo_reporte     VARCHAR(60),
    modulo           VARCHAR(60),
    descripcion      TEXT,
    fecha_generacion TIMESTAMP DEFAULT now()
);

-- =============================== GERENCIA ===============================

CREATE TABLE decision_gerencia (
    id_decision_gerencia SERIAL PRIMARY KEY,
    id_expediente  INTEGER REFERENCES expediente_aprobacion(id_expediente) ON DELETE CASCADE,
    tipo_decision  VARCHAR(40),
    descripcion    TEXT,
    fecha_decision TIMESTAMP DEFAULT now(),
    estado         VARCHAR(20) DEFAULT 'VIGENTE'
);

CREATE TABLE resolucion_gerencial (
    id_resolucion        SERIAL PRIMARY KEY,
    id_decision_gerencia INTEGER REFERENCES decision_gerencia(id_decision_gerencia) ON DELETE CASCADE,
    numero_resolucion    VARCHAR(40),
    titulo               VARCHAR(150),
    contenido            TEXT,
    fecha_emision        DATE DEFAULT CURRENT_DATE,
    estado               VARCHAR(20) DEFAULT 'VIGENTE'
);

-- Indices utiles para consultas frecuentes.
CREATE INDEX idx_operacion_cliente ON operacion(id_cliente);
CREATE INDEX idx_operacion_fecha   ON operacion(fecha_operacion);
CREATE INDEX idx_venta_fecha       ON venta(fecha_venta);
CREATE INDEX idx_cliente_estado    ON cliente(estado);
CREATE INDEX idx_producto_stock    ON producto(stock_actual, stock_minimo);

-- Fin del esquema.

-- ============================== AUDITORÍA ==============================
-- Bitácora de acciones (inicios de sesión, altas, cambios, bajas, respaldos).
-- Las protecciones adicionales (disparador de inmutabilidad, rol de
-- privilegios mínimos, CHECK) están en db/seguridad.sql.
CREATE TABLE auditoria (
    id_auditoria SERIAL PRIMARY KEY,
    fecha        TIMESTAMP    NOT NULL DEFAULT now(),
    usuario      VARCHAR(120) NOT NULL,
    rol          VARCHAR(50),
    ip           VARCHAR(60),
    accion       VARCHAR(40)  NOT NULL,
    recurso      VARCHAR(200),
    estado_http  INTEGER,
    detalle      VARCHAR(300)
);
CREATE INDEX idx_auditoria_fecha ON auditoria (fecha DESC);
