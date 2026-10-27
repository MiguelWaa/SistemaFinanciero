package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.model.pagos.Pago;
import com.utp.sistemafinanciero.util.Json;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * v5.2 · Pagos con Yape, Plin y Tunki desde el Portal del Cliente.
 * Datos semilla: cliente 2 (María) tiene el préstamo de la operación 2
 * (saldo 5750.00, cuota 479.17); cliente 1 (Juan) el empeño de la
 * operación 3 (saldo 800.00).
 */
public class PagoServiceTest {

    private Contenedor c;
    private PagoService pagos;

    @Before
    public void preparar() throws Exception {
        c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-pagos"));
        pagos = c.pagos;
    }

    private static Map<String, Object> pago(Integer idOperacion, String concepto, String monto, String metodo,
                                            String numero) {
        return Json.map("idOperacion", idOperacion, "concepto", concepto, "monto", new BigDecimal(monto),
                "metodo", metodo, "numeroOperacion", numero, "celularOrigen", "987111222");
    }

    private Prestamo prestamoOp2() {
        return c.bd.prestamos().buscarPorOperacion(2);
    }

    @Test
    public void lasCuotasDePrestamosActivosSonPagables() {
        List<Map<String, Object>> l = pagos.pagables(2);
        assertEquals(1, l.size());
        assertEquals("CUOTA_PRESTAMO", l.get(0).get("concepto"));
        assertEquals(new BigDecimal("479.17"), l.get(0).get("montoSugerido"));
        assertEquals(new BigDecimal("5750.00"), l.get(0).get("montoMaximo"));
    }

