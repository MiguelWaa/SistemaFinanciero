package com.utp.sistemafinanciero.web;

/**
 * Observador que recibe el resultado de cada petición a la API, después
 * de responder (patrón <b>Observer</b>). Lo usa la auditoría para
 * registrar quién hizo qué, sin mezclar ese código con los controladores.
 */
@FunctionalInterface
public interface ObservadorPeticion {
    void alFinalizar(Ctx ctx, int estadoHttp);
}
