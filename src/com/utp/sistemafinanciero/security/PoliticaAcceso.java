package com.utp.sistemafinanciero.security;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Control de acceso basado en roles (<b>RBAC</b>) para la API.
 *
 * <p>La política se define como una <b>matriz módulo × rol</b> con dos
 * niveles de permiso: {@code L} (lectura: GET) y {@code E} (escritura:
 * POST, PUT, DELETE). El módulo es el primer segmento de la ruta:
 * {@code /api/clientes/5} → {@code clientes}.</p>
 *
 * <ul>
 *   <li><b>ADMIN</b> puede todo.</li>
 *   <li>Los módulos administrativos (usuarios, respaldos, auditoría e
 *       historial de versiones) son
 *       exclusivos del ADMIN.</li>
 *   <li>Un rol personalizado no listado en la matriz solo obtiene lectura
 *       de los módulos no administrativos (mínimo privilegio).</li>
 *   <li>Un <b>CLIENTE</b> del portal solo accede a sus propios recursos
 *       ({@code /api/portal/{suId}/...}) y a los tipos de cambio.</li>
 * </ul>
 */
public class PoliticaAcceso {

    /** Rutas que no requieren sesión. */
    private static final Set<String> PUBLICAS = Set.of(
            "GET /api/health",
            "POST /api/auth/login",
            "POST /api/portal/login",
            "POST /api/portal/registro");

    /** Rutas disponibles para cualquier sesión válida (personal o cliente). */
    private static final Set<String> CUALQUIER_SESION = Set.of(
            "POST /api/auth/logout",
            "GET /api/auth/sesion");

    private static final Set<String> SOLO_ADMIN = Set.of("usuarios", "respaldos", "auditoria", "versiones");

    /** módulo → (rol → "L" o "LE"). */
    private final Map<String, Map<String, String>> matriz = new HashMap<>();

    public PoliticaAcceso() {
        // Operación de caja y atención al cliente
        permitir("LE", "CAJERO", "clientes", "operaciones", "prestamos", "empenos", "ventas", "solicitudes");
        permitir("L", "CAJERO", "servicios", "tipos-cambio", "productos", "dashboard", "roles", "version");
        // Análisis, marketing, almacén, logística, producción y dirección
        permitir("LE", "ANALISTA", "campanias", "prospectos", "productos", "proveedores",
                "lineas", "ordenes", "expedientes");
        permitir("L", "ANALISTA", "clientes", "servicios", "tipos-cambio", "operaciones", "prestamos",
                "empenos", "ventas", "solicitudes", "dashboard", "roles", "version");
        // Supervisión: puede resolver y aprobar
        permitir("LE", "SUPERVISOR", "solicitudes", "operaciones", "expedientes", "clientes",
                "prestamos", "empenos");
        permitir("L", "SUPERVISOR", "servicios", "tipos-cambio", "ventas", "productos", "proveedores",
                "campanias", "prospectos", "lineas", "ordenes", "dashboard", "roles", "version");
    }

    private void permitir(String nivel, String rol, String... modulos) {
        for (String m : modulos) {
            matriz.computeIfAbsent(m, k -> new HashMap<>()).put(rol, nivel);
        }
    }

    /** ¿La ruta es pública (no requiere token)? */
    public boolean esPublica(String metodo, String ruta) {
        return PUBLICAS.contains(metodo.toUpperCase(Locale.ROOT) + " " + ruta);
    }

    /**
     * Verifica la autorización; lanza {@link AccesoDenegadoException} si
     * el rol de la sesión no tiene permiso.
     */
    public void autorizar(Sesion s, String metodo, String ruta) {
        if (!permite(s, metodo, ruta)) {
            throw new AccesoDenegadoException("Su rol (" + s.getRol() + ") no tiene permiso para "
                    + descripcion(metodo) + " en el módulo «" + modulo(ruta) + "».");
        }
    }

    public boolean permite(Sesion s, String metodo, String ruta) {
        String m = metodo.toUpperCase(Locale.ROOT);
        if (CUALQUIER_SESION.contains(m + " " + ruta)) {
            return true;
        }
        String modulo = modulo(ruta);

        // ---- Clientes del portal: solo lo suyo ----
        if (s.esCliente()) {
            if ("portal".equals(modulo)) {
                String[] p = ruta.split("/");          // "", api, portal, {id}, recurso
                return p.length >= 4 && p[3].equals(String.valueOf(s.getIdSujeto()));
            }
            return "tipos-cambio".equals(modulo) && "GET".equals(m);
        }

        // ---- Personal ----
        if ("portal".equals(modulo)) {
            return false; // el personal no opera como cliente
        }
        if ("ADMIN".equals(s.getRol())) {
            return true;
        }
        if (SOLO_ADMIN.contains(modulo)) {
            return false;
        }
        boolean lectura = "GET".equals(m);
        Map<String, String> porRol = matriz.get(modulo);
        String nivel = porRol == null ? null : porRol.get(s.getRol());
        if (nivel == null) {
            // Rol personalizado o módulo no listado: solo lectura de módulos conocidos.
            boolean rolConocido = matriz.values().stream().anyMatch(x -> x.containsKey(s.getRol()));
            return lectura && !rolConocido && porRol != null;
        }
        return lectura || nivel.contains("E");
    }

    /** Matriz de permisos (para mostrarla en la pantalla de Seguridad). */
    public Map<String, Map<String, String>> matriz() {
        Map<String, Map<String, String>> copia = new java.util.TreeMap<>();
        matriz.forEach((k, v) -> copia.put(k, new java.util.TreeMap<>(v)));
        for (String m : SOLO_ADMIN) {
            copia.put(m, new java.util.TreeMap<>());
        }
        copia.values().forEach(v -> v.put("ADMIN", "LE"));
        return copia;
    }

    static String modulo(String ruta) {
        String[] p = ruta.split("/");
        return p.length >= 3 ? p[2] : "";
    }

    private static String descripcion(String metodo) {
        switch (metodo.toUpperCase(Locale.ROOT)) {
            case "GET": return "consultar";
            case "POST": return "registrar";
            case "PUT": return "modificar";
            case "DELETE": return "eliminar";
            default: return metodo;
        }
    }
}
