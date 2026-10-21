package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.VentaDao;
import com.utp.sistemafinanciero.model.ventas.Venta;

import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio para ventas. */
public class VentaService {

    private final VentaDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public VentaService(VentaDao dao) {
        this.dao = dao;
    }

    public List<Venta> listar() {
        return dao.listar();
    }

    public Venta buscar(int id) {
        Venta v = dao.buscar(id);
        if (v == null) {
            throw new ReglaNegocioException("No existe la venta con id " + id + ".");
        }
        return v;
    }

    public Venta crear(Venta v) {
        if (v.getImporte() == null || v.getImporte().signum() < 0) {
            throw new ReglaNegocioException("El importe de la venta no es valido.");
        }
        if (v.getMonto() == null) {
            v.setMonto(v.getImporte());
        }
        v.setFechaVenta(LocalDateTime.now());
        if (v.getEstado() == null || v.getEstado().isBlank()) {
            v.setEstado("REGISTRADA");
        }
        v.setIdVenta(dao.crear(v));
        return v;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe la venta con id " + id + ".");
        }
    }
}
