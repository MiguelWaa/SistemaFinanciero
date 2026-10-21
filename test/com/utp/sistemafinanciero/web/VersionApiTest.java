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
 * Integración de la API de versiones con la seguridad por roles:
 * {@code /api/version} para todo el personal y {@code /api/versiones}
 * (historial del repositorio) solo para ADMIN.
 */
public class VersionApiTest {

    private ApiServer app;
    private String base;
    private final HttpClient http = HttpClient.newHttpClient();

    @Before
    public void levantar() throws Exception {
        Contenedor c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-ver"));
        app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        Rutas.registrar(app, c);
        base = "http://localhost:" + app.iniciar(0) + "/api";
    }

    @After
    public void detener() {
        app.detener();
    }

    private HttpResponse<String> get(String ruta, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta)).GET();
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String email, String clave) throws Exception {
        HttpRequest r = HttpRequest.newBuilder(URI.create(base + "/auth/login"))
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}"))
                .header("Content-Type", "application/json").build();
        return (String) Json.parseObject(http.send(r, HttpResponse.BodyHandlers.ofString()).body()).get("token");
    }

    @Test
    public void laVersionRequiereSesion() throws Exception {
        assertEquals(401, get("/version", null).statusCode());
        assertEquals(401, get("/versiones", null).statusCode());
    }

    @Test
    public void cualquierRolDelPersonalVeLaVersion() throws Exception {
        HttpResponse<String> r = get("/version", login("lucia.ramos@sigsf.com", "lucia123"));
        assertEquals(200, r.statusCode());
        Map<String, Object> v = Json.parseObject(r.body());
        assertNotNull(v.get("version"));
        assertEquals("5.0.0", v.get("versionEsquemaEsperada"));
        assertEquals(Boolean.TRUE, v.get("esquemaAlDia"));
    }

    @Test
    public void elHistorialDelRepositorioEsSoloParaAdmin() throws Exception {
        assertEquals(403, get("/versiones", login("lucia.ramos@sigsf.com", "lucia123")).statusCode());
        assertEquals(403, get("/versiones", login("jorge.torres@sigsf.com", "jorge123")).statusCode());
        HttpResponse<String> r = get("/versiones", login("admin@sigsf.com", "admin123"));
        assertEquals(200, r.statusCode());
        Map<String, Object> d = Json.parseObject(r.body());
        assertTrue(d.containsKey("commits"));
        assertTrue(d.containsKey("esquema"));
        assertTrue(d.containsKey("lanzamientos"));
    }
}
