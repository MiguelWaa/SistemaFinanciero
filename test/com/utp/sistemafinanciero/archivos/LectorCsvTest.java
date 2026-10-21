package com.utp.sistemafinanciero.archivos;

import com.utp.sistemafinanciero.service.ReglaNegocioException;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/** Lectura de CSV: separadores, comillas, codificación y límites (importación de ficheros). */
public class LectorCsvTest {

    private static List<String[]> leer(String texto) {
        return LectorCsv.leer(texto.getBytes(StandardCharsets.UTF_8), 100);
    }

    @Test
    public void leeComasComillasYSaltosDeLineaDentroDeUnValor() {
        List<String[]> f = leer("nombre,descripcion\r\n\"Pérez, Ana\",\"Dice \"\"hola\"\"\nsegunda línea\"\r\n");
        assertEquals(2, f.size());
        assertEquals("Pérez, Ana", f.get(1)[0]);
        assertEquals("Dice \"hola\"\nsegunda línea", f.get(1)[1]);
    }

    @Test
    public void detectaPuntoYComaYQuitaElBom() {
        List<String[]> f = leer("\uFEFFNombres;Documento\nLuis;07001122\n");
        assertEquals("Nombres", f.get(0)[0]);
        assertEquals("07001122", f.get(1)[1]); // conserva el cero inicial
    }

    @Test
    public void aceptaCsvGuardadoPorExcelEnWindows1252() {
        byte[] ansi = "Razón social;RUC\nPanadería Ñusta;20111111111\n".getBytes(Charset.forName("windows-1252"));
        List<String[]> f = LectorCsv.leer(ansi, 10);
        assertEquals("Razón social", f.get(0)[0]);
        assertEquals("Panadería Ñusta", f.get(1)[0]);
    }

    @Test
    public void rechazaBinariosComillasSinCerrarYExcesoDeFilas() {
        assertThrows(ReglaNegocioException.class, () -> LectorCsv.leer(new byte[]{'P', 'K', 3, 4, 0}, 10));
        assertThrows(ReglaNegocioException.class, () -> leer("a,b\n\"sin cerrar,1\n"));
        StringBuilder sb = new StringBuilder("a\n");
        for (int i = 0; i < 5; i++) sb.append(i).append('\n');
        assertThrows(ReglaNegocioException.class, () -> LectorCsv.leer(sb.toString().getBytes(), 3));
    }
}
