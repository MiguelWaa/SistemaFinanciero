package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ProspectoDao;
import com.utp.sistemafinanciero.model.marketing.Prospecto;

import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio para prospectos de marketing. */
public class ProspectoService implements ServicioCrud<Prospecto, Prospecto> {

    private final ProspectoDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public ProspectoService(ProspectoDao dao) {
        this.dao = dao;
    }

    public List<Prospecto> listar() {
        return dao.listar();
    }

    public Prospecto buscar(int id) {
        Prospecto p = dao.buscar(id);
        if (p == null) {
            throw new ReglaNegocioException("No existe el prospecto con id " + id + ".");
        }
        return p;
    }

    public Prospecto crear(Prospecto p) {
        if (p.getNombres() == null || p.getNombres().isBlank()) {
            throw new ReglaNegocioException("Los nombres del prospecto son obligatorios.");
        }
        if (p.getEstado() == null || p.getEstado().isBlank()) {
            p.setEstado("NUEVO");
        }
        p.setFechaRegistro(LocalDateTime.now());
        p.setIdProspecto(dao.crear(p));
        return p;
    }

    public Prospecto actualizar(int id, Prospecto p) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el prospecto con id " + id + ".");
        }
        p.setIdProspecto(id);
        dao.actualizar(p);
        return p;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el prospecto con id " + id + ".");
        }
    }
}
