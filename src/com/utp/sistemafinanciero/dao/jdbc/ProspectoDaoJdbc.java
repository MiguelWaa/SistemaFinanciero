package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ProspectoDao;

import com.utp.sistemafinanciero.model.marketing.Prospecto;
import com.utp.sistemafinanciero.util.Db;

import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code prospecto}. */
public class ProspectoDaoJdbc implements ProspectoDao {

    private static final Db.RowMapper<Prospecto> MAPPER = rs -> new Prospecto(
            rs.getObject("id_prospecto", Integer.class),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("documento"),
            rs.getString("telefono"),
            rs.getString("correo"),
            rs.getString("origen"),
            rs.getString("estado"),
            rs.getObject("fecha_registro", LocalDateTime.class));

    private static final String COLS =
            "id_prospecto, nombres, apellidos, documento, telefono, correo, origen, estado, fecha_registro";

    @Override
    public List<Prospecto> listar() {
        return Db.query("SELECT " + COLS + " FROM prospecto ORDER BY id_prospecto DESC", MAPPER);
    }

    @Override
    public Prospecto buscar(int idProspecto) {
        return Db.queryOne("SELECT " + COLS + " FROM prospecto WHERE id_prospecto = ?", MAPPER, idProspecto);
    }

    @Override
    public Integer crear(Prospecto p) {
        return Db.insert(
                "INSERT INTO prospecto (nombres, apellidos, documento, telefono, correo, origen, estado, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                p.getNombres(), p.getApellidos(), p.getDocumento(), p.getTelefono(),
                p.getCorreo(), p.getOrigen(), p.getEstado(), p.getFechaRegistro());
    }

    @Override
    public int actualizar(Prospecto p) {
        return Db.update(
                "UPDATE prospecto SET nombres = ?, apellidos = ?, documento = ?, telefono = ?, "
                + "correo = ?, origen = ?, estado = ? WHERE id_prospecto = ?",
                p.getNombres(), p.getApellidos(), p.getDocumento(), p.getTelefono(),
                p.getCorreo(), p.getOrigen(), p.getEstado(), p.getIdProspecto());
    }

    @Override
    public int eliminar(int idProspecto) {
        return Db.update("DELETE FROM prospecto WHERE id_prospecto = ?", idProspecto);
    }
}
