package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.ClienteDao;

import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.util.Db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Implementación JDBC/PostgreSQL del DAO de la tabla {@code cliente}. */
public class ClienteDaoJdbc implements ClienteDao {

    private static final Db.RowMapper<Cliente> MAPPER = rs -> new Cliente(
            rs.getObject("id_cliente", Integer.class),
            rs.getString("tipo_documento"),
            rs.getString("documento"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getObject("fecha_nacimiento", LocalDate.class),
            rs.getString("telefono"),
            rs.getString("correo"),
            rs.getString("direccion"),
            rs.getString("estado"),
            rs.getObject("fecha_registro", LocalDateTime.class));

    private static final String COLS =
            "id_cliente, tipo_documento, documento, nombres, apellidos, fecha_nacimiento, "
            + "telefono, correo, direccion, estado, fecha_registro";

    @Override
    public List<Cliente> listar() {
        return Db.query("SELECT " + COLS + " FROM cliente ORDER BY id_cliente", MAPPER);
    }

    @Override
    public Cliente buscar(int idCliente) {
        return Db.queryOne("SELECT " + COLS + " FROM cliente WHERE id_cliente = ?", MAPPER, idCliente);
    }

    @Override
    public Cliente buscarPorDocumento(String documento) {
        return Db.queryOne("SELECT " + COLS + " FROM cliente WHERE documento = ?", MAPPER, documento);
    }

    @Override
    public Integer crear(Cliente c) {
        return Db.insert(
                "INSERT INTO cliente (tipo_documento, documento, nombres, apellidos, fecha_nacimiento, "
                + "telefono, correo, direccion, estado, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.getTipoDocumento(), c.getDocumento(), c.getNombres(), c.getApellidos(),
                c.getFechaNacimiento(), c.getTelefono(), c.getCorreo(), c.getDireccion(),
                c.getEstado(), c.getFechaRegistro());
    }

    @Override
    public int actualizar(Cliente c) {
        return Db.update(
                "UPDATE cliente SET tipo_documento = ?, documento = ?, nombres = ?, apellidos = ?, "
                + "fecha_nacimiento = ?, telefono = ?, correo = ?, direccion = ?, estado = ? "
                + "WHERE id_cliente = ?",
                c.getTipoDocumento(), c.getDocumento(), c.getNombres(), c.getApellidos(),
                c.getFechaNacimiento(), c.getTelefono(), c.getCorreo(), c.getDireccion(),
                c.getEstado(), c.getIdCliente());
    }

    @Override
    public int eliminar(int idCliente) {
        return Db.update("DELETE FROM cliente WHERE id_cliente = ?", idCliente);
    }

    @Override
    public int contar() {
        Integer n = Db.queryOne("SELECT COUNT(*) AS n FROM cliente WHERE estado = 'ACTIVO'",
                rs -> rs.getInt("n"));
        return n == null ? 0 : n;
    }
}
