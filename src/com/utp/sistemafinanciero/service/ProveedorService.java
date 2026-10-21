package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ProveedorDao;
import com.utp.sistemafinanciero.model.logistica.Proveedor;

import java.util.List;

/** Reglas de negocio para los proveedores (logistica). */
public class ProveedorService implements ServicioCrud<Proveedor, Proveedor> {

    private final ProveedorDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public ProveedorService(ProveedorDao dao) {
        this.dao = dao;
    }

    public List<Proveedor> listar() {
        return dao.listar();
    }

    public Proveedor buscar(int id) {
        Proveedor p = dao.buscar(id);
        if (p == null) {
            throw new ReglaNegocioException("No existe el proveedor con id " + id + ".");
        }
        return p;
    }

    public Proveedor crear(Proveedor p) {
        if (p.getRazonSocial() == null || p.getRazonSocial().isBlank()) {
            throw new ReglaNegocioException("La razon social es obligatoria.");
        }
        if (p.getEstado() == null || p.getEstado().isBlank()) {
            p.setEstado("ACTIVO");
        }
        p.setIdProveedor(dao.crear(p));
        return p;
    }

    public Proveedor actualizar(int id, Proveedor p) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el proveedor con id " + id + ".");
        }
        p.setIdProveedor(id);
        dao.actualizar(p);
        return p;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el proveedor con id " + id + ".");
        }
    }
}
