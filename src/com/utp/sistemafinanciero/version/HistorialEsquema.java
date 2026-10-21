package com.utp.sistemafinanciero.version;

import com.utp.sistemafinanciero.model.sistema.VersionEsquema;

import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de las versiones conocidas del esquema de la base de datos.
 * Es la única fuente de verdad que usan {@code db/version_esquema.sql},
 * el verificador de arranque (PostgreSQL) y los datos de demostración
 * (memoria).
 *
 * <p>La versión del esquema es independiente de la versión de la
 * aplicación: un parche (5.0.1) no cambia la base, así que el esquema
 * sigue en 5.0.0.</p>
 */
public final class HistorialEsquema {

    /** Versión, descripción, script y tabla que demuestra que se aplicó. */
    private static final String[][] VERSIONES = {
            {"1.0.0", "Esquema inicial: 31 tablas de las 9 áreas y datos de ejemplo",
                    "db/schema.sql + db/datos_ejemplo.sql", "cliente"},
            {"1.1.0", "Portal del cliente: accesos de clientes (cliente_acceso)",
                    "db/portal_cliente.sql", "cliente_acceso"},
            {"2.0.0", "Seguridad: auditoría inmutable, restricciones CHECK, índices y rol sigsf_app",
                    "db/seguridad.sql", "auditoria"},
            {"5.0.0", "Control de versiones: historial de versiones del esquema (version_esquema)",
                    "db/version_esquema.sql", "version_esquema"},
    };

    private HistorialEsquema() {
    }

    /** Versiones en orden de aplicación (nuevas instancias en cada llamada). */
    public static List<VersionEsquema> conocidas() {
        List<VersionEsquema> lista = new ArrayList<>();
        for (String[] v : VERSIONES) {
            lista.add(new VersionEsquema(v[0], v[1], v[2]));
        }
        return lista;
    }

    /** Tabla cuya existencia indica que la versión dada ya está aplicada. */
    public static String tablaMarcadora(String version) {
        for (String[] v : VERSIONES) {
            if (v[0].equals(version)) {
                return v[3];
            }
        }
        return null;
    }

    /** Versión más reciente del esquema que espera este código. */
    public static String esperada() {
        return VERSIONES[VERSIONES.length - 1][0];
    }
}
