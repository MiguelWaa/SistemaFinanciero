-- FUENTE db/schema.sql
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


-- FUENTE db/portal_cliente.sql
-- ============================================================
--  SIGSF · Portal del Cliente — tabla de accesos
--
--  EJECUTAR UNA SOLA VEZ sobre tu base de datos existente.
--  NO borra ni modifica ninguna información que ya tengas.
--
--  Crea la tabla donde se guarda la contraseña con la que cada
--  cliente entra al Portal del Cliente.
-- ============================================================

CREATE TABLE IF NOT EXISTS cliente_acceso (
    id_cliente INTEGER PRIMARY KEY REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    password   VARCHAR(200) NOT NULL
);

-- A los clientes que ya existen se les habilita el acceso usando
-- su propio número de documento como contraseña inicial.
INSERT INTO cliente_acceso (id_cliente, password)
SELECT c.id_cliente, c.documento
FROM cliente c
WHERE c.documento IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM cliente_acceso a WHERE a.id_cliente = c.id_cliente);

-- Consulta para ver con qué documento puede entrar cada cliente:
-- SELECT c.documento AS usuario, a.password AS contrasena, c.nombres, c.apellidos
-- FROM cliente c JOIN cliente_acceso a ON a.id_cliente = c.id_cliente;


-- FUENTE db/seguridad.sql
-- ============================================================
--  SIGSF · Seguridad y configuración de la base de datos
--  Ejecutar en pgAdmin (Query Tool) sobre la base SistemaFinanciero
--  DESPUÉS de schema.sql, datos_ejemplo.sql y portal_cliente.sql.
--
--  Es seguro ejecutarlo varias veces (idempotente): no borra datos.
--
--  Contenido:
--    1. Tabla de auditoría (bitácora de acciones).
--    2. Restricciones CHECK de integridad (montos, plazos, stock, estados).
--    3. Índices de rendimiento adicionales.
--    4. Migración de contraseñas en texto plano del Portal (→ hash).
--    5. Rol de aplicación con privilegios mínimos (sigsf_app).
-- ============================================================

-- ------------------------------------------------------------
-- 1) AUDITORÍA
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
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
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha   ON auditoria (fecha DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria (usuario);

-- La bitácora no se modifica ni se borra: un disparador lo impide
-- (incluso si alguien intenta hacerlo directamente con SQL).
CREATE OR REPLACE FUNCTION fn_auditoria_inmutable() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'La bitácora de auditoría no puede modificarse ni eliminarse.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_auditoria_inmutable ON auditoria;
CREATE TRIGGER trg_auditoria_inmutable
    BEFORE UPDATE OR DELETE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria_inmutable();

-- ------------------------------------------------------------
-- 2) RESTRICCIONES CHECK (integridad en la propia base de datos)
--    NOT VALID: se aplican a los datos nuevos sin fallar por datos antiguos.
-- ------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_operacion_monto') THEN
        ALTER TABLE operacion ADD CONSTRAINT ck_operacion_monto CHECK (monto IS NULL OR monto > 0) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_prestamo_valores') THEN
        ALTER TABLE prestamo ADD CONSTRAINT ck_prestamo_valores
            CHECK (monto_prestamo > 0 AND (plazo_meses IS NULL OR plazo_meses > 0)
                   AND (tasa_interes IS NULL OR tasa_interes >= 0)) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_empeno_valores') THEN
        ALTER TABLE empeno ADD CONSTRAINT ck_empeno_valores
            CHECK (monto_prestamo > 0 AND (plazo_dias IS NULL OR plazo_dias > 0)) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_venta_importe') THEN
        ALTER TABLE venta ADD CONSTRAINT ck_venta_importe CHECK (importe IS NULL OR importe >= 0) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_producto_stock') THEN
        ALTER TABLE producto ADD CONSTRAINT ck_producto_stock
            CHECK (stock_actual >= 0 AND stock_minimo >= 0) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_orden_avance') THEN
        ALTER TABLE orden_produccion ADD CONSTRAINT ck_orden_avance
            CHECK (avance BETWEEN 0 AND 100 AND (cantidad IS NULL OR cantidad > 0)) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_tipo_cambio_valores') THEN
        ALTER TABLE tipo_cambio ADD CONSTRAINT ck_tipo_cambio_valores CHECK (compra > 0 AND venta > 0) NOT VALID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_usuario_estado') THEN
        ALTER TABLE usuario ADD CONSTRAINT ck_usuario_estado
            CHECK (estado IN ('ACTIVO','INACTIVO','PENDIENTE','BLOQUEADO')) NOT VALID;
    END IF;
