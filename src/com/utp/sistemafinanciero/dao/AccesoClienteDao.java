package com.utp.sistemafinanciero.dao;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code cliente_acceso}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.AccesoClienteDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.AccesoClienteDaoMemoria} (pruebas / demostración).</p>
 */
public interface AccesoClienteDao {
    /** Devuelve la contraseña guardada del cliente, o null si no tiene acceso. */
    String passwordDe(int idCliente);

    /** Crea o actualiza la contraseña del cliente. */
    void guardar(int idCliente, String password);
}
