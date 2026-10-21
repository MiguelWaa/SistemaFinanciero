package com.utp.sistemafinanciero.web;

import com.sun.net.httpserver.HttpExchange;
import com.utp.sistemafinanciero.security.Sesion;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import com.utp.sistemafinanciero.util.Json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Contexto de una petición HTTP: envuelve {@link HttpExchange} y expone
 * el cuerpo JSON, los parámetros de ruta ({@code /api/usuarios/{id}}),
 * la sesión autenticada, la IP de origen y métodos para responder.
 */
public class Ctx {

    private final HttpExchange ex;
    private final Map<String, String> params;
    private final int maxBytesCuerpo;
    private final Map<String, Object> atributos = new HashMap<>();
    private String bodyCache;
    private Sesion sesion;
    private int estado = 0;

    public Ctx(HttpExchange ex, Map<String, String> params, int maxBytesCuerpo) {
        this.ex = ex;
        this.params = params == null ? new HashMap<>() : params;
        this.maxBytesCuerpo = maxBytesCuerpo;
    }

    public Ctx(HttpExchange ex, Map<String, String> params) {
        this(ex, params, 1024 * 1024);
    }

    public String metodo() { return ex.getRequestMethod(); }

    public String ruta() { return ex.getRequestURI().getPath(); }

    public String param(String nombre) { return params.get(nombre); }

    public int paramInt(String nombre) { return Integer.parseInt(params.get(nombre)); }

    /**
     * Parámetro de la cadena de consulta ({@code ?formato=csv}); null si no viene.
     * Se decodifica en UTF-8 y se recorta a 100 caracteres.
     */
    public String query(String nombre) {
        String q = ex.getRequestURI().getRawQuery();
        if (q == null || q.isEmpty()) return null;
        for (String par : q.split("&")) {
            int i = par.indexOf('=');
            String k = i < 0 ? par : par.substring(0, i);
            if (k.equals(nombre)) {
                String v = i < 0 ? "" : java.net.URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8);
                return v.length() > 100 ? v.substring(0, 100) : v;
            }
        }
        return null;
    }

    public String header(String nombre) { return ex.getRequestHeaders().getFirst(nombre); }

    /** Token enviado en la cabecera {@code Authorization: Bearer <token>}. */
    public String token() {
        String h = header("Authorization");
        if (h != null && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        return null;
    }

    /** IP del cliente que hace la petición. */
    public String ip() {
        return ex.getRemoteAddress() == null ? "" : ex.getRemoteAddress().getAddress().getHostAddress();
    }

    public Sesion sesion() { return sesion; }

    public void setSesion(Sesion s) { this.sesion = s; }

    /** Atributos libres de la petición (por ejemplo, el usuario que intenta iniciar sesión). */
    public void atributo(String clave, Object valor) { atributos.put(clave, valor); }

    public Object atributo(String clave) { return atributos.get(clave); }

    /** Código HTTP con el que se respondió (0 si aún no se respondió). */
    public int estado() { return estado; }

    /** Cuerpo de la petición como texto, con límite de tamaño (protección DoS). */
    public String body() throws IOException {
        if (bodyCache == null) {
            try (InputStream in = ex.getRequestBody()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n, total = 0;
                while ((n = in.read(buf)) != -1) {
                    total += n;
                    if (total > maxBytesCuerpo) {
                        throw new ReglaNegocioException("La petición excede el tamaño permitido.");
                    }
                    out.write(buf, 0, n);
                }
                bodyCache = out.toString(StandardCharsets.UTF_8);
            }
        }
        return bodyCache;
    }

    /** Cuerpo de la petición parseado como objeto JSON (Map). */
    public Map<String, Object> bodyMap() throws IOException {
        String b = body();
        if (b == null || b.isBlank()) {
            return new HashMap<>();
        }
        try {
            return Json.parseObject(b);
        } catch (RuntimeException e) {
            throw new ReglaNegocioException("El cuerpo de la petición no es un JSON válido.");
        }
    }

    // ---- Respuestas ----

    public void json(int status, Object cuerpo) throws IOException {
        String texto = (cuerpo instanceof String) ? (String) cuerpo : Json.stringify(cuerpo);
        byte[] out = texto.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        estado = status;
        if (status == 204) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        ex.sendResponseHeaders(status, out.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(out);
        }
    }

    public void ok(Object cuerpo) throws IOException {
        json(200, cuerpo);
    }

    public void error(int status, String mensaje) throws IOException {
        json(status, Json.map("error", mensaje, "codigo", status));
    }

    /** Envía un archivo binario (Excel, respaldo) como descarga. */
    public void descargar(String nombre, String contentType, byte[] datos) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + nombre + "\"");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        estado = 200;
        ex.sendResponseHeaders(200, datos.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(datos);
        }
    }

    public HttpExchange exchange() { return ex; }
}
