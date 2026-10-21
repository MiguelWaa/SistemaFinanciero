package com.utp.sistemafinanciero.version;

import com.utp.sistemafinanciero.dao.memoria.VersionEsquemaDaoMemoria;
import com.utp.sistemafinanciero.model.sistema.VersionEsquema;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * RF-VER-01…04 · Control de versiones: versión en ejecución, historial de
 * Git, lanzamientos del CHANGELOG y versión del esquema de la base.
 * Usa un repositorio Git <b>simulado</b> (no depende de tener git instalado).
 */
public class VersionServiceTest {

    private static final String S = "\u001f";

    /** Git falso: responde según el subcomando. */
    static class GitFalso implements FuenteGit {
        final Map<String, String> respuestas = new HashMap<>();
        boolean disponible = true;
        String ultimoFormatoLog;

        @Override public boolean disponible() { return disponible; }

        @Override public Optional<String> ejecutar(String... a) {
            String clave = a[0];
            if ("rev-parse".equals(clave)) clave = "rama";
            if ("log".equals(clave)) {
                ultimoFormatoLog = a[a.length - 1];
                clave = String.join(" ", a).contains("--graph") ? "grafo"
                        : String.join(" ", a).contains("--all") ? "historial" : "ultimo";
            }
            if ("for-each-ref".equals(clave)) clave = a[a.length - 1].equals("refs/tags") ? "tags" : "ramas";
            return Optional.ofNullable(respuestas.get(clave));
        }
    }

    private GitFalso git;
    private VersionEsquemaDaoMemoria esquema;
    private Path raiz;

    @Before
    public void preparar() throws Exception {
        git = new GitFalso();
        esquema = new VersionEsquemaDaoMemoria();
        raiz = Files.createTempDirectory("sigsf-version");
        Files.writeString(raiz.resolve("VERSION"), "5.0.0\n");
        git.respuestas.put("rama", "develop");
        git.respuestas.put("ultimo", "abc1234def5678" + S + "Ricardo" + S + "2026-10-20T10:00:00-05:00" + S + "feat(version): API");
        git.respuestas.put("describe", "v5.0.0-2-gabc1234");
        git.respuestas.put("status", "");
    }

    private VersionService servicio(Properties compilacion) {
        return new VersionService(git, esquema, raiz, compilacion);
    }

    @Test
    public void conRepositorioGitInformaRamaCommitYEtiqueta() {
        InfoVersion v = servicio(new Properties()).actual();
        assertEquals("5.0.0", v.getVersion());
        assertEquals("develop", v.getRama());
        assertEquals("abc1234", v.getCommitCorto());
        assertEquals("v5.0.0-2-gabc1234", v.getEtiqueta());
        assertEquals("Ricardo", v.getAutor());
        assertEquals("git", v.getOrigen());
        assertFalse(v.isCambiosSinCommit());
    }

    @Test
    public void detectaCambiosSinCommit() {
        git.respuestas.put("status", " M src/Main.java");
        assertTrue(servicio(new Properties()).actual().isCambiosSinCommit());
    }

    @Test
    public void sinGitUsaVersionPropertiesGeneradoAlCompilar() {
        git.disponible = false;
        Properties p = new Properties();
        p.setProperty("version", "5.0.0");
        p.setProperty("commit", "fedcba9876543210");
        p.setProperty("rama", "main");
        p.setProperty("fecha.compilacion", "2026-10-20T12:00:00");
        InfoVersion v = servicio(p).actual();
        assertEquals("version.properties", v.getOrigen());
        assertEquals("fedcba9", v.getCommitCorto());
        assertEquals("main", v.getRama());
    }

    @Test
    public void sinGitNiCompilacionUsaElArchivoVersion() {
        git.disponible = false;
        InfoVersion v = servicio(new Properties()).actual();
        assertEquals("VERSION", v.getOrigen());
        assertEquals("5.0.0", v.getVersion());
        assertNull(v.getCommit());
    }

