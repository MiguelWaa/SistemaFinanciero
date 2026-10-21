package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.ClienteService;
import com.utp.sistemafinanciero.service.ServicioFinancieroService;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;

import static com.utp.sistemafinanciero.controller.Exportar.texto;

/** Módulo Clientes y Servicios Financieros. */
public class ClienteController implements Controlador {

    private final ClienteService clientes;
    private final ServicioFinancieroService servicios;

    public ClienteController(ClienteService clientes, ServicioFinancieroService servicios) {
        this.clientes = clientes;
        this.servicios = servicios;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/clientes/excel", ctx -> Exportar.excel("clientes.xlsx", "Clientes",
                new String[]{"N°", "Documento", "Nombres", "Apellidos", "Teléfono", "Correo", "Estado"},
                clientes.listar(), c -> new String[]{
                        texto(c.getIdCliente()), texto(c.getTipoDocumento()) + " " + texto(c.getDocumento()),
                        texto(c.getNombres()), texto(c.getApellidos()), texto(c.getTelefono()),
                        texto(c.getCorreo()), texto(c.getEstado())}));
        new ControladorCrud<>("clientes", clientes, MapeadorEntrada::aCliente).registrar(app);
        // RF-CLI-06: activar o bloquear la cuenta del cliente
        app.put("/api/clientes/{id}/estado", ctx ->
                clientes.cambiarEstado(ctx.paramInt("id"), Json.str(ctx.bodyMap(), "estado")));

        new ControladorCrud<>("servicios", servicios, MapeadorEntrada::aServicio).registrar(app);
    }
}
