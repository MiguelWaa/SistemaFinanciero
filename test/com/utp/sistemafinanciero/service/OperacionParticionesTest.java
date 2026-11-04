package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.dao.memoria.OperacionDaoMemoria;
import com.utp.sistemafinanciero.dao.memoria.TipoCambioDaoMemoria;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PU-OPE · Cambio de moneda (RF-OPE-01/02).
 *
 * <p>Partición de equivalencia y valores límite sobre cliente, monto,
 * tipo de cambio y estado inicial. Reglas: 0 &lt; monto ≤ 10 000 000.00
 * (2 decimales), tipo de cambio &gt; 0, estado ∈ {COMPLETADA, PENDIENTE,
 * ANULADA} (vacío = COMPLETADA).</p>
 */
@RunWith(Parameterized.class)
public class OperacionParticionesTest {

    @Parameters(name = "{0}: cliente={1} monto={2} tc={3} estado={4} -> {5}")
    public static Collection<Object[]> casos() {
        return Arrays.asList(new Object[][]{
                // id,          cliente, monto,         tc,       estado,       total esperado (null = rechazo)
                {"PU-OPE-01", 1,    "100",          "3.7500", null,         "375.00"},
                {"PU-OPE-02", null, "100",          "3.7500", null,         null},     // PE sin cliente
                {"PU-OPE-03", 1,    "0",            "3.7500", null,         null},     // VL monto 0
                {"PU-OPE-04", 1,    "0.01",         "3.7500", null,         "0.04"},   // VL monto mínimo (redondeo)
                {"PU-OPE-05", 1,    "10000000.00",  "3.7500", null,         "37500000.00"}, // VL máximo
                {"PU-OPE-06", 1,    "10000000.01",  "3.7500", null,         null},     // VL máximo + 0.01
                {"PU-OPE-07", 1,    "1e20",         "3.7500", null,         null},     // PE enorme (antes: HTTP 500)
                {"PU-OPE-08", 1,    "100",          "0",      null,         null},     // VL tipo de cambio 0
                {"PU-OPE-09", 1,    "100",          "3.7500", "pendiente",  "375.00"}, // PE estado válido en minúsculas
                {"PU-OPE-10", 1,    "100",          "3.7500", "HACKEADO",   null},     // PE estado inventado (antes se guardaba)
        });
    }

    @Parameter(0) public String id;
    @Parameter(1) public Integer cliente;
    @Parameter(2) public String monto;
    @Parameter(3) public String tipoCambio;
    @Parameter(4) public String estado;
    @Parameter(5) public String totalEsperado;

    @Test
    public void aplicaLaReglaDelPlan() {
        OperacionService servicio = new OperacionService(new OperacionDaoMemoria(new MemoriaDaoFactory()), new TipoCambioDaoMemoria());
        Operacion o = new Operacion();
        o.setIdCliente(cliente);
        o.setTipoOperacion("CAMBIO");
        o.setMonto(new BigDecimal(monto));
        o.setTipoCambio(new BigDecimal(tipoCambio));
        o.setEstado(estado);

        if (totalEsperado != null) {
            Operacion creada = servicio.crear(o);
            assertEquals(id, new BigDecimal(totalEsperado), creada.getTotalRecibir());
            assertTrue(id + ": estado normalizado", creada.getEstado().matches("COMPLETADA|PENDIENTE|ANULADA"));
        } else {
            assertThrows(id, ReglaNegocioException.class, () -> servicio.crear(o));
        }
    }
}
