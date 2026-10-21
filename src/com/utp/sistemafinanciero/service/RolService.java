package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.RolDao;
import com.utp.sistemafinanciero.model.seguridad.Rol;

import java.util.List;

/** Reglas de negocio para roles del sistema. */
public class RolService implements ServicioCrud<Rol, Rol> {

    private final RolDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public RolService(RolDao dao) {
        this.dao = dao;
    }

    public List<Rol> listar() {
        return dao.listar();
    }

    public Rol buscar(int id) {
        Rol r = dao.buscar(id);
        if (r == null) {
            throw new ReglaNegocioException("No existe el rol con id " + id + ".");
        }
        return r;
    }

    public Rol crear(Rol r) {
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del rol es obligatorio.");
        }
        r.setIdRol(dao.crear(r));
        return r;
    }

    public Rol actualizar(int id, Rol r) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el rol con id " + id + ".");
        }
        r.setIdRol(id);
        dao.actualizar(r);
        return r;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el rol con id " + id + ".");
        }
    }
}
