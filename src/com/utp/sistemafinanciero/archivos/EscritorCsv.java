package com.utp.sistemafinanciero.archivos;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Escritor de archivos CSV según RFC 4180: separador coma, saltos de
 * línea CRLF, comillas dobles cuando el valor contiene separador, comillas
 * o saltos de línea, y BOM UTF-8 al inicio para que Excel muestre bien las
 * tildes y la «ñ».
 *
 * <p>Las celdas de texto pasan por {@link ProteccionFormulas} (evita la
 * inyección de fórmulas al abrir el archivo en una hoja de cálculo).</p>
 */
public final class EscritorCsv {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final char SEPARADOR = ',';

    private EscritorCsv() {
    }

    /**
     * @param encabezados títulos de columna
     * @param filas       filas de datos
     * @param numericas   true en las columnas numéricas (no se neutralizan); puede ser null
     */
    public static byte[] generar(String[] encabezados, List<String[]> filas, boolean[] numericas) {
        StringBuilder sb = new StringBuilder();
        linea(sb, encabezados, null);
        for (String[] f : filas) {
            linea(sb, f, numericas);
        }
        byte[] texto = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] salida = new byte[BOM.length + texto.length];
        System.arraycopy(BOM, 0, salida, 0, BOM.length);
        System.arraycopy(texto, 0, salida, BOM.length, texto.length);
        return salida;
    }

    private static void linea(StringBuilder sb, String[] valores, boolean[] numericas) {
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) sb.append(SEPARADOR);
            String v = valores[i] == null ? "" : valores[i];
            boolean numerica = numericas != null && i < numericas.length && numericas[i];
            if (!numerica) v = ProteccionFormulas.neutralizar(v);
            sb.append(escapar(v));
        }
        sb.append("\r\n");
    }

    static String escapar(String v) {
        boolean comillas = v.indexOf(SEPARADOR) >= 0 || v.indexOf(';') >= 0 || v.indexOf('"') >= 0
                || v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0;
        if (!comillas) return v;
        return '"' + v.replace("\"", "\"\"") + '"';
    }
}
