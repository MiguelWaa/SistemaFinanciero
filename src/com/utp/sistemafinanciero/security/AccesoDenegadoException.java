package com.utp.sistemafinanciero.security;

/** El usuario tiene sesión pero su rol no permite la acción. HTTP 403. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
