package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.RolDao;

import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.util.Db;

import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code rol}. */
public class RolDaoJdbc implements RolDao {

    private static final Db.RowMapper<Rol> MAPPER = rs -> new Rol(
            rs.getObject("id_rol", Integer.class),
            rs.getString("nombre"),
            rs.getString("descripcion"));

    @Override
    public List<Rol> listar() {
        return Db.query("SELECT id_rol, nombre, descripcion FROM rol ORDER BY id_rol", MAPPER);
    }

    @Override
    public Rol buscar(int idRol) {
        return Db.queryOne(
                "SELECT id_rol, nombre, descripcion FROM rol WHERE id_rol = ?",
                MAPPER, idRol);
    }

    @Override
    public Integer crear(Rol r) {
        return Db.insert(
                "INSERT INTO rol (nombre, descripcion) VALUES (?, ?)",
                r.getNombre(), r.getDescripcion());
    }

    @Override
    public int actualizar(Rol r) {
        return Db.update(
                "UPDATE rol SET nombre = ?, descripcion = ? WHERE id_rol = ?",
                r.getNombre(), r.getDescripcion(), r.getIdRol());
    }

    @Override
    public int eliminar(int idRol) {
        return Db.update("DELETE FROM rol WHERE id_rol = ?", idRol);
    }
}
