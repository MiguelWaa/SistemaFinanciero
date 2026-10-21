package com.utp.sistemafinanciero.version;

import com.utp.sistemafinanciero.dao.VersionEsquemaDao;
import com.utp.sistemafinanciero.model.sistema.VersionEsquema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de <b>control de versiones</b> del SIGSF: informa qué versión
 * se está ejecutando y expone el historial del repositorio Git (commits,
 * ramas, etiquetas y grafo «network») junto con la versión del esquema de
 * la base de datos y los lanzamientos del CHANGELOG.
 *
 * <p>La versión se resuelve en este orden:</p>
 * <ol>
 *   <li><b>git</b>: si el proyecto es un repositorio (NetBeans / desarrollo).</li>
 *   <li><b>version.properties</b>: lo genera Ant al compilar (build.xml) y
 *       viaja dentro del .jar (despliegue sin carpeta .git).</li>
 *   <li><b>VERSION</b>: archivo de texto de la raíz del proyecto.</li>
 * </ol>
 */
public class VersionService {

    /** Separador de campos en la salida de git (carácter US, 0x1F). */
    private static final String SEP = "\u001f";
    /** El mismo separador escrito como escape de formato de git log (seguro en Windows). */
    private static final String F = "%x1f";
    /** En git for-each-ref el escape hexadecimal se escribe sin la «x». */
    private static final String FR = "%1f";
    private static final Pattern LANZAMIENTO = Pattern.compile("^## \\[([^\\]]+)\\](?:\\s*-\\s*(.*))?$");

    private final FuenteGit git;
    private final VersionEsquemaDao esquema;
    private final Path raiz;
    private final Properties compilacion;

    public VersionService(FuenteGit git, VersionEsquemaDao esquema, Path raiz) {
        this(git, esquema, raiz, cargarCompilacion());
    }

    /** Constructor para pruebas: permite simular el version.properties. */
    public VersionService(FuenteGit git, VersionEsquemaDao esquema, Path raiz, Properties compilacion) {
        this.git = git;
        this.esquema = esquema;
        this.raiz = raiz;
        this.compilacion = compilacion;
    }

    // ===================== Versión actual =====================

    public InfoVersion actual() {
        String version = leerArchivoVersion().orElse(compilacion.getProperty("version", "desconocida"));
        String[] esq = versionEsquema();

        if (git.disponible()) {
            String rama = git.ejecutar("rev-parse", "--abbrev-ref", "HEAD").orElse("?");
            String[] c = git.ejecutar("log", "-1", "--date=iso-strict",
                    "--format=%H" + F + "%an" + F + "%ad" + F + "%s").orElse("").split(SEP, -1);
            String etiqueta = git.ejecutar("describe", "--tags", "--always").orElse(null);
            boolean sucio = !git.ejecutar("status", "--porcelain", "--untracked-files=no").orElse("").isEmpty();
            return new InfoVersion(version, rama, campo(c, 0), etiqueta, campo(c, 2), campo(c, 1), campo(c, 3),
                    sucio, compilacion.getProperty("fecha.compilacion"), "git", esq[0], esq[1]);
        }
        if (!compilacion.getProperty("commit", "").isBlank()) {
            return new InfoVersion(compilacion.getProperty("version", version), compilacion.getProperty("rama"),
                    compilacion.getProperty("commit"), compilacion.getProperty("etiqueta"),
                    compilacion.getProperty("fecha.commit"), compilacion.getProperty("autor"),
                    compilacion.getProperty("mensaje"), false, compilacion.getProperty("fecha.compilacion"),
                    "version.properties", esq[0], esq[1]);
        }
        return new InfoVersion(version, null, null, null, null, null, null, false, null, "VERSION", esq[0], esq[1]);
    }

    // ===================== Historial (solo ADMIN) =====================

