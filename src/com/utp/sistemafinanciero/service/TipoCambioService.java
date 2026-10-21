package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.TipoCambioDao;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;

import java.time.LocalDate;
import java.util.List;

/** Reglas de negocio para los tipos de cambio. */
public class TipoCambioService implements ServicioCrud<TipoCambio, TipoCambio> {

    private final TipoCambioDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public TipoCambioService(TipoCambioDao dao) {
        this.dao = dao;
    }

    public List<TipoCambio> listar() {
        return dao.listar();
    }

    public TipoCambio buscar(int id) {
        TipoCambio t = dao.buscar(id);
        if (t == null) {
            throw new ReglaNegocioException("No existe el tipo de cambio con id " + id + ".");
        }
        return t;
    }

    public TipoCambio crear(TipoCambio t) {
        if (t.getMonedaOrigen() == null || t.getMonedaDestino() == null) {
            throw new ReglaNegocioException("Debe indicar la moneda de origen y destino.");
        }
        if (t.getCompra() == null || t.getVenta() == null) {
            throw new ReglaNegocioException("Debe indicar los valores de compra y venta.");
        }
        if (t.getFechaInicio() == null) {
            t.setFechaInicio(LocalDate.now());
        }
        if (t.getEstado() == null || t.getEstado().isBlank()) {
            t.setEstado("ACTIVO");
        }
        t.setIdTipoCambio(dao.crear(t));
        return t;
    }

    public TipoCambio actualizar(int id, TipoCambio t) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el tipo de cambio con id " + id + ".");
        }
        t.setIdTipoCambio(id);
        dao.actualizar(t);
        return t;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el tipo de cambio con id " + id + ".");
        }
    }
}
