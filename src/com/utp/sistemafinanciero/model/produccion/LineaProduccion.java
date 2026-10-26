package com.utp.sistemafinanciero.model.produccion;

/**
 * Entidad que representa la tabla lineas_produccion del
 * Sistema Integral de Gestion de Servicios Financieros.
 */
public class LineaProduccion {

    // Identificador único de la línea (PK) -> columna: id_linea
    private Integer idLinea;

    // Código identificador de la línea -> columna: codigo
    private String codigo;

    // Nombre de la línea de producción -> columna: nombre
    private String nombre;

    // Descripción de la línea -> columna: descripcion
    private String descripcion;

    // Estado de la línea -> columna: estado
    private String estado;

    public LineaProduccion() {
    }

    public LineaProduccion(Integer idLinea, String codigo, String nombre, String descripcion, String estado) {
        this.idLinea = idLinea;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Integer getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(Integer idLinea) {
        this.idLinea = idLinea;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "LineaProduccion{" + "idLinea=" + idLinea + ", " + "codigo=" + codigo + ", " + "nombre=" + nombre + ", " + "descripcion=" + descripcion + ", " + "estado=" + estado + "}";
    }
}