    /** Todo lo que muestra la pantalla «Control de versiones». */
    public Map<String, Object> detalle() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("actual", actual());
        d.put("repositorio", git.disponible());
        d.put("commits", commits(40));
        d.put("ramas", ramas());
        d.put("etiquetas", etiquetas());
        d.put("grafo", grafo(60));
        d.put("esquema", esquema.listar());
        d.put("lanzamientos", lanzamientos());
        return d;
    }

    public List<Map<String, Object>> commits(int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        Optional<String> salida = git.disponible() ? git.ejecutar("log", "--all", "--topo-order", "-n", String.valueOf(limite),
                "--date=iso-strict", "--format=%h" + F + "%an" + F + "%ad" + F + "%s" + F + "%D" + F + "%p")
                : Optional.empty();
        for (String linea : lineas(salida)) {
            String[] f = linea.split(SEP, -1);
            if (f.length < 6) continue;
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("hash", f[0]);
            c.put("autor", f[1]);
            c.put("fecha", f[2]);
            c.put("mensaje", f[3]);
            c.put("referencias", f[4]);
            c.put("padres", f[5]);
            c.put("tipo", tipoCommit(f[3], f[5]));
            lista.add(c);
        }
        return lista;
    }

    public List<Map<String, Object>> ramas() {
        return referencias("refs/heads", "refs/remotes", "-committerdate");
    }

    public List<Map<String, Object>> etiquetas() {
        return referencias("refs/tags", null, "-creatordate");
    }

    /** Grafo de texto (como {@code git log --graph}) para la vista «Network». */
    public List<String> grafo(int limite) {
        if (!git.disponible()) return List.of();
        return lineas(git.ejecutar("log", "--all", "--graph", "--color=never", "-n", String.valueOf(limite),
                "--format=%h %s%d"));
    }

    /** Lanzamientos registrados en CHANGELOG.md ({@code ## [x.y.z] - descripción}). */
    public List<Map<String, Object>> lanzamientos() {
        List<Map<String, Object>> lista = new ArrayList<>();
        Path archivo = raiz.resolve("CHANGELOG.md");
        if (!Files.isReadable(archivo)) return lista;
        try {
            Map<String, Object> actual = null;
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                Matcher m = LANZAMIENTO.matcher(linea.trim());
                if (m.matches()) {
                    actual = new LinkedHashMap<>();
                    actual.put("version", m.group(1));
                    actual.put("titulo", m.group(2) == null ? "" : m.group(2).trim());
                    actual.put("cambios", new ArrayList<String>());
                    lista.add(actual);
                } else if (actual != null && linea.startsWith("- ")) {
                    @SuppressWarnings("unchecked")
                    List<String> cambios = (List<String>) actual.get("cambios");
                    cambios.add(linea.substring(2).trim());
                }
            }
        } catch (IOException e) {
            return lista;
        }
        return lista;
    }

    /** Clasifica el commit según Conventional Commits (para colorearlo en la vista). */
    static String tipoCommit(String mensaje, String padres) {
        if (padres != null && padres.trim().contains(" ")) return "merge";
        if (mensaje == null) return "otro";
        if (mensaje.startsWith("Revert") || mensaje.startsWith("revert")) return "revert";
        int i = mensaje.indexOf(':');
        if (i <= 0) return "otro";
        String tipo = mensaje.substring(0, i).replaceAll("\\(.*\\)", "").replace("!", "").trim();
        return tipo.matches("[a-z]+") ? tipo : "otro";
    }

    // ===================== Apoyo =====================

    private List<Map<String, Object>> referencias(String ref1, String ref2, String orden) {
        List<Map<String, Object>> lista = new ArrayList<>();
        if (!git.disponible()) return lista;
        String formato = "--format=%(refname:short)" + FR + "%(objectname:short)" + FR
                + "%(creatordate:iso-strict)" + FR + "%(contents:subject)";
        Optional<String> salida = ref2 == null
                ? git.ejecutar("for-each-ref", "--sort=" + orden, formato, ref1)
                : git.ejecutar("for-each-ref", "--sort=" + orden, formato, ref1, ref2);
        for (String linea : lineas(salida)) {
            String[] f = linea.split(SEP, -1);
            if (f.length < 4 || f[0].endsWith("/HEAD") || "origin".equals(f[0])) continue;
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("nombre", f[0]);
            r.put("commit", f[1]);
            r.put("fecha", f[2]);
            r.put("descripcion", f[3]);
            r.put("remota", f[0].startsWith("origin/"));
            lista.add(r);
        }
        return lista;
    }

    private String[] versionEsquema() {
        String actual = null;
        try {
            VersionEsquema v = esquema.actual();
            actual = v == null ? null : v.getVersion();
        } catch (RuntimeException sinTabla) {
            // La base aún no tiene la tabla version_esquema.
        }
        return new String[]{actual, HistorialEsquema.esperada()};
    }

    private Optional<String> leerArchivoVersion() {
        try {
            Path f = raiz.resolve("VERSION");
            if (Files.isReadable(f)) {
                String v = Files.readString(f, StandardCharsets.UTF_8).trim();
                return v.isEmpty() ? Optional.empty() : Optional.of(v);
            }
        } catch (IOException ignore) {
            // se usa version.properties
        }
        return Optional.empty();
    }

    private static Properties cargarCompilacion() {
        Properties p = new Properties();
        try (InputStream in = VersionService.class.getResourceAsStream("/version.properties")) {
            if (in != null) {
                p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException ignore) {
            // sin datos de compilación
        }
        return p;
    }

    private static List<String> lineas(Optional<String> salida) {
        List<String> l = new ArrayList<>();
        salida.filter(s -> !s.isBlank()).ifPresent(s -> l.addAll(List.of(s.split("\\R"))));
        return l;
    }

    private static String campo(String[] f, int i) {
        return i < f.length && !f[i].isEmpty() ? f[i] : null;
    }
}
