package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ExpedienteAprobacionDao;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;

import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio para expedientes de aprobacion (direccion/gerencia). */
public class ExpedienteService {

    private final ExpedienteAprobacionDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public ExpedienteService(ExpedienteAprobacionDao dao) {
        this.dao = dao;
    }

    public List<ExpedienteAprobacion> listar() {
        return dao.listar();
    }

    public ExpedienteAprobacion buscar(int id) {
        ExpedienteAprobacion e = dao.buscar(id);
        if (e == null) {
            throw new ReglaNegocioException("No existe el expediente con id " + id + ".");
        }
        return e;
    }

    public ExpedienteAprobacion crear(ExpedienteAprobacion e) {
        if (e.getCodigo() == null || e.getCodigo().isBlank()) {
            throw new ReglaNegocioException("El codigo del expediente es obligatorio.");
        }
        if (e.getEstado() == null || e.getEstado().isBlank()) {
            e.setEstado("PENDIENTE");
        }
        e.setFechaSolicitud(LocalDateTime.now());
        e.setIdExpediente(dao.crear(e));
        return e;
    }

    public ExpedienteAprobacion resolver(int id, String estado) {
        ExpedienteAprobacion e = dao.buscar(id);
        if (e == null) {
            throw new ReglaNegocioException("No existe el expediente con id " + id + ".");
        }
        if (estado == null || !java.util.Set.of("APROBADO", "OBSERVADO", "RECHAZADO", "PENDIENTE")
                .contains(estado.toUpperCase())) {
            throw new ReglaNegocioException("Estado no válido. Use APROBADO, OBSERVADO o RECHAZADO.");
        }
        e.setEstado(estado.toUpperCase());
        e.setFechaResolucion(LocalDateTime.now());
        dao.actualizar(e);
        return e;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el expediente con id " + id + ".");
        }
    }
}
