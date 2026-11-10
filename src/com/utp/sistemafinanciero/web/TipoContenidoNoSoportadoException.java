package com.utp.sistemafinanciero.web;

/** Error del contrato HTTP: el endpoint JSON admite application/json en UTF-8. */
public final class TipoContenidoNoSoportadoException extends RuntimeException {
    public TipoContenidoNoSoportadoException() {
        super("Use Content-Type: application/json; charset=utf-8.");
    }
}
