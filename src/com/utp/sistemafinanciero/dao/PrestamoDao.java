package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import java.math.BigDecimal;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code prestamo}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.PrestamoDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.PrestamoDaoMemoria} (pruebas / demostración).</p>
 */
public interface PrestamoDao extends CrudDao<Prestamo> {
    BigDecimal totalColocado();

    /** Cambia el estado del registro asociado a una operación. */
    int actualizarEstadoPorOperacion(int idOperacion, String estado);
}
