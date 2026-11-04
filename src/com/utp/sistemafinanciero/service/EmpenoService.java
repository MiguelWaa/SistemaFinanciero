package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.EmpenoDao;
import com.utp.sistemafinanciero.model.operaciones.Empeno;

import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio para empenos. Establece el saldo pendiente inicial
 * (igual al monto prestado) y calcula la fecha de vencimiento a partir
 * del plazo en dias.
 */
public class EmpenoService {

    /** Límites del empeño (Plan de Pruebas, casos PU-EMP-01…08). */
    public static final java.math.BigDecimal MONTO_MAXIMO = new java.math.BigDecimal("1000000.00");
    public static final int PLAZO_MAXIMO_DIAS = 365;

    private final EmpenoDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public EmpenoService(EmpenoDao dao) {
        this.dao = dao;
    }

    public List<Empeno> listar() {
        return dao.listar();
    }

    public Empeno buscar(int id) {
        Empeno e = dao.buscar(id);
        if (e == null) {
            throw new ReglaNegocioException("No existe el empeno con id " + id + ".");
        }
        return e;
    }

    /** Valida monto, plazo y tasa sin guardar nada (la usa también el Portal, DEF-007). */
    public void validar(Empeno e) {
        ReglasMonto.exigirMonto(e.getMontoPrestamo(), new java.math.BigDecimal("0.01"), MONTO_MAXIMO,
                "El monto del empeño");
        ReglasMonto.exigirPlazo(e.getPlazoDias(), PLAZO_MAXIMO_DIAS, "días");
        if (e.getTasaInteres() != null) {
            ReglasMonto.exigirTasa(e.getTasaInteres());
        }
    }

    public Empeno crear(Empeno e) {
        validar(e);
        if (e.getFechaEmpeno() == null) {
            e.setFechaEmpeno(LocalDate.now());
        }
        e.setFechaVencimiento(e.getFechaEmpeno().plusDays(e.getPlazoDias()));
        if (e.getSaldoPendiente() == null) {
            e.setSaldoPendiente(e.getMontoPrestamo());
        }
        if (e.getEstado() == null || e.getEstado().isBlank()) {
            e.setEstado("ACTIVO");
        }
        e.setIdEmpeno(dao.crear(e));
        return e;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el empeno con id " + id + ".");
        }
    }
}
