package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.archivos.ArchivoGenerado;
import com.utp.sistemafinanciero.archivos.Columna;
import com.utp.sistemafinanciero.archivos.DefinicionArchivo;
import com.utp.sistemafinanciero.archivos.EscritorCsv;
import com.utp.sistemafinanciero.archivos.FormatoArchivo;
import com.utp.sistemafinanciero.archivos.LectorCsv;
import com.utp.sistemafinanciero.archivos.LectorExcel;
import com.utp.sistemafinanciero.archivos.TipoDato;
import com.utp.sistemafinanciero.dto.ResultadoImportacionDTO;
import com.utp.sistemafinanciero.util.ExcelExporter;
import com.utp.sistemafinanciero.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio de <b>exportación e importación de ficheros</b> de todas las
 * áreas del negocio (Marketing, Ventas, Logística, Almacén, Producción,
 * Operaciones, Dirección y Gerencia, Clientes y Seguridad).
 *
 * <h2>Exportar</h2>
 * Genera el archivo (XLSX, CSV o JSON) con las columnas definidas en la
 * {@link DefinicionArchivo} del recurso.
 *
 * <h2>Importar (CSV o XLSX)</h2>
 * <ol>
 *   <li>Valida extensión, firma del archivo y tamaño.</li>
 *   <li>Lee las filas ({@link LectorCsv} o {@link LectorExcel}).</li>
 *   <li>Relaciona los encabezados con las columnas importables (sin
 *       importar mayúsculas, tildes ni espacios). Las columnas desconocidas
 *       o calculadas por el servidor (id, fechas de registro, totales) se
 *       <b>ignoran</b> y se informan.</li>
 *   <li>Cada fila se convierte con el {@code MapeadorEntrada} y se registra
 *       con el servicio del área: se aplican las mismas reglas que en la
 *       pantalla. Las filas con error se informan con su número.</li>
 * </ol>
 * Con {@code soloValidar = true} se hace todo menos registrar (vista previa).
 */
public class ArchivoService {

    private static final Logger LOG = LoggerFactory.getLogger(ArchivoService.class);
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final int MAX_LARGO_CELDA = 500;
    private static final LocalDate ORIGEN_EXCEL = LocalDate.of(1899, 12, 30);

    private final int maxFilas;

    public ArchivoService(int maxFilas) {
        this.maxFilas = Math.max(1, maxFilas);
    }

    public int maxFilas() {
        return maxFilas;
    }

    // ================================================================ EXPORTAR

