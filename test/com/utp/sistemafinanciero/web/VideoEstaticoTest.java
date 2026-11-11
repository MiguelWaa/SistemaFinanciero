package com.utp.sistemafinanciero.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import static org.junit.Assert.*;

/** Pruebas HTTP reales del clip, rangos para reproducción y revalidación. */
public class VideoEstaticoTest {
    private ApiServer app;
    private URI uri;
    private byte[] original;
    private final HttpClient http = HttpClient.newHttpClient();

    @Before public void iniciar() throws Exception {
        original = Files.readAllBytes(Paths.get("web/video/presentacion-sigsf.mp4"));
        app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        uri = URI.create("http://localhost:" + app.iniciar(0) + "/video/presentacion-sigsf.mp4");
    }
    @After public void detener() { if (app != null) app.detener(); }
    private HttpResponse<byte[]> get(String... cabeceras) throws Exception {
        var b = HttpRequest.newBuilder(uri).GET();
        for (int i = 0; i < cabeceras.length; i += 2) b.header(cabeceras[i], cabeceras[i + 1]);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofByteArray());
    }
    private void rango(String valor, int inicio, int fin) throws Exception {
        var r = get("Range", valor);
        assertEquals(206, r.statusCode());
        assertEquals("bytes " + inicio + "-" + fin + "/" + original.length,
                r.headers().firstValue("Content-Range").orElse(""));
        assertEquals(Integer.toString(fin - inicio + 1), r.headers().firstValue("Content-Length").orElse(""));
        assertArrayEquals(Arrays.copyOfRange(original, inicio, fin + 1), r.body());
    }
    @Test public void mp4ConservaBytesTipoYCsp() throws Exception {
        var r = get("Accept-Encoding", "gzip");
        assertEquals(200, r.statusCode());
        assertEquals("video/mp4", r.headers().firstValue("Content-Type").orElse(""));
        assertEquals("bytes", r.headers().firstValue("Accept-Ranges").orElse(""));
        assertEquals("nosniff", r.headers().firstValue("X-Content-Type-Options").orElse(""));
        assertTrue(r.headers().firstValue("Content-Security-Policy").orElse("").contains("default-src 'self'"));
        assertFalse(r.headers().firstValue("Content-Encoding").isPresent());
        assertArrayEquals(original, r.body());
    }
    @Test public void rangoInicialEIntermedio() throws Exception { rango("bytes=0-1023", 0, 1023); rango("bytes=1200-1900", 1200, 1900); }
    @Test public void rangoAbiertoYSufijo() throws Exception {
        rango("bytes=" + (original.length - 80) + "-", original.length - 80, original.length - 1);
        rango("bytes=-64", original.length - 64, original.length - 1);
    }
    @Test public void limiteSeAjustaAlFinalDelArchivo() throws Exception {
        rango("bytes=" + (original.length - 10) + "-9999999", original.length - 10, original.length - 1);
    }
    @Test public void rangoFueraDeArchivoRetorna416() throws Exception {
        for (String valor : new String[]{"bytes=" + original.length + "-", "bytes=10-2", "bytes=-0"}) {
            var r = get("Range", valor);
            assertEquals(416, r.statusCode());
            assertEquals("bytes */" + original.length, r.headers().firstValue("Content-Range").orElse(""));
            assertEquals(0, r.body().length);
        }
    }
    @Test public void rangosNoReconocidosDevuelvenArchivoCompleto() throws Exception {
        for (String valor : new String[]{"bytes=0-3,7-9", "bytes=hola", "bytes=-", "bytes=999999999999999999999-"}) {
            var r = get("Range", valor);
            assertEquals(200, r.statusCode());
            assertFalse(r.headers().firstValue("Content-Range").isPresent());
            assertArrayEquals(original, r.body());
        }
    }
    @Test public void headYRevalidacionNoDescarganVideo() throws Exception {
        var head = http.send(HttpRequest.newBuilder(uri).header("Range", "bytes=0-31")
                .method("HEAD", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200, head.statusCode());
        assertEquals(Integer.toString(original.length), head.headers().firstValue("Content-Length").orElse(""));
        assertEquals(0, head.body().length);
        var cached = get("If-None-Match", head.headers().firstValue("ETag").orElseThrow(), "Range", "bytes=0-31");
        assertEquals(304, cached.statusCode());
        assertEquals(0, cached.body().length);
    }
    @Test public void ifRangeSoloReutilizaLaVersionCorrecta() throws Exception {
        var etag = get().headers().firstValue("ETag").orElseThrow();
        var parcial = get("Range", "bytes=0-31", "If-Range", etag);
        assertEquals(206, parcial.statusCode());
        assertArrayEquals(Arrays.copyOf(original, 32), parcial.body());
        var completa = get("Range", "bytes=0-31", "If-Range", "\"version-anterior\"");
        assertEquals(200, completa.statusCode());
        assertArrayEquals(original, completa.body());
    }
}
