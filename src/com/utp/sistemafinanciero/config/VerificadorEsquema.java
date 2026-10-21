package com.utp.sistemafinanciero.config;

import com.utp.sistemafinanciero.model.sistema.VersionEsquema;
import com.utp.sistemafinanciero.version.HistorialEsquema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Revisa al arrancar que la base de datos tenga las tablas que usa SIGSF.
 *
 * <ul>
 *   <li>Si falta la tabla {@code auditoria} (bases creadas antes del taller),
 *       la crea automáticamente junto con su índice y el disparador que la
 *       hace inmutable (no se puede modificar ni borrar un evento).</li>
 *   <li>Si faltan otras tablas principales, solo lo avisa en el log: deben
 *       crearse con {@code db/schema.sql}.</li>
 * </ul>
 *
 * <p>Solo ejecuta sentencias fijas (sin datos del usuario) y todas son
 * idempotentes ({@code IF NOT EXISTS}), así que puede correr en cada inicio.</p>
 */
public final class VerificadorEsquema {

    private static final Logger LOG = LoggerFactory.getLogger(VerificadorEsquema.class);

    private static final String[] TABLAS_PRINCIPALES = {
            "rol", "usuario", "cliente", "servicio_financiero", "tipo_cambio", "operacion",
            "prestamo", "empeno", "venta", "producto", "proveedor", "campania", "prospecto",
            "linea_produccion", "orden_produccion", "expediente_aprobacion"};

    private static final String[] CREAR_AUDITORIA = {
            "CREATE TABLE IF NOT EXISTS auditoria ("
                    + " id_auditoria SERIAL PRIMARY KEY,"
                    + " fecha        TIMESTAMP    NOT NULL DEFAULT now(),"
                    + " usuario      VARCHAR(120) NOT NULL,"
                    + " rol          VARCHAR(50),"
                    + " ip           VARCHAR(60),"
                    + " accion       VARCHAR(40)  NOT NULL,"
                    + " recurso      VARCHAR(200),"
                    + " estado_http  INTEGER,"
                    + " detalle      VARCHAR(300))",
            "CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria (fecha DESC)",
            "CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria (usuario)",
            "CREATE OR REPLACE FUNCTION fn_auditoria_inmutable() RETURNS trigger AS $$ "
                    + "BEGIN RAISE EXCEPTION 'La bitácora de auditoría no se puede modificar ni eliminar'; END; "
                    + "$$ LANGUAGE plpgsql",
            "DROP TRIGGER IF EXISTS trg_auditoria_inmutable ON auditoria",
            "CREATE TRIGGER trg_auditoria_inmutable BEFORE UPDATE OR DELETE ON auditoria "
                    + "FOR EACH ROW EXECUTE FUNCTION fn_auditoria_inmutable()"
    };

    /** Tabla de versionamiento del esquema (igual a db/version_esquema.sql). */
    private static final String CREAR_VERSION_ESQUEMA =
            "CREATE TABLE IF NOT EXISTS version_esquema ("
                    + " id_version   SERIAL       PRIMARY KEY,"
                    + " version      VARCHAR(20)  NOT NULL UNIQUE,"
                    + " descripcion  VARCHAR(200) NOT NULL,"
                    + " script       VARCHAR(120) NOT NULL,"
                    + " aplicado_en  TIMESTAMP    NOT NULL DEFAULT now(),"
                    + " aplicado_por VARCHAR(60)  NOT NULL DEFAULT current_user,"
                    + " CONSTRAINT ck_version_semver CHECK (version ~ '^[0-9]+\\.[0-9]+\\.[0-9]+$'))";

    private static final String REGISTRAR_VERSION =
            "INSERT INTO version_esquema (version, descripcion, script) VALUES (?, ?, ?) "
                    + "ON CONFLICT (version) DO NOTHING";

    private VerificadorEsquema() {
    }

    public static void verificar(Connection cn) {
        try {
            if (!existe(cn, "auditoria")) {
                LOG.warn("La tabla «auditoria» no existe en la base: se crea automáticamente.");
                try (Statement st = cn.createStatement()) {
                    for (String sql : CREAR_AUDITORIA) {
                        st.execute(sql);
                    }
                }
                LOG.info("Tabla «auditoria» creada (con índice y disparador de inmutabilidad).");
            }
            verificarVersionEsquema(cn);
            List<String> faltan = new ArrayList<>();
            for (String t : TABLAS_PRINCIPALES) {
                if (!existe(cn, t)) faltan.add(t);
            }
            if (faltan.isEmpty()) {
                LOG.info("Esquema de la base verificado: todas las tablas principales existen.");
            } else {
                LOG.warn("Faltan tablas en la base {}: {}. Ejecute db/schema.sql en pgAdmin.",
                        Configuracion.dbName(), faltan);
            }
        } catch (SQLException e) {
            LOG.error("No se pudo verificar o crear el esquema: {}", e.getMessage());
        }
    }

    /**
     * Crea {@code version_esquema} si falta y registra las versiones cuyo
     * script ya se aplicó (se detecta por la existencia de su tabla).
     * Avisa si la base está en una versión anterior a la que espera el código.
     */
    private static void verificarVersionEsquema(Connection cn) throws SQLException {
        if (!existe(cn, "version_esquema")) {
            LOG.warn("La tabla «version_esquema» no existe: se crea automáticamente.");
            try (Statement st = cn.createStatement()) {
                st.execute(CREAR_VERSION_ESQUEMA);
            }
        }
        try (PreparedStatement ps = cn.prepareStatement(REGISTRAR_VERSION)) {
            for (VersionEsquema v : HistorialEsquema.conocidas()) {
                if (existe(cn, HistorialEsquema.tablaMarcadora(v.getVersion()))) {
                    ps.setString(1, v.getVersion());
                    ps.setString(2, v.getDescripcion());
                    ps.setString(3, v.getScript());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
        String actual = null;
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT version FROM version_esquema ORDER BY id_version DESC LIMIT 1")) {
            if (rs.next()) {
                actual = rs.getString(1);
            }
        }
        if (HistorialEsquema.esperada().equals(actual)) {
            LOG.info("Versión del esquema de la base: {} (la esperada por el código).", actual);
        } else {
            LOG.warn("Versión del esquema de la base: {} · el código espera {}. Ejecute los scripts de db/.",
                    actual, HistorialEsquema.esperada());
        }
    }

    private static boolean existe(Connection cn, String tabla) throws SQLException {
        DatabaseMetaData md = cn.getMetaData();
        try (ResultSet rs = md.getTables(null, "public", tabla, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
