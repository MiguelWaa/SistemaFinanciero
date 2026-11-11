package com.utp.sistemafinanciero.web;

import org.junit.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.*;

/** Verifica que la bienvenida se entregue como audio sin alterar los bytes ni la CSP. */
public class AudioEstaticoTest {
    @Test public void sirveMp3OriginalConTipoCorrectoYCabecerasSeguras() throws Exception {
        ApiServer app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        try {
            URI uri = URI.create("http://localhost:" + app.iniciar(0) + "/audio/bienvenida.mp3");
            HttpResponse<byte[]> r = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, r.statusCode());
            assertEquals("audio/mpeg", r.headers().firstValue("Content-Type").orElse(""));
            assertEquals("nosniff", r.headers().firstValue("X-Content-Type-Options").orElse(""));
            assertTrue(r.headers().firstValue("Content-Security-Policy").orElse("").contains("default-src 'self'"));
            assertArrayEquals(Files.readAllBytes(Paths.get("web/audio/bienvenida.mp3")), r.body());
        } finally { app.detener(); }
    }
    @Test public void audioAdmiteHeadYRevalidacionSinVolverADescargar() throws Exception {
        ApiServer app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        try {
            URI uri = URI.create("http://localhost:" + app.iniciar(0) + "/audio/bienvenida.mp3");
            HttpClient http = HttpClient.newHttpClient();
            HttpResponse<byte[]> head = http.send(HttpRequest.newBuilder(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, head.statusCode());
            assertEquals(0, head.body().length);
            assertEquals("audio/mpeg", head.headers().firstValue("Content-Type").orElse(""));
            String etag = head.headers().firstValue("ETag").orElseThrow();
            HttpResponse<byte[]> cached = http.send(HttpRequest.newBuilder(uri)
                    .header("If-None-Match", etag).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(304, cached.statusCode());
            assertEquals(0, cached.body().length);
        } finally { app.detener(); }
    }
}
