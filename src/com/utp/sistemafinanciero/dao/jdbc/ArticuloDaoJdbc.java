package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ArticuloDao;

import com.utp.sistemafinanciero.model.operaciones.Articulo;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Acceso a datos de la tabla {@code articulo} (artículos en empeño). */
public class ArticuloDaoJdbc implements ArticuloDao {

    private static final Db.RowMapper<Articulo> MAPPER = rs -> new Articulo(
            rs.getObject("id_articulo", Integer.class),
            rs.getObject("id_cliente", Integer.class),
            rs.getString("descripcion"),
            rs.getString("categoria"),
            rs.getString("marca"),
            rs.getString("modelo"),
            rs.getString("numero_serie"),
            rs.getString("estado_fisico"),
            rs.getObject("valor_estimado", BigDecimal.class),
            rs.getObject("fecha_registro", LocalDate.class),
            rs.getString("estado"));

    private static final String COLS =
            "id_articulo, id_cliente, descripcion, categoria, marca, modelo, "
            + "numero_serie, estado_fisico, valor_estimado, fecha_registro, estado";

    @Override
    public List<Articulo> listar() {
        return Db.query("SELECT " + COLS + " FROM articulo ORDER BY id_articulo DESC", MAPPER);
    }

    @Override
    public Articulo buscar(int idArticulo) {
        return Db.queryOne("SELECT " + COLS + " FROM articulo WHERE id_articulo = ?", MAPPER, idArticulo);
    }

    @Override
    public Integer crear(Articulo a) {
        return Db.insert(
                "INSERT INTO articulo (id_cliente, descripcion, categoria, marca, modelo, "
                + "numero_serie, estado_fisico, valor_estimado, fecha_registro, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                a.getIdCliente(), a.getDescripcion(), a.getCategoria(), a.getMarca(), a.getModelo(),
                a.getNumeroSerie(), a.getEstadoFisico(), a.getValorEstimado(),
                a.getFechaRegistro(), a.getEstado());
    }

    @Override
    public int eliminar(int idArticulo) {
        return Db.update("DELETE FROM articulo WHERE id_articulo = ?", idArticulo);
    }
}
