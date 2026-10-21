package com.utp.sistemafinanciero.web;

/**
 * Representa un archivo que la API entrega para descargar (por ejemplo un
 * Excel). Cuando un handler devuelve un objeto de este tipo, el servidor
 * lo envía como archivo adjunto en lugar de como JSON.
 */
public class Descarga {

    public final String nombre;
    public final String contentType;
    public final byte[] datos;

    public Descarga(String nombre, String contentType, byte[] datos) {
        this.nombre = nombre;
        this.contentType = contentType;
        this.datos = datos;
    }
}
