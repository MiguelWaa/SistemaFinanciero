package com.utp.sistemafinanciero.config;

import com.utp.sistemafinanciero.web.AreaNegocio;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Verifica la configuración de registro (src/logback.xml) y la
 * clasificación de rutas por área que alimenta el MDC de cada línea de log.
 */
public class LogbackConfigTest {

    private static Document leer() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return f.newDocumentBuilder().parse(new File("src/logback.xml"));
    }

    @Test
    public void defineLosArchivosDeLogYLaRotacion() throws Exception {
        Document d = leer();
        Set<String> nombres = new HashSet<>();
        NodeList apps = d.getElementsByTagName("appender");
        for (int i = 0; i < apps.getLength(); i++) {
            nombres.add(((Element) apps.item(i)).getAttribute("name"));
        }
        assertTrue(nombres.containsAll(Set.of("CONSOLA", "ARCHIVO", "ERRORES", "ACCESO", "SEGURIDAD", "AREAS")));
        assertTrue(d.getElementsByTagName("rollingPolicy").getLength() >= 5);
        assertTrue(d.getElementsByTagName("maxHistory").getLength() >= 5);
    }

    @Test
    public void losPatronesEliminanSaltosDeLineaContraLogInjection() throws Exception {
        NodeList props = leer().getElementsByTagName("property");
        int conReplace = 0;
        for (int i = 0; i < props.getLength(); i++) {
            String v = ((Element) props.item(i)).getAttribute("value");
            if (v.contains("%msg")) {
                assertTrue("Patrón sin %replace: " + v, v.contains("%replace(%msg)"));
                conReplace++;
            }
        }
        assertEquals(2, conReplace);
    }

    @Test
    public void cadaRutaDeLaApiPerteneceASuArea() {
        assertEquals(AreaNegocio.MARKETING, AreaNegocio.deRuta("/api/prospectos/importar"));
        assertEquals(AreaNegocio.OPERACIONES, AreaNegocio.deRuta("/api/tipos-cambio/3"));
        assertEquals(AreaNegocio.DIRECCION_GERENCIA, AreaNegocio.deRuta("/api/expedientes"));
        assertEquals(AreaNegocio.SEGURIDAD, AreaNegocio.deRuta("/api/auth/login"));
        assertEquals(AreaNegocio.SISTEMA, AreaNegocio.deRuta("/api/desconocido"));
    }
}
