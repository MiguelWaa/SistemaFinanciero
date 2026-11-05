package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.ProductoDaoMemoria;
import com.utp.sistemafinanciero.model.almacen.Producto;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * v5.5 · Plan de pruebas · Nivel UNITARIO · Almacén.
 * CP-UNI-ALM-01…04 · RF-ALM-01/02: registro de productos con valores por defecto,
 * nombre obligatorio y mantenimiento (actualizar / eliminar).
 */
public class ProductoServiceTest {

    private ProductoService servicio;

    @Before
    public void preparar() {
        servicio = new ProductoService(new ProductoDaoMemoria());
    }

    private static Producto producto(String nombre) {
        Producto p = new Producto();
        p.setCodigo("PRD-T01");
        p.setNombre(nombre);
        return p;
    }

    @Test
    public void registraProductoConStockEnCeroYEstadoActivo() {
        Producto p = servicio.crear(producto("Sobres de seguridad"));
        assertNotNull(p.getIdProducto());
        assertEquals(Integer.valueOf(0), p.getStockActual());
        assertEquals(Integer.valueOf(0), p.getStockMinimo());
        assertEquals("ACTIVO", p.getEstado());
    }

    @Test
    public void elNombreEsObligatorio() {
        ReglaNegocioException e = assertThrows(ReglaNegocioException.class, () -> servicio.crear(producto("  ")));
        assertTrue(e.getMessage().contains("obligatorio"));
        assertThrows(ReglaNegocioException.class, () -> servicio.crear(producto(null)));
    }

    @Test
    public void actualizaElStockDeUnProductoExistente() {
        Producto p = servicio.crear(producto("Tinta para sellos"));
        Producto cambio = producto("Tinta para sellos");
        cambio.setStockActual(60);
        cambio.setStockMinimo(25);
        cambio.setEstado("ACTIVO");
        servicio.actualizar(p.getIdProducto(), cambio);
        assertEquals(Integer.valueOf(60), servicio.buscar(p.getIdProducto()).getStockActual());
    }

    @Test
    public void noActualizaNiEliminaUnProductoInexistente() {
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(999, producto("X")));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(999));
        assertThrows(ReglaNegocioException.class, () -> servicio.buscar(999));
    }
}
