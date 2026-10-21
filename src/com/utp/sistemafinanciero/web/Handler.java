package com.utp.sistemafinanciero.web;

/**
 * Manejador de una ruta de la API. Recibe el contexto de la peticion y
 * devuelve el objeto que se serializara como JSON (o null para 204).
 */
@FunctionalInterface
public interface Handler {
    Object handle(Ctx ctx) throws Exception;
}
