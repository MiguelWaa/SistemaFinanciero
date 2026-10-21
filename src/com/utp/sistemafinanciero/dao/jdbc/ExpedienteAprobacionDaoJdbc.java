package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ExpedienteAprobacionDao;

import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;
import com.utp.sistemafinanciero.util.Db;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code expediente_aprobacion}. */
public class ExpedienteAprobacionDaoJdbc implements ExpedienteAprobacionDao {

    private static final Db.RowMapper<ExpedienteAprobacion> MAPPER = rs -> new ExpedienteAprobacion(
            rs.getObject("id_expediente", Integer.class),
            rs.getString("codigo"),
            rs.getString("area"),
            rs.getString("solicitante"),
            rs.getObject("monto", BigDecimal.class),
            rs.getString("impacto"),
            rs.getString("descripcion"),
            rs.getString("estado"),
            rs.getObject("fecha_solicitud", LocalDateTime.class),
            rs.getObject("fecha_resolucion", LocalDateTime.class));

    private static final String COLS =
            "id_expediente, codigo, area, solicitante, monto, impacto, descripcion, estado, "
            + "fecha_solicitud, fecha_resolucion";

    @Override
    public List<ExpedienteAprobacion> listar() {
        return Db.query("SELECT " + COLS + " FROM expediente_aprobacion ORDER BY id_expediente DESC", MAPPER);
    }

    @Override
    public ExpedienteAprobacion buscar(int idExpediente) {
        return Db.queryOne("SELECT " + COLS + " FROM expediente_aprobacion WHERE id_expediente = ?",
                MAPPER, idExpediente);
    }

    @Override
    public Integer crear(ExpedienteAprobacion e) {
        return Db.insert(
                "INSERT INTO expediente_aprobacion (codigo, area, solicitante, monto, impacto, descripcion, "
                + "estado, fecha_solicitud, fecha_resolucion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                e.getCodigo(), e.getArea(), e.getSolicitante(), e.getMonto(), e.getImpacto(),
                e.getDescripcion(), e.getEstado(), e.getFechaSolicitud(), e.getFechaResolucion());
    }

    @Override
    public int actualizar(ExpedienteAprobacion e) {
        return Db.update(
                "UPDATE expediente_aprobacion SET codigo = ?, area = ?, solicitante = ?, monto = ?, "
                + "impacto = ?, descripcion = ?, estado = ?, fecha_resolucion = ? WHERE id_expediente = ?",
                e.getCodigo(), e.getArea(), e.getSolicitante(), e.getMonto(), e.getImpacto(),
                e.getDescripcion(), e.getEstado(), e.getFechaResolucion(), e.getIdExpediente());
    }

    @Override
    public int eliminar(int idExpediente) {
        return Db.update("DELETE FROM expediente_aprobacion WHERE id_expediente = ?", idExpediente);
    }

    @Override
    public int contarPendientes() {
        Integer n = Db.queryOne(
                "SELECT COUNT(*) AS n FROM expediente_aprobacion WHERE estado = 'PENDIENTE'",
                rs -> rs.getInt("n"));
        return n == null ? 0 : n;
    }
}
