package com.utp.sistemafinanciero.integracion;

import com.utp.sistemafinanciero.config.VerificadorEsquema;
import com.utp.sistemafinanciero.dao.jdbc.EmpenoDaoJdbc;
import com.utp.sistemafinanciero.dao.jdbc.PrestamoDaoJdbc;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.service.EmpenoService;
import com.utp.sistemafinanciero.service.PrestamoService;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import com.utp.sistemafinanciero.util.Db;
import com.utp.sistemafinanciero.version.HistorialEsquema;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PI-BD · Integración Servicio ↔ DAO JDBC ↔ PostgreSQL
 * real (base SistemaFinanciero_test). Verifica lo que las pruebas con
 * DAO en memoria NO pueden verificar: esquema, restricciones CHECK,
 * disparadores y que las reglas del servicio coincidan con las de la base.
 */
public class PostgresIntegracionTest {

    @Before
    public void requiereBase() {
        BdPruebas.requerir();
    }

    private static long contar(Connection cn, String sql) throws SQLException {
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @Test
    public void PI_BD_01_esquemaCompletoYVersionEsperadaPorElCodigo() throws Exception {
        try (Connection cn = Db.getConnection()) {
            VerificadorEsquema.verificar(cn);   // no debe lanzar excepción
            assertEquals(1, contar(cn, "SELECT count(*) FROM version_esquema WHERE version = '"
                    + HistorialEsquema.esperada() + "'"));
            assertTrue("deben existir al menos 33 tablas",
                    contar(cn, "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'") >= 33);
        }
    }

    @Test
    public void PI_BD_02_auditoriaNoSePuedeModificarNiBorrar() throws Exception {
        try (Connection cn = Db.getConnection()) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO auditoria (usuario, accion, recurso, estado_http, detalle) VALUES (?,?,?,?,?)")) {
                ps.setString(1, "plan-de-pruebas");
                ps.setString(2, "PRUEBA");
                ps.setString(3, "PI-BD-02");
                ps.setInt(4, 200);
                ps.setString(5, "Fila de prueba de integración");
                ps.executeUpdate();
            }
            try (Statement st = cn.createStatement()) {
                st.executeUpdate("UPDATE auditoria SET detalle = 'manipulado' WHERE usuario = 'plan-de-pruebas'");
                fail("El disparador trg_auditoria_inmutable debía impedir el UPDATE");
            } catch (SQLException esperado) {
                assertTrue(esperado.getMessage().toLowerCase().contains("auditor"));
            }
            try (Statement st = cn.createStatement()) {
                st.executeUpdate("DELETE FROM auditoria WHERE usuario = 'plan-de-pruebas'");
                fail("El disparador debía impedir el DELETE");
            } catch (SQLException esperado) {
                // correcto: la bitácora es inmutable
            }
        }
    }

    @Test
    public void PI_BD_03_restriccionCheckImpideStockNegativo() throws Exception {
        try (Connection cn = Db.getConnection(); Statement st = cn.createStatement()) {
            st.executeUpdate("UPDATE producto SET stock_actual = -1 WHERE codigo = 'PRD-001'");
            fail("ck_producto_stock debía rechazar el stock negativo");
        } catch (SQLException e) {
            assertEquals("23514", e.getSQLState());   // check_violation
        }
    }

    @Test
    public void PI_BD_04_tasaNegativaLaRechazaElServicioAntesQueLaBase() {
        // Antes del Avance 4 la validación solo existía en PostgreSQL (CHECK) y en
        // modo memoria se aceptaba: los dos modos se comportaban distinto (DEF-002).
        PrestamoService servicio = new PrestamoService(new PrestamoDaoJdbc());
        Prestamo p = new Prestamo();
        p.setIdOperacion(1);
        p.setMontoPrestamo(new BigDecimal("1000"));
        p.setTasaInteres(new BigDecimal("-5"));
        p.setPlazoMeses(12);
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(p));
    }

    @Test
    public void PI_BD_05_tasaFueraDelRangoNumericNoLlegaALaBase() {
        // NUMERIC(6,3) admite hasta 999.999: con 1500 PostgreSQL respondía error 22003 → HTTP 500 (DEF-003).
        PrestamoService servicio = new PrestamoService(new PrestamoDaoJdbc());
        Prestamo p = new Prestamo();
        p.setIdOperacion(1);
        p.setMontoPrestamo(new BigDecimal("1000"));
        p.setTasaInteres(new BigDecimal("1500"));
        p.setPlazoMeses(12);
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(p));
    }

    @Test
    public void PI_BD_06_prestamoValidoSeGuardaYSeLeeIgual() {
        PrestamoService servicio = new PrestamoService(new PrestamoDaoJdbc());
        Prestamo p = new Prestamo();
        p.setIdOperacion(1);
        p.setMontoPrestamo(new BigDecimal("5000.00"));
        p.setTasaInteres(new BigDecimal("15"));
        p.setPlazoMeses(12);
        Prestamo creado = servicio.crear(p);
        try {
            Prestamo leido = servicio.buscar(creado.getIdPrestamo());
            assertEquals(0, new BigDecimal("1166.67").compareTo(leido.getCuotaMensual()));
            assertEquals(0, new BigDecimal("14000.00").compareTo(leido.getSaldoPendiente()));
            assertEquals(creado.getFechaVencimiento(), leido.getFechaVencimiento());
        } finally {
            servicio.eliminar(creado.getIdPrestamo());
        }
    }

    @Test
    public void PI_BD_07_empenoConTasaNegativaSeRechazaEnElServicio() {
        EmpenoService servicio = new EmpenoService(new EmpenoDaoJdbc());
        Empeno e = new Empeno();
        e.setIdOperacion(1);
        e.setIdArticulo(1);
        e.setMontoPrestamo(new BigDecimal("800"));
        e.setTasaInteres(new BigDecimal("-1"));
        e.setPlazoDias(30);
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(e));
    }

    @Test
    public void PI_BD_08_datosDeDemostracionCompletos() throws Exception {
        try (Connection cn = Db.getConnection()) {
            for (String tabla : new String[]{"cliente", "usuario", "producto", "proveedor", "venta",
                    "campania", "orden_produccion", "expediente_aprobacion", "tipo_cambio"}) {
                assertTrue(tabla + " debe tener 100 registros", contar(cn, "SELECT count(*) FROM " + tabla) >= 100);
            }
            assertTrue(contar(cn, "SELECT count(*) FROM operacion") >= 300);
        }
    }
}
