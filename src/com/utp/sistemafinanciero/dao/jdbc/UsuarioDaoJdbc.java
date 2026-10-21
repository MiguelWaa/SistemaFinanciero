package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.UsuarioDao;

import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.util.Db;

import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code usuario}. */
public class UsuarioDaoJdbc implements UsuarioDao {

    private static final Db.RowMapper<Usuario> MAPPER = rs -> new Usuario(
            rs.getObject("id_usuario", Integer.class),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getObject("id_rol", Integer.class),
            rs.getString("estado"),
            rs.getObject("fecha_registro", LocalDateTime.class));

    private static final String COLS =
            "id_usuario, nombre, apellido, email, password, id_rol, estado, fecha_registro";

    @Override
    public List<Usuario> listar() {
        return Db.query("SELECT " + COLS + " FROM usuario ORDER BY id_usuario", MAPPER);
    }

    @Override
    public Usuario buscar(int idUsuario) {
        return Db.queryOne("SELECT " + COLS + " FROM usuario WHERE id_usuario = ?", MAPPER, idUsuario);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return Db.queryOne("SELECT " + COLS + " FROM usuario WHERE lower(email) = lower(?)", MAPPER, email);
    }

    @Override
    public Integer crear(Usuario u) {
        return Db.insert(
                "INSERT INTO usuario (nombre, apellido, email, password, id_rol, estado, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                u.getNombre(), u.getApellido(), u.getEmail(), u.getPassword(),
                u.getIdRol(), u.getEstado(), u.getFechaRegistro());
    }

    @Override
    public int actualizar(Usuario u) {
        return Db.update(
                "UPDATE usuario SET nombre = ?, apellido = ?, email = ?, id_rol = ?, estado = ? "
                + "WHERE id_usuario = ?",
                u.getNombre(), u.getApellido(), u.getEmail(), u.getIdRol(),
                u.getEstado(), u.getIdUsuario());
    }

    @Override
    public int actualizarPassword(int idUsuario, String passwordHash) {
        return Db.update("UPDATE usuario SET password = ? WHERE id_usuario = ?", passwordHash, idUsuario);
    }

    @Override
    public int eliminar(int idUsuario) {
        return Db.update("DELETE FROM usuario WHERE id_usuario = ?", idUsuario);
    }

    @Override
    public int contar() {
        Integer n = Db.queryOne("SELECT COUNT(*) AS n FROM usuario",
                rs -> rs.getInt("n"));
        return n == null ? 0 : n;
    }
}
