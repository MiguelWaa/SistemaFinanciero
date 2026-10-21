package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.OperacionDao;
import com.utp.sistemafinanciero.dto.SolicitudDTO;

import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code operacion}. */
public class OperacionDaoJdbc implements OperacionDao {

    private static final Db.RowMapper<Operacion> MAPPER = rs -> new Operacion(
            rs.getObject("id_operacion", Integer.class),
            rs.getObject("id_cliente", Integer.class),
            rs.getObject("id_tipo_cambio", Integer.class),
            rs.getString("tipo_operacion"),
            rs.getString("moneda_origen"),
            rs.getString("moneda_destino"),
            rs.getObject("monto", BigDecimal.class),
            rs.getObject("tipo_cambio", BigDecimal.class),
            rs.getObject("total_entregar", BigDecimal.class),
            rs.getObject("total_recibir", BigDecimal.class),
            rs.getString("estado"),
            rs.getObject("fecha_operacion", LocalDateTime.class));

    private static final String COLS =
            "id_operacion, id_cliente, id_tipo_cambio, tipo_operacion, moneda_origen, moneda_destino, "
            + "monto, tipo_cambio, total_entregar, total_recibir, estado, fecha_operacion";

    @Override
    public List<Operacion> listar() {
        return Db.query("SELECT " + COLS + " FROM operacion ORDER BY id_operacion DESC", MAPPER);
    }

    @Override
    public List<Operacion> listarPorCliente(int idCliente) {
        return Db.query("SELECT " + COLS + " FROM operacion WHERE id_cliente = ? ORDER BY id_operacion DESC",
                MAPPER, idCliente);
    }

    @Override
    public Operacion buscar(int idOperacion) {
        return Db.queryOne("SELECT " + COLS + " FROM operacion WHERE id_operacion = ?", MAPPER, idOperacion);
    }

    @Override
    public Integer crear(Operacion o) {
        return Db.insert(
                "INSERT INTO operacion (id_cliente, id_tipo_cambio, tipo_operacion, moneda_origen, "
                + "moneda_destino, monto, tipo_cambio, total_entregar, total_recibir, estado, fecha_operacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                o.getIdCliente(), o.getIdTipoCambio(), o.getTipoOperacion(), o.getMonedaOrigen(),
                o.getMonedaDestino(), o.getMonto(), o.getTipoCambio(), o.getTotalEntregar(),
                o.getTotalRecibir(), o.getEstado(), o.getFechaOperacion());
    }

    @Override
    public int actualizarEstado(int idOperacion, String estado) {
        return Db.update("UPDATE operacion SET estado = ? WHERE id_operacion = ?", estado, idOperacion);
    }

    @Override
    public int eliminar(int idOperacion) {
        return Db.update("DELETE FROM operacion WHERE id_operacion = ?", idOperacion);
    }

    /** Cantidad de operaciones registradas hoy. */
    @Override
    public int contarHoy() {
        Integer n = Db.queryOne(
                "SELECT COUNT(*) AS n FROM operacion WHERE fecha_operacion::date = CURRENT_DATE",
                rs -> rs.getInt("n"));
        return n == null ? 0 : n;
    }

    /** Suma de montos de operaciones del mes actual. */
    @Override
    public BigDecimal totalMonto() {
        BigDecimal v = Db.queryOne(
                "SELECT COALESCE(SUM(monto), 0) AS s FROM operacion "
                + "WHERE date_trunc('month', fecha_operacion) = date_trunc('month', CURRENT_DATE)",
                rs -> rs.getObject("s", BigDecimal.class));
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * Solicitudes con los datos del cliente y el detalle del trámite
     * (préstamo o empeño). Se usa en la bandeja del administrador.
     */
    @Override
    public List<SolicitudDTO> solicitudes(String estado) {
        String sql =
                "SELECT o.id_operacion, o.tipo_operacion, o.monto, o.total_recibir, o.estado, "
                + "o.moneda_origen, o.moneda_destino, o.fecha_operacion, o.id_cliente, "
                + "COALESCE(TRIM(CONCAT(c.nombres, ' ', COALESCE(c.apellidos, ''))), '') AS cliente, "
                + "COALESCE(c.documento, '') AS documento, COALESCE(c.telefono, '') AS telefono, "
                + "COALESCE(c.correo, '') AS correo, "
                + "p.cuota_mensual, p.plazo_meses, p.tasa_interes, "
                + "e.plazo_dias, a.descripcion AS articulo "
                + "FROM operacion o "
                + "LEFT JOIN cliente c ON c.id_cliente = o.id_cliente "
                + "LEFT JOIN prestamo p ON p.id_operacion = o.id_operacion "
                + "LEFT JOIN empeno e ON e.id_operacion = o.id_operacion "
                + "LEFT JOIN articulo a ON a.id_articulo = e.id_articulo "
                + (estado == null ? "" : "WHERE o.estado = ? ")
                + "ORDER BY o.id_operacion DESC";

        Db.RowMapper<SolicitudDTO> mapper = rs -> {
            SolicitudDTO d = new SolicitudDTO();
            d.setIdOperacion(rs.getObject("id_operacion", Integer.class));
            d.setIdCliente(rs.getObject("id_cliente", Integer.class));
            d.setCliente(rs.getString("cliente"));
            d.setDocumento(rs.getString("documento"));
            d.setTelefono(rs.getString("telefono"));
            d.setCorreo(rs.getString("correo"));
            d.setTipoOperacion(rs.getString("tipo_operacion"));
            d.setMonto(rs.getObject("monto", BigDecimal.class));
            d.setTotalRecibir(rs.getObject("total_recibir", BigDecimal.class));
            d.setMonedaOrigen(rs.getString("moneda_origen"));
            d.setMonedaDestino(rs.getString("moneda_destino"));
            d.setEstado(rs.getString("estado"));
            d.setFechaOperacion(rs.getObject("fecha_operacion", LocalDateTime.class));
            d.setCuotaMensual(rs.getObject("cuota_mensual", BigDecimal.class));
            d.setPlazoMeses(rs.getObject("plazo_meses", Integer.class));
            d.setTasaInteres(rs.getObject("tasa_interes", BigDecimal.class));
            d.setPlazoDias(rs.getObject("plazo_dias", Integer.class));
            d.setArticulo(rs.getString("articulo"));
            return d;
        };
        return estado == null ? Db.query(sql, mapper) : Db.query(sql, mapper, estado);
    }
}
