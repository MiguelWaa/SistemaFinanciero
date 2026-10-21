package com.utp.sistemafinanciero.util;

import com.utp.sistemafinanciero.config.Configuracion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pequena capa de ayuda para trabajar con JDBC sobre PostgreSQL.
 *
 * <p>Centraliza la apertura de conexiones y ofrece metodos genericos
 * ({@link #query}, {@link #queryOne}, {@link #update}, {@link #insert})
 * que reciben el SQL, los parametros y, cuando corresponde, una funcion
 * {@link RowMapper} que convierte cada fila del {@link ResultSet} en un
 * objeto. Asi los DAO quedan cortos y legibles.</p>
 *
 * <p>El driver de PostgreSQL se carga de forma reflexiva, por lo que el
 * proyecto compila aunque el .jar del driver aun no este agregado; solo
 * se necesita en tiempo de ejecucion (colocarlo en la carpeta
 * {@code lib/}).</p>
 */
public final class Db {

    private static boolean driverListo = false;

    private Db() {
        // Clase de utilidad.
    }

    /** Convierte una fila del ResultSet en un objeto de tipo T. */
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** Abre una nueva conexion a la base de datos. */
    public static Connection getConnection() throws SQLException {
        if (!driverListo) {
            try {
                Class.forName("org.postgresql.Driver");
                driverListo = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "No se encontro el driver de PostgreSQL. Agrega el archivo "
                    + "postgresql-42.x.jar en la carpeta lib/ del proyecto "
                    + "(o en las librerias de NetBeans).", e);
            }
        }
        return DriverManager.getConnection(
                Configuracion.jdbcUrl(),
                Configuracion.dbUser(),
                Configuracion.dbPassword());
    }

    /** Ejecuta un SELECT y devuelve la lista de resultados mapeados. */
    public static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        List<T> lista = new ArrayList<>();
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en consulta: " + sql, e);
        }
        return lista;
    }

    /** Ejecuta un SELECT que devuelve una sola fila (o null). */
    public static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> lista = query(sql, mapper, params);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Ejecuta un INSERT/UPDATE/DELETE y devuelve las filas afectadas. */
    public static int update(String sql, Object... params) {
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en actualizacion: " + sql, e);
        }
    }

    /**
     * Ejecuta un INSERT y devuelve la clave primaria generada (SERIAL).
     * Se asume que la primera columna generada es el id entero.
     */
    public static Integer insert(String sql, Object... params) {
        try (Connection cn = getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getObject(1, Integer.class);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar: " + sql, e);
        }
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
