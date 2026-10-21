package com.utp.sistemafinanciero.archivos;

/** Archivo listo para descargar: nombre, tipo MIME y contenido. */
public final class ArchivoGenerado {

    private final String nombre;
    private final String contentType;
    private final byte[] datos;

    public ArchivoGenerado(String nombre, String contentType, byte[] datos) {
        this.nombre = nombre;
        this.contentType = contentType;
        this.datos = datos;
    }

    public String nombre() { return nombre; }

    public String contentType() { return contentType; }

    public byte[] datos() { return datos; }
}
