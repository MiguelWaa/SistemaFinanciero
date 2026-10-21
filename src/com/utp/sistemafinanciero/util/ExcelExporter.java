package com.utp.sistemafinanciero.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generador de archivos Excel (.xlsx) escrito en Java puro, sin librerías
 * externas (no usa Apache POI). Un .xlsx es en realidad un ZIP con varios
 * archivos XML; aquí se arman esos XML mínimos necesarios y se comprimen.
 *
 * <p>El libro generado tiene: encabezado en negrita con fondo azul, fila
 * de títulos inmovilizada, autofiltro y ancho de columnas ajustado al
 * contenido.</p>
 *
 * <p>Seguridad y calidad de datos:</p>
 * <ul>
 *   <li>Todo texto se escribe como <i>inline string</i> y se escapa para
 *       XML: Excel nunca lo evalúa como fórmula.</li>
 *   <li>Los números con ceros a la izquierda (DNI, RUC, teléfonos) se
 *       guardan como texto para no perder los ceros.</li>
 * </ul>
 *
 * <p>Uso: {@code ExcelExporter.generar("Ventas", encabezados, filas)} →
 * devuelve los bytes del archivo listo para guardar o descargar.</p>
 */
public final class ExcelExporter {

    private static final int ESTILO_ENCABEZADO = 1;

    private ExcelExporter() {
    }

    /**
     * Crea un libro de Excel con una sola hoja. Decide qué celdas son
     * numéricas mirando el valor (sin ceros a la izquierda).
     */
    public static byte[] generar(String hoja, String[] encabezados, List<String[]> filas) {
        return generar(hoja, encabezados, filas, null);
    }

    /**
     * Crea un libro de Excel con una sola hoja.
     *
     * @param hoja        nombre visible de la hoja
     * @param encabezados primera fila (títulos de columna)
     * @param filas       resto de filas; cada arreglo es una fila
     * @param numericas   qué columnas son numéricas (null = detectar por el valor)
     * @return bytes del archivo .xlsx
     */
    public static byte[] generar(String hoja, String[] encabezados, List<String[]> filas, boolean[] numericas) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bos)) {
                escribir(zip, "[Content_Types].xml", contentTypes());
                escribir(zip, "_rels/.rels", rels());
                escribir(zip, "xl/workbook.xml", workbook(hoja));
                escribir(zip, "xl/_rels/workbook.xml.rels", workbookRels());
                escribir(zip, "xl/styles.xml", estilos());
                escribir(zip, "xl/worksheets/sheet1.xml", hojaXml(encabezados, filas, numericas));
            }
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el Excel: " + e.getMessage(), e);
        }
    }

    private static void escribir(ZipOutputStream zip, String nombre, String contenido) throws Exception {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
             + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
             + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
             + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
             + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
             + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
             + "</Types>";
    }

    private static String rels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
             + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
             + "</Relationships>";
    }

    private static String workbook(String hoja) {
        String nombre = hoja == null || hoja.isBlank() ? "Hoja1" : hoja.replaceAll("[\\\\/?*\\[\\]:]", " ");
        if (nombre.length() > 31) nombre = nombre.substring(0, 31);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""
             + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
             + "<sheets><sheet name=\"" + escapar(nombre) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
             + "</workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
             + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
             + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
             + "</Relationships>";
    }

    /** Estilo 0 = normal · estilo 1 = encabezado (negrita, letra blanca, fondo azul). */
    private static String estilos() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
             + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
             + "<fonts count=\"2\">"
             + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
             + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
             + "</fonts>"
             + "<fills count=\"3\">"
             + "<fill><patternFill patternType=\"none\"/></fill>"
             + "<fill><patternFill patternType=\"gray125\"/></fill>"
             + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1E3A8A\"/><bgColor indexed=\"64\"/></patternFill></fill>"
             + "</fills>"
             + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
             + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
             + "<cellXfs count=\"2\">"
             + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
             + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
             + "</cellXfs>"
             + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
             + "</styleSheet>";
    }

    private static String hojaXml(String[] encabezados, List<String[]> filas, boolean[] numericas) {
        int columnas = encabezados == null ? 0 : encabezados.length;
        int[] anchos = new int[Math.max(columnas, 1)];
        for (int i = 0; i < columnas; i++) anchos[i] = largo(encabezados[i]);
        if (filas != null) {
            for (String[] f : filas) {
                for (int i = 0; i < Math.min(f.length, anchos.length); i++) {
                    anchos[i] = Math.max(anchos[i], largo(f[i]));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        if (columnas > 0) {
            // Fila de títulos inmovilizada
            sb.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" "
                    + "activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
            sb.append("<cols>");
            for (int i = 0; i < columnas; i++) {
                int ancho = Math.min(Math.max(anchos[i] + 2, 8), 50);
                sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                  .append("\" width=\"").append(ancho).append("\" customWidth=\"1\"/>");
            }
            sb.append("</cols>");
        }
        sb.append("<sheetData>");
        int nFila = 1;
        if (columnas > 0) {
            sb.append(fila(nFila++, encabezados, null, ESTILO_ENCABEZADO));
        }
        if (filas != null) {
            for (String[] f : filas) {
                sb.append(fila(nFila++, f, numericas, 0));
            }
        }
        sb.append("</sheetData>");
        if (columnas > 0) {
            sb.append("<autoFilter ref=\"A1:").append(columna(columnas - 1)).append(Math.max(nFila - 1, 1))
              .append("\"/>");
        }
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static String fila(int numero, String[] valores, boolean[] numericas, int estilo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<row r=\"").append(numero).append("\">");
        for (int i = 0; i < valores.length; i++) {
            String ref = columna(i) + numero;
            String valor = valores[i] == null ? "" : valores[i];
            String s = estilo == 0 ? "" : " s=\"" + estilo + "\"";
            boolean numero_ = estilo == 0 && (numericas == null ? esNumero(valor)
                    : (i < numericas.length && numericas[i] && esDecimal(valor)));
            if (numero_) {
                sb.append("<c r=\"").append(ref).append("\"").append(s).append("><v>").append(valor).append("</v></c>");
            } else {
                sb.append("<c r=\"").append(ref).append("\"").append(s)
                  .append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                  .append(escapar(valor)).append("</t></is></c>");
            }
        }
        sb.append("</row>");
        return sb.toString();
    }

    /** Convierte un índice de columna (0,1,2...) en letra(s) (A,B,...,Z,AA...). */
    private static String columna(int indice) {
        StringBuilder sb = new StringBuilder();
        int n = indice;
        do {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    /** Número "seguro": sin ceros a la izquierda y de hasta 15 cifras (precisión de Excel). */
    static boolean esNumero(String s) {
        return s != null && s.matches("-?(0|[1-9]\\d{0,14})(\\.\\d+)?");
    }

    private static boolean esDecimal(String s) {
        return s != null && s.matches("-?\\d{1,15}(\\.\\d+)?");
    }

    private static int largo(String s) {
        return s == null ? 0 : Math.min(s.length(), 60);
    }

    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default:
                    // Los caracteres de control no son válidos en XML 1.0 (salvo tab y saltos).
                    if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') continue;
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
