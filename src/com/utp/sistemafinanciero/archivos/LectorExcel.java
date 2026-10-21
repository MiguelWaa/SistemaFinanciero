package com.utp.sistemafinanciero.archivos;

import com.utp.sistemafinanciero.service.ReglaNegocioException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Lector de archivos Excel (.xlsx) escrito con el JDK, sin Apache POI:
 * un .xlsx es un ZIP con XML. Se lee la <b>primera hoja</b> del libro.
 *
 * <p>Medidas de seguridad (el archivo lo sube un usuario, no es confiable):</p>
 * <ul>
 *   <li><b>Firma</b>: debe empezar con la cabecera ZIP {@code PK\3\4}.</li>
 *   <li><b>Bomba ZIP</b>: límite de entradas, de tamaño descomprimido por
 *       entrada y total; solo se leen los XML necesarios.</li>
 *   <li><b>XXE</b> (XML External Entities): el analizador StAX se configura
 *       sin DTD ni entidades externas, y se rechaza cualquier
 *       {@code <!DOCTYPE>}.</li>
 *   <li>Se toman los <b>valores</b> de las celdas; las fórmulas nunca se
 *       ejecutan (se lee el último resultado guardado).</li>
 * </ul>
 */
public final class LectorExcel {

    static final int MAX_ENTRADAS = 500;
    static final long MAX_BYTES_ENTRADA = 10L * 1024 * 1024;
    static final long MAX_BYTES_TOTAL = 30L * 1024 * 1024;

    private LectorExcel() {
    }

    /** Lee todas las filas de la primera hoja (la primera fila es el encabezado). */
    public static List<String[]> leer(byte[] datos, int maxFilas) {
        if (datos.length < 4 || datos[0] != 'P' || datos[1] != 'K' || datos[2] != 3 || datos[3] != 4) {
            throw new ReglaNegocioException("El archivo no es un Excel .xlsx válido.");
        }
        Map<String, byte[]> partes = descomprimir(datos);
        List<String> compartidas = partes.containsKey("xl/sharedStrings.xml")
                ? textosCompartidos(partes.get("xl/sharedStrings.xml")) : List.of();
        String hoja = rutaPrimeraHoja(partes);
        byte[] xmlHoja = partes.get(hoja);
        if (xmlHoja == null) {
            throw new ReglaNegocioException("El Excel no contiene hojas legibles.");
        }
        return filas(xmlHoja, compartidas, maxFilas);
    }

    // ---------------------------------------------------------------- ZIP