    @Test
    public void elPagoDelPortalQuedaPorVerificarYNoTocaElSaldo() {
        Pago p = pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "479.17", "YAPE", "12345678"));
        assertEquals(Pago.POR_VERIFICAR, p.getEstado());
        assertEquals("PORTAL", p.getOrigen());
        assertEquals("C2-OP2", p.getReferencia());
        assertNull("Sin confirmar no hay comprobante", p.getNumeroComprobante());
        assertEquals(new BigDecimal("5750.00"), prestamoOp2().getSaldoPendiente());
        assertEquals(1, pagos.listar("POR_VERIFICAR").size());
    }

    @Test
    public void confirmarDescuentaElSaldoYEmiteComprobante() {
        Pago p = pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "479.17", "PLIN", "A1B2C3D4"));
        Pago ok = pagos.confirmar(p.getIdPago(), "lucia.ramos@sigsf.com");
        assertEquals(Pago.CONFIRMADO, ok.getEstado());
        assertEquals("lucia.ramos@sigsf.com", ok.getVerificadoPor());
        assertEquals(String.format("RC-%06d", ok.getIdPago()), ok.getNumeroComprobante());
        assertEquals(new BigDecimal("5270.83"), prestamoOp2().getSaldoPendiente());
        assertThrows("No se confirma dos veces", ReglaNegocioException.class,
                () -> pagos.confirmar(p.getIdPago(), "otro"));
    }

    @Test
    public void rechazarExigeMotivoYNoDescuenta() {
        Pago p = pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "100", "TUNKI", "99887766"));
        assertThrows(ReglaNegocioException.class, () -> pagos.rechazar(p.getIdPago(), " ", "lucia"));
        Pago r = pagos.rechazar(p.getIdPago(), "No llegó el abono", "lucia");
        assertEquals(Pago.RECHAZADO, r.getEstado());
        assertEquals("No llegó el abono", r.getObservacion());
        assertEquals(new BigDecimal("5750.00"), prestamoOp2().getSaldoPendiente());
    }

    @Test
    public void unNumeroDeOperacionNoSeReutilizaConLaMismaBilletera() {
        pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "50", "YAPE", "55554444"));
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "50", "YAPE", "55554444")));
        assertTrue(e.getMessage().contains("ya fue registrado"));
        // Con otra billetera el mismo número sí es válido (son sistemas distintos).
        assertNotNull(pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "50", "PLIN", "55554444")));
    }

    @Test
    public void noSePuedePagarMasDeLoQueSeDebeContandoLoPorVerificar() {
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "5750.01", "YAPE", "10000001")));
        pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "5000", "YAPE", "10000002"));
        // Quedan 750 disponibles mientras los 5000 están por verificar.
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "751", "YAPE", "10000003")));
        assertEquals(new BigDecimal("750.00"), pagos.pagables(2).get(0).get("montoMaximo"));
    }

    @Test
    public void pagarTodoElEmpenoLoMarcaRecuperado() {
        Pago p = pagos.registrarDesdePortal(1, pago(3, "RESCATE_EMPENO", "800", "YAPE", "77776666"));
        pagos.confirmar(p.getIdPago(), "lucia");
        Empeno e = c.bd.empenos().buscarPorOperacion(3);
        assertEquals(0, e.getSaldoPendiente().signum());
        assertEquals("RECUPERADO", e.getEstado());
        assertTrue("Ya no hay nada que pagar", pagos.pagables(1).isEmpty());
    }

    @Test
    public void unClienteNoPuedePagarTramitesAjenos() {
        // La operación 2 es de María (cliente 2), no de Juan (cliente 1).
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(1, pago(2, "CUOTA_PRESTAMO", "10", "YAPE", "12121212")));
    }

    @Test
    public void validaBilleteraNumeroDeOperacionYCelular() {
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "10", "BITCOIN", "12345678")));
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "10", "YAPE", "")));
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "10", "YAPE", "12<script>")));
        Map<String, Object> d = pago(2, "CUOTA_PRESTAMO", "10", "YAPE", "31313131");
        d.put("celularOrigen", "12345");
        assertThrows(ReglaNegocioException.class, () -> pagos.registrarDesdePortal(2, d));
    }

    @Test
    public void unaBilleteraDesactivadaNoRecibePagos() {
        int idTunki = c.bd.billeteras().buscarPorCodigo("TUNKI").getIdBilletera();
        pagos.actualizarBilletera(idTunki, Json.map("activo", false));
        assertEquals(2, pagos.billeterasActivas().size());
        assertThrows(ReglaNegocioException.class,
                () -> pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "10", "TUNKI", "40404040")));
    }

    @Test
    public void laBilleteraSoloAceptaImagenesYCelularesValidos() {
        int idYape = c.bd.billeteras().buscarPorCodigo("YAPE").getIdBilletera();
        assertThrows(ReglaNegocioException.class,
                () -> pagos.actualizarBilletera(idYape, Json.map("qrImagen", "javascript:alert(1)")));
        assertThrows(ReglaNegocioException.class,
                () -> pagos.actualizarBilletera(idYape, Json.map("numeroCelular", "01234")));
        String png = "data:image/png;base64,iVBORw0KGgo=";
        assertTrue(pagos.actualizarBilletera(idYape, Json.map("qrImagen", png, "titular", "Caja Central")).isTieneQr());
    }

    @Test
    public void elPagoEnCajaQuedaConfirmadoAlInstante() {
        Map<String, Object> d = Json.map("idCliente", 1, "idOperacion", 3, "concepto", "RESCATE_EMPENO",
                "monto", new BigDecimal("300"), "metodo", "EFECTIVO");
        Pago p = pagos.registrarEnCaja(d, "lucia.ramos@sigsf.com");
        assertEquals(Pago.CONFIRMADO, p.getEstado());
        assertEquals("CAJA", p.getOrigen());
        assertEquals(new BigDecimal("500.00"), c.bd.empenos().buscarPorOperacion(3).getSaldoPendiente());
    }

    @Test
    public void elResumenAgrupaLoCobradoPorBilletera() {
        Pago a = pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "100", "YAPE", "20000001"));
        Pago b = pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "50", "PLIN", "20000002"));
        pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "25", "YAPE", "20000003"));
        pagos.confirmar(a.getIdPago(), "x");
        pagos.confirmar(b.getIdPago(), "x");
        Map<String, Object> r = pagos.resumen();
        assertEquals(1, r.get("porVerificar"));
        assertEquals(new BigDecimal("150.00"), r.get("cobradoHoy"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> metodos = (List<Map<String, Object>>) r.get("porMetodo");
        Map<String, Object> yape = metodos.stream().filter(m -> "YAPE".equals(m.get("metodo"))).findFirst().get();
        assertEquals(1, yape.get("cantidad"));
        assertEquals(new BigDecimal("100.00"), yape.get("monto"));
    }

    @Test
    public void elEstadoDeCuentaSumaDeudaYPagos() {
        Pago p = pagos.registrarDesdePortal(2, pago(2, "CUOTA_PRESTAMO", "750", "YAPE", "30000001"));
        pagos.confirmar(p.getIdPago(), "x");
        Map<String, Object> ec = pagos.estadoCuenta(2);
        assertEquals(new BigDecimal("5000.00"), ec.get("deudaTotal"));
        assertEquals(new BigDecimal("750.00"), ec.get("totalPagado"));
    }
}
