package com.utp.sistemafinanciero.web;

/**
 * Filtro que se ejecuta antes de cada petición a la API (patrón
 * <b>Intercepting Filter / Chain of Responsibility</b>). Si el filtro
 * lanza una excepción, la cadena se corta y el controlador no se ejecuta.
 * Ejemplo: {@code security.FiltroSeguridad} (autenticación + autorización).
 */
@FunctionalInterface
public interface Filtro {
    void aplicar(Ctx ctx) throws Exception;
}
