package com.utp.sistemafinanciero.model.pagos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cotización (proforma) de un préstamo, un empeño o un cambio de moneda.
 * Tabla {@code cotizacion}.
 *
 * <p>La calcula el servidor con las mismas fórmulas del sistema, tiene un
 * número correlativo (COT-000001) y una vigencia. El cliente puede
 * imprimirla o convertirla en solicitud con un clic.</p>
 */
public class Cotizacion {

    public static final String VIGENTE = "VIGENTE";
    public static final String CONVERTIDA = "CONVERTIDA";
    public static final String VENCIDA = "VENCIDA";

    private Integer idCotizacion;      // id_cotizacion
    private Integer idCliente;         // id_cliente
    private String tipo;               // PRESTAMO, EMPENO, CAMBIO
    private BigDecimal monto;          // monto solicitado / a cambiar
    private String moneda;             // moneda del monto
    private Integer plazo;             // plazo (meses o días)
    private String unidadPlazo;        // MESES o DIAS
    private BigDecimal tasa;           // tasa de interés (%)
    private BigDecimal cuota;          // cuota mensual (préstamo)
    private BigDecimal totalInteres;   // interés total
    private BigDecimal totalPagar;     // total a pagar (préstamo/empeño)
    private Integer idTipoCambio;      // tipo de cambio usado (cambio)
    private BigDecimal tipoCambio;     // valor del tipo de cambio
    private BigDecimal montoRecibir;   // lo que recibe el cliente (cambio)
    private String monedaRecibir;      // moneda que recibe (cambio)
    private String descripcion;        // artículo del empeño u observación
    private BigDecimal valorEstimado;  // valor del artículo (empeño)
    private String estado;             // VIGENTE, CONVERTIDA, VENCIDA
    private LocalDateTime fechaEmision;
    private LocalDate fechaVencimiento;
    private Integer idOperacion;       // solicitud creada a partir de la cotización
    private String origen;             // PORTAL o CAJA

    // Apoyo para la vista
    private String cliente;
    private String documento;

    public Cotizacion() {
    }

    /** Número legible de la cotización (COT-000001). */
    public String getNumero() {
        return idCotizacion == null ? null : String.format("COT-%06d", idCotizacion);
    }

    public Integer getIdCotizacion() { return idCotizacion; }
    public void setIdCotizacion(Integer idCotizacion) { this.idCotizacion = idCotizacion; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public Integer getPlazo() { return plazo; }
    public void setPlazo(Integer plazo) { this.plazo = plazo; }
    public String getUnidadPlazo() { return unidadPlazo; }
    public void setUnidadPlazo(String unidadPlazo) { this.unidadPlazo = unidadPlazo; }
    public BigDecimal getTasa() { return tasa; }
    public void setTasa(BigDecimal tasa) { this.tasa = tasa; }
    public BigDecimal getCuota() { return cuota; }
    public void setCuota(BigDecimal cuota) { this.cuota = cuota; }
    public BigDecimal getTotalInteres() { return totalInteres; }
    public void setTotalInteres(BigDecimal totalInteres) { this.totalInteres = totalInteres; }
    public BigDecimal getTotalPagar() { return totalPagar; }
    public void setTotalPagar(BigDecimal totalPagar) { this.totalPagar = totalPagar; }
    public Integer getIdTipoCambio() { return idTipoCambio; }
    public void setIdTipoCambio(Integer idTipoCambio) { this.idTipoCambio = idTipoCambio; }
    public BigDecimal getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }
    public BigDecimal getMontoRecibir() { return montoRecibir; }
    public void setMontoRecibir(BigDecimal montoRecibir) { this.montoRecibir = montoRecibir; }
    public String getMonedaRecibir() { return monedaRecibir; }
    public void setMonedaRecibir(String monedaRecibir) { this.monedaRecibir = monedaRecibir; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(BigDecimal valorEstimado) { this.valorEstimado = valorEstimado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public Integer getIdOperacion() { return idOperacion; }
    public void setIdOperacion(Integer idOperacion) { this.idOperacion = idOperacion; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
}