END $$;

-- ------------------------------------------------------------
-- 3) ÍNDICES ADICIONALES
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_operacion_estado ON operacion (estado);
CREATE INDEX IF NOT EXISTS idx_prestamo_operacion ON prestamo (id_operacion);
CREATE INDEX IF NOT EXISTS idx_empeno_operacion ON empeno (id_operacion);

-- ------------------------------------------------------------
-- 4) CONTRASEÑAS DEL PORTAL EN TEXTO PLANO → HASH
--    portal_cliente.sql guardaba el DNI tal cual. Aquí se convierte a
--    SHA-256; el sistema lo migrará a PBKDF2 con sal en el siguiente
--    inicio de sesión de cada cliente.
-- ------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'cliente_acceso') THEN
        UPDATE cliente_acceso
           SET password = encode(sha256(convert_to(password, 'UTF8')), 'hex')
         WHERE password !~ '^[0-9a-f]{64}$'
           AND password NOT LIKE 'pbkdf2$%';
    END IF;
END $$;

-- ------------------------------------------------------------
-- 5) ROL DE APLICACIÓN CON PRIVILEGIOS MÍNIMOS
--    El sistema NO debería conectarse como "postgres" (superusuario).
--    Cree este rol, cambie la contraseña y úsela en db.properties:
--        db.user=sigsf_app
--        db.password=<la contraseña que defina>
--    Nota: el respaldo con pg_dump requiere permiso de lectura (ya incluido).
-- ------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sigsf_app') THEN
        CREATE ROLE sigsf_app LOGIN PASSWORD 'Cambie_Esta_Clave_2026'
            NOSUPERUSER NOCREATEDB NOCREATEROLE CONNECTION LIMIT 20;
    END IF;
END $$;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO sigsf_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO sigsf_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO sigsf_app;
-- La aplicación solo puede insertar y consultar la auditoría.
REVOKE UPDATE, DELETE ON auditoria FROM sigsf_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO sigsf_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO sigsf_app;

-- Verificación rápida:
-- SELECT conname FROM pg_constraint WHERE conname LIKE 'ck_%';
-- SELECT * FROM auditoria ORDER BY id_auditoria DESC LIMIT 20;


-- FUENTE db/version_esquema.sql
-- ============================================================
--  SIGSF · Versionamiento del esquema de la base de datos (v5.0.0)
--  Ejecutar en pgAdmin (Query Tool) sobre la base SistemaFinanciero
--  DESPUÉS de schema.sql, datos_ejemplo.sql, portal_cliente.sql y
--  seguridad.sql.
--
--  Es idempotente (se puede ejecutar varias veces): no borra datos.
--  Si no se ejecuta, el sistema crea la tabla solo al arrancar
--  (VerificadorEsquema) y registra las versiones que detecta.
--
--  Cada script SQL nuevo del proyecto debe terminar con un INSERT en
--  esta tabla: así se sabe qué versión tiene la base de cada equipo.
-- ============================================================

CREATE TABLE IF NOT EXISTS version_esquema (
    id_version   SERIAL       PRIMARY KEY,
    version      VARCHAR(20)  NOT NULL UNIQUE,
    descripcion  VARCHAR(200) NOT NULL,
    script       VARCHAR(120) NOT NULL,
    aplicado_en  TIMESTAMP    NOT NULL DEFAULT now(),
    aplicado_por VARCHAR(60)  NOT NULL DEFAULT current_user,
    CONSTRAINT ck_version_semver CHECK (version ~ '^[0-9]+\.[0-9]+\.[0-9]+$')
);

COMMENT ON TABLE version_esquema IS 'Historial de versiones del esquema (scripts SQL aplicados).';

-- Versiones del esquema del SIGSF (mismo catálogo que version/HistorialEsquema.java)
INSERT INTO version_esquema (version, descripcion, script) VALUES
 ('1.0.0', 'Esquema inicial: 31 tablas de las 9 áreas y datos de ejemplo', 'db/schema.sql + db/datos_ejemplo.sql'),
 ('1.1.0', 'Portal del cliente: accesos de clientes (cliente_acceso)',      'db/portal_cliente.sql'),
 ('2.0.0', 'Seguridad: auditoría inmutable, restricciones CHECK, índices y rol sigsf_app', 'db/seguridad.sql'),
 ('5.0.0', 'Control de versiones: historial de versiones del esquema (version_esquema)', 'db/version_esquema.sql')
