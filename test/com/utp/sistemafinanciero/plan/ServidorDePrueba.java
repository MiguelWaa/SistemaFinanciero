package com.utp.sistemafinanciero.plan;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Rutas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * v5.5 · Plan de pruebas · Entorno de prueba reutilizable.
 *
 * <p>Levanta el SIGSF completo (frontend de la carpeta {@code web/} + API +
 * servicios + DAO en memoria) en un puerto libre, igual que {@code ServerMain}
 * pero sin PostgreSQL. Lo usan las pruebas de sistema, aceptación, seguridad y
 * rendimiento del paquete {@code plan}. No es una prueba (no termina en Test).</p>
 */
public final class ServidorDePrueba implements AutoCloseable {

    private final ApiServer app;
    private final String raiz;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    public final Contenedor contenedor;

    private ServidorDePrueba(ApiServer app, String raiz, Contenedor c) {
        this.app = app;
        this.raiz = raiz;
        this.contenedor = c;
    }

    /** Arranca el sistema con los datos de ejemplo en memoria. */
    public static ServidorDePrueba iniciar() throws Exception {
        Contenedor c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-plan-pruebas"));
        ApiServer app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        Rutas.registrar(app, c);
        int puerto = app.iniciar(0);
        return new ServidorDePrueba(app, "http://localhost:" + puerto, c);
    }

    @Override
    public void close() {
        app.detener();
    }

    /** URL base del frontend (sin /api). */
    public String raiz() {
        return raiz;
    }

    /** Petición a la API. {@code ruta} empieza en /api/... o en /archivo.html. */
    public HttpResponse<String> pedir(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(raiz + ruta))
                .timeout(Duration.ofSeconds(10))
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> get(String ruta, String token) throws Exception {
        return pedir("GET", ruta, token, null);
    }

    /** Inicia sesión del personal y devuelve el token. */
    public String loginPersonal(String email, String clave) throws Exception {
        HttpResponse<String> r = pedir("POST", "/api/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}");
        if (r.statusCode() != 200) throw new AssertionError("Login de " + email + " falló: " + r.body());
        return (String) Json.parseObject(r.body()).get("token");
    }

    /** Inicia sesión de un cliente en el portal (clave = documento en los datos de ejemplo). */
    public String loginCliente(String documento, String clave) throws Exception {
        HttpResponse<String> r = pedir("POST", "/api/portal/login", null,
                "{\"documento\":\"" + documento + "\",\"password\":\"" + clave + "\"}");
        if (r.statusCode() != 200) throw new AssertionError("Login portal de " + documento + " falló: " + r.body());
        return (String) Json.parseObject(r.body()).get("token");
    }

    public static Map<String, Object> objeto(HttpResponse<String> r) {
        return Json.parseObject(r.body());
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> lista(HttpResponse<String> r) {
        return (List<Map<String, Object>>) Json.parse(r.body());
    }

    public static int entero(Map<String, Object> m, String clave) {
        return ((Number) m.get(clave)).intValue();
    }

    public static double decimal(Map<String, Object> m, String clave) {
        return ((Number) m.get(clave)).doubleValue();
    }
}
