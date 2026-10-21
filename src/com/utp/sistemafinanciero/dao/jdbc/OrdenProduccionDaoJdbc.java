package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.OrdenProduccionDao;

import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code orden_produccion}. */
public class OrdenProduccionDaoJdbc implements OrdenProduccionDao {

    private static final Db.RowMapper<OrdenProduccion> MAPPER = rs -> new OrdenProduccion(
            rs.getObject("id_orden", Integer.class),
            rs.getObject("id_producto", Integer.class),
            rs.getObject("id_linea", Integer.class),
            rs.getObject("cantidad", Integer.class),
            rs.getObject("fecha_inicio", LocalDate.class),
            rs.getObject("fecha_fin_estimada", LocalDate.class),
            rs.getObject("fecha_fin_real", LocalDate.class),
            rs.getObject("avance", BigDecimal.class),
            rs.getString("estado"),
            rs.getString("observaciones"));

    private static final String COLS = "id_orden, id_producto, id_linea, cantidad, fecha_inicio, "
            + "fecha_fin_estimada, fecha_fin_real, avance, estado, observaciones";

    @Override
    public List<OrdenProduccion> listar() {
        return Db.query("SELECT " + COLS + " FROM orden_produccion ORDER BY id_orden DESC", MAPPER);
    }

    @Override
    public OrdenProduccion buscar(int id) {
        return Db.queryOne("SELECT " + COLS + " FROM orden_produccion WHERE id_orden = ?", MAPPER, id);
    }

    @Override
    public Integer crear(OrdenProduccion o) {
        return Db.insert("INSERT INTO orden_produccion (id_producto, id_linea, cantidad, fecha_inicio, "
                + "fecha_fin_estimada, fecha_fin_real, avance, estado, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                o.getIdProducto(), o.getIdLinea(), o.getCantidad(), o.getFechaInicio(),
                o.getFechaFinEstimada(), o.getFechaFinReal(), o.getAvance(), o.getEstado(), o.getObservaciones());
    }

    @Override
    public int actualizar(OrdenProduccion o) {
        return Db.update("UPDATE orden_produccion SET id_producto = ?, id_linea = ?, cantidad = ?, "
                + "fecha_inicio = ?, fecha_fin_estimada = ?, fecha_fin_real = ?, avance = ?, "
                + "estado = ?, observaciones = ? WHERE id_orden = ?",
                o.getIdProducto(), o.getIdLinea(), o.getCantidad(), o.getFechaInicio(),
                o.getFechaFinEstimada(), o.getFechaFinReal(), o.getAvance(), o.getEstado(),
                o.getObservaciones(), o.getIdOrden());
    }

    @Override
    public int eliminar(int id) {
        return Db.update("DELETE FROM orden_produccion WHERE id_orden = ?", id);
    }
}
