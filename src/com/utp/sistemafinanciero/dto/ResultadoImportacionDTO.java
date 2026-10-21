package com.utp.sistemafinanciero.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO con el resultado de importar un archivo: cuántas filas se leyeron,
 * cuántas se registraron, cuáles se rechazaron y por qué. Es lo que la
 * pantalla muestra al usuario después de subir un CSV o un Excel.
 */
public class ResultadoImportacionDTO {

    /** Error de una fila concreta del archivo (número de fila tal como se ve en Excel). */
    public static class ErrorFila {
        private final int fila;
        private final String mensaje;

        public ErrorFila(int fila, String mensaje) {
            this.fila = fila;
            this.mensaje = mensaje;
        }

        public int getFila() { return fila; }
        public String getMensaje() { return mensaje; }
    }

    /** Máximo de errores detallados que se devuelven (el resto solo se cuenta). */
    public static final int MAX_ERRORES = 100;

    private final String recurso;
    private final String archivo;
    private final String formato;
    private final boolean soloValidacion;
    private int filasLeidas;
    private int filasValidas;
    private int filasImportadas;
    private int filasRechazadas;
    private final List<String> columnasReconocidas = new ArrayList<>();
    private final List<String> columnasIgnoradas = new ArrayList<>();
    private final List<ErrorFila> errores = new ArrayList<>();

    public ResultadoImportacionDTO(String recurso, String archivo, String formato, boolean soloValidacion) {
        this.recurso = recurso;
        this.archivo = archivo;
        this.formato = formato;
        this.soloValidacion = soloValidacion;
    }

    public void sumarLeida() { filasLeidas++; }

    public void sumarValida() { filasValidas++; }

    public void sumarImportada() { filasImportadas++; }

    public void rechazar(int fila, String mensaje) {
        filasRechazadas++;
        if (errores.size() < MAX_ERRORES) {
            errores.add(new ErrorFila(fila, mensaje));
        }
    }

    public String getRecurso() { return recurso; }
    public String getArchivo() { return archivo; }
    public String getFormato() { return formato; }
    public boolean isSoloValidacion() { return soloValidacion; }
    public int getFilasLeidas() { return filasLeidas; }
    public int getFilasValidas() { return filasValidas; }
    public int getFilasImportadas() { return filasImportadas; }
    public int getFilasRechazadas() { return filasRechazadas; }
    public List<String> getColumnasReconocidas() { return columnasReconocidas; }
    public List<String> getColumnasIgnoradas() { return columnasIgnoradas; }
    public List<ErrorFila> getErrores() { return errores; }
}
