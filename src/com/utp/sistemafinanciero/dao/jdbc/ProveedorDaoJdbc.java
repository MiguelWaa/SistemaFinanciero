package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ProveedorDao;

import com.utp.sistemafinanciero.model.logistica.Proveedor;
import com.utp.sistemafinanciero.util.Db;

import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code proveedor}. */
public class ProveedorDaoJdbc implements ProveedorDao {

    private static final Db.RowMapper<Proveedor> MAPPER = rs -> new Proveedor(
            rs.getObject("id_proveedor", Integer.class),
            rs.getString("razon_social"),
            rs.getString("ruc"),
            rs.getString("contacto"),
            rs.getString("telefono"),
            rs.getString("correo"),
            rs.getString("direccion"),
            rs.getString("estado"));

    private static final String COLS =
            "id_proveedor, razon_social, ruc, contacto, telefono, correo, direccion, estado";

    @Override
    public List<Proveedor> listar() {
        return Db.query("SELECT " + COLS + " FROM proveedor ORDER BY id_proveedor", MAPPER);
    }

    @Override
    public Proveedor buscar(int idProveedor) {
        return Db.queryOne("SELECT " + COLS + " FROM proveedor WHERE id_proveedor = ?", MAPPER, idProveedor);
    }

    @Override
    public Integer crear(Proveedor p) {
        return Db.insert(
                "INSERT INTO proveedor (razon_social, ruc, contacto, telefono, correo, direccion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                p.getRazonSocial(), p.getRuc(), p.getContacto(), p.getTelefono(),
                p.getCorreo(), p.getDireccion(), p.getEstado());
    }

    @Override
    public int actualizar(Proveedor p) {
        return Db.update(
                "UPDATE proveedor SET razon_social = ?, ruc = ?, contacto = ?, telefono = ?, "
                + "correo = ?, direccion = ?, estado = ? WHERE id_proveedor = ?",
                p.getRazonSocial(), p.getRuc(), p.getContacto(), p.getTelefono(),
                p.getCorreo(), p.getDireccion(), p.getEstado(), p.getIdProveedor());
    }

    @Override
    public int eliminar(int idProveedor) {
        return Db.update("DELETE FROM proveedor WHERE id_proveedor = ?", idProveedor);
    }
}
