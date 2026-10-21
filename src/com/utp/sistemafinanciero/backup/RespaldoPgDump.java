package com.utp.sistemafinanciero.backup;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Respaldo completo con la herramienta oficial {@code pg_dump}: genera un
 * script SQL plano con la estructura (tablas, índices, restricciones) y
 * los datos. Se restaura con {@code psql -f archivo.sql} o desde el Query
 * Tool de pgAdmin (ver {@code scripts/restaurar_bd.bat}).
 *
 * <p>La contraseña se pasa por la variable de entorno {@code PGPASSWORD}
 * del proceso hijo, nunca por la línea de comandos (no queda visible en
 * la lista de procesos del sistema).</p>
 */
public class RespaldoPgDump implements EstrategiaRespaldo {

    private final String ejecutable;
    private final String host;
    private final int puerto;
    private final String base;
    private final String usuario;
    private final String clave;

    public RespaldoPgDump(String rutaConfigurada, String host, int puerto, String base,
                          String usuario, String clave) {
        this.ejecutable = localizar(rutaConfigurada);
        this.host = host;
        this.puerto = puerto;
        this.base = base;
        this.usuario = usuario;
        this.clave = clave;
    }

    @Override public String nombre() { return "pgdump"; }
    @Override public String extension() { return "sql"; }

    @Override
    public boolean disponible() {
        return ejecutable != null;
    }

    @Override
    public void generar(Path destino) throws Exception {
        List<String> cmd = new ArrayList<>(List.of(ejecutable,
                "-h", host, "-p", String.valueOf(puerto), "-U", usuario,
                "--format=plain", "--no-owner", "--no-privileges", "--clean", "--if-exists",
                "--encoding=UTF8", "-f", destino.toString(), base));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().put("PGPASSWORD", clave);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String salida = new String(p.getInputStream().readAllBytes());
        if (!p.waitFor(5, TimeUnit.MINUTES)) {
            p.destroyForcibly();
            throw new IllegalStateException("pg_dump tardó demasiado y fue detenido.");
        }
        if (p.exitValue() != 0) {
            throw new IllegalStateException("pg_dump terminó con error: " + salida.trim());
        }
    }

    /** Busca pg_dump: ruta configurada, PATH o carpetas típicas de Windows/Linux. */
    static String localizar(String configurada) {
        if (configurada != null && !configurada.isBlank() && !"auto".equalsIgnoreCase(configurada)) {
            return Files.isRegularFile(Paths.get(configurada)) ? configurada : null;
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
        String exe = windows ? "pg_dump.exe" : "pg_dump";
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(File.pathSeparator)) {
                Path c = Paths.get(dir, exe);
                if (Files.isRegularFile(c)) return c.toString();
            }
        }
        List<Path> raices = windows
                ? List.of(Paths.get("C:\\Program Files\\PostgreSQL"), Paths.get("C:\\Program Files (x86)\\PostgreSQL"))
                : List.of(Paths.get("/usr/lib/postgresql"));
        for (Path raiz : raices) {
            if (!Files.isDirectory(raiz)) continue;
            try (var versiones = Files.list(raiz)) {
                for (Path v : (Iterable<Path>) versiones.sorted((a, b) -> b.compareTo(a))::iterator) {
                    Path c = v.resolve("bin").resolve(exe);
                    if (Files.isRegularFile(c)) return c.toString();
                }
            } catch (Exception ignore) {
                // se sigue buscando
            }
        }
        return null;
    }
}
