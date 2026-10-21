package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code servicio_financiero}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ServicioFinancieroDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ServicioFinancieroDaoMemoria} (pruebas / demostración).</p>
 */
public interface ServicioFinancieroDao extends CrudDao<ServicioFinanciero> {
}
