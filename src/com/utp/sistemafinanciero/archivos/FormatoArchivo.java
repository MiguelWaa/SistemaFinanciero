package com.utp.sistemafinanciero.archivos;

import com.utp.sistemafinanciero.service.ReglaNegocioException;

import java.util.Locale;

/**
 * Formatos de fichero que SIGSF sabe exportar e importar.
 *
 * <table>
 *   <caption>Formatos</caption>
 *   <tr><th>Formato</th><th>Exporta</th><th>Importa</th></tr>
 *   <tr><td>XLSX (Excel)</td><td>sí</td><td>sí</td></tr>
 *   <tr><td>CSV (UTF-8, separador «,» o «;»)</td><td>sí</td><td>sí</td></tr>
 *   <tr><td>JSON</td><td>sí</td><td>no</td></tr>
 * </table>
 */
public enum FormatoArchivo {

    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true),
    CSV("csv", "text/csv; charset=utf-8", true),
    JSON("json", "application/json; charset=utf-8", false);

    private final String extension;
    private final String contentType;
    private final boolean importable;

    FormatoArchivo(String extension, String contentType, boolean importable) {
        this.extension = extension;
        this.contentType = contentType;
        this.importable = importable;
    }

    public String extension() { return extension; }

    public String contentType() { return contentType; }

    public boolean importable() { return importable; }

    /** Formato pedido en {@code ?formato=}; por defecto XLSX. */
    public static FormatoArchivo desdeParametro(String valor) {
        if (valor == null || valor.isBlank()) return XLSX;
        for (FormatoArchivo f : values()) {
            if (f.extension.equalsIgnoreCase(valor.trim())) return f;
        }
        throw new ReglaNegocioException("Formato no soportado: use xlsx, csv o json.");
    }

    /**
     * Formato de un archivo que se quiere importar, según su extensión
     * (lista blanca: .xlsx, .csv y .txt). Cualquier otra extensión se rechaza.
     */
    public static FormatoArchivo desdeNombreArchivo(String nombre) {
        String n = nombre == null ? "" : nombre.trim().toLowerCase(Locale.ROOT);
        if (n.endsWith(".xlsx")) return XLSX;
        if (n.endsWith(".csv") || n.endsWith(".txt")) return CSV;
        throw new ReglaNegocioException("Tipo de archivo no permitido. Solo se importan archivos .xlsx o .csv.");
    }
}
