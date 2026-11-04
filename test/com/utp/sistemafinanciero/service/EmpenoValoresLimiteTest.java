package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.EmpenoDaoMemoria;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
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
 * Plan de Pruebas · PU-EMP · Empeños (RF-OPE-06). Valores límite:
 * 0 &lt; monto ≤ 1 000 000.00, 0 ≤ tasa ≤ 100, 1 ≤ plazo ≤ 365 días.
 */
@RunWith(Parameterized.class)
public class EmpenoValoresLimiteTest {

    @Parameters(name = "{0}: monto={1} tasa={2} dias={3} -> {4}")
    public static Collection<Object[]> casos() {
        return Arrays.asList(new Object[][]{
                {"PU-EMP-01", "0.01",       "5",    30,  true},
                {"PU-EMP-02", "0",          "5",    30,  false},
                {"PU-EMP-03", "1000000.01", "5",    30,  false},
                {"PU-EMP-04", "800",        "-1",   30,  false},   // antes se aceptaba en memoria y PostgreSQL lo rechazaba
                {"PU-EMP-05", "800",        "100.5", 30, false},
                {"PU-EMP-06", "800",        "5",    1,   true},
                {"PU-EMP-07", "800",        "5",    365, true},
                {"PU-EMP-08", "800",        "5",    366, false},
        });
    }

    @Parameter(0) public String id;
    @Parameter(1) public String monto;
    @Parameter(2) public String tasa;
    @Parameter(3) public Integer dias;
    @Parameter(4) public boolean valido;

    @Test
    public void aplicaLaReglaDelPlan() {
        EmpenoService servicio = new EmpenoService(new EmpenoDaoMemoria());
        Empeno e = new Empeno();
        e.setIdArticulo(1);
        e.setMontoPrestamo(new BigDecimal(monto));
        e.setTasaInteres(new BigDecimal(tasa));
        e.setPlazoDias(dias);
        e.setFechaEmpeno(LocalDate.of(2026, 11, 3));
        if (valido) {
            Empeno creado = servicio.crear(e);
            assertEquals(id, LocalDate.of(2026, 11, 3).plusDays(dias), creado.getFechaVencimiento());
        } else {
            assertThrows(id, ReglaNegocioException.class, () -> servicio.crear(e));
        }
    }
}
