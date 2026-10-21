package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.EmpenoDao;
import com.utp.sistemafinanciero.dao.OperacionDao;
import com.utp.sistemafinanciero.dao.PrestamoDao;
import com.utp.sistemafinanciero.dto.SolicitudDTO;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.util.Json;

import java.util.List;
import java.util.Map;

/**
 * Bandeja de solicitudes del administrador: reúne lo que los clientes
 * piden desde el Portal del Cliente y permite aprobarlo o rechazarlo.
 *
 * <p>Al aprobar una solicitud también se activa el préstamo o el empeño
 * asociado. Solo se pueden resolver solicitudes en estado PENDIENTE.</p>
 */
public class SolicitudService {

    private final OperacionDao operacionDao;
    private final PrestamoDao prestamoDao;
    private final EmpenoDao empenoDao;

    public SolicitudService(OperacionDao operacionDao, PrestamoDao prestamoDao, EmpenoDao empenoDao) {
        this.operacionDao = operacionDao;
        this.prestamoDao = prestamoDao;
        this.empenoDao = empenoDao;
    }

    /** Lista las solicitudes; si se indica un estado, filtra por él. */
    public List<SolicitudDTO> listar(String estado) {
        return operacionDao.solicitudes(estado == null || estado.isBlank() ? null : estado.toUpperCase());
    }

    /** Aprueba la solicitud y activa el trámite relacionado. */
    public Map<String, Object> aprobar(int idOperacion) {
        Operacion o = exigirPendiente(idOperacion);
        operacionDao.actualizarEstado(idOperacion, "COMPLETADA");
        cambiarRelacionado(o, "ACTIVO");
        return Json.map("ok", true, "idOperacion", idOperacion, "estado", "COMPLETADA");
    }

    /** Rechaza la solicitud y anula el trámite relacionado. */
    public Map<String, Object> rechazar(int idOperacion) {
        Operacion o = exigirPendiente(idOperacion);
        operacionDao.actualizarEstado(idOperacion, "ANULADA");
        cambiarRelacionado(o, "ANULADO");
        return Json.map("ok", true, "idOperacion", idOperacion, "estado", "ANULADA");
    }

    private void cambiarRelacionado(Operacion o, String estado) {
        String tipo = o.getTipoOperacion() == null ? "" : o.getTipoOperacion().toUpperCase();
        if (tipo.contains("PRESTAMO")) {
            prestamoDao.actualizarEstadoPorOperacion(o.getIdOperacion(), estado);
        } else if (tipo.contains("EMPENO")) {
            empenoDao.actualizarEstadoPorOperacion(o.getIdOperacion(), estado);
        }
    }

    private Operacion exigirPendiente(int idOperacion) {
        Operacion o = operacionDao.buscar(idOperacion);
        if (o == null) {
            throw new ReglaNegocioException("No existe la solicitud con id " + idOperacion + ".");
        }
        if (!"PENDIENTE".equalsIgnoreCase(o.getEstado())) {
            throw new ReglaNegocioException("La solicitud ya fue atendida (estado " + o.getEstado() + ").");
        }
        return o;
    }
}
