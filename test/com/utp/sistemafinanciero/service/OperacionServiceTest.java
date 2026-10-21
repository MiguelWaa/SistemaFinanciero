package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/** RF-OPE-01/02/03 · Cambio de moneda calculado en el servidor. */
public class OperacionServiceTest {

    private OperacionService servicio;
    private int idTipoCambio;

    @Before
    public void preparar() {
        MemoriaDaoFactory bd = new MemoriaDaoFactory();
        idTipoCambio = bd.tiposCambio().crear(new TipoCambio(null, "USD", "PEN",
                new BigDecimal("3.7200"), new BigDecimal("3.7850"), LocalDate.now(), "ACTIVO"));
        servicio = new OperacionService(bd.operaciones(), bd.tiposCambio());
    }

    private Operacion cambio(String monto) {
        Operacion o = new Operacion();
        o.setIdCliente(1);
        o.setIdTipoCambio(idTipoCambio);
        o.setTipoOperacion("CAMBIO");
        o.setMonto(new BigDecimal(monto));
        return o;
    }

    @Test
    public void calculaElTotalARecibirConElTipoDeCambioVigente() {
        Operacion o = servicio.crear(cambio("100"));
        assertEquals(new BigDecimal("378.50"), o.getTotalRecibir()); // 100 × 3.7850
        assertEquals(new BigDecimal("100"), o.getTotalEntregar());
        assertEquals("USD", o.getMonedaOrigen());
        assertEquals("COMPLETADA", o.getEstado());
    }

    @Test
    public void ignoraTotalesYCotizacionManipuladosDesdeElNavegador() {
        Operacion o = cambio("100");
        o.setTipoCambio(new BigDecimal("99"));
        o.setTotalRecibir(new BigDecimal("999999"));
        Operacion creada = servicio.crear(o);
        assertEquals(new BigDecimal("378.50"), creada.getTotalRecibir());
    }

    @Test
    public void exigeClienteYMontoPositivo() {
        Operacion sinCliente = cambio("100");
        sinCliente.setIdCliente(null);
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(sinCliente));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(cambio("-5")));
    }

    @Test
    public void soloAceptaEstadosValidos() {
        Operacion o = servicio.crear(cambio("50"));
        assertEquals("ANULADA", servicio.cambiarEstado(o.getIdOperacion(), "anulada").getEstado());
        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(o.getIdOperacion(), "HACKEADA"));
    }
}
