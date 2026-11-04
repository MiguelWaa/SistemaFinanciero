package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.dto.IndicadoresDTO;
import com.utp.sistemafinanciero.model.almacen.Producto;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;
import com.utp.sistemafinanciero.model.logistica.Proveedor;
import com.utp.sistemafinanciero.model.marketing.Campania;
import com.utp.sistemafinanciero.model.marketing.Prospecto;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Plan de Pruebas · PU-CAT · Servicios de catálogo de las áreas
 * (Seguridad, Clientes, Operaciones, Almacén, Logística, Marketing,
 * Dirección) y el tablero de indicadores. Cubre el ciclo CRUD y los
 * valores por defecto; agrega las reglas que antes solo validaba la base
 * de datos (stock y tipo de cambio).
 */
public class CatalogosServiceTest {

    private Contenedor c;

    @Before
    public void preparar() throws Exception {
        c = Contenedor.enMemoria(Files.createTempDirectory("sigsf-cat"));
    }

    @Test
    public void PU_CAT_01_rolCrudCompleto() {
        Rol r = c.roles.crear(new Rol(null, "AUDITOR", "Solo lectura"));
        assertNotNull(r.getIdRol());
        r.setDescripcion("Lectura de auditoría");
        assertEquals("Lectura de auditoría", c.roles.actualizar(r.getIdRol(), r).getDescripcion());
        c.roles.eliminar(r.getIdRol());
        assertThrows(ReglaNegocioException.class, () -> c.roles.buscar(r.getIdRol()));
        assertThrows(ReglaNegocioException.class, () -> c.roles.crear(new Rol(null, " ", null)));
    }

    @Test
    public void PU_CAT_02_tipoDeCambioValidaValoresPositivos() {
        TipoCambio ok = c.tiposCambio.crear(new TipoCambio(null, "USD", "PEN",
                new BigDecimal("3.70"), new BigDecimal("3.78"), null, null));
        assertEquals("ACTIVO", ok.getEstado());
        assertNotNull(ok.getFechaInicio());
        // La base tiene CHECK (compra > 0 AND venta > 0); en memoria antes se aceptaba.
        assertThrows(ReglaNegocioException.class, () -> c.tiposCambio.crear(new TipoCambio(null, "USD", "PEN",
                BigDecimal.ZERO, new BigDecimal("3.78"), null, null)));
        assertThrows(ReglaNegocioException.class, () -> c.tiposCambio.crear(new TipoCambio(null, "USD", null,
                new BigDecimal("3.70"), new BigDecimal("3.78"), null, null)));
    }

    @Test
    public void PU_CAT_03_productoConStockPorDefectoYSinNegativos() {
        Producto p = new Producto();
        p.setNombre("Sobres de seguridad");
        Producto creado = c.productos.crear(p);
        assertEquals(Integer.valueOf(0), creado.getStockActual());
        assertEquals("ACTIVO", creado.getEstado());
        Producto negativo = new Producto();
        negativo.setNombre("Cinta");
        negativo.setStockActual(-1);
        assertThrows(ReglaNegocioException.class, () -> c.productos.crear(negativo));
    }

    @Test
    public void PU_CAT_04_proveedorYServicioFinancieroQuedanActivos() {
        Proveedor pr = new Proveedor();
        pr.setRazonSocial("Imprenta Lima SAC");
        assertEquals("ACTIVO", c.proveedores.crear(pr).getEstado());
        assertThrows(ReglaNegocioException.class, () -> c.proveedores.crear(new Proveedor()));

        ServicioFinanciero s = new ServicioFinanciero();
        s.setNombre("Cuenta de ahorros");
        assertEquals("ACTIVO", c.servicios.crear(s).getEstado());
        assertThrows(ReglaNegocioException.class, () -> c.servicios.crear(new ServicioFinanciero()));
    }

    @Test
    public void PU_CAT_05_marketingCampaniaYProspecto() {
        Campania ca = new Campania();
        ca.setNombre("Préstamos escolares");
        Campania creada = c.campanias.crear(ca);
        assertEquals("ACTIVA", creada.getEstado());
        assertNotNull(creada.getFechaInicio());

        Prospecto p = new Prospecto();
        p.setNombres("Carlos");
        Prospecto creado = c.prospectos.crear(p);
        assertEquals("NUEVO", creado.getEstado());
        assertNotNull(creado.getFechaRegistro());
    }

    @Test
    public void PU_CAT_06_expedienteSeResuelveSoloConEstadosValidos() {
        ExpedienteAprobacion e = new ExpedienteAprobacion();
        e.setCodigo("EXP-PLAN-01");
        e.setMonto(new BigDecimal("15000"));
        ExpedienteAprobacion creado = c.expedientes.crear(e);
        assertEquals("PENDIENTE", creado.getEstado());
        ExpedienteAprobacion resuelto = c.expedientes.resolver(creado.getIdExpediente(), "aprobado");
        assertEquals("APROBADO", resuelto.getEstado());
        assertNotNull(resuelto.getFechaResolucion());
        assertThrows(ReglaNegocioException.class, () -> c.expedientes.resolver(creado.getIdExpediente(), "QUIZÁS"));
    }

    @Test
    public void PU_CAT_07_tableroCalculaLosIndicadoresSinFallar() {
        IndicadoresDTO d = c.dashboard.indicadores();
        assertTrue(d.getClientesTotales() > 0);
        assertTrue(d.getClientesActivos() <= d.getClientesTotales());
        assertNotNull(d.getMontoOperacionesMes());
        assertNotNull(d.getPrestamosColocados());
    }
}
