package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.operaciones.TipoCambio;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code tipo_cambio}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.TipoCambioDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.TipoCambioDaoMemoria} (pruebas / demostración).</p>
 */
public interface TipoCambioDao extends CrudDao<TipoCambio> {
    TipoCambio vigente(String origen, String destino);
}
