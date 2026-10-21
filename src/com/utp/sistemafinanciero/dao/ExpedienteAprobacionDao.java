package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code expediente_aprobacion}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.ExpedienteAprobacionDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.ExpedienteAprobacionDaoMemoria} (pruebas / demostración).</p>
 */
public interface ExpedienteAprobacionDao extends CrudDao<ExpedienteAprobacion> {
    int contarPendientes();
}
