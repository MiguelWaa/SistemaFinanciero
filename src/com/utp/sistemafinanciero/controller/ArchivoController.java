package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.archivos.ArchivoGenerado;
import com.utp.sistemafinanciero.archivos.DefinicionArchivo;
import com.utp.sistemafinanciero.archivos.FormatoArchivo;
import com.utp.sistemafinanciero.service.ArchivoService;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Descarga;

import java.util.Base64;
import java.util.Map;

/**
 * Controlador de <b>importación y exportación de ficheros</b>. Para cada
 * recurso del {@link CatalogoArchivos} publica:
 *
 * <pre>
 *   GET  /api/{recurso}/exportar?formato=xlsx|csv|json   descargar los datos
 *   GET  /api/{recurso}/plantilla                         plantilla CSV para importar
 *   POST /api/{recurso}/importar                          subir un .csv o .xlsx
 *        cuerpo: {"nombre": "clientes.csv", "contenido": "&lt;base64&gt;", "soloValidar": true}
 * </pre>
 *
 * <p>Seguridad: las rutas están bajo {@code /api/{recurso}}, así que el
 * {@code FiltroSeguridad} exige sesión y aplica la matriz de permisos:
 * exportar requiere lectura (L) del módulo; importar requiere escritura
 * (LE). Cada exportación e importación queda en la auditoría.</p>
 */
public class ArchivoController implements Controlador {

    private final ArchivoService archivos;
    private final Map<String, DefinicionArchivo<?>> catalogo;

    public ArchivoController(ArchivoService archivos, Map<String, DefinicionArchivo<?>> catalogo) {
        this.archivos = archivos;
        this.catalogo = catalogo;
    }

    @Override
    public void registrar(ApiServer app) {
        for (DefinicionArchivo<?> def : catalogo.values()) {
            String base = "/api/" + def.recurso();
            app.get(base + "/exportar", ctx ->
                    descarga(archivos.exportar(def, FormatoArchivo.desdeParametro(ctx.query("formato")))));
            if (def.importable()) {
                app.get(base + "/plantilla", ctx -> descarga(archivos.plantilla(def)));
                app.post(base + "/importar", ctx -> {
                    Map<String, Object> b = ctx.bodyMap();
                    String nombre = Json.str(b, "nombre");
                    byte[] datos = decodificar(Json.str(b, "contenido"));
                    boolean soloValidar = Boolean.TRUE.equals(b.get("soloValidar"));
                    return archivos.importar(def, nombre, datos, soloValidar);
                });
            }
        }
    }

    private static Descarga descarga(ArchivoGenerado a) {
        return new Descarga(a.nombre(), a.contentType(), a.datos());
    }

    private static byte[] decodificar(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new ReglaNegocioException("No se recibió el contenido del archivo.");
        }
        String limpio = base64.contains(",") ? base64.substring(base64.indexOf(',') + 1) : base64; // data:...;base64,
        try {
            return Base64.getMimeDecoder().decode(limpio);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("El contenido del archivo no está en Base64 válido.");
        }
    }
}
