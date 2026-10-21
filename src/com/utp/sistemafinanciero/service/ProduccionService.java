package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.LineaProduccionDao;
import com.utp.sistemafinanciero.dao.OrdenProduccionDao;
import com.utp.sistemafinanciero.dao.ProductoDao;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Reglas de negocio del módulo de Producción (líneas y órdenes). */
public class ProduccionService {

    private final LineaProduccionDao lineaDao;
    private final OrdenProduccionDao ordenDao;
    private final ProductoDao productoDao;

    public ProduccionService(LineaProduccionDao lineaDao, OrdenProduccionDao ordenDao, ProductoDao productoDao) {
        this.lineaDao = lineaDao;
        this.ordenDao = ordenDao;
        this.productoDao = productoDao;
    }

    // ---- Líneas de producción ----

    public List<LineaProduccion> listarLineas() {
        return lineaDao.listar();
    }

    public LineaProduccion crearLinea(LineaProduccion l) {
        if (l.getNombre() == null || l.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre de la línea es obligatorio.");
        }
        if (l.getEstado() == null || l.getEstado().isBlank()) {
            l.setEstado("ACTIVA");
        }
        l.setIdLinea(lineaDao.crear(l));
        return l;
    }

    public LineaProduccion actualizarLinea(int id, LineaProduccion l) {
        if (lineaDao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe la línea con id " + id + ".");
        }
        l.setIdLinea(id);
        lineaDao.actualizar(l);
        return l;
    }

    public void eliminarLinea(int id) {
        if (lineaDao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe la línea con id " + id + ".");
        }
    }

    // ---- Órdenes de producción ----

    public List<OrdenProduccion> listarOrdenes() {
        return ordenDao.listar();
    }

    public OrdenProduccion crearOrden(OrdenProduccion o) {
        if (o.getCantidad() == null || o.getCantidad() <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
        validarReferencias(o);
        if (o.getFechaInicio() == null) {
            o.setFechaInicio(LocalDate.now());
        }
        if (o.getAvance() == null) {
            o.setAvance(BigDecimal.ZERO);
        }
        if (o.getEstado() == null || o.getEstado().isBlank()) {
            o.setEstado("PENDIENTE");
        }
        o.setIdOrden(ordenDao.crear(o));
        return o;
    }

    public OrdenProduccion actualizarOrden(int id, OrdenProduccion o) {
        OrdenProduccion actual = ordenDao.buscar(id);
        if (actual == null) {
            throw new ReglaNegocioException("No existe la orden con id " + id + ".");
        }
        o.setIdOrden(id);
        if (o.getCantidad() == null || o.getCantidad() <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero.");
        }
        if (o.getAvance() != null && (o.getAvance().signum() < 0 || o.getAvance().compareTo(new BigDecimal("100")) > 0)) {
            throw new ReglaNegocioException("El avance debe estar entre 0 y 100 %.");
        }
        validarReferencias(o);
        if (o.getFechaInicio() == null) {
            o.setFechaInicio(actual.getFechaInicio());
        }
        // Si el avance llega al 100% se marca como terminada.
        if (o.getAvance() != null && o.getAvance().compareTo(new BigDecimal("100")) >= 0) {
            o.setEstado("TERMINADA");
            o.setFechaFinReal(LocalDate.now());
        }
        ordenDao.actualizar(o);
        return o;
    }

    public void eliminarOrden(int id) {
        if (ordenDao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe la orden con id " + id + ".");
        }
    }

    /** RNF-PRD-03: la orden debe referenciar un producto y una línea existentes. */
    private void validarReferencias(OrdenProduccion o) {
        if (o.getIdProducto() == null || productoDao.buscar(o.getIdProducto()) == null) {
            throw new ReglaNegocioException("Seleccione un producto existente para la orden.");
        }
        if (o.getIdLinea() == null || lineaDao.buscar(o.getIdLinea()) == null) {
            throw new ReglaNegocioException("Seleccione una línea de producción existente.");
        }
    }
}
