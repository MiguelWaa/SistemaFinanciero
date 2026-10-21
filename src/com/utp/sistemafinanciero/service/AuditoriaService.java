package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.AuditoriaDao;
import com.utp.sistemafinanciero.model.seguridad.Auditoria;
import com.utp.sistemafinanciero.security.Sesion;
import com.utp.sistemafinanciero.web.Ctx;
import com.utp.sistemafinanciero.web.ObservadorPeticion;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bitácora de auditoría. Observa cada petición a la API (patrón
 * Observer) y registra los eventos relevantes para la trazabilidad:
 * inicios de sesión (correctos y fallidos), altas, cambios, bajas,
 * exportaciones, respaldos y accesos denegados.
 *
 * <p>Nunca guarda el cuerpo de la petición (podría contener contraseñas).</p>
 *
 * <p>Además de la tabla {@code auditoria}, los eventos de seguridad
 * (inicio de sesión fallido, acceso denegado, respaldo, importación y
 * exportación de ficheros) se escriben en el logger {@code SIGSF.SEGURIDAD}
 * (archivo {@code logs/sigsf-seguridad.log}), útil si la base no está
 * disponible o para enviarlos a un monitor externo.</p>
 */
public class AuditoriaService implements ObservadorPeticion {

    private static final Logger SEGURIDAD = LoggerFactory.getLogger("SIGSF.SEGURIDAD");
    private static final Set<String> EVENTOS_SEGURIDAD = Set.of("LOGIN_FALLIDO", "ACCESO_DENEGADO",
            "RESPALDO", "IMPORTAR", "EXPORTAR", "LOGIN_OK", "LOGOUT");

    /** Atributo del contexto con el usuario que intenta iniciar sesión. */
    public static final String USUARIO_LOGIN = "auditoria.usuarioLogin";

    private final AuditoriaDao dao;

    public AuditoriaService(AuditoriaDao dao) {
        this.dao = dao;
    }

    @Override
    public void alFinalizar(Ctx ctx, int estado) {
        String accion = clasificar(ctx.metodo(), ctx.ruta(), estado);
        if (accion == null) {
            return;
        }
        Sesion s = ctx.sesion();
        String usuario = s != null ? s.getUsuario() : (String) ctx.atributo(USUARIO_LOGIN);
        String rol = s != null ? s.getRol() : null;
        String detalle = estado >= 400 ? "Rechazado (HTTP " + estado + ")" : "Correcto";
        if (EVENTOS_SEGURIDAD.contains(accion)) {
            String quien = usuario == null ? "anónimo" : usuario;
            if (estado >= 400) {
                SEGURIDAD.warn("{} usuario={} rol={} ip={} recurso={} {} http={}", accion, quien, rol,
                        ctx.ip(), ctx.metodo(), ctx.ruta(), estado);
            } else {
                SEGURIDAD.info("{} usuario={} rol={} ip={} recurso={} {} http={}", accion, quien, rol,
                        ctx.ip(), ctx.metodo(), ctx.ruta(), estado);
            }
        }
        try {
            dao.registrar(new Auditoria(usuario == null ? "anónimo" : usuario, rol, ctx.ip(), accion,
                    ctx.metodo() + " " + ctx.ruta(), estado, detalle));
        } catch (Exception e) {
            SEGURIDAD.error("No se pudo registrar la auditoría en la base: {}", e.getMessage());
        }
    }

    /** Decide qué acción registrar; null = no se audita (consultas normales). */
    static String clasificar(String metodo, String ruta, int estado) {
        if (ruta.endsWith("/auth/login") || ruta.endsWith("/portal/login")) {
            return estado == 200 ? "LOGIN_OK" : "LOGIN_FALLIDO";
        }
        if (ruta.endsWith("/auth/logout")) return "LOGOUT";
        if (estado == 401 || estado == 403) return "ACCESO_DENEGADO";
        switch (metodo.toUpperCase()) {
            case "POST":
                if (ruta.startsWith("/api/respaldos")) return "RESPALDO";
                if (ruta.endsWith("/importar")) return "IMPORTAR";
                if (ruta.endsWith("/portal/registro")) return "REGISTRO_CLIENTE";
                return "CREAR";
            case "PUT": return "ACTUALIZAR";
            case "DELETE": return "ELIMINAR";
            case "GET":
                return (ruta.endsWith("/excel") || ruta.endsWith("/descargar") || ruta.endsWith("/exportar")
                        || ruta.endsWith("/plantilla")) ? "EXPORTAR" : null;
            default: return null;
        }
    }

    public List<Auditoria> recientes(int limite) {
        return dao.recientes(Math.max(1, Math.min(limite, 500)));
    }
}
