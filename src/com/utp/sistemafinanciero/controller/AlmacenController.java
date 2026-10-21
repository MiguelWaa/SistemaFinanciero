package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.service.ProductoService;
import com.utp.sistemafinanciero.web.ApiServer;

import static com.utp.sistemafinanciero.controller.Exportar.texto;

/** Módulo Almacén: productos, alerta de stock bajo y exportación. */
public class AlmacenController implements Controlador {

    private final ProductoService productos;

    public AlmacenController(ProductoService productos) {
        this.productos = productos;
    }

    @Override
    public void registrar(ApiServer app) {
        app.get("/api/productos/excel", ctx -> Exportar.excel("productos.xlsx", "Productos",
                new String[]{"N°", "Código", "Nombre", "Categoría", "Stock actual", "Stock mínimo", "Estado"},
                productos.listar(), p -> new String[]{
                        texto(p.getIdProducto()), texto(p.getCodigo()), texto(p.getNombre()),
                        texto(p.getCategoria()),
                        p.getStockActual() == null ? "0" : String.valueOf(p.getStockActual()),
                        p.getStockMinimo() == null ? "0" : String.valueOf(p.getStockMinimo()),
                        texto(p.getEstado())}));
        new ControladorCrud<>("productos", productos, MapeadorEntrada::aProducto).registrar(app);
    }
}
