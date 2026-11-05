package com.utp.sistemafinanciero.plan;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.Socket;
import java.net.URI;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static com.utp.sistemafinanciero.plan.ServidorDePrueba.*;
import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Prueba NO FUNCIONAL de SEGURIDAD (OWASP Top 10).
 *
 * <p>Ataques típicos lanzados contra el sistema en ejecución. Cada caso indica
 * la categoría OWASP 2021 que verifica.</p>
 */
public class SeguridadOwaspApiTest {

    private ServidorDePrueba s;

    @Before
    public void levantar() throws Exception {
        s = ServidorDePrueba.iniciar();
    }

    @After
    public void detener() {
        s.close();
    }

    /** CP-SEG-01 · A03 Inyección: SQL injection clásica en el login no da acceso. */
    @Test
    public void cpSeg01_inyeccionSqlEnLoginNoDaAcceso() throws Exception {
        HttpResponse<String> r = s.pedir("POST", "/api/auth/login", null,
                "{\"email\":\"admin@sigsf.com' OR '1'='1\",\"password\":\"x' OR '1'='1\"}");
        assertEquals(400, r.statusCode());
        assertNull(objeto(r).get("token"));
    }

    /** CP-SEG-02 · A01 Control de acceso: path traversal no sale de la carpeta web/. */
    @Test
    public void cpSeg02_pathTraversalNoExponeArchivosDelServidor() throws Exception {
        for (String ruta : new String[]{"/../db.properties.ejemplo", "/..%2f..%2fdb.properties.ejemplo",
                "/%2e%2e/src/logback.xml", "/..\\..\\build.xml"}) {
            String crudo = peticionCruda(ruta);
            assertFalse("Se filtró un archivo con " + ruta, crudo.startsWith("HTTP/1.1 200"));
            assertFalse(crudo.contains("db.password"));
        }
    }

    /** CP-SEG-03 · A07 Autenticación: 5 intentos fallidos bloquean la cuenta (fuerza bruta). */
    @Test
    public void cpSeg03_fuerzaBrutaBloqueaLaCuenta() throws Exception {
        for (int i = 0; i < 5; i++) {
            s.pedir("POST", "/api/auth/login", null, "{\"email\":\"jorge.torres@sigsf.com\",\"password\":\"mala" + i + "\"}");
        }
        // Ni siquiera la clave correcta entra mientras dura el bloqueo
        HttpResponse<String> r = s.pedir("POST", "/api/auth/login", null,
                "{\"email\":\"jorge.torres@sigsf.com\",\"password\":\"jorge123\"}");
        assertEquals(400, r.statusCode());
        assertTrue(objeto(r).get("error").toString().toLowerCase().contains("bloquead"));
    }

    /** CP-SEG-04 · A01 IDOR: un cliente no puede pagar ni consultar trámites de otro cliente. */
    @Test
    public void cpSeg04_clienteNoOperaSobreTramitesAjenos() throws Exception {
        String juan = s.loginCliente("70000001", "70000001");
        // Intenta pagar el préstamo de María (operación 2) desde su propia cuenta
        HttpResponse<String> r = s.pedir("POST", "/api/portal/1/pagos", juan,
                "{\"idOperacion\":2,\"concepto\":\"CUOTA_PRESTAMO\",\"monto\":10,\"metodo\":\"YAPE\",\"numeroOperacion\":\"12121212\"}");
        assertEquals(400, r.statusCode());
        // Y cambiando el id de la URL obtiene 403
        assertEquals(403, s.get("/api/portal/2/pagos", juan).statusCode());
        assertEquals(403, s.get("/api/portal/2/operaciones", juan).statusCode());
    }

    /** CP-SEG-05 · A04 Diseño inseguro: el cliente no puede fijar su propia tasa de interés. */
    @Test
    public void cpSeg05_tasaDeInteresNoManipulableDesdeElPortal() throws Exception {
        String juan = s.loginCliente("70000001", "70000001");
        HttpResponse<String> r = s.pedir("POST", "/api/portal/1/prestamo", juan,
                "{\"montoPrestamo\":1000,\"plazoMeses\":2,\"tasaInteres\":0.01}");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> p = (java.util.Map<String, Object>) objeto(r).get("prestamo");
        assertEquals(15, entero(p, "tasaInteres"));
    }

