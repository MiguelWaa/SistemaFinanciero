package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.LineaProduccionDao;

import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.util.Db;

import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code linea_produccion}. */
public class LineaProduccionDaoJdbc implements LineaProduccionDao {

    private static final Db.RowMapper<LineaProduccion> MAPPER = rs -> new LineaProduccion(
            rs.getObject("id_linea", Integer.class),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getString("estado"));

    private static final String COLS = "id_linea, codigo, nombre, descripcion, estado";

    @Override
    public List<LineaProduccion> listar() {
        return Db.query("SELECT " + COLS + " FROM linea_produccion ORDER BY id_linea DESC", MAPPER);
    }

    @Override
    public LineaProduccion buscar(int id) {
        return Db.queryOne("SELECT " + COLS + " FROM linea_produccion WHERE id_linea = ?", MAPPER, id);
    }

    @Override
    public Integer crear(LineaProduccion l) {
        return Db.insert("INSERT INTO linea_produccion (codigo, nombre, descripcion, estado) VALUES (?, ?, ?, ?)",
                l.getCodigo(), l.getNombre(), l.getDescripcion(), l.getEstado());
    }

    @Override
    public int actualizar(LineaProduccion l) {
        return Db.update("UPDATE linea_produccion SET codigo = ?, nombre = ?, descripcion = ?, estado = ? WHERE id_linea = ?",
                l.getCodigo(), l.getNombre(), l.getDescripcion(), l.getEstado(), l.getIdLinea());
    }

    @Override
    public int eliminar(int id) {
        return Db.update("DELETE FROM linea_produccion WHERE id_linea = ?", id);
    }
}
