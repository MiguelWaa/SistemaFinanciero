package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ProductoDao;
import com.utp.sistemafinanciero.model.almacen.Producto;

/** Implementación en memoria del {@link ProductoDao}. */
public class ProductoDaoMemoria extends RepositorioMemoria<Producto> implements ProductoDao {

    public ProductoDaoMemoria() {
        super(Producto::getIdProducto, Producto::setIdProducto, false);
    }

    @Override
    public int contarStockBajo() {
        return filtrar(p -> p.getStockActual() != null && p.getStockMinimo() != null
                && p.getStockActual() <= p.getStockMinimo()).size();
    }
}
