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

    public Empeno crear(Empeno e) {
        if (e.getMontoPrestamo() == null || e.getMontoPrestamo().signum() <= 0) {
            throw new ReglaNegocioException("El monto del empeno debe ser mayor que cero.");
        }
        if (e.getPlazoDias() == null || e.getPlazoDias() <= 0) {
            throw new ReglaNegocioException("El plazo en dias debe ser mayor que cero.");
        }
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
