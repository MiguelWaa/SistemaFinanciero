package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.VentaDao;
import com.utp.sistemafinanciero.dto.VentaReporteDTO;

import com.utp.sistemafinanciero.model.ventas.Venta;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code venta}. */
public class VentaDaoJdbc implements VentaDao {

    private static final Db.RowMapper<Venta> MAPPER = rs -> new Venta(
            rs.getObject("id_venta", Integer.class),
            rs.getObject("id_cliente", Integer.class),
            rs.getObject("id_servicio", Integer.class),
            rs.getObject("fecha_venta", LocalDateTime.class),
            rs.getObject("monto", BigDecimal.class),
            rs.getObject("importe", BigDecimal.class),
            rs.getString("estado"));

    private static final String COLS =
            "id_venta, id_cliente, id_servicio, fecha_venta, monto, importe, estado";

    @Override
    public List<Venta> listar() {
        return Db.query("SELECT " + COLS + " FROM venta ORDER BY id_venta DESC", MAPPER);
    }

    @Override
    public Venta buscar(int idVenta) {
        return Db.queryOne("SELECT " + COLS + " FROM venta WHERE id_venta = ?", MAPPER, idVenta);
    }

    @Override
    public Integer crear(Venta v) {
        return Db.insert(
                "INSERT INTO venta (id_cliente, id_servicio, fecha_venta, monto, importe, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
                v.getIdCliente(), v.getIdServicio(), v.getFechaVenta(), v.getMonto(),
                v.getImporte(), v.getEstado());
    }

    @Override
    public int actualizar(Venta v) {
        return Db.update(
                "UPDATE venta SET id_cliente = ?, id_servicio = ?, fecha_venta = ?, monto = ?, "
                + "importe = ?, estado = ? WHERE id_venta = ?",
                v.getIdCliente(), v.getIdServicio(), v.getFechaVenta(), v.getMonto(),
                v.getImporte(), v.getEstado(), v.getIdVenta());
    }

    @Override
    public int eliminar(int idVenta) {
        return Db.update("DELETE FROM venta WHERE id_venta = ?", idVenta);
    }

    /** Ingresos (importe) del mes actual. */
    @Override
    public BigDecimal ingresosDelMes() {
        BigDecimal v = Db.queryOne(
                "SELECT COALESCE(SUM(importe), 0) AS s FROM venta "
                + "WHERE date_trunc('month', fecha_venta) = date_trunc('month', CURRENT_DATE)",
                rs -> rs.getObject("s", BigDecimal.class));
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * Filas para el reporte Excel: incluye el nombre del cliente y del
     * servicio (mediante JOIN) en lugar de solo sus identificadores.
     */
    @Override
    public List<VentaReporteDTO> reporte() {
        String sql =
                "SELECT v.id_venta, "
                + "COALESCE(TRIM(CONCAT(c.nombres, ' ', COALESCE(c.apellidos, ''))), '') AS cliente, "
                + "COALESCE(s.nombre, '') AS servicio, "
                + "v.fecha_venta AS fecha, "
                + "COALESCE(v.importe, 0) AS importe, "
                + "COALESCE(v.estado, '') AS estado "
                + "FROM venta v "
                + "LEFT JOIN cliente c ON c.id_cliente = v.id_cliente "
                + "LEFT JOIN servicio_financiero s ON s.id_servicio = v.id_servicio "
                + "ORDER BY v.id_venta";
        return Db.query(sql, rs -> new VentaReporteDTO(
                rs.getInt("id_venta"),
                rs.getString("cliente"),
                rs.getString("servicio"),
                rs.getObject("fecha", LocalDateTime.class),
                rs.getBigDecimal("importe"),
                rs.getString("estado")));
    }
}
