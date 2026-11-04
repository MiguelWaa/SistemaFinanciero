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

    /** Límites del préstamo (Plan de Pruebas, casos PU-PRE-01…19). */
    public static final BigDecimal MONTO_MINIMO = new BigDecimal("100.00");
    public static final BigDecimal MONTO_MAXIMO = new BigDecimal("1000000.00");
    public static final int PLAZO_MAXIMO_MESES = 360;

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

    /**
     * Valida monto, plazo y tasa sin guardar nada. La usa también el Portal
     * del Cliente ANTES de grabar la operación, para no dejar operaciones
     * huérfanas si el préstamo se rechaza (DEF-007).
     */
    public void validar(Prestamo p) {
        ReglasMonto.exigirMonto(p.getMontoPrestamo(), MONTO_MINIMO, MONTO_MAXIMO, "El monto del préstamo");
        ReglasMonto.exigirPlazo(p.getPlazoMeses(), PLAZO_MAXIMO_MESES, "meses");
        p.setTasaInteres(ReglasMonto.exigirTasa(p.getTasaInteres()));
    }

    public Prestamo crear(Prestamo p) {
        validar(p);

        BigDecimal[] calculo = calcular(p.getMontoPrestamo(), p.getTasaInteres(), p.getPlazoMeses());
        p.setCuotaMensual(calculo[0]);
        p.setSaldoPendiente(calculo[1]);

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

    /**
     * Cálculo de la cuota con interés simple: total = monto × (1 + tasa%/100 × plazoMeses)
     * y cuota = total / plazo. Lo usan el registro de préstamos y las cotizaciones,
     * así la cotización siempre coincide con lo que luego se cobra.
     *
     * @return {cuota mensual, total a pagar}, ambos con 2 decimales
     */
    public static BigDecimal[] calcular(BigDecimal monto, BigDecimal tasaInteres, int plazoMeses) {
        BigDecimal tasa = tasaInteres == null ? BigDecimal.ZERO : tasaInteres;
        BigDecimal tasaFraccion = tasa
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(plazoMeses));
        BigDecimal totalPagar = monto.multiply(BigDecimal.ONE.add(tasaFraccion));
        BigDecimal cuota = totalPagar.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        return new BigDecimal[]{cuota, totalPagar.setScale(2, RoundingMode.HALF_UP)};
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el prestamo con id " + id + ".");
        }
    }
}
