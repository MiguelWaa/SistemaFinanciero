package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.PrestamoDao;

import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code prestamo}. */
public class PrestamoDaoJdbc implements PrestamoDao {

    private static final Db.RowMapper<Prestamo> MAPPER = rs -> new Prestamo(
            rs.getObject("id_prestamo", Integer.class),
            rs.getObject("id_operacion", Integer.class),
            rs.getObject("monto_prestamo", BigDecimal.class),
            rs.getObject("tasa_interes", BigDecimal.class),
            rs.getObject("plazo_meses", Integer.class),
            rs.getObject("cuota_mensual", BigDecimal.class),
            rs.getObject("fecha_inicio", LocalDate.class),
            rs.getObject("fecha_vencimiento", LocalDate.class),
            rs.getObject("saldo_pendiente", BigDecimal.class),
            rs.getString("estado"));

    private static final String COLS =
            "id_prestamo, id_operacion, monto_prestamo, tasa_interes, plazo_meses, cuota_mensual, "
            + "fecha_inicio, fecha_vencimiento, saldo_pendiente, estado";

    @Override
    public List<Prestamo> listar() {
        return Db.query("SELECT " + COLS + " FROM prestamo ORDER BY id_prestamo DESC", MAPPER);
    }

    @Override
    public Prestamo buscar(int idPrestamo) {
        return Db.queryOne("SELECT " + COLS + " FROM prestamo WHERE id_prestamo = ?", MAPPER, idPrestamo);
    }

    @Override
    public Integer crear(Prestamo p) {
        return Db.insert(
                "INSERT INTO prestamo (id_operacion, monto_prestamo, tasa_interes, plazo_meses, cuota_mensual, "
                + "fecha_inicio, fecha_vencimiento, saldo_pendiente, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getIdOperacion(), p.getMontoPrestamo(), p.getTasaInteres(), p.getPlazoMeses(),
                p.getCuotaMensual(), p.getFechaInicio(), p.getFechaVencimiento(),
                p.getSaldoPendiente(), p.getEstado());
    }

    @Override
    public int actualizar(Prestamo p) {
        return Db.update(
                "UPDATE prestamo SET monto_prestamo = ?, tasa_interes = ?, plazo_meses = ?, cuota_mensual = ?, "
                + "fecha_inicio = ?, fecha_vencimiento = ?, saldo_pendiente = ?, estado = ? "
                + "WHERE id_prestamo = ?",
                p.getMontoPrestamo(), p.getTasaInteres(), p.getPlazoMeses(), p.getCuotaMensual(),
                p.getFechaInicio(), p.getFechaVencimiento(), p.getSaldoPendiente(), p.getEstado(),
                p.getIdPrestamo());
    }

    @Override
    public int eliminar(int idPrestamo) {
        return Db.update("DELETE FROM prestamo WHERE id_prestamo = ?", idPrestamo);
    }

    @Override
    public BigDecimal totalColocado() {
        BigDecimal v = Db.queryOne(
                "SELECT COALESCE(SUM(monto_prestamo), 0) AS s FROM prestamo WHERE estado = 'ACTIVO'",
                rs -> rs.getObject("s", BigDecimal.class));
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Cambia el estado del registro asociado a una operación. */
    @Override
    public int actualizarEstadoPorOperacion(int idOperacion, String estado) {
        return Db.update("UPDATE prestamo SET estado = ? WHERE id_operacion = ?", estado, idOperacion);
    }
}
