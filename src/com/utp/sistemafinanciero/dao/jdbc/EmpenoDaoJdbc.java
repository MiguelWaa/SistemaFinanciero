package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.EmpenoDao;

import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code empeno}. */
public class EmpenoDaoJdbc implements EmpenoDao {

    private static final Db.RowMapper<Empeno> MAPPER = rs -> new Empeno(
            rs.getObject("id_empeno", Integer.class),
            rs.getObject("id_operacion", Integer.class),
            rs.getObject("id_articulo", Integer.class),
            rs.getObject("monto_prestamo", BigDecimal.class),
            rs.getObject("tasa_interes", BigDecimal.class),
            rs.getObject("plazo_dias", Integer.class),
            rs.getObject("fecha_empeno", LocalDate.class),
            rs.getObject("fecha_vencimiento", LocalDate.class),
            rs.getObject("saldo_pendiente", BigDecimal.class),
            rs.getString("estado"));

    private static final String COLS =
            "id_empeno, id_operacion, id_articulo, monto_prestamo, tasa_interes, plazo_dias, "
            + "fecha_empeno, fecha_vencimiento, saldo_pendiente, estado";

    @Override
    public List<Empeno> listar() {
        return Db.query("SELECT " + COLS + " FROM empeno ORDER BY id_empeno DESC", MAPPER);
    }

    @Override
    public Empeno buscar(int idEmpeno) {
        return Db.queryOne("SELECT " + COLS + " FROM empeno WHERE id_empeno = ?", MAPPER, idEmpeno);
    }

    @Override
    public Integer crear(Empeno e) {
        return Db.insert(
                "INSERT INTO empeno (id_operacion, id_articulo, monto_prestamo, tasa_interes, plazo_dias, "
                + "fecha_empeno, fecha_vencimiento, saldo_pendiente, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getIdOperacion(), e.getIdArticulo(), e.getMontoPrestamo(), e.getTasaInteres(),
                e.getPlazoDias(), e.getFechaEmpeno(), e.getFechaVencimiento(),
                e.getSaldoPendiente(), e.getEstado());
    }

    @Override
    public int actualizar(Empeno e) {
        return Db.update(
                "UPDATE empeno SET monto_prestamo = ?, tasa_interes = ?, plazo_dias = ?, fecha_empeno = ?, "
                + "fecha_vencimiento = ?, saldo_pendiente = ?, estado = ? WHERE id_empeno = ?",
                e.getMontoPrestamo(), e.getTasaInteres(), e.getPlazoDias(), e.getFechaEmpeno(),
                e.getFechaVencimiento(), e.getSaldoPendiente(), e.getEstado(), e.getIdEmpeno());
    }

    @Override
    public int eliminar(int idEmpeno) {
        return Db.update("DELETE FROM empeno WHERE id_empeno = ?", idEmpeno);
    }

    /** Cambia el estado del registro asociado a una operación. */
    @Override
    public int actualizarEstadoPorOperacion(int idOperacion, String estado) {
        return Db.update("UPDATE empeno SET estado = ? WHERE id_operacion = ?", estado, idOperacion);
    }
}
