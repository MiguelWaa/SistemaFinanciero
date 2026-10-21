package com.utp.sistemafinanciero.dto;

import java.time.LocalDateTime;

/** DTO que describe un archivo de respaldo (backup) generado por el sistema. */
public class RespaldoDTO {

    private final String nombre;
    private final long bytes;
    private final LocalDateTime fecha;
    private final String metodo;

    public RespaldoDTO(String nombre, long bytes, LocalDateTime fecha, String metodo) {
        this.nombre = nombre;
        this.bytes = bytes;
        this.fecha = fecha;
        this.metodo = metodo;
    }

    public String getNombre() { return nombre; }
    public long getBytes() { return bytes; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMetodo() { return metodo; }

    /** Tamaño legible, por ejemplo "12.4 KB". */
    public String getTamanio() {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024));
    }
}
