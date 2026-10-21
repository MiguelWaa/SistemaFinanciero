package com.utp.sistemafinanciero.model.almacen;

/**
 * Entidad que representa la tabla productos del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Producto {

    // Identificador único del producto (PK) -> columna: id_producto
    private Integer idProducto;

    // Código único de inventario -> columna: codigo
    private String codigo;

    // Nombre del producto -> columna: nombre
    private String nombre;

    // Descripción del producto -> columna: descripcion
    private String descripcion;

    // Categoría del producto -> columna: categoria
    private String categoria;

    // Unidad de medida -> columna: unidad_medida
    private String unidadMedida;

    // Cantidad actual en stock -> columna: stock_actual
    private Integer stockActual;

    // Cantidad mínima permitida -> columna: stock_minimo
    private Integer stockMinimo;

    // Estado del producto -> columna: estado
    private String estado;

    public Producto() {
    }

    public Producto(Integer idProducto, String codigo, String nombre, String descripcion, String categoria, String unidadMedida, Integer stockActual, Integer stockMinimo, String estado) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(Integer stockActual) {
        this.stockActual = stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Producto{" + "idProducto=" + idProducto + ", " + "codigo=" + codigo + ", " + "nombre=" + nombre + ", " + "descripcion=" + descripcion + ", " + "categoria=" + categoria + ", " + "unidadMedida=" + unidadMedida + ", " + "stockActual=" + stockActual + ", " + "stockMinimo=" + stockMinimo + ", " + "estado=" + estado + "}";
    }
}
