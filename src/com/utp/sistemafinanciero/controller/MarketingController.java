package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.CampaniaService;
import com.utp.sistemafinanciero.service.ProspectoService;
import com.utp.sistemafinanciero.web.ApiServer;

/** Módulo Marketing: campañas y prospectos (clientes potenciales). */
public class MarketingController implements Controlador {

    private final CampaniaService campanias;
    private final ProspectoService prospectos;

    public MarketingController(CampaniaService campanias, ProspectoService prospectos) {
        this.campanias = campanias;
        this.prospectos = prospectos;
    }

    @Override
    public void registrar(ApiServer app) {
        new ControladorCrud<>("campanias", campanias, MapeadorEntrada::aCampania).registrar(app);
        new ControladorCrud<>("prospectos", prospectos, MapeadorEntrada::aProspecto).registrar(app);
    }
}
