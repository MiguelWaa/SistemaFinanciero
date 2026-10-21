package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.ServicioCrud;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

import java.util.Map;
import java.util.function.Function;

/**
 * Controlador genérico de mantenimiento (CRUD). Publica para cualquier
 * {@link ServicioCrud} los cinco endpoints REST estándar:
 *
 * <pre>
 *   GET    /api/{recurso}        listar
 *   GET    /api/{recurso}/{id}   buscar
 *   POST   /api/{recurso}        crear
 *   PUT    /api/{recurso}/{id}   actualizar
 *   DELETE /api/{recurso}/{id}   eliminar
 * </pre>
 *
 * @param <E> entidad que recibe el servicio
 * @param <S> tipo que devuelve el servicio (entidad o DTO)
 */
public class ControladorCrud<E, S> implements Controlador {

    private final String base;
    private final ServicioCrud<E, S> servicio;
    private final Function<Map<String, Object>, E> mapeador;

    public ControladorCrud(String recurso, ServicioCrud<E, S> servicio,
                           Function<Map<String, Object>, E> mapeador) {
        this.base = "/api/" + recurso;
        this.servicio = servicio;
        this.mapeador = mapeador;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get(base, ctx -> servicio.listar());
        app.get(base + "/{id}", ctx -> servicio.buscar(ctx.paramInt("id")));
        app.post(base, ctx -> servicio.crear(mapeador.apply(ctx.bodyMap())));
        app.put(base + "/{id}", ctx -> servicio.actualizar(ctx.paramInt("id"), mapeador.apply(ctx.bodyMap())));
        app.delete(base + "/{id}", ctx -> {
            servicio.eliminar(ctx.paramInt("id"));
            return Json.map("ok", true);
        });
    }
}
