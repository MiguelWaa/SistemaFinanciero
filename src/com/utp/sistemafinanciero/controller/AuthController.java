package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.security.Sesion;
import com.utp.sistemafinanciero.service.AuditoriaService;
import com.utp.sistemafinanciero.service.AuthService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

/** Seguridad y usuarios: inicio y cierre de sesión del personal. */
public class AuthController implements Controlador {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @Override
    public void registrar(ApiServer app) {
        app.post("/api/auth/login", ctx -> {
            LoginDTO datos = LoginDTO.personal(ctx.bodyMap());
            ctx.atributo(AuditoriaService.USUARIO_LOGIN, datos.getUsuario());
            return auth.login(datos);
        });
        app.post("/api/auth/logout", ctx -> {
            auth.logout(ctx.token());
            return Json.map("ok", true);
        });
        // Datos de la sesión actual (para que la Vista muestre nombre y rol).
        app.get("/api/auth/sesion", ctx -> {
            Sesion s = ctx.sesion();
            return Json.map("usuario", s.getUsuario(), "rol", s.getRol(),
                    "tipo", s.getTipo().name(), "expira", s.getExpira().toString());
        });
    }
}
