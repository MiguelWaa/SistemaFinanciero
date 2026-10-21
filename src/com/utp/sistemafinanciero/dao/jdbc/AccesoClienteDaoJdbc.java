package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.AccesoClienteDao;

import com.utp.sistemafinanciero.util.Db;

/**
 * Acceso a la tabla {@code cliente_acceso}, que guarda la contraseña con la
 * que cada cliente entra al Portal del Cliente. Se mantiene separada de la
 * tabla {@code cliente} para no mezclar datos personales con credenciales.
 */
public class AccesoClienteDaoJdbc implements AccesoClienteDao {

    /** Devuelve la contraseña guardada del cliente, o null si no tiene acceso. */
    @Override
    public String passwordDe(int idCliente) {
        return Db.queryOne(
                "SELECT password FROM cliente_acceso WHERE id_cliente = ?",
                rs -> rs.getString("password"), idCliente);
    }

    /** Crea o actualiza la contraseña del cliente. */
    @Override
    public void guardar(int idCliente, String password) {
        int filas = Db.update(
                "UPDATE cliente_acceso SET password = ? WHERE id_cliente = ?", password, idCliente);
        if (filas == 0) {
            Db.update("INSERT INTO cliente_acceso (id_cliente, password) VALUES (?, ?)",
                    idCliente, password);
        }
    }
}
