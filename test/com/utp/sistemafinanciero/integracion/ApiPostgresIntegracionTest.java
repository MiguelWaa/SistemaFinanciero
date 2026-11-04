package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.backup.RespaldoJdbcSql;
import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.dao.jdbc.JdbcDaoFactory;
import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.LimitadorIntentos;
import com.utp.sistemafinanciero.security.Pbkdf2PasswordHasher;
import com.utp.sistemafinanciero.util.Db;
import com.utp.sistemafinanciero.util.Json;
import com.utp.sistemafinanciero.web.ApiServer;
import com.utp.sistemafinanciero.web.Rutas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PI-API · Integración completa con la base real:
 * Vista HTTP → ApiServer → FiltroSeguridad → Controlador → Servicio →
 * DAO JDBC → PostgreSQL (SistemaFinanciero_test). Se comprueba en la
 * propia base que los datos y la auditoría quedaron grabados.
 */
public class ApiPostgresIntegracionTest {

    private ApiServer app;
    private String base;
    private final HttpClient http = HttpClient.newHttpClient();

    @Before
    public void levantar() throws Exception {
        BdPruebas.requerir();
        Contenedor c = new Contenedor(new JdbcDaoFactory(), new Pbkdf2PasswordHasher(1000),
                new GestorSesiones(30), new LimitadorIntentos(5, 5), List.of(new RespaldoJdbcSql()),
                Files.createTempDirectory("sigsf-pi"), 2);
        app = new ApiServer(Paths.get("web"), "", 1024 * 1024);
        Rutas.registrar(app, c);
        base = "http://localhost:" + app.iniciar(0) + "/api";
    }

    @After
    public void detener() {
        if (app != null) app.detener();
    }

    private HttpResponse<String> pedir(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta))
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                .header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String email, String clave) throws Exception {
        HttpResponse<String> r = pedir("POST", "/auth/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}");
        assertEquals(r.body(), 200, r.statusCode());
        return (String) Json.parseObject(r.body()).get("token");
    }

    private static long contar(String sql, String parametro) throws Exception {
        try (Connection cn = Db.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    @Test
    public void PI_API_01_loginConHashPbkdf2GuardadoEnLaBase() throws Exception {
        assertNotNull(login("admin@sigsf.com", "admin123"));
    }

    @Test
    public void PI_API_02_clienteCreadoPorApiQuedaEnPostgresYEnLaAuditoria() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        String doc = "8" + BdPruebas.unico();
        long auditoriaAntes = contar("SELECT count(*) FROM auditoria WHERE recurso = ?", "POST /api/clientes");

        HttpResponse<String> r = pedir("POST", "/clientes", t,
                "{\"nombres\":\"Integración\",\"apellidos\":\"Postgres\",\"documento\":\"" + doc + "\"}");
        assertEquals(r.body(), 200, r.statusCode());
        Number id = (Number) Json.parseObject(r.body()).get("idCliente");
        try {
            assertEquals(1, contar("SELECT count(*) FROM cliente WHERE documento = ?", doc));
            assertEquals(auditoriaAntes + 1, contar("SELECT count(*) FROM auditoria WHERE recurso = ?", "POST /api/clientes"));
        } finally {
            pedir("DELETE", "/clientes/" + id.intValue(), t, null);
        }
    }

    @Test
    public void PI_API_03_documentoDuplicadoDevuelve400ConMensaje() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        HttpResponse<String> r = pedir("POST", "/clientes", t, "{\"nombres\":\"X\",\"documento\":\"71000001\"}");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("Ya existe un cliente"));
    }

    @Test
    public void PI_API_04_valoresFueraDeRangoNoProducenError500() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        HttpResponse<String> tasa = pedir("POST", "/prestamos", t,
                "{\"idOperacion\":1,\"montoPrestamo\":1000,\"tasaInteres\":1500,\"plazoMeses\":12}");
        HttpResponse<String> monto = pedir("POST", "/operaciones", t,
                "{\"idCliente\":1,\"monto\":1e20,\"tipoCambio\":3.7}");
        assertEquals(tasa.body(), 400, tasa.statusCode());
        assertEquals(monto.body(), 400, monto.statusCode());
    }

    @Test
    public void PI_API_05_clienteInexistenteDevuelveMensajeClaro() throws Exception {
        String t = login("admin@sigsf.com", "admin123");
        HttpResponse<String> r = pedir("POST", "/operaciones", t,
                "{\"idCliente\":999999,\"monto\":100,\"tipoCambio\":3.7}");
        assertEquals(r.body(), 400, r.statusCode());
        assertTrue(r.body(), r.body().contains("no existe"));
    }

    @Test
    public void PI_API_06_rolesDeLaBaseControlanElAcceso() throws Exception {
        String cajero = login("lucia.ramos@sigsf.com", "lucia123");
        assertEquals(403, pedir("GET", "/usuarios", cajero, null).statusCode());
        assertEquals(200, pedir("GET", "/operaciones", cajero, null).statusCode());
    }

    @Test
    public void PI_API_07_inyeccionSqlEnElLoginNoEntra() throws Exception {
        HttpResponse<String> r = pedir("POST", "/auth/login", null,
                "{\"email\":\"admin@sigsf.com' OR '1'='1\",\"password\":\"x' OR '1'='1\"}");
        assertEquals(400, r.statusCode());
        assertFalse(r.body().contains("token"));
    }
}
