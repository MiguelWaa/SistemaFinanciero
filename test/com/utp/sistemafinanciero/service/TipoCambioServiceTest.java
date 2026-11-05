package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.TipoCambioDaoMemoria;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Nivel UNITARIO · Operaciones (tipos de cambio).
 * CP-UNI-TC-01…04 · RF-OPE-02: monedas y valores obligatorios, fecha y estado por defecto.
 */
public class TipoCambioServiceTest {

    private TipoCambioService servicio;

    @Before
    public void preparar() {
        servicio = new TipoCambioService(new TipoCambioDaoMemoria());
    }

    private static TipoCambio tc(String origen, String destino, String compra, String venta) {
        TipoCambio t = new TipoCambio();
        t.setMonedaOrigen(origen);
        t.setMonedaDestino(destino);
        t.setCompra(compra == null ? null : new BigDecimal(compra));
        t.setVenta(venta == null ? null : new BigDecimal(venta));
        return t;
    }

    @Test
    public void registraConFechaDeHoyYEstadoActivo() {
        TipoCambio t = servicio.crear(tc("USD", "PEN", "3.72", "3.78"));
        assertNotNull(t.getIdTipoCambio());
        assertEquals(LocalDate.now(), t.getFechaInicio());
        assertEquals("ACTIVO", t.getEstado());
    }

    @Test
    public void exigeMonedaDeOrigenYDestino() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> servicio.crear(tc(null, "PEN", "3.72", "3.78")));
        assertTrue(e.getMessage().contains("moneda"));
    }

    @Test
    public void exigeValoresDeCompraYVenta() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(tc("USD", "PEN", null, "3.78")));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(tc("USD", "PEN", "3.72", null)));
    }

    @Test
    public void actualizaYEliminaUnTipoDeCambio() {
        TipoCambio t = servicio.crear(tc("EUR", "PEN", "4.05", "4.15"));
        TipoCambio nuevo = tc("EUR", "PEN", "4.10", "4.20");
        nuevo.setEstado("ACTIVO");
        servicio.actualizar(t.getIdTipoCambio(), nuevo);
        assertEquals(0, new BigDecimal("4.20").compareTo(servicio.buscar(t.getIdTipoCambio()).getVenta()));
        servicio.eliminar(t.getIdTipoCambio());
        assertThrows(ReglaNegocioException.class, () -> servicio.buscar(t.getIdTipoCambio()));
    }
}
