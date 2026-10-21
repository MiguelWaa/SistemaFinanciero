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
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Prueba de integración de la arquitectura completa (Vista HTTP →
 * ApiServer → FiltroSeguridad → Controlador → Servicio → DAO) usando la
 * persistencia en memoria. Levanta el servidor real en un puerto libre.
 */
public class ApiIntegracionTest {

    private ApiServer app;
    private String base;
    private final HttpClient http = HttpClient.newHttpClient();

    @Before
    public void levantar() throws Exception {
        Contenedor c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-api"));
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

    private String login(String email, String clave) throws Exception {
        HttpResponse<String> r = pedir("POST", "/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}");
        assertEquals(200, r.statusCode());
        return (String) Json.parseObject(r.body()).get("token");
    }

    @Test
    public void healthEsPublico() throws Exception {
        assertEquals(200, pedir("GET", "/health", null, null).statusCode());
    }

    @Test
    public void sinTokenLaApiResponde401() throws Exception {
        assertEquals(401, pedir("GET", "/clientes", null, null).statusCode());
        assertEquals(401, pedir("GET", "/clientes", "token-inventado", null).statusCode());
    }

    @Test
    public void conTokenSeListanClientesYSeRegistraUnoNuevo() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        assertEquals(200, pedir("GET", "/clientes", t, null).statusCode());
        HttpResponse<String> r = pedir("POST", "/clientes", t,
                "{\"nombres\":\"Rosa\",\"apellidos\":\"Quispe\",\"documento\":\"71234567\"}");
        assertEquals(200, r.statusCode());
        Map<String, Object> creado = Json.parseObject(r.body());
        assertEquals("ACTIVO", creado.get("estado"));
    }

    @Test
    public void reglaDeNegocioDevuelve400ConMensajeClaro() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        HttpResponse<String> r = pedir("POST", "/clientes", t, "{\"nombres\":\"X\",\"documento\":\"70000001\"}");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("Ya existe un cliente"));
    }

    @Test
    public void cajeroNoPuedeAdministrarUsuarios() throws Exception {
        String t = login("lucia.ramos@sigsf.com", "lucia123");
        assertEquals(403, pedir("GET", "/usuarios", t, null).statusCode());
        assertEquals(200, pedir("GET", "/operaciones", t, null).statusCode());
    }

    @Test
    public void laListaDeUsuariosNoExponeContrasenas() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        String body = pedir("GET", "/usuarios", t, null).body();
        assertFalse(body.contains("password"));
        assertFalse(body.contains("pbkdf2"));
    }

    @Test
    public void clienteDelPortalNoVeTramitesDeOtroCliente() throws Exception {
        HttpResponse<String> r = pedir("POST", "/portal/login", null, "{\"documento\":\"70000001\",\"password\":\"70000001\"}");
        String t = (String) Json.parseObject(r.body()).get("token");
        assertEquals(200, pedir("GET", "/portal/1/operaciones", t, null).statusCode());
        assertEquals(403, pedir("GET", "/portal/2/operaciones", t, null).statusCode());
    }

    @Test
    public void respuestasIncluyenCabecerasDeSeguridad() throws Exception {
        HttpResponse<String> r = pedir("GET", "/health", null, null);
        assertEquals("nosniff", r.headers().firstValue("X-Content-Type-Options").orElse(""));
        assertTrue(r.headers().firstValue("Content-Security-Policy").isPresent());
        assertFalse("CORS no debe estar abierto a todos",
                r.headers().firstValue("Access-Control-Allow-Origin").isPresent());
    }

    @Test
    public void logoutInvalidaElToken() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        assertEquals(200, pedir("POST", "/auth/logout", t, null).statusCode());
        assertEquals(401, pedir("GET", "/clientes", t, null).statusCode());
    }

    @Test
    public void adminGeneraRespaldoYQuedaEnLaAuditoria() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        assertEquals(200, pedir("POST", "/respaldos", t, "{}").statusCode());
        String auditoria = pedir("GET", "/auditoria", t, null).body();
        assertTrue(auditoria.contains("RESPALDO"));
        assertTrue(auditoria.contains("LOGIN_OK"));
    }
}
