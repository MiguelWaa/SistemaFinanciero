package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.operaciones.Empeno;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code empeno}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.EmpenoDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.EmpenoDaoMemoria} (pruebas / demostración).</p>
 */
public interface EmpenoDao extends CrudDao<Empeno> {
    /** Cambia el estado del registro asociado a una operación. */
    int actualizarEstadoPorOperacion(int idOperacion, String estado);

    /** Empeño asociado a una operación; null si no hay. */
    default Empeno buscarPorOperacion(int idOperacion) {
        for (Empeno e : listar()) {
            if (e.getIdOperacion() != null && e.getIdOperacion() == idOperacion) return e;
        }
        return null;
    }
}
