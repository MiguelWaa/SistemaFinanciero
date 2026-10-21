package com.utp.sistemafinanciero.security;

/** Petición sin sesión válida. La capa web la traduce a HTTP 401. */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
