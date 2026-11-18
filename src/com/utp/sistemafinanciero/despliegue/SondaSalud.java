package com.utp.sistemafinanciero.despliegue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Sonda de salud usada por el despliegue (Docker HEALTHCHECK, systemd y CI/CD).
 *
 * <p>Consulta {@code GET /api/health} del servidor local y termina con código 0
 * si responde HTTP 200 con {@code "status":"ok"}; en cualquier otro caso
 * termina con 1. No necesita curl ni wget dentro de la imagen: basta la JRE.</p>
 *
 * <pre>
 *   java -cp dist/SIGSF.jar com.utp.sistemafinanciero.despliegue.SondaSalud [url] [ms]
 * </pre>
 *
 * <p>Por defecto usa {@code http://127.0.0.1:${SIGSF_SERVER_PORT:-8091}/api/health}
 * y 3000 ms de espera. Solo se permiten direcciones http locales para que la
 * sonda no pueda usarse como cliente hacia otros equipos (SSRF).</p>
 */
public final class SondaSalud {

    static final int MAX_BYTES = 4096;

    private SondaSalud() {
    }

    public static void main(String[] args) {
        String puerto = System.getenv().getOrDefault("SIGSF_SERVER_PORT", "8091");
        String url = args.length > 0 ? args[0] : "http://127.0.0.1:" + puerto + "/api/health";
        int espera = args.length > 1 ? Integer.parseInt(args[1]) : 3000;
        Resultado r = verificar(url, espera);
        System.out.println(r.mensaje());
        System.exit(r.sano() ? 0 : 1);
    }

    /** Resultado inmutable de una verificación. */
    public record Resultado(boolean sano, int codigoHttp, String mensaje) {
    }

    /** Valida que la URL sea http hacia el propio equipo. */
    static URI validarUrlLocal(String url) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        if (!"http".equals(uri.getScheme()) || host == null
                || !(host.equals("127.0.0.1") || host.equals("localhost") || host.equals("[::1]") || host.equals("::1"))) {
            throw new IllegalArgumentException("La sonda solo consulta http://127.0.0.1 o localhost");
        }
        return uri;
    }

    /** Interpreta la respuesta: sano solo con HTTP 200 y status ok. */
    static boolean cuerpoSano(int codigo, String cuerpo) {
        return codigo == 200 && cuerpo != null && cuerpo.replace(" ", "").contains("\"status\":\"ok\"");
    }

    public static Resultado verificar(String url, int esperaMs) {
        HttpURLConnection cn = null;
        try {
            cn = (HttpURLConnection) validarUrlLocal(url).toURL().openConnection();
            cn.setConnectTimeout(esperaMs);
            cn.setReadTimeout(esperaMs);
            cn.setRequestProperty("Accept", "application/json");
            cn.setInstanceFollowRedirects(false);
            int codigo = cn.getResponseCode();
            String cuerpo;
            try (InputStream in = codigo < 400 ? cn.getInputStream() : cn.getErrorStream()) {
                cuerpo = in == null ? "" : new String(in.readNBytes(MAX_BYTES), StandardCharsets.UTF_8);
            }
            boolean sano = cuerpoSano(codigo, cuerpo);
            return new Resultado(sano, codigo, (sano ? "SANO " : "NO SANO ") + codigo + " " + cuerpo.strip());
        } catch (IllegalArgumentException e) {
            return new Resultado(false, -1, "URL rechazada: " + e.getMessage());
        } catch (IOException e) {
            return new Resultado(false, -1, "SIN RESPUESTA: " + e.getClass().getSimpleName() + " " + e.getMessage());
        } finally {
            if (cn != null) {
                cn.disconnect();
            }
        }
    }
}
