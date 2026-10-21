package com.utp.sistemafinanciero.security;

import com.utp.sistemafinanciero.web.Ctx;
import com.utp.sistemafinanciero.web.Filtro;

/**
 * Filtro de seguridad de la API: <b>autenticación</b> (¿quién eres?) y
 * <b>autorización</b> (¿puedes hacer esto?). Se ejecuta antes de que la
 * petición llegue a cualquier controlador, por lo que ningún endpoint
 * protegido queda expuesto por olvido (seguridad por defecto).
 */
public class FiltroSeguridad implements Filtro {

    private final GestorSesiones sesiones;
    private final PoliticaAcceso politica;

    public FiltroSeguridad(GestorSesiones sesiones, PoliticaAcceso politica) {
        this.sesiones = sesiones;
        this.politica = politica;
    }

    @Override
    public void aplicar(Ctx ctx) {
        String metodo = ctx.metodo();
        String ruta = ctx.ruta();
        if (politica.esPublica(metodo, ruta)) {
            return;
        }
        Sesion s = sesiones.validar(ctx.token()).orElseThrow(() ->
                new NoAutenticadoException("Sesión no válida o expirada. Inicie sesión nuevamente."));
        ctx.setSesion(s);
        politica.autorizar(s, metodo, ruta);
    }
}
