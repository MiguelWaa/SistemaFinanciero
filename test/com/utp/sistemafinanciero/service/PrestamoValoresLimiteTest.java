package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.PrestamoDaoMemoria;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PU-PRE · Préstamos (RF-OPE-05).
 *
 * <p>Técnica de caja negra: <b>análisis de valores límite (VL)</b> y
 * <b>partición de equivalencia (PE)</b> sobre las tres entradas del
 * préstamo. Reglas acordadas en el Avance 4:</p>
 * <ul>
 *   <li>monto: 100.00 ≤ monto ≤ 1 000 000.00 y como máximo 2 decimales
 *       (con montos menores la cuota se redondeaba a S/ 0.00: hallazgo PU-PRE-02)</li>
 *   <li>tasa mensual: 0 ≤ tasa ≤ 100 (vacía = 0)</li>
 *   <li>plazo: 1 ≤ meses ≤ 360</li>
 * </ul>
 * Cada fila es un caso de prueba del plan (PU-PRE-01 … PU-PRE-19).
 */
@RunWith(Parameterized.class)
public class PrestamoValoresLimiteTest {

    @Parameters(name = "{0}: monto={1} tasa={2} plazo={3} -> {4}")
    public static Collection<Object[]> casos() {
        return Arrays.asList(new Object[][]{
                // id,          monto,          tasa,      plazo, ¿válido?  técnica
                {"PU-PRE-01", "0",            "2",       12,   false},  // PE monto cero
                {"PU-PRE-02", "99.99",        "2",       12,   false},  // VL monto: mínimo - 0.01 (antes cuota S/ 0.00 con 0.01)
                {"PU-PRE-03", "-500",         "2",       12,   false},  // PE monto negativo
                {"PU-PRE-19", "100.00",       "2",       12,   true},   // VL monto: mínimo
                {"PU-PRE-04", "1000000.00",   "2",       12,   true},   // VL monto: máximo
                {"PU-PRE-05", "1000000.01",   "2",       12,   false},  // VL monto: máximo + 0.01
                {"PU-PRE-06", "999999999999999", "2",    12,   false},  // PE monto enorme (antes: HTTP 500)
                {"PU-PRE-07", "1000.555",     "2",       12,   false},  // PE monto con 3 decimales
                {"PU-PRE-08", "1000",         "-0.001",  12,   false},  // VL tasa: justo bajo cero
                {"PU-PRE-09", "1000",         "0",       12,   true},   // VL tasa: mínimo
                {"PU-PRE-10", "1000",         null,      12,   true},   // PE tasa vacía = 0
                {"PU-PRE-11", "1000",         "100",     12,   true},   // VL tasa: máximo
                {"PU-PRE-12", "1000",         "100.001", 12,   false},  // VL tasa: máximo + delta
                {"PU-PRE-13", "1000",         "1500",    12,   false},  // PE tasa absurda (antes: HTTP 500)
                {"PU-PRE-14", "1000",         "2",       0,    false},  // VL plazo: 0
                {"PU-PRE-15", "1000",         "2",       1,    true},   // VL plazo: mínimo
                {"PU-PRE-16", "1000",         "2",       360,  true},   // VL plazo: máximo (30 años)
                {"PU-PRE-17", "1000",         "2",       361,  false},  // VL plazo: máximo + 1
                {"PU-PRE-18", "1000",         "2",       null, false},  // PE plazo vacío
        });
    }

    @Parameter(0) public String id;
    @Parameter(1) public String monto;
    @Parameter(2) public String tasa;
    @Parameter(3) public Integer plazo;
    @Parameter(4) public boolean valido;

    @Test
    public void aplicaLaReglaDelPlan() {
        PrestamoService servicio = new PrestamoService(new PrestamoDaoMemoria());
        Prestamo p = new Prestamo();
        p.setMontoPrestamo(new BigDecimal(monto));
        p.setTasaInteres(tasa == null ? null : new BigDecimal(tasa));
        p.setPlazoMeses(plazo);
        p.setFechaInicio(LocalDate.of(2026, 11, 3));

        if (valido) {
            Prestamo creado = servicio.crear(p);
            assertNotNull(id + ": debe guardarse", creado.getIdPrestamo());
            assertTrue(id + ": la cuota debe ser positiva", creado.getCuotaMensual().signum() > 0);
            assertEquals(id + ": la cuota tiene 2 decimales", 2, creado.getCuotaMensual().scale());
        } else {
            try {
                servicio.crear(p);
                fail(id + ": el servicio debía rechazar el préstamo");
            } catch (ReglaNegocioException esperado) {
                assertFalse(id + ": el mensaje debe explicar el motivo", esperado.getMessage().isBlank());
            }
        }
    }
}