ON CONFLICT (version) DO NOTHING;

-- El rol de aplicación (db/seguridad.sql) solo puede leer y agregar versiones.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sigsf_app') THEN
        GRANT SELECT, INSERT ON version_esquema TO sigsf_app;
        GRANT USAGE, SELECT ON SEQUENCE version_esquema_id_version_seq TO sigsf_app;
    END IF;
END $$;

-- Consulta de verificación
SELECT version, descripcion, script, aplicado_en FROM version_esquema ORDER BY id_version;


-- FUENTE db/pagos_qr.sql
-- ============================================================
--  SIGSF · v5.2.0 · Pagos con QR (Yape, Plin, Tunki) y cotizaciones
--
--  Crea las tablas billetera, pago y cotizacion.
--  Es RE-EJECUTABLE y NO borra datos (usa IF NOT EXISTS / ON CONFLICT).
--
--  Nota: el servidor también crea estas tablas solo al arrancar si no
--  existen (config/VerificadorEsquema). Este script sirve para crearlas
--  a mano desde pgAdmin o en un servidor nuevo.
--
--  Orden completo: schema.sql, datos_ejemplo.sql, portal_cliente.sql,
--  seguridad.sql, version_esquema.sql, datos_100_registros.sql, pagos_qr.sql
-- ============================================================

-- ---------- Billeteras digitales con las que la entidad cobra ----------
CREATE TABLE IF NOT EXISTS billetera (
    id_billetera        SERIAL       PRIMARY KEY,
    codigo              VARCHAR(10)  NOT NULL UNIQUE,
    nombre              VARCHAR(40)  NOT NULL,
    numero_celular      VARCHAR(15),
    titular             VARCHAR(120),
    qr_imagen           TEXT,
    instrucciones       VARCHAR(300),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_actualizacion TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_billetera_codigo CHECK (codigo IN ('YAPE', 'PLIN', 'TUNKI'))
);
COMMENT ON TABLE billetera IS 'Cuentas de Yape, Plin y Tunki de la entidad (número, titular e imagen del QR de cobro).';

-- ---------- Pagos ----------
CREATE TABLE IF NOT EXISTS pago (
    id_pago            SERIAL        PRIMARY KEY,
    id_cliente         INTEGER       NOT NULL REFERENCES cliente(id_cliente),
    id_operacion       INTEGER       REFERENCES operacion(id_operacion) ON DELETE SET NULL,
    concepto           VARCHAR(20)   NOT NULL,
    descripcion        VARCHAR(200),
    metodo             VARCHAR(15)   NOT NULL,
    monto              NUMERIC(12,2) NOT NULL,
    moneda             VARCHAR(3)    NOT NULL DEFAULT 'PEN',
    numero_operacion   VARCHAR(30),
    celular_origen     VARCHAR(15),
    referencia         VARCHAR(40),
    origen             VARCHAR(10)   NOT NULL DEFAULT 'PORTAL',
    estado             VARCHAR(15)   NOT NULL DEFAULT 'POR_VERIFICAR',
    fecha_pago         TIMESTAMP     NOT NULL DEFAULT now(),
    fecha_verificacion TIMESTAMP,
    verificado_por     VARCHAR(120),
    observacion        VARCHAR(200),
    constancia         TEXT,
    CONSTRAINT ck_pago_monto    CHECK (monto > 0),
    CONSTRAINT ck_pago_estado   CHECK (estado IN ('POR_VERIFICAR', 'CONFIRMADO', 'RECHAZADO')),
    CONSTRAINT ck_pago_metodo   CHECK (metodo IN ('YAPE', 'PLIN', 'TUNKI', 'EFECTIVO', 'TRANSFERENCIA')),
    CONSTRAINT ck_pago_concepto CHECK (concepto IN ('CUOTA_PRESTAMO', 'RESCATE_EMPENO', 'CAMBIO_MONEDA', 'OTRO'))
);
COMMENT ON TABLE pago IS 'Pagos de clientes con Yape/Plin/Tunki (portal) o en caja. No se eliminan: se confirman o rechazan.';
-- Un número de operación no puede usarse dos veces con la misma billetera (evita reutilizar constancias).
CREATE UNIQUE INDEX IF NOT EXISTS ux_pago_metodo_numero ON pago (metodo, upper(numero_operacion))
    WHERE numero_operacion IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_pago_estado  ON pago (estado);
