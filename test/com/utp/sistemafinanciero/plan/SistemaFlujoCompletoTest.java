package com.utp.sistemafinanciero.plan;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.utp.sistemafinanciero.plan.ServidorDePrueba.*;
import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Nivel SISTEMA (caja negra, de extremo a extremo).
 *
 * <p>Se prueba el sistema completo tal como lo usa el navegador: frontend
 * servido por el mismo servidor, API REST, seguridad, servicios y DAO. Cada
 * caso recorre varios módulos en el orden real del negocio.</p>
 */
public class SistemaFlujoCompletoTest {

    private ServidorDePrueba s;

    @Before
    public void levantar() throws Exception {
        s = ServidorDePrueba.iniciar();
    }

    @After
    public void detener() {
        s.close();
    }

    /** CP-SIS-01 · Todas las pantallas del frontend se sirven (mismo origen que la API). */
    @Test
    public void cpSis01_todasLasPantallasDelFrontendResponden() throws Exception {
        String[] pantallas = {"/", "/login.html", "/index.html", "/portal.html", "/portal-inicio.html",
                "/pagos.html", "/solicitudes.html", "/reportes.html", "/usuarios.html", "/seguridad.html",
                "/respaldos.html", "/versiones.html", "/almacen.html", "/produccion.html", "/Ventas.html",
                "/Marketing.html", "/Logistica.html", "/documento.html", "/ayuda.html", "/configuracion.html"};
        for (String p : pantallas) {
            HttpResponse<String> r = s.get(p, null);
            assertEquals("Pantalla " + p, 200, r.statusCode());
            assertTrue("Pantalla " + p + " debe ser HTML",
                    r.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
        }
        // Pantallas con espacios en el nombre (URL codificada, como las abre el menú)
        assertEquals(200, s.get("/operaciones%20financieras.html", null).statusCode());
        assertEquals(200, s.get("/clientes%20y%20servicios%20financieros.html", null).statusCode());
    }

    /** CP-SIS-02 · Recursos estáticos locales (Bootstrap, tema) y página inexistente. */
    @Test
    public void cpSis02_recursosEstaticosLocalesYNoEncontrado() throws Exception {
        assertEquals(200, s.get("/vendor/bootstrap/css/bootstrap.min.css", null).statusCode());
        assertEquals(200, s.get("/vendor/bootstrap/js/bootstrap.bundle.min.js", null).statusCode());
        assertEquals(200, s.get("/sigsf-ui.css", null).statusCode());
        assertEquals(200, s.get("/api.js", null).statusCode());
        assertEquals(404, s.get("/no-existe.html", null).statusCode());
    }

    /**
     * CP-SIS-03 · Ciclo de vida de un préstamo: el cliente se registra en el
     * portal → solicita → el supervisor aprueba → el cliente paga con Yape →
     * caja confirma → baja el saldo → estado de cuenta y comprobante.
     */
    @Test
    public void cpSis03_cicloCompletoDeUnPrestamoDesdeElPortal() throws Exception {
        // 1. Registro en el portal (entra directo con su token)
        HttpResponse<String> reg = s.pedir("POST", "/api/portal/registro", null,
                "{\"documento\":\"48887766\",\"nombres\":\"Pedro\",\"apellidos\":\"López\",\"password\":\"clave123\"}");
        assertEquals(reg.body(), 200, reg.statusCode());
        Map<String, Object> cli = objeto(reg);
        int idCliente = entero(cli, "idCliente");
        String tokenCliente = (String) cli.get("token");
        assertNotNull(tokenCliente);

        // 2. Solicita un préstamo de S/ 2000 a 4 meses (intenta imponer tasa 1 %: se ignora)
        HttpResponse<String> sol = s.pedir("POST", "/api/portal/" + idCliente + "/prestamo", tokenCliente,
                "{\"montoPrestamo\":2000,\"plazoMeses\":4,\"tasaInteres\":1}");
        assertEquals(sol.body(), 200, sol.statusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> prestamo = (Map<String, Object>) objeto(sol).get("prestamo");
        @SuppressWarnings("unchecked")
        Map<String, Object> operacion = (Map<String, Object>) objeto(sol).get("operacion");
        int idOperacion = entero(operacion, "idOperacion");
        int idPrestamo = entero(prestamo, "idPrestamo");
        assertEquals(15, entero(prestamo, "tasaInteres"));
        assertEquals("PENDIENTE", prestamo.get("estado"));

        // 3. El supervisor la ve en la bandeja y la aprueba
        String supervisor = s.loginPersonal("admin@sigsf.com", "admin123");
        assertTrue(s.get("/api/solicitudes/pendientes", supervisor).body().contains("\"idOperacion\":" + idOperacion));
        assertEquals(200, s.pedir("PUT", "/api/solicitudes/" + idOperacion + "/aprobar", supervisor, "{}").statusCode());
        assertEquals("ACTIVO", objeto(s.get("/api/prestamos/" + idPrestamo, supervisor)).get("estado"));
        double saldoInicial = decimal(objeto(s.get("/api/prestamos/" + idPrestamo, supervisor)), "saldoPendiente");

        // 4. El cliente paga su cuota con Yape
        List<Map<String, Object>> pagables = lista(s.get("/api/portal/" + idCliente + "/pagables", tokenCliente));
        assertEquals(1, pagables.size());
        double cuota = decimal(pagables.get(0), "montoSugerido");
        HttpResponse<String> pago = s.pedir("POST", "/api/portal/" + idCliente + "/pagos", tokenCliente,
                "{\"idOperacion\":" + idOperacion + ",\"concepto\":\"CUOTA_PRESTAMO\",\"monto\":" + cuota
                        + ",\"metodo\":\"YAPE\",\"numeroOperacion\":\"77665544\"}");
        assertEquals(pago.body(), 200, pago.statusCode());
        int idPago = entero(objeto(pago), "idPago");
        assertEquals("POR_VERIFICAR", objeto(pago).get("estado"));

        // 5. Caja confirma
        String cajero = s.loginPersonal("lucia.ramos@sigsf.com", "lucia123");
        HttpResponse<String> conf = s.pedir("PUT", "/api/pagos/" + idPago + "/confirmar", cajero, "{}");
        assertEquals(200, conf.statusCode());
        assertEquals(String.format("RC-%06d", idPago), objeto(conf).get("numeroComprobante"));

        // 6. El saldo baja exactamente en la cuota y el estado de cuenta lo refleja
        double saldoFinal = decimal(objeto(s.get("/api/prestamos/" + idPrestamo, cajero)), "saldoPendiente");
        assertEquals(saldoInicial - cuota, saldoFinal, 0.001);
        String estadoCuenta = s.get("/api/portal/" + idCliente + "/estado-cuenta", tokenCliente).body();
        assertTrue(estadoCuenta.contains("CONFIRMADO"));
    }

    /**
     * CP-SIS-04 · Atención en ventanilla: el cajero registra un cliente y una
     * operación de cambio; el tipo de cambio lo pone el servidor; la operación
     * aparece en el panel y se exporta a Excel.
     */
    @Test
    public void cpSis04_atencionEnVentanillaYExportacion() throws Exception {
        String cajero = s.loginPersonal("lucia.ramos@sigsf.com", "lucia123");
        int antes = entero(objeto(s.get("/api/dashboard", cajero)), "operacionesHoy");

        HttpResponse<String> c = s.pedir("POST", "/api/clientes", cajero,
                "{\"nombres\":\"Rosa\",\"apellidos\":\"Quispe\",\"documento\":\"45612378\",\"telefono\":\"987654321\"}");
        assertEquals(c.body(), 200, c.statusCode());
        int idCliente = entero(objeto(c), "idCliente");

        // El navegador intenta mandar tipoCambio 1.00: el servidor usa el vigente (venta 3.78)
        HttpResponse<String> op = s.pedir("POST", "/api/operaciones", cajero,
                "{\"idCliente\":" + idCliente + ",\"idTipoCambio\":1,\"monto\":100,\"tipoCambio\":1}");
        assertEquals(op.body(), 200, op.statusCode());
        assertEquals(378.00, decimal(objeto(op), "totalRecibir"), 0.001);

        assertEquals(antes + 1, entero(objeto(s.get("/api/dashboard", cajero)), "operacionesHoy"));

        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        HttpResponse<String> xlsx = s.get("/api/operaciones/exportar?formato=xlsx", admin);
        assertEquals(200, xlsx.statusCode());
        assertTrue(xlsx.headers().firstValue("Content-Disposition").orElse("").contains(".xlsx"));
        assertTrue(xlsx.body().startsWith("PK"));   // un .xlsx es un ZIP
    }

    /** CP-SIS-05 · Sesión: ingresar, consultar la sesión, salir y quedar sin acceso. */
    @Test
    public void cpSis05_cicloDeSesionDelPersonal() throws Exception {
        String t = s.loginPersonal("jorge.torres@sigsf.com", "jorge123");
        Map<String, Object> sesion = objeto(s.get("/api/auth/sesion", t));
        assertEquals("ANALISTA", String.valueOf(sesion.get("rol")).toUpperCase());
        assertEquals(200, s.pedir("POST", "/api/auth/logout", t, null).statusCode());
        assertEquals(401, s.get("/api/auth/sesion", t).statusCode());
        assertEquals(401, s.get("/api/clientes", t).statusCode());
    }

    /** CP-SIS-06 · Trazabilidad: las acciones del flujo quedan en la bitácora de auditoría. */
    @Test
    public void cpSis06_lasAccionesQuedanEnLaAuditoria() throws Exception {
        String admin = s.loginPersonal("admin@sigsf.com", "admin123");
        s.pedir("POST", "/api/auth/login", null, "{\"email\":\"admin@sigsf.com\",\"password\":\"equivocada\"}");
        s.pedir("POST", "/api/clientes", admin, "{\"nombres\":\"Auditado\",\"documento\":\"40001234\"}");
        String bitacora = s.get("/api/auditoria", admin).body();
        assertTrue(bitacora.contains("LOGIN_OK"));
        assertTrue(bitacora.contains("LOGIN_FALLIDO"));
        assertTrue(bitacora.contains("CREAR"));
    }

    /** CP-SIS-07 · Disponibilidad: /api/health responde sin sesión e informa la persistencia. */
    @Test
    public void cpSis07_healthCheck() throws Exception {
        HttpResponse<String> r = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(s.raiz() + "/api/health")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, r.statusCode());
        assertEquals("ok", objeto(r).get("status"));
        assertNotNull(objeto(r).get("persistencia"));
    }
}
