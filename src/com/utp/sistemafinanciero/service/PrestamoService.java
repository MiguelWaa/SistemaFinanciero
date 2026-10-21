package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.PrestamoDao;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio para prestamos. Calcula la cuota mensual con interes
 * simple sobre el plazo y establece el saldo pendiente y la fecha de
 * vencimiento.
 */
public class PrestamoService {

    private final PrestamoDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public PrestamoService(PrestamoDao dao) {
        this.dao = dao;
    }

    public List<Prestamo> listar() {
        return dao.listar();
    }

    public Prestamo buscar(int id) {
        Prestamo p = dao.buscar(id);
        if (p == null) {
            throw new ReglaNegocioException("No existe el prestamo con id " + id + ".");
        }
        return p;
    }

    public Prestamo crear(Prestamo p) {
        if (p.getMontoPrestamo() == null || p.getMontoPrestamo().signum() <= 0) {
            throw new ReglaNegocioException("El monto del prestamo debe ser mayor que cero.");
        }
        if (p.getPlazoMeses() == null || p.getPlazoMeses() <= 0) {
            throw new ReglaNegocioException("El plazo en meses debe ser mayor que cero.");
        }
        if (p.getTasaInteres() == null) {
            p.setTasaInteres(BigDecimal.ZERO);
        }

        // Interes simple: total = monto * (1 + tasa%/100 * plazoMeses).
        BigDecimal tasaFraccion = p.getTasaInteres()
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(p.getPlazoMeses()));
        BigDecimal totalPagar = p.getMontoPrestamo()
                .multiply(BigDecimal.ONE.add(tasaFraccion));
        BigDecimal cuota = totalPagar.divide(BigDecimal.valueOf(p.getPlazoMeses()), 2, RoundingMode.HALF_UP);

        p.setCuotaMensual(cuota);
        p.setSaldoPendiente(totalPagar.setScale(2, RoundingMode.HALF_UP));

        if (p.getFechaInicio() == null) {
            p.setFechaInicio(LocalDate.now());
        }
        p.setFechaVencimiento(p.getFechaInicio().plusMonths(p.getPlazoMeses()));

        if (p.getEstado() == null || p.getEstado().isBlank()) {
            p.setEstado("ACTIVO");
        }
        p.setIdPrestamo(dao.crear(p));
        return p;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el prestamo con id " + id + ".");
        }
    }
}
