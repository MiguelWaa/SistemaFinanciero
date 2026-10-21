package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.dto.SolicitudDTO;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato DAO (Data Access Object) para la tabla {@code operacion}.
 *
 * <p>La capa de servicios depende de esta interfaz y no de una
 * implementación concreta (principio de inversión de dependencias, DIP).
 * Implementaciones: {@code dao.jdbc.OperacionDaoJdbc} (PostgreSQL) y
 * {@code dao.memoria.OperacionDaoMemoria} (pruebas / demostración).</p>
 */
public interface OperacionDao extends LecturaDao<Operacion>, EscrituraDao<Operacion> {
    List<Operacion> listarPorCliente(int idCliente);

    int actualizarEstado(int idOperacion, String estado);

    /** Cantidad de operaciones registradas hoy. */
    int contarHoy();

    /** Suma de montos de operaciones del mes actual. */
    BigDecimal totalMonto();

    /**
     * Solicitudes con los datos del cliente y el detalle del trámite
     * (préstamo o empeño). Se usa en la bandeja del administrador.
     *
     * @param estado estado a filtrar o null para todas
     */
    List<SolicitudDTO> solicitudes(String estado);
}