CREATE INDEX IF NOT EXISTS idx_pago_cliente ON pago (id_cliente);
CREATE INDEX IF NOT EXISTS idx_pago_fecha   ON pago (fecha_pago DESC);

-- ---------- Cotizaciones ----------
CREATE TABLE IF NOT EXISTS cotizacion (
    id_cotizacion     SERIAL        PRIMARY KEY,
    id_cliente        INTEGER       NOT NULL REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    tipo              VARCHAR(10)   NOT NULL,
    monto             NUMERIC(14,2) NOT NULL,
    moneda            VARCHAR(3),
    plazo             INTEGER,
    unidad_plazo      VARCHAR(5),
    tasa              NUMERIC(6,2),
    cuota             NUMERIC(14,2),
    total_interes     NUMERIC(14,2),
    total_pagar       NUMERIC(14,2),
    id_tipo_cambio    INTEGER       REFERENCES tipo_cambio(id_tipo_cambio) ON DELETE SET NULL,
    tipo_cambio       NUMERIC(10,4),
    monto_recibir     NUMERIC(14,2),
    moneda_recibir    VARCHAR(3),
    descripcion       VARCHAR(200),
    valor_estimado    NUMERIC(14,2),
    estado            VARCHAR(12)   NOT NULL DEFAULT 'VIGENTE',
    fecha_emision     TIMESTAMP     NOT NULL DEFAULT now(),
    fecha_vencimiento DATE          NOT NULL,
    id_operacion      INTEGER       REFERENCES operacion(id_operacion) ON DELETE SET NULL,
    origen            VARCHAR(10)   NOT NULL DEFAULT 'PORTAL',
    CONSTRAINT ck_cotizacion_tipo   CHECK (tipo IN ('PRESTAMO', 'EMPENO', 'CAMBIO')),
    CONSTRAINT ck_cotizacion_estado CHECK (estado IN ('VIGENTE', 'CONVERTIDA', 'VENCIDA')),
    CONSTRAINT ck_cotizacion_monto  CHECK (monto > 0)
);
COMMENT ON TABLE cotizacion IS 'Cotizaciones (proformas) de préstamo, empeño y cambio, con número COT-000001 y vigencia.';
CREATE INDEX IF NOT EXISTS idx_cotizacion_cliente ON cotizacion (id_cliente);

-- ---------- Billeteras iniciales (el ADMIN cambia número, titular y sube el QR desde Pagos > Billeteras) ----------
INSERT INTO billetera (codigo, nombre, numero_celular, titular, instrucciones, activo) VALUES
 ('YAPE',  'Yape',  '987654321', 'SIGSF Servicios Financieros', 'Abre Yape > Escanear QR, paga el monto exacto y escribe el código de referencia en el mensaje.', TRUE),
 ('PLIN',  'Plin',  '987654322', 'SIGSF Servicios Financieros', 'Desde la app de tu banco con Plin, escanea el QR o paga al número indicado.', TRUE),
 ('TUNKI', 'Tunki', '987654323', 'SIGSF Servicios Financieros', 'Abre Tunki > Pagar con QR, ingresa el monto exacto y confirma.', TRUE)
ON CONFLICT (codigo) DO NOTHING;

-- ---------- Versión del esquema ----------
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'version_esquema') THEN
        INSERT INTO version_esquema (version, descripcion, script)
        VALUES ('5.2.0', 'Pagos con QR (Yape, Plin, Tunki), billeteras y cotizaciones', 'db/pagos_qr.sql')
        ON CONFLICT (version) DO NOTHING;
    END IF;
    -- Rol de aplicación de mínimo privilegio (db/seguridad.sql): sin DELETE sobre pagos.
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sigsf_app') THEN
        GRANT SELECT, INSERT, UPDATE ON billetera, pago, cotizacion TO sigsf_app;
        GRANT USAGE, SELECT ON SEQUENCE billetera_id_billetera_seq, pago_id_pago_seq,
              cotizacion_id_cotizacion_seq TO sigsf_app;
    END IF;
END $$;

-- Consultas de verificación
SELECT codigo, numero_celular, titular, activo FROM billetera ORDER BY id_billetera;
SELECT estado, COUNT(*) AS pagos, COALESCE(SUM(monto), 0) AS total FROM pago GROUP BY estado;
