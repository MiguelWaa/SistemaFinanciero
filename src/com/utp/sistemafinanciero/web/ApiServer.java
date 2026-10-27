package com.utp.sistemafinanciero.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.utp.sistemafinanciero.security.AccesoDenegadoException;
import com.utp.sistemafinanciero.security.NoAutenticadoException;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor HTTP basado en el {@code HttpServer} del JDK. Es el
 * <b>Front Controller</b> de la arquitectura MVC: recibe todas las
 * peticiones y las reparte.
 *
 * <ol>
 *   <li>Sirve la <b>Vista</b>: archivos del frontend (carpeta {@code web/}).</li>
 *   <li>Enruta {@code /api/**} hacia los <b>Controladores</b> registrados,
 *       pasando antes por la cadena de {@link Filtro} (seguridad) y
 *       notificando después a los {@link ObservadorPeticion} (auditoría).</li>
 * </ol>
 *
 * <p>Seguridad aplicada aquí: cabeceras HTTP seguras, CORS restringido,
 * límite de tamaño del cuerpo, protección contra <i>path traversal</i> y
 * mensajes de error que no revelan detalles internos (SQL, trazas).</p>
 *
 * <p>Registro (Logback): cada petición a la API deja una línea en el logger
 * {@code SIGSF.ACCESO} (método, ruta, código HTTP y milisegundos). Durante
 * la petición se cargan en el MDC el área del negocio, la IP, el usuario y
 * un identificador de petición, para que cualquier mensaje de los servicios
 * salga etiquetado. Los errores inesperados van al logger de la clase con
 * su traza completa (archivo {@code logs/sigsf-errores.log}).</p>
 */
public class ApiServer {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServer.class);
    /** Logger de acceso: una línea por petición a la API (archivo sigsf-acceso.log). */
    private static final Logger ACCESO = LoggerFactory.getLogger("SIGSF.ACCESO");

    /** Ruta registrada: método + patrón dividido en segmentos + manejador. */
    private static final class Route {
        final String metodo;
        final String[] segmentos;
        final boolean conParametros;
        final Handler handler;

        Route(String metodo, String patron, Handler handler) {
            this.metodo = metodo;
            this.segmentos = patron.split("/");
            boolean p = false;
            for (String s : segmentos) {
                if (s.startsWith("{") && s.endsWith("}")) p = true;
            }
            this.conParametros = p;
            this.handler = handler;
        }
    }

    private static final String CSP = "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline'; "
            // Bootstrap y Font Awesome se sirven desde web/vendor (sin CDN): solo se permite Google Fonts.
            + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
            + "font-src 'self' data: https://fonts.gstatic.com; "
            + "img-src 'self' data: https:; connect-src 'self'; frame-ancestors 'self'; "
            + "base-uri 'self'; form-action 'self'";

    private final List<Route> rutas = new ArrayList<>();
    private final List<Filtro> filtros = new ArrayList<>();
    private final List<ObservadorPeticion> observadores = new ArrayList<>();
    private final Path webRoot;
    private final String origenCors;
    private final int maxBytesCuerpo;
    private HttpServer server;
    private ExecutorService hilos;

    /**
     * @param webRoot        carpeta del frontend
     * @param origenCors     origen permitido para CORS (vacío = solo mismo origen)
     * @param maxBytesCuerpo tamaño máximo del cuerpo de una petición
     */
    public ApiServer(Path webRoot, String origenCors, int maxBytesCuerpo) {
        this.webRoot = webRoot.toAbsolutePath().normalize();
        this.origenCors = origenCors == null ? "" : origenCors.trim();
        this.maxBytesCuerpo = maxBytesCuerpo;
    }

    // --- Registro de rutas, filtros y observadores ---
    public ApiServer get(String patron, Handler h)    { return add("GET", patron, h); }
    public ApiServer post(String patron, Handler h)   { return add("POST", patron, h); }
    public ApiServer put(String patron, Handler h)    { return add("PUT", patron, h); }
    public ApiServer delete(String patron, Handler h) { return add("DELETE", patron, h); }

    private ApiServer add(String metodo, String patron, Handler h) {
        rutas.add(new Route(metodo, patron, h));
        return this;
    }

    public ApiServer usarFiltro(Filtro f) {
        filtros.add(f);
        return this;
    }

    public ApiServer observar(ObservadorPeticion o) {
        observadores.add(o);
        return this;
    }

    /** Arranca el servidor en el puerto indicado (0 = puerto libre aleatorio). */
    public int iniciar(int puerto) throws IOException {
        server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.createContext("/", this::despachar);
        hilos = Executors.newFixedThreadPool(Math.max(16, Runtime.getRuntime().availableProcessors() * 4));
        server.setExecutor(hilos);
        server.start();
        return server.getAddress().getPort();
    }

    public void detener() {
        if (server != null) {
            server.stop(0);
            hilos.shutdownNow();
        }
    }

    // --- Despachador principal ---
    private void despachar(HttpExchange ex) {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        try {
            cabecerasSeguras(ex);
            if ("OPTIONS".equalsIgnoreCase(metodo)) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            if (ruta.startsWith("/api/")) {
                atenderApi(ex, metodo, ruta);
            } else if ("GET".equalsIgnoreCase(metodo) || "HEAD".equalsIgnoreCase(metodo)) {
                servirArchivo(ex, ruta);
            } else {
                responderTexto(ex, 405, "Método no permitido");
            }
        } catch (Exception e) {
            LOG.error("Fallo al atender {} {}", metodo, ruta, e);
        } finally {
            ex.close();
            MDC.clear();
        }
    }

    private void atenderApi(HttpExchange ex, String metodo, String ruta) throws IOException {
        long inicio = System.nanoTime();
        MDC.put("peticion", UUID.randomUUID().toString().substring(0, 8));
        MDC.put("area", AreaNegocio.deRuta(ruta).name());
        MDC.put("ip", ex.getRemoteAddress() == null ? "" : ex.getRemoteAddress().getAddress().getHostAddress());
        MDC.put("usuario", "-");
        String[] partes = ruta.split("/");
        Route elegida = null;
        Map<String, String> params = null;
        // Dos pasadas: primero rutas exactas (/api/productos/excel) y luego
        // las que llevan parámetros (/api/productos/{id}).
        for (int pasada = 0; pasada < 2 && elegida == null; pasada++) {
            for (Route r : rutas) {
                if (!r.metodo.equalsIgnoreCase(metodo) || (pasada == 0) == r.conParametros) continue;
                Map<String, String> p = emparejar(r.segmentos, partes);
                if (p != null) {
                    elegida = r;
                    params = p;
                    break;
                }
            }
        }

        Ctx ctx = new Ctx(ex, params, maxBytesCuerpo);
        try {
            if (elegida == null) {
                ctx.error(404, "Recurso no encontrado: " + metodo + " " + ruta);
                return;
            }
            for (Filtro f : filtros) {
                f.aplicar(ctx);
            }
            if (ctx.sesion() != null) {
                MDC.put("usuario", ctx.sesion().getUsuario());
            }
            Object resultado = elegida.handler.handle(ctx);
            if (resultado instanceof Descarga) {
                Descarga d = (Descarga) resultado;
                ctx.descargar(d.nombre, d.contentType, d.datos);
            } else if (resultado == null) {
                ctx.json(204, "");
            } else {
                ctx.ok(resultado);
            }
        } catch (Exception e) {
            responderError(ctx, e);
        } finally {
            for (ObservadorPeticion o : observadores) {
                try {
                    o.alFinalizar(ctx, ctx.estado());
                } catch (Exception e) {
                    // La auditoría nunca debe tumbar una respuesta.
                    LOG.warn("Un observador de la petición falló: {}", e.getMessage());
                }
            }
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            if (ctx.estado() >= 500) {
                ACCESO.error("{} {} -> {} ({} ms)", metodo, ruta, ctx.estado(), ms);
            } else if (ctx.estado() >= 400) {
                ACCESO.warn("{} {} -> {} ({} ms)", metodo, ruta, ctx.estado(), ms);
            } else {
                ACCESO.info("{} {} -> {} ({} ms)", metodo, ruta, ctx.estado(), ms);
            }
        }
    }

    /** Traduce excepciones a códigos HTTP sin exponer detalles internos. */
    private void responderError(Ctx ctx, Exception e) throws IOException {
        if (e instanceof ReglaNegocioException) {
            ctx.error(400, e.getMessage());
        } else if (e instanceof NoAutenticadoException) {
            ctx.error(401, e.getMessage());
        } else if (e instanceof AccesoDenegadoException) {
            ctx.error(403, e.getMessage());
        } else if (e instanceof NumberFormatException) {
            ctx.error(400, "Identificador inválido.");
        } else {
            SQLException sql = causaSql(e);
            if (sql != null && "23505".equals(sql.getSQLState())) {
                ctx.error(409, "Ya existe un registro con esos datos (valor duplicado).");
            } else if (sql != null && "23503".equals(sql.getSQLState())) {
                ctx.error(409, "La operación no es posible: hay registros relacionados.");
            } else if (sql != null && "23514".equals(sql.getSQLState())) {
                ctx.error(400, "Algún valor no cumple las reglas de la base de datos.");
            } else if (sql != null && "42P01".equals(sql.getSQLState())) {
                LOG.error("Falta una tabla en la base de datos: {}", sql.getMessage());
                ctx.error(503, "Falta una tabla en la base de datos. Reinicie el servidor o ejecute db/schema.sql.");
            } else if (sql != null && sql.getSQLState() != null && sql.getSQLState().startsWith("28")) {
                LOG.error("PostgreSQL rechazó el usuario o la contraseña de db.properties: {}", sql.getMessage());
                ctx.error(503, "No se pudo ingresar a la base de datos: revise db.user y db.password en db.properties.");
            } else if (sql != null && sql.getSQLState() != null && sql.getSQLState().startsWith("08")
                    || sql != null && sql.getMessage() != null && sql.getMessage().contains("driver")) {
                LOG.error("Base de datos no disponible: {}", sql.getMessage());
                ctx.error(503, "La base de datos no está disponible. Revise que PostgreSQL esté iniciado.");
            } else {
                // Error inesperado: el detalle va a la consola del servidor, no al navegador.
                String ref = UUID.randomUUID().toString().substring(0, 8);
                LOG.error("Error interno ref={} en {} {}", ref, ctx.metodo(), ctx.ruta(), e);
                ctx.error(500, "Error interno del servidor (referencia " + ref + ").");
            }
        }
    }

    private static SQLException causaSql(Throwable t) {
        while (t != null) {
            if (t instanceof SQLException) return (SQLException) t;
            t = t.getCause();
        }
        return null;
    }

    private Map<String, String> emparejar(String[] patron, String[] ruta) {
        if (patron.length != ruta.length) return null;
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < patron.length; i++) {
            String p = patron[i];
            String v = ruta[i];
            if (p.startsWith("{") && p.endsWith("}")) {
                params.put(p.substring(1, p.length() - 1), URLDecoder.decode(v, StandardCharsets.UTF_8));
            } else if (!p.equals(v)) {
                return null;
            }
        }
        return params;
    }

    // --- Cabeceras de seguridad (OWASP Secure Headers) ---
    private void cabecerasSeguras(HttpExchange ex) {
        var h = ex.getResponseHeaders();
        h.set("X-Content-Type-Options", "nosniff");
        h.set("X-Frame-Options", "SAMEORIGIN");
        h.set("Referrer-Policy", "no-referrer");
        h.set("Permissions-Policy", "camera=(), geolocation=(), microphone=(self)");
        h.set("Content-Security-Policy", CSP);
        if (!origenCors.isEmpty()) {
            h.set("Access-Control-Allow-Origin", origenCors);
            h.set("Vary", "Origin");
            h.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            h.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
    }

    // --- Servir archivos estáticos del frontend (la Vista) ---
    private void servirArchivo(HttpExchange ex, String ruta) throws IOException {
        if (ruta.equals("/") || ruta.isEmpty()) {
            ruta = "/splash.html";
        }
        String relativa = URLDecoder.decode(ruta.substring(1), StandardCharsets.UTF_8);
        if (relativa.contains("..") || relativa.startsWith("_respaldo")) {
            responderTexto(ex, 404, "Archivo no encontrado");
            return;
        }
        Path archivo = webRoot.resolve(relativa).normalize();
        if (!archivo.startsWith(webRoot) || !Files.isRegularFile(archivo)) {
            responderTexto(ex, 404, "Archivo no encontrado: " + ruta);
            return;
        }
        String tipo = tipoContenido(archivo);
        var h = ex.getResponseHeaders();
        h.set("Content-Type", tipo);
        // Librerías de terceros (web/vendor) no cambian entre versiones: caché de 7 días.
        // Las páginas y scripts propios se revalidan siempre (ETag por fecha y tamaño).
        boolean vendor = relativa.startsWith("vendor/");
        h.set("Cache-Control", vendor ? "public, max-age=604800" : "no-cache");
        String etag = "\"" + Long.toHexString(Files.getLastModifiedTime(archivo).toMillis()) + "-"
                + Long.toHexString(Files.size(archivo)) + "\"";
        h.set("ETag", etag);
        if (etag.equals(ex.getRequestHeaders().getFirst("If-None-Match"))) {
            ex.sendResponseHeaders(304, -1);
            return;
        }
        byte[] datos = Files.readAllBytes(archivo);
        String acepta = ex.getRequestHeaders().getFirst("Accept-Encoding");
        boolean comprimible = datos.length > 1024 && (tipo.startsWith("text/") || tipo.contains("javascript")
                || tipo.contains("json") || tipo.contains("svg"));
        if (comprimible && acepta != null && acepta.contains("gzip")) {
            datos = gzip(archivo, datos);
            h.set("Content-Encoding", "gzip");
            h.set("Vary", "Accept-Encoding");
        }
        if ("HEAD".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(200, -1);
            return;
        }
        ex.sendResponseHeaders(200, datos.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(datos);
        }
    }

    /** Caché de archivos comprimidos (clave: ruta + fecha de modificación). */
    private final Map<String, byte[]> gzipCache = new java.util.concurrent.ConcurrentHashMap<>();

    private byte[] gzip(Path archivo, byte[] datos) throws IOException {
        String clave = archivo + "@" + Files.getLastModifiedTime(archivo).toMillis();
        byte[] listo = gzipCache.get(clave);
        if (listo != null) return listo;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(datos.length / 3);
        try (java.util.zip.GZIPOutputStream gz = new java.util.zip.GZIPOutputStream(out)) {
            gz.write(datos);
        }
        listo = out.toByteArray();
        if (gzipCache.size() > 200) gzipCache.clear();
        gzipCache.put(clave, listo);
        return listo;
    }

    private void responderTexto(HttpExchange ex, int status, String msg) throws IOException {
        byte[] out = msg.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, out.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(out);
        }
    }

    private String tipoContenido(Path archivo) {
        String n = archivo.getFileName().toString().toLowerCase();
        if (n.endsWith(".html") || n.endsWith(".htm")) return "text/html; charset=utf-8";
        if (n.endsWith(".css"))  return "text/css; charset=utf-8";
        if (n.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (n.endsWith(".json")) return "application/json; charset=utf-8";
        if (n.endsWith(".png"))  return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".gif"))  return "image/gif";
        if (n.endsWith(".svg"))  return "image/svg+xml";
        if (n.endsWith(".ico"))  return "image/x-icon";
        if (n.endsWith(".woff")) return "font/woff";
        if (n.endsWith(".woff2")) return "font/woff2";
        if (n.endsWith(".ttf"))  return "font/ttf";
        if (n.endsWith(".webp")) return "image/webp";
        if (n.endsWith(".map"))  return "application/json; charset=utf-8";
        if (n.endsWith(".txt"))  return "text/plain; charset=utf-8";
        return "application/octet-stream";
    }
}