    @Test
    public void informaSiElEsquemaDeLaBaseEstaAlDia() {
        HistorialEsquema.conocidas().forEach(esquema::registrar);
        InfoVersion v = servicio(new Properties()).actual();
        assertEquals(HistorialEsquema.esperada(), v.getVersionEsquema());
        assertTrue(v.isEsquemaAlDia());
    }

    @Test
    public void esquemaAtrasadoNoEstaAlDia() {
        esquema.registrar(new VersionEsquema("1.0.0", "inicial", "db/schema.sql"));
        assertFalse(servicio(new Properties()).actual().isEsquemaAlDia());
    }

    @Test
    public void noSeRegistraDosVecesLaMismaVersionDelEsquema() {
        assertNotNull(esquema.registrar(new VersionEsquema("1.0.0", "inicial", "db/schema.sql")));
        assertNull(esquema.registrar(new VersionEsquema("1.0.0", "repetida", "db/schema.sql")));
        assertEquals(1, esquema.listar().size());
    }

    @Test
    public void historialClasificaMergeRevertYConventionalCommits() {
        git.respuestas.put("historial", String.join("\n",
                "m1" + S + "Miguel" + S + "f" + S + "Merge branch 'feature/x' into develop" + S + "HEAD -> develop" + S + "a1 b2",
                "r1" + S + "Ricardo" + S + "f" + S + "Revert \"perf(prestamos): redondeo\"" + S + "" + S + "a1",
                "c1" + S + "Cesar" + S + "f" + S + "feat(ui): pantalla versiones" + S + "" + S + "a1",
                "c2" + S + "Cesar" + S + "f" + S + "fix!: corrige puerto" + S + "tag: v5.0.1" + S + "a1"));
        List<Map<String, Object>> c = servicio(new Properties()).commits(10);
        assertEquals(4, c.size());
        assertEquals("merge", c.get(0).get("tipo"));
        assertEquals("revert", c.get(1).get("tipo"));
        assertEquals("feat", c.get(2).get("tipo"));
        assertEquals("fix", c.get(3).get("tipo"));
        assertEquals("tag: v5.0.1", c.get(3).get("referencias"));
    }

    @Test
    public void ramasYEtiquetasOmitenHeadSimbolico() {
        git.respuestas.put("ramas", String.join("\n",
                "develop" + S + "abc" + S + "f" + S + "msg",
                "origin/HEAD" + S + "abc" + S + "f" + S + "msg",
                "origin/develop" + S + "abc" + S + "f" + S + "msg"));
        git.respuestas.put("tags", "v5.0.0" + S + "def" + S + "f" + S + "SIGSF 5.0.0");
        VersionService s = servicio(new Properties());
        assertEquals(2, s.ramas().size());
        assertEquals(Boolean.TRUE, s.ramas().get(1).get("remota"));
        assertEquals("v5.0.0", s.etiquetas().get(0).get("nombre"));
    }

    @Test
    public void leeLanzamientosDelChangelog() throws Exception {
        Files.writeString(raiz.resolve("CHANGELOG.md"), String.join("\n",
                "# Registro de cambios", "", "## [Sin publicar]", "",
                "## [5.0.0] - 2026-10-21", "### Agregado", "- Control de versiones", "- Hooks",
                "## [4.0.0] - Taller de recursos", "- Logback"), StandardCharsets.UTF_8);
        List<Map<String, Object>> l = servicio(new Properties()).lanzamientos();
        assertEquals(3, l.size());
        assertEquals("5.0.0", l.get(1).get("version"));
        assertEquals("2026-10-21", l.get(1).get("titulo"));
        assertEquals(2, ((List<?>) l.get(1).get("cambios")).size());
    }

    @Test
    public void elFormatoDeGitUsaEscapesHexadecimales() {
        servicio(new Properties()).actual();
        assertTrue("el separador debe ir como %x1f (seguro en Windows)", git.ultimoFormatoLog.contains("%x1f"));
        assertFalse(git.ultimoFormatoLog.contains(S));
    }

    @Test(expected = IllegalArgumentException.class)
    public void gitCliRechazaSubcomandosQueModifican() {
        new GitCli(raiz).ejecutar("push", "origin", "main");
    }
}
