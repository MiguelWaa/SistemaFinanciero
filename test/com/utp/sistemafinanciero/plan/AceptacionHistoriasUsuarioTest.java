package com.utp.sistemafinanciero.plan;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.utp.sistemafinanciero.plan.ServidorDePrueba.*;
import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Nivel ACEPTACIÓN (pruebas de aceptación automatizadas).
 *
 * <p>Cada método es un criterio de aceptación de una historia de usuario,
 * escrito en formato <b>Dado / Cuando / Entonces</b> (estilo BDD) y con el
 * mismo código HU-xx que la matriz de aceptación de
 * {@code docs/10_PLAN_DE_PRUEBAS.md}. El usuario de negocio firma la
 * aceptación revisando esta misma lista con el sistema en vivo.</p>
 */
public class AceptacionHistoriasUsuarioTest {

    private ServidorDePrueba s;

    @Before
    public void levantar() throws Exception {
        s = ServidorDePrueba.iniciar();
    }

    @After
    public void detener() {
        s.close();
    }

    /** HU-01 · Como cliente quiero crear mi cuenta en el portal para hacer trámites sin ir a la agencia. */
    @Test
    public void hu01_clienteCreaSuCuentaYEntraConSuDocumento() throws Exception {
        // Dado un ciudadano sin cuenta
        // Cuando se registra con documento, nombres y una clave de 6+ caracteres
        HttpResponse<String> r = s.pedir("POST", "/api/portal/registro", null,
                "{\"documento\":\"46665544\",\"nombres\":\"Ana\",\"apellidos\":\"Torres\",\"password\":\"segura1\"}");
        // Entonces queda ACTIVO y puede volver a entrar con su documento
        assertEquals(r.body(), 200, r.statusCode());
        assertEquals("ACTIVO", objeto(r).get("estado"));
        assertNotNull(s.loginCliente("46665544", "segura1"));
        // Y no puede registrarse dos veces con el mismo documento
        assertEquals(400, s.pedir("POST", "/api/portal/registro", null,
                "{\"documento\":\"46665544\",\"nombres\":\"Ana\",\"password\":\"segura1\"}").statusCode());
    }

    /** HU-02 · Como cliente quiero cotizar un préstamo y convertir la cotización en solicitud. */
    @Test
    public void hu02_clienteCotizaYConvierteEnSolicitud() throws Exception {
        String juan = s.loginCliente("70000001", "70000001");
        // Cuando cotiza S/ 3000 a 6 meses
        HttpResponse<String> cot = s.pedir("POST", "/api/portal/1/cotizaciones", juan,
                "{\"tipo\":\"PRESTAMO\",\"monto\":3000,\"plazo\":6}");
        Map<String, Object> c = objeto(cot);
        // Entonces recibe número COT-, cuota y vigencia
        assertTrue(String.valueOf(c.get("numero")).startsWith("COT-"));
        assertEquals("VIGENTE", c.get("estado"));
        assertTrue(decimal(c, "cuota") > 0);
        // Y al solicitarla pasa a CONVERTIDA y aparece en la bandeja de solicitudes
        int id = entero(c, "idCotizacion");
        HttpResponse<String> conv = s.pedir("POST", "/api/portal/1/cotizaciones/" + id + "/solicitar", juan, "{}");
        assertEquals(conv.body(), 200, conv.statusCode());
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        assertTrue(s.get("/api/solicitudes/pendientes", admin).body().contains("70000001"));
        // Y no se puede convertir dos veces
        assertEquals(400, s.pedir("POST", "/api/portal/1/cotizaciones/" + id + "/solicitar", juan, "{}").statusCode());
    }

    /** HU-03 · Como cliente quiero pagar con Yape/Plin/Tunki y recibir mi comprobante. */
    @Test
    public void hu03_clientePagaConQrYRecibeComprobante() throws Exception {
        String maria = s.loginCliente("70000002", "70000002");
        // Dado que tiene un trámite pagable
        List<Map<String, Object>> pagables = lista(s.get("/api/portal/2/pagables", maria));
        assertFalse(pagables.isEmpty());
        Map<String, Object> p = pagables.get(0);
        // Cuando paga y registra su número de operación
        HttpResponse<String> r = s.pedir("POST", "/api/portal/2/pagos", maria,
                "{\"idOperacion\":" + entero(p, "idOperacion") + ",\"concepto\":\"" + p.get("concepto")
                        + "\",\"monto\":" + decimal(p, "montoSugerido") + ",\"metodo\":\"PLIN\",\"numeroOperacion\":\"55443322\"}");
        int idPago = entero(objeto(r), "idPago");
        // Entonces queda POR_VERIFICAR hasta que caja lo confirma, y luego tiene comprobante RC-
        assertEquals("POR_VERIFICAR", objeto(r).get("estado"));
        String cajero = s.loginPersonal("lucia.ramos@sigsf.com", "lucia123");
        s.pedir("PUT", "/api/pagos/" + idPago + "/confirmar", cajero, "{}");
        Map<String, Object> suyo = objeto(s.get("/api/portal/2/pagos/" + idPago, maria));
        assertEquals("CONFIRMADO", suyo.get("estado"));
        assertTrue(String.valueOf(suyo.get("numeroComprobante")).startsWith("RC-"));
    }

