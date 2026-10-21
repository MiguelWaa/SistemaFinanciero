package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.model.almacen.Producto;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.*;

/** RF-PRD-02/03 · RNF-PRD-01/02/03 · Órdenes de producción. */
public class ProduccionServiceTest {

    private ProduccionService servicio;
    private int idProducto;
    private int idLinea;

    @Before
    public void preparar() {
        MemoriaDaoFactory bd = new MemoriaDaoFactory();
        servicio = new ProduccionService(bd.lineasProduccion(), bd.ordenesProduccion(), bd.productos());
        idProducto = bd.productos().crear(new Producto(null, "P1", "Talonario", null, null, "UND", 10, 5, "ACTIVO"));
        idLinea = servicio.crearLinea(new LineaProduccion(null, "L1", "Impresión", null, null)).getIdLinea();
    }

    private OrdenProduccion orden(Integer cantidad, String avance) {
        OrdenProduccion o = new OrdenProduccion();
        o.setIdProducto(idProducto);
        o.setIdLinea(idLinea);
        o.setCantidad(cantidad);
        o.setAvance(avance == null ? null : new BigDecimal(avance));
        return o;
    }

    @Test
    public void lineaNuevaQuedaActiva() {
        assertEquals("ACTIVA", servicio.listarLineas().get(0).getEstado());
    }

    @Test
    public void ordenNuevaQuedaPendienteConAvanceCero() {
        OrdenProduccion o = servicio.crearOrden(orden(100, null));
        assertEquals("PENDIENTE", o.getEstado());
        assertEquals(BigDecimal.ZERO, o.getAvance());
    }

    @Test
    public void alLlegarAl100PorCientoPasaATerminada() {
        OrdenProduccion o = servicio.crearOrden(orden(100, null));
        OrdenProduccion act = servicio.actualizarOrden(o.getIdOrden(), orden(100, "100"));
        assertEquals("TERMINADA", act.getEstado());
        assertEquals(LocalDate.now(), act.getFechaFinReal());
    }

    @Test
    public void cantidadDebeSerMayorACero() {
        assertThrows(ReglaNegocioException.class, () -> servicio.crearOrden(orden(0, null)));
        assertThrows(ReglaNegocioException.class, () -> servicio.crearOrden(orden(null, null)));
    }

    @Test
    public void rechazaProductoOLineaInexistentes() {
        OrdenProduccion o = orden(10, null);
        o.setIdProducto(999);
        assertThrows(ReglaNegocioException.class, () -> servicio.crearOrden(o));
    }

    @Test
    public void avanceFueraDeRangoEsInvalido() {
        OrdenProduccion o = servicio.crearOrden(orden(10, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizarOrden(o.getIdOrden(), orden(10, "150")));
    }
}
