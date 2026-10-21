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
