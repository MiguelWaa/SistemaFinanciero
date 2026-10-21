package com.utp.sistemafinanciero.service;

/**
 * Excepcion para violaciones de reglas de negocio o validaciones
 * (por ejemplo, campos obligatorios faltantes o datos duplicados).
 * La capa web la traduce a una respuesta HTTP 400 con el mensaje.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
