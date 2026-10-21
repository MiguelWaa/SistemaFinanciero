package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.TipoCambioDao;

import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code tipo_cambio}. */
public class TipoCambioDaoJdbc implements TipoCambioDao {

    private static final Db.RowMapper<TipoCambio> MAPPER = rs -> new TipoCambio(
            rs.getObject("id_tipo_cambio", Integer.class),
            rs.getString("moneda_origen"),
            rs.getString("moneda_destino"),
            rs.getObject("compra", BigDecimal.class),
            rs.getObject("venta", BigDecimal.class),
            rs.getObject("fecha_inicio", LocalDate.class),
            rs.getString("estado"));

    private static final String COLS =
            "id_tipo_cambio, moneda_origen, moneda_destino, compra, venta, fecha_inicio, estado";

    @Override
    public List<TipoCambio> listar() {
        return Db.query("SELECT " + COLS + " FROM tipo_cambio ORDER BY id_tipo_cambio DESC", MAPPER);
    }

    @Override
    public TipoCambio buscar(int idTipoCambio) {
        return Db.queryOne("SELECT " + COLS + " FROM tipo_cambio WHERE id_tipo_cambio = ?", MAPPER, idTipoCambio);
    }

    @Override
    public TipoCambio vigente(String origen, String destino) {
        return Db.queryOne(
                "SELECT " + COLS + " FROM tipo_cambio WHERE moneda_origen = ? AND moneda_destino = ? "
                + "AND estado = 'ACTIVO' ORDER BY fecha_inicio DESC LIMIT 1",
                MAPPER, origen, destino);
    }

    @Override
    public Integer crear(TipoCambio t) {
        return Db.insert(
                "INSERT INTO tipo_cambio (moneda_origen, moneda_destino, compra, venta, fecha_inicio, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                t.getMonedaOrigen(), t.getMonedaDestino(), t.getCompra(), t.getVenta(),
                t.getFechaInicio(), t.getEstado());
    }

    @Override
    public int actualizar(TipoCambio t) {
        return Db.update(
                "UPDATE tipo_cambio SET moneda_origen = ?, moneda_destino = ?, compra = ?, venta = ?, "
                + "fecha_inicio = ?, estado = ? WHERE id_tipo_cambio = ?",
                t.getMonedaOrigen(), t.getMonedaDestino(), t.getCompra(), t.getVenta(),
                t.getFechaInicio(), t.getEstado(), t.getIdTipoCambio());
    }

    @Override
    public int eliminar(int idTipoCambio) {
        return Db.update("DELETE FROM tipo_cambio WHERE id_tipo_cambio = ?", idTipoCambio);
    }
}
