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
