package com.utp.sistemafinanciero.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Lee el repositorio Git ejecutando el comando {@code git} del sistema.
 *
 * <p><b>Seguridad</b> (OWASP A03 Inyección de comandos):</p>
 * <ul>
 *   <li>Se usa {@link ProcessBuilder} con argumentos separados: nunca se
 *       arma una línea de comandos ni se usa una shell.</li>
 *   <li>Solo se permiten subcomandos de <b>lectura</b> (lista blanca).</li>
 *   <li>Los argumentos los fija el código; ningún dato del usuario llega
 *       al comando.</li>
 *   <li>Tiempo máximo de 5 s y salida limitada a 512 KB.</li>
 * </ul>
 */
public class GitCli implements FuenteGit {

    private static final Logger LOG = LoggerFactory.getLogger(GitCli.class);
    private static final Set<String> SOLO_LECTURA = Set.of(
            "rev-parse", "log", "describe", "status", "for-each-ref", "branch", "tag");
    private static final int MAX_BYTES = 512 * 1024;

    private final Path raiz;
    private Boolean disponible;

    public GitCli(Path raiz) {
        this.raiz = raiz.toAbsolutePath().normalize();
    }

    @Override
    public synchronized boolean disponible() {
        if (disponible == null) {
            disponible = Files.exists(raiz.resolve(".git"))
                    && ejecutar("rev-parse", "--is-inside-work-tree").map("true"::equals).orElse(false);
            if (!disponible) {
                LOG.info("Sin repositorio Git en {}: la versión se toma de version.properties o VERSION.", raiz);
            }
        }
        return disponible;
    }

    @Override
    public Optional<String> ejecutar(String... argumentos) {
        if (argumentos.length == 0 || !SOLO_LECTURA.contains(argumentos[0])) {
            throw new IllegalArgumentException("Subcomando de Git no permitido: "
                    + (argumentos.length == 0 ? "(vacío)" : argumentos[0]));
        }
        List<String> cmd = new ArrayList<>();
        cmd.add("git");
        cmd.add("-c");
        cmd.add("core.quotepath=false");
        cmd.addAll(List.of(argumentos));
        try {
            Process p = new ProcessBuilder(cmd).directory(raiz.toFile())
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            p.getOutputStream().close();
            byte[] salida = leerLimitado(p.getInputStream());
            if (!p.waitFor(5, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return Optional.empty();
            }
            if (p.exitValue() != 0) {
                return Optional.empty();
            }
            // Solo se quitan los saltos de línea finales: trim() borraría también
            // el separador 0x1F de los campos vacíos (ej. commit raíz sin padres).
            return Optional.of(new String(salida, StandardCharsets.UTF_8).replaceAll("[\\r\\n]+$", ""));
        } catch (IOException e) {
            return Optional.empty();               // git no instalado
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private static byte[] leerLimitado(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            if (out.size() + n > MAX_BYTES) {
                out.write(buf, 0, MAX_BYTES - out.size());
                break;
            }
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}
