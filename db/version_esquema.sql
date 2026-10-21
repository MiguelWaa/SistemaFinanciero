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
