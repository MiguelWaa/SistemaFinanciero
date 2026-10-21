package com.utp.sistemafinanciero.backup;

import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.dto.RespaldoDTO;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/** Respaldo (backup): generación, retención, estrategia alternativa y seguridad. */
public class RespaldoServiceTest {

    private Path carpeta;

    @Before
    public void preparar() throws Exception {
        carpeta = Files.createTempDirectory("sigsf-respaldos");
    }

    /** Estrategia falsa para simular que pg_dump no está instalado o falla. */
    private static EstrategiaRespaldo falla(boolean disponible) {
        return new EstrategiaRespaldo() {
            public String nombre() { return "pgdump"; }
            public String extension() { return "sql"; }
            public boolean disponible() { return disponible; }
            public void generar(Path destino) { throw new IllegalStateException("pg_dump falló"); }
        };
    }

    @Test
    public void generaUnArchivoConLosDatos() throws Exception {
        MemoriaDaoFactory bd = new MemoriaDaoFactory();
        RespaldoService s = new RespaldoService(List.of(new RespaldoMemoriaJson(bd)), carpeta, 5);
        RespaldoDTO r = s.generar();
        assertTrue(r.getNombre().matches("sigsf_\\d{8}_\\d{9}_memoria\\.json"));
        assertTrue(r.getBytes() > 0);
        String contenido = new String(s.leer(r.getNombre()), StandardCharsets.UTF_8);
        assertTrue(contenido.contains("\"cliente\""));
    }

    @Test
    public void siLaPrimeraEstrategiaFallaUsaLaSiguiente() {
        RespaldoService s = new RespaldoService(
                List.of(falla(false), falla(true), new RespaldoMemoriaJson(new MemoriaDaoFactory())), carpeta, 5);
        assertEquals("memoria", s.generar().getMetodo());
    }

    @Test
    public void conservaSoloLosUltimosNRespaldos() throws Exception {
        RespaldoService s = new RespaldoService(List.of(new RespaldoMemoriaJson(new MemoriaDaoFactory())), carpeta, 2);
        for (int i = 0; i < 4; i++) {
            s.generar();
            Thread.sleep(5);
        }
        assertEquals(2, s.listar().size());
    }

    @Test
    public void impidePathTraversalAlDescargar() {
        RespaldoService s = new RespaldoService(List.of(new RespaldoMemoriaJson(new MemoriaDaoFactory())), carpeta, 2);
        assertThrows(ReglaNegocioException.class, () -> s.leer("../../db.properties"));
        assertThrows(ReglaNegocioException.class, () -> s.leer("sigsf_20260101_000000000_memoria.json"));
    }

    @Test
    public void literalSqlEscapaComillas() {
        assertEquals("'O''Brien'", RespaldoJdbcSql.literal("O'Brien", java.sql.Types.VARCHAR));
        assertEquals("NULL", RespaldoJdbcSql.literal(null, java.sql.Types.VARCHAR));
        assertEquals("12.50", RespaldoJdbcSql.literal("12.50", java.sql.Types.NUMERIC));
    }
}
