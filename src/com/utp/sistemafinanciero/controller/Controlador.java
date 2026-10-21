package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.web.ApiServer;

/**
 * <b>Controlador</b> de la arquitectura MVC. Cada módulo del negocio
 * (Marketing, Ventas, Logística, Almacén, Producción, Operaciones,
 * Dirección/Gerencia, Clientes, Seguridad...) tiene su propio
 * controlador que publica sus endpoints REST y delega la lógica en los
 * servicios (el <b>Modelo</b>). La <b>Vista</b> es el frontend web.
 *
 * <p>Un controlador NO contiene reglas de negocio ni SQL: solo traduce
 * HTTP ↔ objetos (responsabilidad única).</p>
 */
public interface Controlador {

    /** Registra las rutas del módulo en el servidor. */
    void registrar(ApiServer app);
}
