package com.utp.sistemafinanciero.backup;

import com.utp.sistemafinanciero.util.Db;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Respaldo de <b>datos</b> escrito en Java puro (JDBC), útil cuando
 * {@code pg_dump} no está instalado. Genera un script SQL con los
 * {@code INSERT} de todas las tablas del esquema {@code public} y
 * reajusta las secuencias {@code SERIAL}.
 *
 * <p>Para restaurar: ejecutar primero (estructura)
 * y luego este archivo.</p>
 */
public class RespaldoJdbcSql implements EstrategiaRespaldo {

    @Override public String nombre() { return "jdbc"; }
    @Override public String extension() { return "sql"; }

    @Override
    public boolean disponible() {
        try (Connection cn = Db.getConnection()) {
            return cn.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void generar(Path destino) throws Exception {
        try (Connection cn = Db.getConnection();
             BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            cn.setReadOnly(true);
            cn.setAutoCommit(false); // instantánea consistente de todas las tablas
            w.write("-- SIGSF · Respaldo de datos generado por el sistema (JDBC)\n");
            w.write("-- Fecha: " + LocalDateTime.now() + "\n");
            w.write("-- Restaurar: 1) ejecutar db/schema.sql  2) ejecutar este archivo\n\n");
            w.write("BEGIN;\nSET session_replication_role = replica; -- desactiva FK durante la carga\n\n");

            List<String> tablas = new ArrayList<>();
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT table_name FROM information_schema.tables "
                         + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name")) {
                while (rs.next()) tablas.add(rs.getString(1));
            }
            for (String t : tablas) {
                w.write("-- Tabla " + t + "\nDELETE FROM " + t + ";\n");
                try (Statement st = cn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM " + t)) {
                    ResultSetMetaData md = rs.getMetaData();
                    StringBuilder cols = new StringBuilder();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        if (i > 1) cols.append(", ");
                        cols.append(md.getColumnName(i));
                    }
                    while (rs.next()) {
                        StringBuilder v = new StringBuilder();
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            if (i > 1) v.append(", ");
                            v.append(literal(rs.getString(i), md.getColumnType(i)));
                        }
                        w.write("INSERT INTO " + t + " (" + cols + ") VALUES (" + v + ");\n");
                    }
                }
                w.write("\n");
            }
            // Reajuste de secuencias SERIAL
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT c.table_name, c.column_name, pg_get_serial_sequence(c.table_name, c.column_name) "
                         + "FROM information_schema.columns c WHERE c.table_schema = 'public' "
                         + "AND c.column_default LIKE 'nextval(%'")) {
                while (rs.next()) {
                    if (rs.getString(3) == null) continue;
                    w.write("SELECT setval('" + rs.getString(3) + "', COALESCE((SELECT MAX("
                            + rs.getString(2) + ") FROM " + rs.getString(1) + "), 0) + 1, false);\n");
                }
            }
            w.write("\nSET session_replication_role = DEFAULT;\nCOMMIT;\n");
            cn.rollback();
        }
    }

    /** Convierte un valor a literal SQL escapando comillas (evita inyección en el script). */
    static String literal(String valor, int tipo) {
        if (valor == null) return "NULL";
        switch (tipo) {
            case Types.INTEGER: case Types.BIGINT: case Types.SMALLINT:
            case Types.NUMERIC: case Types.DECIMAL: case Types.DOUBLE: case Types.REAL:
                return valor;
            case Types.BOOLEAN: case Types.BIT:
                return "t".equals(valor) || "true".equalsIgnoreCase(valor) ? "TRUE" : "FALSE";
            default:
                return "'" + valor.replace("'", "''") + "'";
        }
    }
}
