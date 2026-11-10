package com.utp.sistemafinanciero.util;

import org.junit.Test;
import java.util.Map;
import static org.junit.Assert.*;

/** Contrato de entradas JSON, regresión de JSON-SUF y límites de deserialización. */
public class JsonEstrictoTest {
    private void invalido(String texto) {
        assertThrows("No debe aceptar: " + texto.substring(0, Math.min(80, texto.length())),
                RuntimeException.class, () -> Json.parse(texto));
    }

    @Test public void documentoValidoConEspacios() {
        Map<String, Object> m = Json.parseObject(" \r\n {\"texto\":\"á\\n\\u00f1\",\"numero\":-12.5e2,\"ok\":true} \t");
        assertEquals("á\nñ", m.get("texto"));
        assertEquals(-1250.0, (Double) m.get("numero"), 0);
        assertEquals(Boolean.TRUE, m.get("ok"));
    }
    @Test public void rechazaSufijoYSegundoDocumento() {
        for (String s : new String[]{"{} trailing", "{}{}", "[]null", "true false", "1 2"}) invalido(s);
    }
    @Test public void rechazaClavesDuplicadasInclusoNulas() {
        invalido("{\"rol\":null,\"rol\":\"ADMIN\"}");
        invalido("{\"x\":1,\"\\u0078\":2}");
    }
    @Test public void rechazaNumerosFueraDeGramatica() {
        for (String s : new String[]{"+1", "01", "-01", ".1", "1.", "1e", "--1", "NaN", "Infinity", "1e999"}) invalido(s);
    }
    @Test public void conservaNumerosValidos() {
        for (String s : new String[]{"0", "-0", "1", "-1", "1.25", "1e+2", "-0.25E-2"}) {
            assertEquals(Double.parseDouble(s), (Double) Json.parse(s), 0);
        }
    }
    @Test public void rechazaControlesSinEscapar() {
        invalido("\"linea\nlinea\""); invalido("\"\u0000\""); invalido("\"\t\"");
    }
    @Test public void rechazaEspaciosNoJson() { invalido("\u000b{}"); invalido("{}\u001c"); }
    @Test public void rechazaUnicodeIncompletoOInvalido() { invalido("\"\\u12\""); invalido("\"\\uZZZZ\""); }
    @Test public void limitaProfundidadSinDesbordarLaPila() {
        assertNotNull(Json.parse("[".repeat(64) + "0" + "]".repeat(64)));
        invalido("[".repeat(65) + "0" + "]".repeat(65));
    }
    @Test public void rechazaDocumentoVacio() { invalido(""); invalido(" \r\n"); }
    @Test public void metadatosSonSoloDatos() {
        Map<String, Object> m = Json.parseObject("{\"@class\":\"java.lang.String\",\"x\":[1,true,null]}");
        assertEquals("java.lang.String", m.get("@class"));
        assertTrue(m.get("x") instanceof java.util.List);
    }
}
