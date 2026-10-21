package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.AuditoriaDao;
import com.utp.sistemafinanciero.model.seguridad.Auditoria;
import com.utp.sistemafinanciero.util.Db;

import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL de la bitácora {@code auditoria}. */
public class AuditoriaDaoJdbc implements AuditoriaDao {

    private static final Db.RowMapper<Auditoria> MAPPER = rs -> {
        Auditoria a = new Auditoria();
        a.setIdAuditoria(rs.getObject("id_auditoria", Integer.class));
        a.setFecha(rs.getObject("fecha", LocalDateTime.class));
        a.setUsuario(rs.getString("usuario"));
        a.setRol(rs.getString("rol"));
        a.setIp(rs.getString("ip"));
        a.setAccion(rs.getString("accion"));
        a.setRecurso(rs.getString("recurso"));
        a.setEstadoHttp(rs.getObject("estado_http", Integer.class));
        a.setDetalle(rs.getString("detalle"));
        return a;
    };

    @Override
    public Integer registrar(Auditoria e) {
        return Db.insert(
                "INSERT INTO auditoria (fecha, usuario, rol, ip, accion, recurso, estado_http, detalle) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                e.getFecha(), e.getUsuario(), e.getRol(), e.getIp(), e.getAccion(),
                e.getRecurso(), e.getEstadoHttp(), e.getDetalle());
    }

    @Override
    public List<Auditoria> recientes(int limite) {
        return Db.query(
                "SELECT id_auditoria, fecha, usuario, rol, ip, accion, recurso, estado_http, detalle "
                + "FROM auditoria ORDER BY id_auditoria DESC LIMIT ?", MAPPER, limite);
    }
}
