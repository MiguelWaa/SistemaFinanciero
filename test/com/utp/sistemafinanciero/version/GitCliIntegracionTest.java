package com.utp.sistemafinanciero.version;

import com.utp.sistemafinanciero.dao.memoria.VersionEsquemaDaoMemoria;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Prueba con un repositorio Git <b>real</b> creado en una carpeta temporal
 * (rama, commit, etiqueta y merge). Se omite si git no está instalado.
 * Detecta diferencias de formato entre subcomandos de git (por ejemplo,
 * {@code log} usa {@code %x1f} y {@code for-each-ref} usa {@code %1f}).
 */
public class GitCliIntegracionTest {

    private static Path repo;

    private static void git(String... args) throws Exception {
        String[] cmd = new String[args.length + 5];
        cmd[0] = "git"; cmd[1] = "-c"; cmd[2] = "user.name=Prueba"; cmd[3] = "-c"; cmd[4] = "user.email=prueba@sigsf.local";
        System.arraycopy(args, 0, cmd, 5, args.length);
        Process p = new ProcessBuilder(cmd).directory(repo.toFile()).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        assertTrue(p.waitFor(10, TimeUnit.SECONDS));
        assertEquals("git " + String.join(" ", args), 0, p.exitValue());
    }

    @BeforeClass
    public static void crearRepositorio() throws Exception {
        repo = Files.createTempDirectory("sigsf-git");
        try {
            Process p = new ProcessBuilder("git", "--version").redirectErrorStream(true).start();
            Assume.assumeTrue(p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0);
        } catch (java.io.IOException sinGit) {
            Assume.assumeTrue("git no está instalado", false);
        }
        git("init", "-q", "-b", "main");
        git("config", "commit.gpgsign", "false");
        git("config", "tag.gpgsign", "false");
        Files.writeString(repo.resolve("VERSION"), "5.0.0\n");
        git("add", "VERSION");
        git("commit", "-q", "-m", "feat: versión inicial");
        git("tag", "-a", "v5.0.0", "-m", "SIGSF 5.0.0");
        git("switch", "-q", "-c", "feature/demo");
        Files.writeString(repo.resolve("A.txt"), "a");
        git("add", "A.txt");
        git("commit", "-q", "-m", "docs: archivo A");
        git("switch", "-q", "main");
        git("merge", "-q", "--no-ff", "feature/demo", "-m", "Merge branch 'feature/demo'");
    }

    private VersionService servicio() {
        return new VersionService(new GitCli(repo), new VersionEsquemaDaoMemoria(), repo, new Properties());
    }

    @Test
    public void leeRamaCommitYEtiquetaReales() {
        InfoVersion v = servicio().actual();
        assertEquals("git", v.getOrigen());
        assertEquals("main", v.getRama());
        assertEquals(40, v.getCommit().length());
        assertTrue(v.getEtiqueta().startsWith("v5.0.0"));
        assertEquals("Prueba", v.getAutor());
    }

    @Test
    public void listaRamasYEtiquetasConSusCampos() {
        List<Map<String, Object>> ramas = servicio().ramas();
        assertEquals(2, ramas.size());
        assertTrue(ramas.stream().anyMatch(r -> "feature/demo".equals(r.get("nombre"))));
        assertFalse(((String) ramas.get(0).get("commit")).isEmpty());
        List<Map<String, Object>> tags = servicio().etiquetas();
        assertEquals("v5.0.0", tags.get(0).get("nombre"));
        assertEquals("SIGSF 5.0.0", tags.get(0).get("descripcion"));
    }

    @Test
    public void historialYGrafoIncluyenElMerge() {
        List<Map<String, Object>> c = servicio().commits(10);
        assertEquals(3, c.size());
        assertEquals("merge", c.get(0).get("tipo"));
        assertTrue(servicio().grafo(10).stream().anyMatch(l -> l.contains("|\\")));
    }
}
