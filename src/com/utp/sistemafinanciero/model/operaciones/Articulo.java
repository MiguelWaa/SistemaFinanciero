package com.utp.sistemafinanciero.model.operaciones;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa la tabla articulos del
 * Sistema Integral de Gestion de Servicios Financieros.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Articulo {

    // Identificador único del artículo (PK) -> columna: id_articulo
    private Integer idArticulo;

    // Cliente propietario (FK) -> columna: id_cliente
    private Integer idCliente;

    // Descripción del artículo -> columna: descripcion
    private String descripcion;

    // Categoría del artículo -> columna: categoria
    private String categoria;

    // Marca del artículo -> columna: marca
    private String marca;

    // Modelo del artículo -> columna: modelo
    private String modelo;

    // Número de serie -> columna: numero_serie
    private String numeroSerie;

    // Estado físico del artículo -> columna: estado_fisico
    private String estadoFisico;

    // Valor estimado -> columna: valor_estimado
    private BigDecimal valorEstimado;

    // Fecha de registro -> columna: fecha_registro
    private LocalDate fechaRegistro;

    // Estado del artículo -> columna: estado
    private String estado;

    public Articulo() {
    }

    public Articulo(Integer idArticulo, Integer idCliente, String descripcion, String categoria, String marca, String modelo, String numeroSerie, String estadoFisico, BigDecimal valorEstimado, LocalDate fechaRegistro, String estado) {
        this.idArticulo = idArticulo;
        this.idCliente = idCliente;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.estadoFisico = estadoFisico;
        this.valorEstimado = valorEstimado;
        this.fechaRegistro = fechaRegistro;
        this.estado = estado;
    }

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Integer idArticulo) {
        this.idArticulo = idArticulo;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public String getEstadoFisico() {
        return estadoFisico;
    }

    public void setEstadoFisico(String estadoFisico) {
        this.estadoFisico = estadoFisico;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Articulo{" + "idArticulo=" + idArticulo + ", " + "idCliente=" + idCliente + ", " + "descripcion=" + descripcion + ", " + "categoria=" + categoria + ", " + "marca=" + marca + ", " + "modelo=" + modelo + ", " + "numeroSerie=" + numeroSerie + ", " + "estadoFisico=" + estadoFisico + ", " + "valorEstimado=" + valorEstimado + ", " + "fechaRegistro=" + fechaRegistro + ", " + "estado=" + estado + "}";
    }
}
