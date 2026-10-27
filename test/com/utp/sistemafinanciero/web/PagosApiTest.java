package com.utp.sistemafinanciero.web;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.util.Json;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * v5.2 · Flujo completo por HTTP: el cliente paga con Yape desde el
 * portal, el pago aparece en el sistema interno, caja lo confirma y el
 * cliente ve su comprobante. Incluye los permisos por rol.
 */
public class PagosApiTest {

    private ApiServer app;
    private String base;
    private final HttpClient http = HttpClient.newHttpClient();

    @Before
    public void levantar() throws Exception {
        Contenedor c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-pagos-api"));
        app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        Rutas.registrar(app, c);
        base = "http://localhost:" + app.iniciar(0) + "/api";
    }

    @After
    public void detener() {
        app.detener();
    }

    private HttpResponse<String> pedir(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta))
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String personal(String email, String clave) throws Exception {
        HttpResponse<String> r = pedir("POST", "/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}");
        return (String) Json.parseObject(r.body()).get("token");
    }

    private String cliente(String doc) throws Exception {
        HttpResponse<String> r = pedir("POST", "/portal/login", null,
                "{\"documento\":\"" + doc + "\",\"password\":\"" + doc + "\"}");
        return (String) Json.parseObject(r.body()).get("token");
    }

    @Test
    public void elPagoDelClienteApareceEnElSistemaYCajaLoConfirma() throws Exception {
        String maria = cliente("70000002");   // cliente 2, préstamo de la operación 2
        assertEquals(200, pedir("GET", "/portal/2/billeteras", maria, null).statusCode());
        assertTrue(pedir("GET", "/portal/2/pagables", maria, null).body().contains("CUOTA_PRESTAMO"));

        HttpResponse<String> r = pedir("POST", "/portal/2/pagos", maria,
                "{\"idOperacion\":2,\"concepto\":\"CUOTA_PRESTAMO\",\"monto\":479.17,\"metodo\":\"YAPE\","
                        + "\"numeroOperacion\":\"00123456\"}");
        assertEquals(r.body(), 200, r.statusCode());
        Map<String, Object> pago = Json.parseObject(r.body());
        assertEquals("POR_VERIFICAR", pago.get("estado"));
        int idPago = ((Number) pago.get("idPago")).intValue();

        // Aparece en el sistema interno y en el panel
        String cajero = personal("lucia.ramos@sigsf.com", "lucia123");
        assertTrue(pedir("GET", "/pagos?estado=POR_VERIFICAR", cajero, null).body().contains("00123456"));
        Map<String, Object> kpi = Json.parseObject(pedir("GET", "/dashboard", cajero, null).body());
        assertEquals(1, ((Number) kpi.get("pagosPorVerificar")).intValue());

        // Caja confirma
        HttpResponse<String> ok = pedir("PUT", "/pagos/" + idPago + "/confirmar", cajero, "{}");
        assertEquals(200, ok.statusCode());
        assertEquals("CONFIRMADO", Json.parseObject(ok.body()).get("estado"));

        // El cliente ve su comprobante
        Map<String, Object> suyo = Json.parseObject(pedir("GET", "/portal/2/pagos/" + idPago, maria, null).body());
        assertEquals(String.format("RC-%06d", idPago), suyo.get("numeroComprobante"));
    }

    @Test
    public void unClienteNoVePagosNiCotizacionesDeOtro() throws Exception {
        String juan = cliente("70000001");
        assertEquals(403, pedir("GET", "/portal/2/pagos", juan, null).statusCode());
        assertEquals(403, pedir("GET", "/portal/2/estado-cuenta", juan, null).statusCode());
        assertEquals(403, pedir("GET", "/pagos", juan, null).statusCode());
        assertEquals(200, pedir("GET", "/portal/1/estado-cuenta", juan, null).statusCode());
    }

    @Test
    public void permisosPorRolEnPagosYBilleteras() throws Exception {
        String analista = personal("jorge.torres@sigsf.com", "jorge123");
        String cajero = personal("lucia.ramos@sigsf.com", "lucia123");
        String admin = personal("admin@sigsf.com", "admin123");
        assertEquals(200, pedir("GET", "/pagos", analista, null).statusCode());
        assertEquals(403, pedir("PUT", "/pagos/1/confirmar", analista, "{}").statusCode());
        assertEquals(200, pedir("GET", "/billeteras", cajero, null).statusCode());
        assertEquals(403, pedir("PUT", "/billeteras/1", cajero, "{\"titular\":\"X\"}").statusCode());
        HttpResponse<String> r = pedir("PUT", "/billeteras/1", admin, "{\"titular\":\"Caja Central\"}");
        assertEquals(200, r.statusCode());
        assertEquals("Caja Central", Json.parseObject(r.body()).get("titular"));
    }

    @Test
    public void cotizacionDesdeElPortalYExportacionDePagos() throws Exception {
        String juan = cliente("70000001");
        HttpResponse<String> r = pedir("POST", "/portal/1/cotizaciones", juan,
                "{\"tipo\":\"PRESTAMO\",\"monto\":3000,\"plazo\":6}");
        assertEquals(r.body(), 200, r.statusCode());
        int id = ((Number) Json.parseObject(r.body()).get("idCotizacion")).intValue();
        Map<String, Object> det = Json.parseObject(pedir("GET", "/portal/1/cotizaciones/" + id, juan, null).body());
        assertEquals(6, ((List<?>) det.get("cronograma")).size());

        String admin = personal("admin@sigsf.com", "admin123");
        assertTrue(pedir("GET", "/cotizaciones", admin, null).body().contains("COT-"));
        HttpResponse<String> csv = pedir("GET", "/pagos/exportar?formato=csv", admin, null);
        assertEquals(200, csv.statusCode());
        assertTrue(csv.body().contains("Comprobante"));
    }
}
