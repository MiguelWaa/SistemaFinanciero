package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.EmpenoDaoMemoria;
import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.ventas.Venta;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/** RF-VNT-02 · RNF-VNT-01 · RF-OPE-06 · Ventas y empeños. */
public class VentaYEmpenoServiceTest {

    @Test
    public void ventaSinMontoCopiaElImporteYQuedaRegistrada() {
        VentaService ventas = new VentaService(new MemoriaDaoFactory().ventas());
        Venta v = new Venta();
        v.setImporte(new BigDecimal("305.00"));
        Venta creada = ventas.crear(v);
        assertEquals(new BigDecimal("305.00"), creada.getMonto());
        assertEquals("REGISTRADA", creada.getEstado());
        assertNotNull(creada.getFechaVenta());
    }

    @Test
    public void ventaConImporteNegativoSeRechaza() {
        VentaService ventas = new VentaService(new MemoriaDaoFactory().ventas());
        Venta v = new Venta();
        v.setImporte(new BigDecimal("-1"));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> ventas.crear(v));
        assertEquals("El importe de la venta no es valido.", e.getMessage());
    }

    @Test
    public void empenoCalculaVencimientoYSaldoInicial() {
        EmpenoService empenos = new EmpenoService(new EmpenoDaoMemoria());
        Empeno e = new Empeno();
        e.setMontoPrestamo(new BigDecimal("800"));
        e.setPlazoDias(30);
        e.setFechaEmpeno(LocalDate.of(2026, 10, 1));
        Empeno creado = empenos.crear(e);
        assertEquals(LocalDate.of(2026, 10, 31), creado.getFechaVencimiento());
        assertEquals(new BigDecimal("800"), creado.getSaldoPendiente());
        assertEquals("ACTIVO", creado.getEstado());
    }
}
