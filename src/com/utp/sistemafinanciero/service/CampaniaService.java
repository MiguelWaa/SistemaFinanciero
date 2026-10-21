package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.CampaniaDao;
import com.utp.sistemafinanciero.model.marketing.Campania;

import java.time.LocalDate;
import java.util.List;

/** Reglas de negocio para campanias de marketing. */
public class CampaniaService implements ServicioCrud<Campania, Campania> {

    private final CampaniaDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public CampaniaService(CampaniaDao dao) {
        this.dao = dao;
    }

    public List<Campania> listar() {
        return dao.listar();
    }

    public Campania buscar(int id) {
        Campania c = dao.buscar(id);
        if (c == null) {
            throw new ReglaNegocioException("No existe la campania con id " + id + ".");
        }
        return c;
    }

    public Campania crear(Campania c) {
        if (c.getNombre() == null || c.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre de la campania es obligatorio.");
        }
        if (c.getFechaInicio() == null) {
            c.setFechaInicio(LocalDate.now());
        }
        if (c.getEstado() == null || c.getEstado().isBlank()) {
            c.setEstado("ACTIVA");
        }
        c.setIdCampania(dao.crear(c));
        return c;
    }

    public Campania actualizar(int id, Campania c) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe la campania con id " + id + ".");
        }
        c.setIdCampania(id);
        dao.actualizar(c);
        return c;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe la campania con id " + id + ".");
        }
    }
}
