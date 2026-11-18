package com.utp.sistemafinanciero.despliegue;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Rutas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Pruebas de la sonda de salud del despliegue (v5.11.0). Levanta el servidor
 * real con DAO en memoria y comprueba los casos sano, caído y URL no local.
 */
public class SondaSaludTest {

    private ApiServer app;
    private int puerto;

    @Before
    public void levantar() throws Exception {
        Contenedor c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-sonda"));
        app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        Rutas.registrar(app, c);
        puerto = app.iniciar(0);
    }

    @After
    public void detener() {
        app.detener();
    }

    @Test
    public void servidorEnEjecucionEsSano() {
        SondaSalud.Resultado r = SondaSalud.verificar("http://127.0.0.1:" + puerto + "/api/health", 3000);
        assertTrue(r.mensaje(), r.sano());
        assertEquals(200, r.codigoHttp());
        assertTrue(r.mensaje().contains("SIGSF API"));
    }

    @Test
    public void rutaInexistenteNoEsSana() {
        SondaSalud.Resultado r = SondaSalud.verificar("http://127.0.0.1:" + puerto + "/api/no-existe", 3000);
        assertFalse(r.sano());
    }

    @Test
    public void servidorDetenidoNoEsSano() {
        app.detener();
        SondaSalud.Resultado r = SondaSalud.verificar("http://127.0.0.1:" + puerto + "/api/health", 1000);
        assertFalse(r.sano());
        assertEquals(-1, r.codigoHttp());
    }

    @Test
    public void rechazaUrlNoLocal() {
        SondaSalud.Resultado r = SondaSalud.verificar("http://ejemplo.com/api/health", 1000);
        assertFalse(r.sano());
        assertTrue(r.mensaje().startsWith("URL rechazada"));
        try {
            SondaSalud.validarUrlLocal("file:///etc/passwd");
            fail("Debe rechazar esquemas distintos de http");
        } catch (IllegalArgumentException esperado) {
            // correcto
        }
    }

    @Test
    public void cuerpoSoloSanoConStatusOk() {
        assertTrue(SondaSalud.cuerpoSano(200, "{\"status\": \"ok\"}"));
        assertFalse(SondaSalud.cuerpoSano(200, "{\"status\":\"degradado\"}"));
        assertFalse(SondaSalud.cuerpoSano(503, "{\"status\":\"ok\"}"));
        assertFalse(SondaSalud.cuerpoSano(200, null));
    }
}
