package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.archivos.ArchivoGenerado;
import com.utp.sistemafinanciero.archivos.DefinicionArchivo;
import com.utp.sistemafinanciero.archivos.FormatoArchivo;
import com.utp.sistemafinanciero.archivos.LectorExcel;
import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.controller.CatalogoArchivos;
import com.utp.sistemafinanciero.dto.ResultadoImportacionDTO;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Importación y exportación de ficheros por área (TDD). Usa el sistema
 * completo en memoria: cada fila importada pasa por el MapeadorEntrada y
 * por el servicio del área, igual que desde la pantalla.
 */
public class ArchivoServiceTest {

    private Contenedor c;
    private Map<String, DefinicionArchivo<?>> catalogo;

    @Before
    public void preparar() throws Exception {
        c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-archivos"));
        catalogo = CatalogoArchivos.crear(c);
    }

    private ResultadoImportacionDTO importar(String recurso, String nombre, String csv, boolean soloValidar) {
        return c.archivos.importar(catalogo.get(recurso), nombre, csv.getBytes(StandardCharsets.UTF_8), soloValidar);
    }

    @Test
    public void importaClientesValidosYRechazaLosQueIncumplenReglasConSuNumeroDeFila() {
        int antes = c.clientes.listar().size();
        String csv = "Tipo de documento;Documento *;Nombres *;Apellidos;Fecha de nacimiento\n"
                + "DNI;70990001;Rosa;Quispe;1990-05-10\n"     // fila 2: válida
                + "DNI;70990001;Rosa;Repetida;\n"             // fila 3: documento duplicado
                + "DNI;70990002;;SinNombre;\n"                // fila 4: falta nombre obligatorio
                + "DNI;70990003;Juan;Mamani;10/02/1988\n";    // fila 5: válida (fecha dd/mm/aaaa)
        ResultadoImportacionDTO r = importar("clientes", "clientes.csv", csv, false);
        assertEquals(4, r.getFilasLeidas());
        assertEquals(2, r.getFilasImportadas());
        assertEquals(2, r.getFilasRechazadas());
        assertEquals(3, r.getErrores().get(0).getFila());
        assertEquals(4, r.getErrores().get(1).getFila());
        assertEquals(antes + 2, c.clientes.listar().size());
        Cliente juan = c.clientes.listar().stream().filter(x -> "70990003".equals(x.getDocumento())).findFirst().get();
        assertEquals("1988-02-10", juan.getFechaNacimiento().toString());
    }

    @Test
    public void soloValidarNoGuardaNada() {
        int antes = c.proveedores.listar().size();
        ResultadoImportacionDTO r = importar("proveedores", "prov.csv",
                "Razón social,RUC\nAndina SAC,20123456789\nNorte EIRL,20999999999\n", true);
        assertTrue(r.isSoloValidacion());
        assertEquals(2, r.getFilasValidas());
        assertEquals(0, r.getFilasImportadas());
        assertEquals(antes, c.proveedores.listar().size());
    }

    @Test
    public void ignoraColumnasCalculadasPorElServidor() {
        // id, fechaRegistro y "estado" de un expediente no se aceptan (mass assignment)
        ResultadoImportacionDTO r = importar("expedientes", "exp.csv",
                "N°,Código,Monto,Estado,Solicitado\n999,EXP-T-01,1200.50,APROBADO,2020-01-01\n", false);
        assertEquals(1, r.getFilasImportadas());
        assertTrue(r.getColumnasIgnoradas().containsAll(List.of("N°", "Estado", "Solicitado")));
        var exp = c.expedientes.listar().stream().filter(e -> "EXP-T-01".equals(e.getCodigo())).findFirst().get();
        assertEquals("PENDIENTE", exp.getEstado());
        assertNotEquals(Integer.valueOf(999), exp.getIdExpediente());
    }

    @Test
    public void rechazaArchivoSinColumnasObligatoriasOExtensionNoPermitida() {
        assertThrows(ReglaNegocioException.class, () -> importar("productos", "p.csv", "Código,Stock actual\nA1,4\n", false));
        assertThrows(ReglaNegocioException.class, () -> importar("productos", "p.exe", "Nombre\nX\n", false));
    }

    @Test
    public void unValorNumericoInvalidoRechazaSoloEsaFila() {
        ResultadoImportacionDTO r = importar("productos", "p.csv", "Nombre,Stock actual\nTóner,diez\nPapel,12\n", false);
        assertEquals(1, r.getFilasImportadas());
        assertEquals(1, r.getFilasRechazadas());
        assertEquals(2, r.getErrores().get(0).getFila());
        assertTrue(r.getErrores().get(0).getMensaje().contains("número"));
    }

    @Test
    public void lasOperacionesFinancierasNoSeImportan() {
        assertFalse(catalogo.get("operaciones").importable());
        assertFalse(catalogo.get("ventas").importable());
        assertFalse(catalogo.get("prestamos").importable());
        assertThrows(ReglaNegocioException.class, () -> importar("operaciones", "o.csv", "Monto\n100\n", false));
    }

    @Test
    public void exportaCadaAreaEnXlsxCsvYJsonYLaPlantillaSeReimporta() {
        for (DefinicionArchivo<?> def : catalogo.values()) {
            for (FormatoArchivo f : FormatoArchivo.values()) {
                ArchivoGenerado a = c.archivos.exportar(def, f);
                assertTrue(def.recurso() + " " + f, a.datos().length > 0);
                assertTrue(a.nombre().endsWith("." + f.extension()));
            }
        }
        ArchivoGenerado xlsx = c.archivos.exportar(catalogo.get("productos"), FormatoArchivo.XLSX);
        assertEquals("N°", LectorExcel.leer(xlsx.datos(), 1000).get(0)[0]);

        // La plantilla descargada (con su fila de ejemplo) se puede importar tal cual.
        ArchivoGenerado plantilla = c.archivos.plantilla(catalogo.get("campanias"));
        ResultadoImportacionDTO r = c.archivos.importar(catalogo.get("campanias"), plantilla.nombre(),
                plantilla.datos(), false);
        assertEquals(1, r.getFilasImportadas());
    }
}
