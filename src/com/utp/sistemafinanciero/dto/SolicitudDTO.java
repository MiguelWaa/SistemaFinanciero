package com.utp.sistemafinanciero.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de la bandeja de solicitudes del administrador. Reúne en un solo
 * objeto datos de cuatro tablas (operacion, cliente, prestamo/empeno y
 * articulo) que el DAO obtiene con un JOIN. Es un ejemplo típico de DTO:
 * no corresponde a ninguna entidad, sino a lo que necesita la vista.
 */
public class SolicitudDTO {

    private Integer idOperacion;
    private Integer idCliente;
    private String cliente;
    private String documento;
    private String telefono;
    private String correo;
    private String tipoOperacion;
    private BigDecimal monto;
    private BigDecimal totalRecibir;
    private String monedaOrigen;
    private String monedaDestino;
    private String estado;
    private LocalDateTime fechaOperacion;
    private BigDecimal cuotaMensual;
    private Integer plazoMeses;
    private BigDecimal tasaInteres;
    private Integer plazoDias;
    private String articulo;

    public Integer getIdOperacion() { return idOperacion; }
    public void setIdOperacion(Integer v) { this.idOperacion = v; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer v) { this.idCliente = v; }
    public String getCliente() { return cliente; }
    public void setCliente(String v) { this.cliente = v; }
    public String getDocumento() { return documento; }
    public void setDocumento(String v) { this.documento = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getCorreo() { return correo; }
    public void setCorreo(String v) { this.correo = v; }
    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String v) { this.tipoOperacion = v; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal v) { this.monto = v; }
    public BigDecimal getTotalRecibir() { return totalRecibir; }
    public void setTotalRecibir(BigDecimal v) { this.totalRecibir = v; }
    public String getMonedaOrigen() { return monedaOrigen; }
    public void setMonedaOrigen(String v) { this.monedaOrigen = v; }
    public String getMonedaDestino() { return monedaDestino; }
    public void setMonedaDestino(String v) { this.monedaDestino = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public LocalDateTime getFechaOperacion() { return fechaOperacion; }
    public void setFechaOperacion(LocalDateTime v) { this.fechaOperacion = v; }
    public BigDecimal getCuotaMensual() { return cuotaMensual; }
    public void setCuotaMensual(BigDecimal v) { this.cuotaMensual = v; }
    public Integer getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(Integer v) { this.plazoMeses = v; }
    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal v) { this.tasaInteres = v; }
    public Integer getPlazoDias() { return plazoDias; }
    public void setPlazoDias(Integer v) { this.plazoDias = v; }
    public String getArticulo() { return articulo; }
    public void setArticulo(String v) { this.articulo = v; }
}
