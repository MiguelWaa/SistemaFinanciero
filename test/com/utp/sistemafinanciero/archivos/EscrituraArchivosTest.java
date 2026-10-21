package com.utp.sistemafinanciero.archivos;

import com.utp.sistemafinanciero.service.ReglaNegocioException;
import com.utp.sistemafinanciero.util.ExcelExporter;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/** Exportación CSV/XLSX: inyección de fórmulas, ceros a la izquierda, ida y vuelta, XXE. */
public class EscrituraArchivosTest {

    @Test
    public void neutralizaFormulasPeroNoNumerosConSigno() {
        assertEquals("'=HYPERLINK(\"http://x\")", ProteccionFormulas.neutralizar("=HYPERLINK(\"http://x\")"));
        assertEquals("'@SUM(A1)", ProteccionFormulas.neutralizar("@SUM(A1)"));
        assertEquals("'+51 987", ProteccionFormulas.neutralizar("+51 987"));
        assertEquals("-15.50", ProteccionFormulas.neutralizar("-15.50"));
        assertEquals("Ana", ProteccionFormulas.neutralizar("Ana"));
    }

    @Test
    public void csvConBomComillasYFormulaNeutralizada() {
        byte[] csv = EscritorCsv.generar(new String[]{"Nombre", "Monto"},
                List.<String[]>of(new String[]{"=cmd|'/c calc'!A1", "-20"}, new String[]{"Pérez, Ana", "10.5"}),
                new boolean[]{false, true});
        assertEquals((byte) 0xEF, csv[0]);
        String texto = new String(csv, StandardCharsets.UTF_8);
        assertTrue(texto.contains("'=cmd|'/c calc'!A1,-20"));
        assertTrue(texto.contains("\"Pérez, Ana\",10.5"));
    }

    @Test
    public void excelIdaYVueltaConservaCerosYTildes() {
        byte[] xlsx = ExcelExporter.generar("Clientes", new String[]{"Documento", "Nombres", "Monto"},
                List.<String[]>of(new String[]{"07123456", "José Ñahui", "1500.75"}), new boolean[]{false, false, true});
        List<String[]> filas = LectorExcel.leer(xlsx, 10);
        assertEquals(2, filas.size());
        assertArrayEquals(new String[]{"Documento", "Nombres", "Monto"}, filas.get(0));
        assertEquals("07123456", filas.get(1)[0]);
        assertEquals("José Ñahui", filas.get(1)[1]);
        assertEquals("1500.75", filas.get(1)[2]);
    }

    @Test
    public void excelRechazaArchivoQueNoEsZipYXmlConDoctype() throws Exception {
        assertThrows(ReglaNegocioException.class, () -> LectorExcel.leer("hola".getBytes(), 10));
        // XLSX malicioso con entidad externa (XXE) en la hoja
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream z = new java.util.zip.ZipOutputStream(bos)) {
            z.putNextEntry(new java.util.zip.ZipEntry("xl/worksheets/sheet1.xml"));
            z.write(("<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]>"
                    + "<worksheet><sheetData><row r=\"1\"><c r=\"A1\" t=\"inlineStr\"><is><t>&e;</t></is></c></row>"
                    + "</sheetData></worksheet>").getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
        }
        assertThrows(ReglaNegocioException.class, () -> LectorExcel.leer(bos.toByteArray(), 10));
    }

    private static void assertArrayEquals(String[] a, String[] b) {
        assertEquals(java.util.Arrays.asList(a), java.util.Arrays.asList(b));
    }
}
