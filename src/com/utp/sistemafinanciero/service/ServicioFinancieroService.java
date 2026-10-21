package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ServicioFinancieroDao;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;

import java.util.List;

/** Reglas de negocio para los servicios financieros ofrecidos. */
public class ServicioFinancieroService implements ServicioCrud<ServicioFinanciero, ServicioFinanciero> {

    private final ServicioFinancieroDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public ServicioFinancieroService(ServicioFinancieroDao dao) {
        this.dao = dao;
    }

    public List<ServicioFinanciero> listar() {
        return dao.listar();
    }

    public ServicioFinanciero buscar(int id) {
        ServicioFinanciero s = dao.buscar(id);
        if (s == null) {
            throw new ReglaNegocioException("No existe el servicio con id " + id + ".");
        }
        return s;
    }

    public ServicioFinanciero crear(ServicioFinanciero s) {
        if (s.getNombre() == null || s.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del servicio es obligatorio.");
        }
        if (s.getEstado() == null || s.getEstado().isBlank()) {
            s.setEstado("ACTIVO");
        }
        s.setIdServicio(dao.crear(s));
        return s;
    }

    public ServicioFinanciero actualizar(int id, ServicioFinanciero s) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el servicio con id " + id + ".");
        }
        s.setIdServicio(id);
        dao.actualizar(s);
        return s;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el servicio con id " + id + ".");
        }
    }
}
