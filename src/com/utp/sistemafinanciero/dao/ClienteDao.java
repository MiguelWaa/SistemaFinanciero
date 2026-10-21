package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.clientes.Cliente;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code cliente}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ClienteDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ClienteDaoMemoria} (pruebas / demostración).</p>
 */
public interface ClienteDao extends CrudDao<Cliente> {
    Cliente buscarPorDocumento(String documento);

    int contar();
}
