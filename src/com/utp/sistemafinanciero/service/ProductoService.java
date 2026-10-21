package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ProductoDao;
import com.utp.sistemafinanciero.model.almacen.Producto;

import java.util.List;

/** Reglas de negocio para los productos de almacen. */
public class ProductoService implements ServicioCrud<Producto, Producto> {

    private final ProductoDao dao;

    /** El DAO se recibe por constructor (inyección de dependencias, DIP). */
    public ProductoService(ProductoDao dao) {
        this.dao = dao;
    }

    public List<Producto> listar() {
        return dao.listar();
    }

    public Producto buscar(int id) {
        Producto p = dao.buscar(id);
        if (p == null) {
            throw new ReglaNegocioException("No existe el producto con id " + id + ".");
        }
        return p;
    }

    public Producto crear(Producto p) {
        if (p.getNombre() == null || p.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio.");
        }
        if (p.getStockActual() == null) p.setStockActual(0);
        if (p.getStockMinimo() == null) p.setStockMinimo(0);
        if (p.getEstado() == null || p.getEstado().isBlank()) {
            p.setEstado("ACTIVO");
        }
        p.setIdProducto(dao.crear(p));
        return p;
    }

    public Producto actualizar(int id, Producto p) {
        if (dao.buscar(id) == null) {
            throw new ReglaNegocioException("No existe el producto con id " + id + ".");
        }
        p.setIdProducto(id);
        dao.actualizar(p);
        return p;
    }

    public void eliminar(int id) {
        if (dao.eliminar(id) == 0) {
            throw new ReglaNegocioException("No existe el producto con id " + id + ".");
        }
    }
}
