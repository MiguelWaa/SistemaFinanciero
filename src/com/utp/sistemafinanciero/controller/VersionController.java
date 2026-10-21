package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.version.VersionService;
import com.utp.sistemafinanciero.web.ApiServer;

/**
 * Control de versiones del sistema.
 * <ul>
 *   <li>{@code GET /api/version}: versión en ejecución (cualquier usuario del
 *       personal con sesión; se muestra en el pie del panel).</li>
 *   <li>{@code GET /api/versiones}: historial del repositorio Git, ramas,
 *       etiquetas, grafo, lanzamientos y versiones del esquema de la base.
 *       <b>Solo ADMIN</b> (revela autores y estructura interna).</li>
 * </ul>
 */
public class VersionController implements Controlador {

    private final VersionService versiones;

    public VersionController(VersionService versiones) {
        this.versiones = versiones;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/version", ctx -> versiones.actual());
        app.get("/api/versiones", ctx -> versiones.detalle());
    }
}