    public <T> ArchivoGenerado exportar(DefinicionArchivo<T> def, FormatoArchivo formato) {
        List<T> datos = def.datos();
        List<Columna<T>> cols = def.columnas();
        String nombre = def.recurso() + "_" + LocalDateTime.now().format(SELLO) + "." + formato.extension();
        byte[] contenido;
        switch (formato) {
            case CSV:
                contenido = EscritorCsv.generar(titulos(cols), filas(cols, datos), numericas(cols));
                break;
            case JSON:
                List<Map<String, Object>> lista = new ArrayList<>();
                for (T fila : datos) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    for (Columna<T> c : cols) m.put(c.clave(), c.valor(fila));
                    lista.add(m);
                }
                contenido = Json.stringify(Json.map("recurso", def.recurso(), "area", def.area().nombre(),
                        "generado", LocalDateTime.now().withNano(0), "total", lista.size(), "datos", lista))
                        .getBytes(StandardCharsets.UTF_8);
                break;
            default:
                contenido = ExcelExporter.generar(def.hoja(), titulos(cols), filas(cols, datos), numericas(cols));
        }
        LOG.info("Exportación {} de {} ({} filas, {} bytes)", formato, def.recurso(), datos.size(), contenido.length);
        return new ArchivoGenerado(nombre, formato.contentType(), contenido);
    }

    /** Plantilla CSV para importar: encabezados de las columnas importables (con * las obligatorias) y una fila de ejemplo. */
    public <T> ArchivoGenerado plantilla(DefinicionArchivo<T> def) {
        exigirImportable(def);
        List<String> titulos = new ArrayList<>();
        List<String> ejemplo = new ArrayList<>();
        List<Boolean> num = new ArrayList<>();
        for (Columna<T> c : def.columnas()) {
            if (!c.importable()) continue;
            titulos.add(c.titulo() + (c.obligatoria() ? " *" : ""));
            ejemplo.add(c.ejemplo());
            num.add(c.tipo() == TipoDato.NUMERO);
        }
        boolean[] numericas = new boolean[num.size()];
        for (int i = 0; i < numericas.length; i++) numericas[i] = num.get(i);
        byte[] csv = EscritorCsv.generar(titulos.toArray(new String[0]),
                List.<String[]>of(ejemplo.toArray(new String[0])), numericas);
        return new ArchivoGenerado("plantilla_" + def.recurso() + ".csv", FormatoArchivo.CSV.contentType(), csv);
    }

    // ================================================================ IMPORTAR

    public <T> ResultadoImportacionDTO importar(DefinicionArchivo<T> def, String nombreArchivo, byte[] datos,
                                               boolean soloValidar) {
        exigirImportable(def);
        if (datos == null || datos.length == 0) {
            throw new ReglaNegocioException("El archivo está vacío.");
        }
        FormatoArchivo formato = FormatoArchivo.desdeNombreArchivo(nombreArchivo);
        List<String[]> filas = formato == FormatoArchivo.XLSX
                ? LectorExcel.leer(datos, maxFilas) : LectorCsv.leer(datos, maxFilas);
        if (filas.isEmpty()) {
            throw new ReglaNegocioException("El archivo no tiene encabezado ni datos.");
        }

        ResultadoImportacionDTO res = new ResultadoImportacionDTO(def.recurso(), nombreSeguro(nombreArchivo),
                formato.name(), soloValidar);
        Map<Integer, Columna<T>> mapa = relacionarEncabezados(def, filas.get(0), res);

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            int numeroFila = i + 1; // como se ve en Excel (la 1 es el encabezado)
            if (vacia(fila)) continue;
            res.sumarLeida();
            try {
                Map<String, Object> valores = valores(fila, mapa);
                for (Columna<T> c : mapa.values()) {
                    if (c.obligatoria() && !valores.containsKey(c.clave())) {
                        throw new ReglaNegocioException("Falta el valor obligatorio «" + c.titulo() + "».");
                    }
                }
                T entidad = def.mapear(valores);
                res.sumarValida();
                if (!soloValidar) {
                    def.registrar(entidad);
                    res.sumarImportada();
                }
            } catch (ReglaNegocioException e) {
                res.rechazar(numeroFila, e.getMessage());
            } catch (NumberFormatException e) {
                res.rechazar(numeroFila, "Hay un valor numérico no válido (use punto decimal, sin símbolos).");
            } catch (RuntimeException e) {
                res.rechazar(numeroFila, mensajeTecnico(e));
                LOG.warn("Fila {} de {} rechazada por error técnico: {}", numeroFila, def.recurso(), e.toString());
            }
        }
        LOG.info("Importación {} de {} archivo={} leídas={} válidas={} importadas={} rechazadas={}",
                soloValidar ? "(validación)" : "", def.recurso(), res.getArchivo(), res.getFilasLeidas(),
                res.getFilasValidas(), res.getFilasImportadas(), res.getFilasRechazadas());
        return res;
    }

    private <T> Map<Integer, Columna<T>> relacionarEncabezados(DefinicionArchivo<T> def, String[] encabezado,
                                                             ResultadoImportacionDTO res) {
        Map<String, Columna<T>> porNombre = new HashMap<>();
        for (Columna<T> c : def.columnas()) {
            if (!c.importable()) continue;
            porNombre.put(normalizar(c.clave()), c);
            porNombre.put(normalizar(c.titulo()), c);
        }
        Map<Integer, Columna<T>> mapa = new LinkedHashMap<>();
        for (int i = 0; i < encabezado.length; i++) {
            String original = encabezado[i] == null ? "" : encabezado[i].trim();
            if (original.isEmpty()) continue;
            Columna<T> c = porNombre.get(normalizar(original));
            if (c != null && !mapa.containsValue(c)) {
                mapa.put(i, c);
                res.getColumnasReconocidas().add(c.titulo());
            } else {
                res.getColumnasIgnoradas().add(recortar(original, 40));
            }
        }
        List<String> faltan = new ArrayList<>();
        for (Columna<T> c : def.columnas()) {
            if (c.obligatoria() && !mapa.containsValue(c)) faltan.add(c.titulo());
        }
        if (!faltan.isEmpty()) {
            throw new ReglaNegocioException("Faltan columnas obligatorias en el encabezado: "
                    + String.join(", ", faltan) + ". Descargue la plantilla del módulo.");
        }
        if (mapa.isEmpty()) {
            throw new ReglaNegocioException("Ninguna columna del archivo coincide con la plantilla del módulo.");
        }
        return mapa;
    }

    private <T> Map<String, Object> valores(String[] fila, Map<Integer, Columna<T>> mapa) {
        Map<String, Object> m = new HashMap<>();
        for (Map.Entry<Integer, Columna<T>> e : mapa.entrySet()) {
            int i = e.getKey();
            if (i >= fila.length || fila[i] == null) continue;
            String v = fila[i].trim();
            if (v.startsWith("'")) v = v.substring(1); // apóstrofo de protección de fórmulas
            if (v.isEmpty()) continue;
            Columna<T> c = e.getValue();
            if (v.length() > MAX_LARGO_CELDA) {
                throw new ReglaNegocioException("El valor de «" + c.titulo() + "» supera "
                        + MAX_LARGO_CELDA + " caracteres.");
            }
            if (c.tipo() == TipoDato.FECHA && v.matches("\\d{1,6}(\\.0+)?")) {
                v = ORIGEN_EXCEL.plusDays(Long.parseLong(v.replaceAll("\\..*", ""))).toString(); // fecha serial de Excel
            } else if (c.tipo() == TipoDato.FECHA && v.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] p = v.split("/"); // dd/mm/aaaa
                v = String.format("%s-%02d-%02d", p[2], Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            } else if (c.tipo() == TipoDato.NUMERO) {
                v = v.replace(" ", "");
                if (!v.matches("-?\\d+(\\.\\d+)?")) {
                    throw new ReglaNegocioException("«" + c.titulo() + "» debe ser un número (use punto decimal): "
                            + recortar(v, 20));
                }
            }
            m.put(c.clave(), v);
        }
        return m;
    }

    // ================================================================ apoyo

    private static <T> String[] titulos(List<Columna<T>> cols) {
        return cols.stream().map(Columna::titulo).toArray(String[]::new);
    }

    private static <T> List<String[]> filas(List<Columna<T>> cols, List<T> datos) {
        List<String[]> filas = new ArrayList<>(datos.size());
        for (T d : datos) {
            String[] f = new String[cols.size()];
            for (int i = 0; i < f.length; i++) f[i] = cols.get(i).texto(d);
            filas.add(f);
        }
        return filas;
    }

    private static <T> boolean[] numericas(List<Columna<T>> cols) {
        boolean[] n = new boolean[cols.size()];
        for (int i = 0; i < n.length; i++) n[i] = cols.get(i).tipo() == TipoDato.NUMERO;
        return n;
    }

    private static void exigirImportable(DefinicionArchivo<?> def) {
        if (!def.importable()) {
            throw new ReglaNegocioException("El módulo «" + def.recurso() + "» no admite importación de archivos: "
                    + "sus registros solo se crean desde el sistema.");
        }
    }

    /** "Razón social *" → "razonsocial" ; "razonSocial" → "razonsocial". */
    static String normalizar(String s) {
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static boolean vacia(String[] fila) {
        for (String v : fila) if (v != null && !v.trim().isEmpty()) return false;
        return true;
    }

    private static String nombreSeguro(String nombre) {
        String n = nombre == null ? "archivo" : nombre.replaceAll("[\\\\/]", "_").replaceAll("[^\\p{L}\\p{N}._ -]", "");
        return recortar(n, 80);
    }

    private static String recortar(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    /** Traduce errores de la base a mensajes claros (sin exponer SQL). */
    private static String mensajeTecnico(Throwable t) {
        Throwable x = t;
        while (x != null) {
            if (x instanceof SQLException) {
                String estado = ((SQLException) x).getSQLState();
                if ("23505".equals(estado)) return "Registro duplicado (ya existe un valor único igual).";
                if ("23503".equals(estado)) return "Hace referencia a un registro que no existe.";
                if ("23514".equals(estado)) return "Algún valor no cumple las reglas de la base de datos.";
                return "No se pudo guardar en la base de datos.";
            }
            x = x.getCause();
        }
        return "No se pudo registrar la fila.";
    }
}