    private static Map<String, byte[]> descomprimir(byte[] datos) {
        Map<String, byte[]> partes = new HashMap<>();
        long total = 0;
        int entradas = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(datos))) {
            ZipEntry e;
            byte[] buf = new byte[8192];
            while ((e = zip.getNextEntry()) != null) {
                if (++entradas > MAX_ENTRADAS) {
                    throw new ReglaNegocioException("El Excel tiene demasiadas partes internas.");
                }
                String nombre = e.getName();
                boolean necesaria = !e.isDirectory() && nombre.startsWith("xl/")
                        && (nombre.endsWith(".xml") || nombre.endsWith(".rels"))
                        && !nombre.contains("..");
                if (!necesaria) continue;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int n;
                long leidos = 0;
                while ((n = zip.read(buf)) > 0) {
                    leidos += n;
                    total += n;
                    if (leidos > MAX_BYTES_ENTRADA || total > MAX_BYTES_TOTAL) {
                        throw new ReglaNegocioException("El Excel es demasiado grande al descomprimirlo.");
                    }
                    out.write(buf, 0, n);
                }
                partes.put(nombre, out.toByteArray());
            }
        } catch (IOException ex) {
            throw new ReglaNegocioException("El Excel está dañado o no se puede leer.");
        }
        return partes;
    }

    // ---------------------------------------------------------------- XML seguro

    /** Fábrica StAX sin DTD ni entidades externas (prevención de XXE). */
    static XMLInputFactory fabricaSegura() {
        XMLInputFactory f = XMLInputFactory.newInstance();
        f.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        return f;
    }

    private static XMLStreamReader lector(byte[] xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml);
        return fabricaSegura().createXMLStreamReader(in, "UTF-8");
    }

    private static void rechazarDtd(int evento) {
        if (evento == XMLStreamConstants.DTD) {
            throw new ReglaNegocioException("El Excel contiene declaraciones DOCTYPE no permitidas.");
        }
    }

    /** Ruta de la primera hoja según workbook.xml y su archivo de relaciones. */
    private static String rutaPrimeraHoja(Map<String, byte[]> partes) {
        String porDefecto = "xl/worksheets/sheet1.xml";
        byte[] libro = partes.get("xl/workbook.xml");
        byte[] rels = partes.get("xl/_rels/workbook.xml.rels");
        if (libro == null || rels == null) return porDefecto;
        try {
            String idRel = null;
            XMLStreamReader r = lector(libro);
            while (r.hasNext() && idRel == null) {
                int ev = r.next();
                rechazarDtd(ev);
                if (ev == XMLStreamConstants.START_ELEMENT && "sheet".equals(r.getLocalName())) {
                    for (int i = 0; i < r.getAttributeCount(); i++) {
                        if ("id".equals(r.getAttributeLocalName(i))) idRel = r.getAttributeValue(i);
                    }
                }
            }
            if (idRel == null) return porDefecto;
            XMLStreamReader rr = lector(rels);
            while (rr.hasNext()) {
                int ev = rr.next();
                rechazarDtd(ev);
                if (ev == XMLStreamConstants.START_ELEMENT && "Relationship".equals(rr.getLocalName())
                        && idRel.equals(rr.getAttributeValue(null, "Id"))) {
                    String destino = rr.getAttributeValue(null, "Target");
                    if (destino == null || destino.contains("..")) return porDefecto;
                    return destino.startsWith("/") ? destino.substring(1) : "xl/" + destino;
                }
            }
        } catch (XMLStreamException e) {
            throw new ReglaNegocioException("El Excel tiene un XML interno inválido.");
        }
        return porDefecto;
    }

    private static List<String> textosCompartidos(byte[] xml) {
        List<String> lista = new ArrayList<>();
        try {
            XMLStreamReader r = lector(xml);
            StringBuilder actual = null;
            boolean enTexto = false;
            boolean enFonetica = false;
            while (r.hasNext()) {
                int ev = r.next();
                rechazarDtd(ev);
                if (ev == XMLStreamConstants.START_ELEMENT) {
                    if ("si".equals(r.getLocalName())) actual = new StringBuilder();
                    else if ("rPh".equals(r.getLocalName())) enFonetica = true;
                    else if ("t".equals(r.getLocalName())) enTexto = !enFonetica;
                } else if (ev == XMLStreamConstants.CHARACTERS && enTexto && actual != null) {
                    actual.append(r.getText());
                } else if (ev == XMLStreamConstants.END_ELEMENT) {
                    if ("t".equals(r.getLocalName())) enTexto = false;
                    else if ("rPh".equals(r.getLocalName())) enFonetica = false;
                    else if ("si".equals(r.getLocalName()) && actual != null) {
                        lista.add(actual.toString());
                        actual = null;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new ReglaNegocioException("El Excel tiene un XML interno inválido.");
        }
        return lista;
    }

    private static List<String[]> filas(byte[] xml, List<String> compartidas, int maxFilas) {
        List<String[]> filas = new ArrayList<>();
        try {
            XMLStreamReader r = lector(xml);
            TreeMap<Integer, String> fila = null;
            String tipo = null;
            int columna = -1;
            StringBuilder valor = new StringBuilder();
            boolean capturar = false;
            while (r.hasNext()) {
                int ev = r.next();
                rechazarDtd(ev);
                if (ev == XMLStreamConstants.START_ELEMENT) {
                    switch (r.getLocalName()) {
                        case "row":
                            fila = new TreeMap<>();
                            break;
                        case "c":
                            tipo = r.getAttributeValue(null, "t");
                            String ref = r.getAttributeValue(null, "r");
                            columna = ref == null ? (fila == null || fila.isEmpty() ? 0 : fila.lastKey() + 1)
                                    : indiceColumna(ref);
                            valor.setLength(0);
                            break;
                        case "v":
                        case "t":
                            capturar = true;
                            break;
                        default:
                            break;
                    }
                } else if (ev == XMLStreamConstants.CHARACTERS && capturar) {
                    valor.append(r.getText());
                } else if (ev == XMLStreamConstants.END_ELEMENT) {
                    switch (r.getLocalName()) {
                        case "v":
                        case "t":
                            capturar = false;
                            break;
                        case "c":
                            if (fila != null && columna >= 0 && columna < 200) {
                                fila.put(columna, interpretar(tipo, valor.toString(), compartidas));
                            }
                            break;
                        case "row":
                            if (fila != null) {
                                agregar(filas, fila, maxFilas);
                            }
                            fila = null;
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new ReglaNegocioException("La hoja del Excel tiene un XML inválido.");
        }
        return filas;
    }

    private static void agregar(List<String[]> filas, TreeMap<Integer, String> fila, int maxFilas) {
        int ancho = fila.isEmpty() ? 0 : fila.lastKey() + 1;
        String[] arr = new String[ancho];
        for (int i = 0; i < ancho; i++) arr[i] = fila.getOrDefault(i, "");
        boolean vacia = fila.values().stream().allMatch(v -> v.trim().isEmpty());
        if (vacia && filas.isEmpty()) return;
        filas.add(arr);
        if (filas.size() > maxFilas + 1) {
            throw new ReglaNegocioException("El archivo supera el máximo de " + maxFilas + " filas por importación.");
        }
    }

    private static String interpretar(String tipo, String crudo, List<String> compartidas) {
        if ("s".equals(tipo)) {
            try {
                int i = Integer.parseInt(crudo.trim());
                return i >= 0 && i < compartidas.size() ? compartidas.get(i) : "";
            } catch (NumberFormatException e) {
                return "";
            }
        }
        if ("b".equals(tipo)) return "1".equals(crudo.trim()) ? "VERDADERO" : "FALSO";
        if (crudo.matches("-?\\d+\\.0+")) return crudo.substring(0, crudo.indexOf('.')); // 12.0 → 12
        return crudo;
    }

    /** "B12" → 1 ; "AA3" → 26. */
    static int indiceColumna(String referencia) {
        int n = 0;
        for (char c : referencia.toCharArray()) {
            if (c < 'A' || c > 'Z') break;
            n = n * 26 + (c - 'A' + 1);
        }
        return n - 1;
    }
}
