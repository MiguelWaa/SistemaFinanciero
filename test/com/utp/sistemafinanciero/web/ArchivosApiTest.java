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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integración de la importación/exportación de ficheros con la API real:
 * permisos por rol, descarga de archivos y auditoría.
 */
public class ArchivosApiTest {

    private ApiServer app;
    private Contenedor c;
    private String base;
    private final HttpClient http = HttpClient.newHttpClient();

    @Before
    public void levantar() throws Exception {
        c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-api-archivos"));
        app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        Rutas.registrar(app, c);
        base = "http://localhost:" + app.iniciar(0) + "/api";
    }

    @After
    public void detener() {
        app.detener();
    }

    private HttpResponse<byte[]> pedir(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta))
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private String login(String email, String clave) throws Exception {
        HttpResponse<byte[]> r = pedir("POST", "/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}");
        return (String) Json.parseObject(new String(r.body(), StandardCharsets.UTF_8)).get("token");
    }

    private static String cuerpoImportacion(String nombre, String csv, boolean soloValidar) {
        return Json.stringify(Json.map("nombre", nombre, "soloValidar", soloValidar,
                "contenido", Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void exportarRequiereSesionYDevuelveUnExcel() throws Exception {
        assertEquals(401, pedir("GET", "/clientes/exportar", null, null).statusCode());
        String t = login("admin@sigsf.com", "admin123");
        HttpResponse<byte[]> r = pedir("GET", "/clientes/exportar?formato=xlsx", t, null);
        assertEquals(200, r.statusCode());
        assertTrue(r.headers().firstValue("Content-Disposition").orElse("").contains(".xlsx"));
        assertEquals('P', r.body()[0]);
        HttpResponse<byte[]> csv = pedir("GET", "/clientes/exportar?formato=csv", t, null);
        assertTrue(csv.headers().firstValue("Content-Type").orElse("").startsWith("text/csv"));
        assertEquals(400, pedir("GET", "/clientes/exportar?formato=exe", t, null).statusCode());
    }

    @Test
    public void cajeroNoPuedeImportarProveedoresNiExportarAuditoria() throws Exception {
        String cajero = login("lucia.ramos@sigsf.com", "lucia123");
        assertEquals(403, pedir("POST", "/proveedores/importar", cajero,
                cuerpoImportacion("p.csv", "Razón social\nX SAC\n", false)).statusCode());
        assertEquals(403, pedir("GET", "/auditoria/exportar", cajero, null).statusCode());
    }

    @Test
    public void adminImportaProspectosYQuedaEnLaAuditoria() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        HttpResponse<byte[]> r = pedir("POST", "/prospectos/importar", t,
                cuerpoImportacion("feria.csv", "Nombres *;Correo;Origen\nCarla;carla@x.com;FERIA\n;sin@nombre.com;WEB\n", false));
        assertEquals(200, r.statusCode());
        Map<String, Object> res = Json.parseObject(new String(r.body(), StandardCharsets.UTF_8));
        assertEquals(1, ((Number) res.get("filasImportadas")).intValue());
        assertEquals(1, ((Number) res.get("filasRechazadas")).intValue());
        assertTrue(c.auditoria.recientes(20).stream().anyMatch(a -> "IMPORTAR".equals(a.getAccion())));
    }

    @Test
    public void operacionesNoTienenRutaDeImportacion() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        assertEquals(404, pedir("POST", "/operaciones/importar", t,
                cuerpoImportacion("o.csv", "Monto\n1\n", false)).statusCode());
        assertEquals(200, pedir("GET", "/operaciones/exportar?formato=json", t, null).statusCode());
    }
}
