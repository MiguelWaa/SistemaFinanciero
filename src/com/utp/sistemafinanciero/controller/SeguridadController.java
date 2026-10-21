package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.PoliticaAcceso;
import com.utp.sistemafinanciero.service.AuditoriaService;
import com.utp.sistemafinanciero.service.RolService;
import com.utp.sistemafinanciero.service.UsuarioService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

/** Módulo Seguridad y Usuarios: usuarios, roles, matriz de permisos y auditoría. */
public class SeguridadController implements Controlador {

    private final UsuarioService usuarios;
    private final RolService roles;
    private final AuditoriaService auditoria;
    private final PoliticaAcceso politica;
    private final GestorSesiones sesiones;

    public SeguridadController(UsuarioService usuarios, RolService roles, AuditoriaService auditoria,
                               PoliticaAcceso politica, GestorSesiones sesiones) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.auditoria = auditoria;
        this.politica = politica;
        this.sesiones = sesiones;
    }

    @Override
    public void registrar(ApiServer app) {
        new ControladorCrud<>("usuarios", usuarios, MapeadorEntrada::aUsuario).registrar(app);
        new ControladorCrud<>("roles", roles, MapeadorEntrada::aRol).registrar(app);

        app.get("/api/auditoria", ctx -> auditoria.recientes(200));
        app.get("/api/auditoria/permisos", ctx -> Json.map(
                "matriz", politica.matriz(),
                "sesionesActivas", sesiones.activas(),
                "minutosSesion", sesiones.minutos()));
    }
}
