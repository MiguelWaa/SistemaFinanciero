package com.utp.sistemafinanciero.archivos;

import com.utp.sistemafinanciero.service.ReglaNegocioException;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lector de archivos CSV (RFC 4180) para la importación.
 *
 * <ul>
 *   <li>Detecta el separador (coma, punto y coma o tabulador) mirando la
 *       primera línea: Excel en español suele guardar con «;».</li>
 *   <li>Decodifica en UTF-8 estricto; si el archivo no es UTF-8 válido
 *       (CSV guardado por Excel en Windows), usa Windows-1252.</li>
 *   <li>Respeta comillas dobles, comillas escapadas ({@code ""}) y saltos de
 *       línea dentro de un valor.</li>
 *   <li>Rechaza archivos binarios (bytes nulos o firma ZIP) y corta cuando
 *       se supera el máximo de filas (protección contra archivos enormes).</li>
 * </ul>
 */
public final class LectorCsv {

    private LectorCsv() {
    }

    /** Lee todas las filas (la primera es el encabezado). */
    public static List<String[]> leer(byte[] datos, int maxFilas) {
        String texto = decodificar(datos);
        if (!texto.isEmpty() && texto.charAt(0) == '\uFEFF') {
            texto = texto.substring(1); // BOM
        }
        char sep = detectarSeparador(texto);
        List<String[]> filas = new ArrayList<>();
        List<String> actual = new ArrayList<>();
        StringBuilder celda = new StringBuilder();
        boolean entreComillas = false;
        int n = texto.length();
        for (int i = 0; i < n; i++) {
            char c = texto.charAt(i);
            if (entreComillas) {
                if (c == '"') {
                    if (i + 1 < n && texto.charAt(i + 1) == '"') {
                        celda.append('"');
                        i++;
                    } else {
                        entreComillas = false;
                    }
                } else {
                    celda.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == sep) {
                actual.add(celda.toString());
                celda.setLength(0);
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < n && texto.charAt(i + 1) == '\n') i++;
                actual.add(celda.toString());
                celda.setLength(0);
                agregar(filas, actual, maxFilas);
                actual = new ArrayList<>();
            } else {
                celda.append(c);
            }
        }
        if (entreComillas) {
            throw new ReglaNegocioException("El CSV tiene comillas sin cerrar.");
        }
        if (celda.length() > 0 || !actual.isEmpty()) {
            actual.add(celda.toString());
            agregar(filas, actual, maxFilas);
        }
        return filas;
    }

    private static void agregar(List<String[]> filas, List<String> fila, int maxFilas) {
        boolean vacia = fila.stream().allMatch(v -> v.trim().isEmpty());
        if (vacia && filas.isEmpty()) return; // líneas en blanco antes del encabezado
        filas.add(fila.toArray(new String[0]));
        if (filas.size() > maxFilas + 1) {
            throw new ReglaNegocioException("El archivo supera el máximo de " + maxFilas + " filas por importación.");
        }
    }

    static String decodificar(byte[] datos) {
        if (datos.length >= 2 && datos[0] == 'P' && datos[1] == 'K') {
            throw new ReglaNegocioException("El archivo parece un Excel (.xlsx), no un CSV. Cambie la extensión o "
                    + "expórtelo como CSV.");
        }
        for (byte b : datos) {
            if (b == 0) throw new ReglaNegocioException("El archivo no es de texto (contiene bytes nulos).");
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(datos)).toString();
        } catch (CharacterCodingException e) {
            return new String(datos, Charset.forName("windows-1252"));
        }
    }

    static char detectarSeparador(String texto) {
        int fin = texto.indexOf('\n');
        String primera = fin < 0 ? texto : texto.substring(0, fin);
        int comas = 0, puntoYComa = 0, tabs = 0;
        boolean comillas = false;
        for (char c : primera.toCharArray()) {
            if (c == '"') comillas = !comillas;
            if (comillas) continue;
            if (c == ',') comas++;
            else if (c == ';') puntoYComa++;
            else if (c == '\t') tabs++;
        }
        if (puntoYComa > comas && puntoYComa >= tabs) return ';';
        if (tabs > comas) return '\t';
        return ',';
    }
}