    /** HU-04 · Como cajero quiero registrar un cambio de moneda con el tipo de cambio vigente. */
    @Test
    public void hu04_cajeroRegistraCambioConTipoVigente() throws Exception {
        String cajero = s.loginPersonal("lucia.ramos@sigsf.com", "lucia123");
        HttpResponse<String> r = s.pedir("POST", "/api/operaciones", cajero,
                "{\"idCliente\":1,\"idTipoCambio\":1,\"monto\":250}");
        assertEquals(r.body(), 200, r.statusCode());
        Map<String, Object> o = objeto(r);
        assertEquals("COMPLETADA", o.get("estado"));
        assertEquals(250 * 3.78, decimal(o, "totalRecibir"), 0.001);
        // Y no se aceptan montos en cero
        assertEquals(400, s.pedir("POST", "/api/operaciones", cajero,
                "{\"idCliente\":1,\"idTipoCambio\":1,\"monto\":0}").statusCode());
    }

    /** HU-05 · Como supervisor quiero rechazar una solicitud y que no se pueda volver a resolver. */
    @Test
    public void hu05_supervisorRechazaSolicitud() throws Exception {
        String juan = s.loginCliente("70000001", "70000001");
        Map<String, Object> sol = objeto(s.pedir("POST", "/api/portal/1/prestamo", juan,
                "{\"montoPrestamo\":1500,\"plazoMeses\":3}"));
        @SuppressWarnings("unchecked")
        int idOp = entero((Map<String, Object>) sol.get("operacion"), "idOperacion");
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        HttpResponse<String> r = s.pedir("PUT", "/api/solicitudes/" + idOp + "/rechazar", admin, "{}");
        assertEquals("ANULADA", objeto(r).get("estado"));
        HttpResponse<String> otraVez = s.pedir("PUT", "/api/solicitudes/" + idOp + "/aprobar", admin, "{}");
        assertEquals(400, otraVez.statusCode());
        assertTrue(objeto(otraVez).get("error").toString().contains("ya fue atendida"));
    }

    /** HU-06 · Como administrador quiero que cada rol vea solo lo que le corresponde. */
    @Test
    public void hu06_cadaRolAccedeSoloASusModulos() throws Exception {
        String cajero = s.loginPersonal("lucia.ramos@sigsf.com", "lucia123");
        String analista = s.loginPersonal("jorge.torres@sigsf.com", "jorge123");
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        assertEquals(403, s.get("/api/usuarios", cajero).statusCode());
        assertEquals(403, s.get("/api/respaldos", cajero).statusCode());
        assertEquals(403, s.pedir("PUT", "/api/pagos/1/confirmar", analista, "{}").statusCode());
        assertEquals(200, s.get("/api/usuarios", admin).statusCode());
        assertEquals(200, s.get("/api/respaldos", admin).statusCode());
        assertEquals(200, s.get("/api/campanias", analista).statusCode());
    }

    /** HU-07 · Como analista quiero descargar los datos en Excel, CSV y JSON. */
    @Test
    public void hu07_exportacionEnTresFormatos() throws Exception {
        String analista = s.loginPersonal("jorge.torres@sigsf.com", "jorge123");
        HttpResponse<String> xlsx = s.get("/api/campanias/exportar?formato=xlsx", analista);
        HttpResponse<String> csv = s.get("/api/campanias/exportar?formato=csv", analista);
        HttpResponse<String> json = s.get("/api/campanias/exportar?formato=json", analista);
        assertEquals(200, xlsx.statusCode());
        assertEquals(200, csv.statusCode());
        assertEquals(200, json.statusCode());
        assertTrue(xlsx.body().startsWith("PK"));
        assertTrue(csv.headers().firstValue("Content-Type").orElse("").contains("csv"));
        assertTrue(objeto(json).containsKey("datos"));
    }

    /** HU-08 · Como gerencia quiero aprobar u observar expedientes. */
    @Test
    public void hu08_gerenciaResuelveExpedientes() throws Exception {
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        HttpResponse<String> nuevo = s.pedir("POST", "/api/expedientes", admin,
                "{\"codigo\":\"EXP-ACEP\",\"area\":\"OPERACIONES\",\"solicitante\":\"Caja\",\"monto\":5000}");
        int id = entero(objeto(nuevo), "idExpediente");
        assertEquals("PENDIENTE", objeto(nuevo).get("estado"));
        HttpResponse<String> r = s.pedir("PUT", "/api/expedientes/" + id, admin, "{\"estado\":\"OBSERVADO\"}");
        assertEquals("OBSERVADO", objeto(r).get("estado"));
        assertNotNull(objeto(r).get("fechaResolucion"));
    }

    /** HU-09 · Como gerente quiero ver los indicadores del negocio en el panel. */
    @Test
    public void hu09_panelConIndicadores() throws Exception {
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        Map<String, Object> k = objeto(s.get("/api/dashboard", admin));
        for (String clave : new String[]{"clientesActivos", "operacionesHoy", "prestamosColocados",
                "productosStockBajo", "expedientesPendientes", "solicitudesPendientes", "pagosPorVerificar"}) {
            assertTrue("Falta el indicador " + clave, k.containsKey(clave));
        }
    }

    /** HU-10 · Como cliente quiero ver mi estado de cuenta y que nadie más pueda verlo. */
    @Test
    public void hu10_estadoDeCuentaPrivado() throws Exception {
        String juan = s.loginCliente("70000001", "70000001");
        HttpResponse<String> propio = s.get("/api/portal/1/estado-cuenta", juan);
        assertEquals(200, propio.statusCode());
        assertTrue(objeto(propio).containsKey("deudas"));
        assertEquals(403, s.get("/api/portal/2/estado-cuenta", juan).statusCode());
    }
}