    /** CP-SEG-06 · A03 XSS: el texto peligroso se devuelve como JSON con nosniff (el frontend lo escapa). */
    @Test
    public void cpSeg06_xssSeGuardaComoTextoYSeSirveComoJson() throws Exception {
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        HttpResponse<String> r = s.pedir("POST", "/api/clientes", admin,
                "{\"nombres\":\"<script>alert(1)</script>\",\"documento\":\"40000999\"}");
        assertEquals(200, r.statusCode());
        assertTrue(r.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        assertEquals("nosniff", r.headers().firstValue("X-Content-Type-Options").orElse(""));
    }

    /** CP-SEG-07 · A05 Configuración: cabeceras de seguridad y CSP en las páginas HTML. */
    @Test
    public void cpSeg07_cabecerasDeSeguridadEnElFrontend() throws Exception {
        HttpResponse<String> r = s.get("/login.html", null);
        String csp = r.headers().firstValue("Content-Security-Policy").orElse("");
        assertTrue(csp.contains("default-src 'self'"));
        assertTrue(csp.contains("frame-ancestors 'self'"));
        assertEquals("SAMEORIGIN", r.headers().firstValue("X-Frame-Options").orElse(""));
        assertEquals("no-referrer", r.headers().firstValue("Referrer-Policy").orElse(""));
    }

    /** CP-SEG-08 · A04 Denegación de servicio: se rechaza un cuerpo mayor al límite configurado. */
    @Test
    public void cpSeg08_cuerpoDemasiadoGrandeEsRechazado() throws Exception {
        String enorme = "{\"email\":\"" + "x".repeat(1_100_000) + "\",\"password\":\"x\"}";
        HttpResponse<String> r = s.pedir("POST", "/api/auth/login", null, enorme);
        assertEquals(400, r.statusCode());
        assertTrue(objeto(r).get("error").toString().contains("tamaño"));
    }

    /** CP-SEG-09 · A02/A07: un token alterado o inventado no da acceso. */
    @Test
    public void cpSeg09_tokenAlteradoNoDaAcceso() throws Exception {
        String t = s.loginPersonal("admin@sigsf.com", "admin123");
        String alterado = (t.charAt(0) == 'A' ? 'B' : 'A') + t.substring(1);
        assertEquals(401, s.get("/api/usuarios", alterado).statusCode());
        assertEquals(401, s.get("/api/usuarios", "eyJhbGciOiJub25lIn0.e30.").statusCode());
    }

    /** CP-SEG-10 · A04: montos negativos o sobre el máximo no se aceptan en pagos. */
    @Test
    public void cpSeg10_montosFueraDeRangoEnPagos() throws Exception {
        String maria = s.loginCliente("70000002", "70000002");
        assertEquals(400, s.pedir("POST", "/api/portal/2/pagos", maria,
                "{\"idOperacion\":2,\"concepto\":\"CUOTA_PRESTAMO\",\"monto\":-5,\"metodo\":\"YAPE\",\"numeroOperacion\":\"11111111\"}").statusCode());
        assertEquals(400, s.pedir("POST", "/api/portal/2/pagos", maria,
                "{\"idOperacion\":2,\"concepto\":\"CUOTA_PRESTAMO\",\"monto\":99999,\"metodo\":\"YAPE\",\"numeroOperacion\":\"22222222\"}").statusCode());
    }

    /** Envía la ruta tal cual (sin normalizar), como haría un atacante con curl --path-as-is. */
    private String peticionCruda(String ruta) throws Exception {
        URI u = URI.create(s.raiz());
        try (Socket so = new Socket(u.getHost(), u.getPort())) {
            so.setSoTimeout(5000);
            OutputStream out = so.getOutputStream();
            out.write(("GET " + ruta + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
            InputStream in = so.getInputStream();
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
