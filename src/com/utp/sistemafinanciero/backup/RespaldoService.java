package com.utp.sistemafinanciero.backup;

import com.utp.sistemafinanciero.dto.RespaldoDTO;
import com.utp.sistemafinanciero.service.ReglaNegocioException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestión de respaldos (backup) de la base de datos.
 *
 * <ul>
 *   <li>Genera respaldos bajo demanda o automáticamente cada N horas.</li>
 *   <li>Usa la primera {@link EstrategiaRespaldo} disponible y, si falla,
 *       intenta con la siguiente (tolerancia a fallos).</li>
 *   <li>Aplica una política de retención: conserva los últimos N archivos.</li>
 *   <li>Valida los nombres de archivo para impedir <i>path traversal</i>.</li>
 * </ul>
 */
public class RespaldoService {

    private static final Logger LOG = LoggerFactory.getLogger(RespaldoService.class);

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("sigsf_\\d{8}_\\d{9}_[a-z]+\\.(sql|json)");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    private final List<EstrategiaRespaldo> estrategias;
    private final Path carpeta;
    private final int retener;
    private ScheduledExecutorService programador;

    public RespaldoService(List<EstrategiaRespaldo> estrategias, Path carpeta, int retener) {
        this.estrategias = estrategias;
        this.carpeta = carpeta.toAbsolutePath().normalize();
        this.retener = Math.max(1, retener);
    }

    /** Genera un respaldo ahora mismo. */
    public synchronized RespaldoDTO generar() {
        List<String> errores = new ArrayList<>();
        for (EstrategiaRespaldo e : estrategias) {
            if (!e.disponible()) {
                errores.add(e.nombre() + ": no disponible");
                continue;
            }
            Path destino = null;
            try {
                Files.createDirectories(carpeta);
                String nombre = "sigsf_" + LocalDateTime.now().format(FORMATO) + "_" + e.nombre() + "." + e.extension();
                destino = carpeta.resolve(nombre);
                e.generar(destino);
                aplicarRetencion();
                RespaldoDTO r = describir(destino);
                LOG.info("Respaldo generado con la estrategia {}: {} ({} bytes)", e.nombre(), r.getNombre(),
                        Files.size(destino));
                return r;
            } catch (Exception ex) {
                LOG.warn("La estrategia de respaldo {} falló: {}", e.nombre(), ex.getMessage());
                errores.add(e.nombre() + ": " + ex.getMessage());
                try {
                    if (destino != null) Files.deleteIfExists(destino);
                } catch (IOException ignore) {
                    // nada que limpiar
                }
            }
        }
        LOG.error("No se pudo generar el respaldo con ninguna estrategia: {}", errores);
        throw new ReglaNegocioException("No se pudo generar el respaldo. " + String.join(" | ", errores));
    }

    /** Respaldos existentes, del más reciente al más antiguo. */
    public List<RespaldoDTO> listar() {
        if (!Files.isDirectory(carpeta)) return List.of();
        try (Stream<Path> s = Files.list(carpeta)) {
            return s.filter(p -> NOMBRE_VALIDO.matcher(p.getFileName().toString()).matches())
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .map(this::describir)
                    .toList();
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer la carpeta de respaldos.");
        }
    }

    /** Contenido de un respaldo para descargarlo. */
    public byte[] leer(String nombre) {
        Path p = resolverSeguro(nombre);
        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer el respaldo " + nombre + ".");
        }
    }

    public void eliminar(String nombre) {
        try {
            Files.delete(resolverSeguro(nombre));
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo eliminar el respaldo " + nombre + ".");
        }
    }

    /** Programa respaldos automáticos cada {@code horas} horas (0 = desactivado). */
    public void programar(int horas) {
        if (horas <= 0) return;
        programador = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sigsf-respaldo-automatico");
            t.setDaemon(true);
            return t;
        });
        programador.scheduleAtFixedRate(() -> {
            try {
                RespaldoDTO r = generar();
                LOG.info("Respaldo automático generado: {}", r.getNombre());
            } catch (Exception e) {
                LOG.error("Falló el respaldo automático: {}", e.getMessage());
            }
        }, horas, horas, TimeUnit.HOURS);
    }

    public Path carpeta() {
        return carpeta;
    }

    /** Valida el nombre y comprueba que el archivo esté dentro de la carpeta de respaldos. */
    private Path resolverSeguro(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new ReglaNegocioException("Nombre de respaldo no válido.");
        }
        Path p = carpeta.resolve(nombre).normalize();
        if (!p.startsWith(carpeta) || !Files.isRegularFile(p)) {
            throw new ReglaNegocioException("No existe el respaldo " + nombre + ".");
        }
        return p;
    }

    private void aplicarRetencion() throws IOException {
        List<RespaldoDTO> todos = listar();
        for (int i = retener; i < todos.size(); i++) {
            Files.deleteIfExists(carpeta.resolve(todos.get(i).getNombre()));
        }
    }

    private RespaldoDTO describir(Path p) {
        try {
            String n = p.getFileName().toString();
            String metodo = n.substring(n.lastIndexOf('_') + 1, n.lastIndexOf('.'));
            FileTime t = Files.getLastModifiedTime(p);
            return new RespaldoDTO(n, Files.size(p),
                    LocalDateTime.ofInstant(t.toInstant(), ZoneId.systemDefault()), metodo);
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer el respaldo.");
        }
    }
}
