package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.PrestamoDaoMemoria;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/** RF-OPE-05 · Préstamo con interés simple, cuota, vencimiento y saldo. */
public class PrestamoServiceTest {

    private PrestamoService servicio;

    @Before
    public void preparar() {
        // TDD: el servicio se prueba con un DAO en memoria (sin PostgreSQL).
        servicio = new PrestamoService(new PrestamoDaoMemoria());
    }

    private static Prestamo prestamo(String monto, String tasa, Integer meses) {
        Prestamo p = new Prestamo();
        p.setMontoPrestamo(monto == null ? null : new BigDecimal(monto));
        p.setTasaInteres(tasa == null ? null : new BigDecimal(tasa));
        p.setPlazoMeses(meses);
        p.setFechaInicio(LocalDate.of(2026, 1, 15));
        return p;
    }

    @Test
    public void calculaCuotaMensualConInteresSimple() {
        // 1000 al 2 % mensual por 10 meses → total 1200 → cuota 120.00
        Prestamo p = servicio.crear(prestamo("1000", "2", 10));
        assertEquals(new BigDecimal("120.00"), p.getCuotaMensual());
        assertEquals(new BigDecimal("1200.00"), p.getSaldoPendiente());
    }

    @Test
    public void calculaLaFechaDeVencimientoSegunElPlazo() {
        Prestamo p = servicio.crear(prestamo("5000", "15", 12));
        assertEquals(LocalDate.of(2027, 1, 15), p.getFechaVencimiento());
        assertEquals("ACTIVO", p.getEstado());
        assertNotNull(p.getIdPrestamo());
    }

    @Test
    public void redondeaLaCuotaADosDecimales() {
        Prestamo p = servicio.crear(prestamo("5000", "15", 12));
        // 5000 × (1 + 0.15 × 12) = 14000 → 1166.666… → 1166.67
        assertEquals(new BigDecimal("1166.67"), p.getCuotaMensual());
    }

    @Test
    public void rechazaMontoYPlazoInvalidos() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(prestamo("0", "2", 10)));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(prestamo(null, "2", 10)));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(prestamo("1000", "2", 0)));
    }

    @Test
    public void sinTasaSeAsumeCero() {
        Prestamo p = servicio.crear(prestamo("600", null, 6));
        assertEquals(new BigDecimal("100.00"), p.getCuotaMensual());
    }
}
