package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ServicioFinancieroDao;

import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;
import com.utp.sistemafinanciero.util.Db;

import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code servicio_financiero}. */
public class ServicioFinancieroDaoJdbc implements ServicioFinancieroDao {

    private static final Db.RowMapper<ServicioFinanciero> MAPPER = rs -> new ServicioFinanciero(
            rs.getObject("id_servicio", Integer.class),
            rs.getString("nombre"),
            rs.getString("tipo_servicio"),
            rs.getString("descripcion"),
            rs.getString("estado"));

    private static final String COLS = "id_servicio, nombre, tipo_servicio, descripcion, estado";

    @Override
    public List<ServicioFinanciero> listar() {
        return Db.query("SELECT " + COLS + " FROM servicio_financiero ORDER BY id_servicio", MAPPER);
    }

    @Override
    public ServicioFinanciero buscar(int idServicio) {
        return Db.queryOne("SELECT " + COLS + " FROM servicio_financiero WHERE id_servicio = ?", MAPPER, idServicio);
    }

    @Override
    public Integer crear(ServicioFinanciero s) {
        return Db.insert(
                "INSERT INTO servicio_financiero (nombre, tipo_servicio, descripcion, estado) "
                + "VALUES (?, ?, ?, ?)",
                s.getNombre(), s.getTipoServicio(), s.getDescripcion(), s.getEstado());
    }

    @Override
    public int actualizar(ServicioFinanciero s) {
        return Db.update(
                "UPDATE servicio_financiero SET nombre = ?, tipo_servicio = ?, descripcion = ?, estado = ? "
                + "WHERE id_servicio = ?",
                s.getNombre(), s.getTipoServicio(), s.getDescripcion(), s.getEstado(), s.getIdServicio());
    }

    @Override
    public int eliminar(int idServicio) {
        return Db.update("DELETE FROM servicio_financiero WHERE id_servicio = ?", idServicio);
    }
}
