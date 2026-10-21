package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ProductoDao;

import com.utp.sistemafinanciero.model.almacen.Producto;
import com.utp.sistemafinanciero.util.Db;

import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code producto}. */
public class ProductoDaoJdbc implements ProductoDao {

    private static final Db.RowMapper<Producto> MAPPER = rs -> new Producto(
            rs.getObject("id_producto", Integer.class),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("descripcion"),
            rs.getString("categoria"),
            rs.getString("unidad_medida"),
            rs.getObject("stock_actual", Integer.class),
            rs.getObject("stock_minimo", Integer.class),
            rs.getString("estado"));

    private static final String COLS =
            "id_producto, codigo, nombre, descripcion, categoria, unidad_medida, "
            + "stock_actual, stock_minimo, estado";

    @Override
    public List<Producto> listar() {
        return Db.query("SELECT " + COLS + " FROM producto ORDER BY id_producto", MAPPER);
    }

    @Override
    public Producto buscar(int idProducto) {
        return Db.queryOne("SELECT " + COLS + " FROM producto WHERE id_producto = ?", MAPPER, idProducto);
    }

    @Override
    public Integer crear(Producto p) {
        return Db.insert(
                "INSERT INTO producto (codigo, nombre, descripcion, categoria, unidad_medida, "
                + "stock_actual, stock_minimo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getUnidadMedida(), p.getStockActual(), p.getStockMinimo(), p.getEstado());
    }

    @Override
    public int actualizar(Producto p) {
        return Db.update(
                "UPDATE producto SET codigo = ?, nombre = ?, descripcion = ?, categoria = ?, "
                + "unidad_medida = ?, stock_actual = ?, stock_minimo = ?, estado = ? WHERE id_producto = ?",
                p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
                p.getUnidadMedida(), p.getStockActual(), p.getStockMinimo(), p.getEstado(),
                p.getIdProducto());
    }

    @Override
    public int eliminar(int idProducto) {
        return Db.update("DELETE FROM producto WHERE id_producto = ?", idProducto);
    }

    /** Productos cuyo stock actual esta en o por debajo del minimo. */
    @Override
    public int contarStockBajo() {
        Integer n = Db.queryOne(
                "SELECT COUNT(*) AS n FROM producto WHERE stock_actual <= stock_minimo",
                rs -> rs.getInt("n"));
        return n == null ? 0 : n;
    }
}
