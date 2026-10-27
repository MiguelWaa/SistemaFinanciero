package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.model.pagos.Cotizacion;
import com.utp.sistemafinanciero.util.Json;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** v5.2 · Cotizaciones de préstamo, empeño y cambio de moneda. */
public class CotizacionServiceTest {

    private Contenedor c;
    private CotizacionService cot;

    @Before
    public void preparar() throws Exception {
        c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-cot"));
        cot = c.cotizaciones;
    }

    @Test
    public void laCotizacionDePrestamoCoincideConLoQueSeCobra() {
        Cotizacion q = cot.cotizar(1, Json.map("tipo", "PRESTAMO", "monto", 5000, "plazo", 12), "PORTAL");
        // Mismo cálculo que PrestamoService: 5000 × (1 + 0.15 × 12) = 14000 → cuota 1166.67
        assertEquals(new BigDecimal("1166.67"), q.getCuota());
        assertEquals(new BigDecimal("14000.00"), q.getTotalPagar());
        assertEquals(new BigDecimal("9000.00"), q.getTotalInteres());
        assertEquals(String.format("COT-%06d", q.getIdCotizacion()), q.getNumero());
        assertEquals(LocalDate.now().plusDays(CotizacionService.VIGENCIA_DIAS), q.getFechaVencimiento());
        assertEquals(Cotizacion.VIGENTE, q.getEstado());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void elCronogramaSumaElTotalYTerminaEnCero() {
        Cotizacion q = cot.cotizar(1, Json.map("tipo", "PRESTAMO", "monto", 5000, "plazo", 12), "PORTAL");
        List<Map<String, Object>> filas = (List<Map<String, Object>>) cot.detalle(q.getIdCotizacion()).get("cronograma");
        assertEquals(12, filas.size());
        BigDecimal suma = BigDecimal.ZERO;
        for (Map<String, Object> f : filas) suma = suma.add((BigDecimal) f.get("cuota"));
        assertEquals(0, suma.compareTo(new BigDecimal("14000.00")));
        assertEquals(0, ((BigDecimal) filas.get(11).get("saldo")).signum());
    }

    @Test
    public void laCotizacionDeCambioUsaElTipoDeVenta() {
        Cotizacion q = cot.cotizar(1, Json.map("tipo", "CAMBIO", "monto", 100, "idTipoCambio", 1), "PORTAL");
        assertEquals(new BigDecimal("3.7800"), q.getTipoCambio());
        assertEquals(new BigDecimal("378.00"), q.getMontoRecibir());
        assertEquals("PEN", q.getMonedaRecibir());
    }

    @Test
    public void elEmpenoNoPuedeSuperarElValorDelArticulo() {
        assertThrows(ReglaNegocioException.class, () -> cot.cotizar(1, Json.map("tipo", "EMPENO", "monto", 2000,
                "valorEstimado", 1500, "descripcion", "Laptop"), "PORTAL"));
        Cotizacion q = cot.cotizar(1, Json.map("tipo", "EMPENO", "monto", 1000, "valorEstimado", 1500,
                "descripcion", "Laptop", "plazo", 30), "PORTAL");
        assertEquals(new BigDecimal("100.00"), q.getTotalInteres());   // 10 % por 30 días
        assertEquals(new BigDecimal("1100.00"), q.getTotalPagar());
    }

    @Test
    public void validaTipoMontoYPlazo() {
        assertThrows(ReglaNegocioException.class, () -> cot.cotizar(1, Json.map("tipo", "HIPOTECA", "monto", 10), "PORTAL"));
        assertThrows(ReglaNegocioException.class, () -> cot.cotizar(1, Json.map("tipo", "PRESTAMO", "monto", 0, "plazo", 6), "PORTAL"));
        assertThrows(ReglaNegocioException.class, () -> cot.cotizar(1, Json.map("tipo", "PRESTAMO", "monto", 100, "plazo", 99), "PORTAL"));
        assertThrows(ReglaNegocioException.class, () -> cot.cotizar(999, Json.map("tipo", "PRESTAMO", "monto", 100, "plazo", 6), "PORTAL"));
    }

    @Test
    public void convertirCreaUnaSolicitudPendienteUnaSolaVez() {
        Cotizacion q = cot.cotizar(1, Json.map("tipo", "PRESTAMO", "monto", 1200, "plazo", 6), "PORTAL");
        int antes = c.solicitudes.listar("PENDIENTE").size();
        Map<String, Object> r = cot.convertir(1, q.getIdCotizacion());
        assertNotNull(r.get("idOperacion"));
        assertEquals(antes + 1, c.solicitudes.listar("PENDIENTE").size());
        Cotizacion despues = (Cotizacion) r.get("cotizacion");
        assertEquals(Cotizacion.CONVERTIDA, despues.getEstado());
        assertThrows(ReglaNegocioException.class, () -> cot.convertir(1, q.getIdCotizacion()));
    }

    @Test
    public void unClienteNoVeNiConvierteCotizacionesAjenas() {
        Cotizacion q = cot.cotizar(1, Json.map("tipo", "PRESTAMO", "monto", 1200, "plazo", 6), "PORTAL");
        assertThrows(ReglaNegocioException.class, () -> cot.detalleDelCliente(2, q.getIdCotizacion()));
        assertThrows(ReglaNegocioException.class, () -> cot.convertir(2, q.getIdCotizacion()));
        assertEquals(1, cot.delCliente(1).size());
        assertEquals(0, cot.delCliente(2).size());
    }
}
