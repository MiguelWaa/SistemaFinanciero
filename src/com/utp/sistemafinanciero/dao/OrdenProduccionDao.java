package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code orden_produccion}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.OrdenProduccionDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.OrdenProduccionDaoMemoria} (pruebas / demostración).</p>
 */
public interface OrdenProduccionDao extends CrudDao<OrdenProduccion> {
}
