package com.utp.sistemafinanciero.plan;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import static org.junit.Assert.*;

/** Pruebas HTTP reales: protecciones transversales, sin conexión a producción. */
public class SeguridadEntradasApiTest {
    private ServidorDePrueba s;
    private static final String LOGIN = "{\"email\":\"admin@sigsf.com\",\"password\":\"admin123\"}";
    @Before public void iniciar() throws Exception { s = ServidorDePrueba.iniciar(); }
    @After public void cerrar() { if (s != null) s.close(); }
    private HttpResponse<String> enviar(byte[] body, String tipo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(s.raiz() + "/api/auth/login"))
                .timeout(Duration.ofSeconds(5)).POST(HttpRequest.BodyPublishers.ofByteArray(body));
        if (tipo != null) b.header("Content-Type", tipo);
        return HttpClient.newHttpClient().send(b.build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> enviar(String body, String tipo) throws Exception {
        return enviar(body.getBytes(StandardCharsets.UTF_8), tipo);
    }
    private void rechazado(HttpResponse<String> r, int codigo) {
        assertEquals(r.body(), codigo, r.statusCode()); assertFalse(r.body().contains("\"token\""));
    }
    @Test public void jsonSobranteRechazadoAunqueLaClaveSeaCorrecta() throws Exception {
        rechazado(enviar(LOGIN + " trailing", "application/json"), 400);
        rechazado(enviar(LOGIN + "{}", "application/json"), 400);
        assertEquals(200, s.get("/api/health", null).statusCode());
    }
    @Test public void tipoIncorrectoOAusenteRechazado() throws Exception {
        for (String t : new String[]{"text/plain", "application/xml", "application/jsonx", null}) rechazado(enviar(LOGIN,t),415);
    }
    @Test public void admiteJsonUtf8YMayusculas() throws Exception {
        assertEquals(200,enviar(LOGIN,"Application/JSON; Charset=\"UTF-8\"").statusCode());
    }
    @Test public void rechazaCharsetNoSoportado() throws Exception {
        rechazado(enviar(LOGIN,"application/json; charset=iso-8859-1"),415);
    }
    @Test public void rechazaRaicesNoObjeto() throws Exception {
        for (String b : new String[]{"[]", "null", "true", "123"}) rechazado(enviar(b,"application/json"),400);
    }
    @Test public void rechazaClavesDuplicadas() throws Exception {
        rechazado(enviar(LOGIN.replace("}",",\"email\":\"admin@sigsf.com\"}"),"application/json"),400);
    }
    @Test public void rechazaSerializacionJava() throws Exception {
        rechazado(enviar(new byte[]{(byte)0xac,(byte)0xed,0,5,0x70},"application/x-java-serialized-object"),415);
    }
    @Test public void rechazaUtf8Malformado() throws Exception {
        rechazado(enviar(new byte[]{'{','"','x','"',':','"',(byte)0xc3,0x28,'"','}'},"application/json"),400);
    }
    @Test public void anidacionProfundaDevuelve400YConservaServicio() throws Exception {
        rechazado(enviar("{\"x\":"+"[".repeat(65)+"0"+"]".repeat(65)+"}","application/json"),400);
        assertEquals(200,enviar(LOGIN,"application/json").statusCode());
    }
    @Test public void cspNoPermiteScriptsInlineNiObjetos() throws Exception {
        String csp=s.get("/login.html",null).headers().firstValue("Content-Security-Policy").orElse("");
        assertTrue(csp.contains("script-src 'self';"));
        assertTrue(csp.contains("script-src-attr 'none'"));
        assertTrue(csp.contains("object-src 'none'"));
    }
}
