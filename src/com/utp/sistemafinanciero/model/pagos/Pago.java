package com.utp.sistemafinanciero.model.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago realizado por un cliente con una billetera digital (Yape, Plin,
 * Tunki) o registrado en caja. Tabla {@code pago}.
 *
 * <p>Ciclo de vida: el cliente paga escaneando el QR desde el Portal y
 * registra el número de operación que le muestra su app → el pago queda
 * {@code POR_VERIFICAR} y aparece en el sistema interno → el cajero lo
 * compara con su billetera y lo {@code CONFIRMADO} (se descuenta del
 * saldo del préstamo o empeño y se emite el comprobante) o lo
 * {@code RECHAZADO}.</p>
 */
public class Pago {

    public static final String POR_VERIFICAR = "POR_VERIFICAR";
    public static final String CONFIRMADO = "CONFIRMADO";
    public static final String RECHAZADO = "RECHAZADO";

    // Identificador del pago (PK) -> columna: id_pago
    private Integer idPago;
    // Cliente que paga (FK) -> columna: id_cliente
    private Integer idCliente;
    // Trámite que se paga (FK, opcional) -> columna: id_operacion
    private Integer idOperacion;
    // CUOTA_PRESTAMO, RESCATE_EMPENO, CAMBIO_MONEDA u OTRO -> columna: concepto
    private String concepto;
    // Texto libre que describe el pago -> columna: descripcion
    private String descripcion;
    // YAPE, PLIN, TUNKI (portal) · EFECTIVO, TRANSFERENCIA (solo caja) -> columna: metodo
    private String metodo;
    // Importe pagado -> columna: monto
    private BigDecimal monto;
    // Moneda (las billeteras digitales operan en soles) -> columna: moneda
    private String moneda;
    // Número de operación que muestra la app de la billetera -> columna: numero_operacion
    private String numeroOperacion;
    // Celular desde el que se pagó -> columna: celular_origen
    private String celularOrigen;
    // Código que el cliente escribe en el mensaje del pago (ej. C12-OP45) -> columna: referencia
    private String referencia;
    // PORTAL (lo registró el cliente) o CAJA (lo registró el personal) -> columna: origen
    private String origen;
    // POR_VERIFICAR, CONFIRMADO o RECHAZADO -> columna: estado
    private String estado;
    // Fecha y hora del registro -> columna: fecha_pago
    private LocalDateTime fechaPago;
    // Fecha y hora de la verificación -> columna: fecha_verificacion
    private LocalDateTime fechaVerificacion;
    // Usuario del personal que verificó -> columna: verificado_por
    private String verificadoPor;
    // Motivo del rechazo u observación -> columna: observacion
    private String observacion;
    // Captura de la constancia (data URL de imagen, opcional) -> columna: constancia
    private String constancia;

    // Datos de apoyo para la vista (no son columnas de la tabla)
    /** Los listados no traen la imagen: el DAO marca aquí si existe. */
    private Boolean conConstancia;
    private String cliente;
    private String documento;

    public Pago() {
    }

    /** Código legible del pago (PG-000001). */
    public String getCodigo() {
        return idPago == null ? null : String.format("PG-%06d", idPago);
    }

    /** Número del comprobante de pago; solo existe cuando el pago está confirmado. */
    public String getNumeroComprobante() {
        return idPago == null || !CONFIRMADO.equals(estado) ? null : String.format("RC-%06d", idPago);
    }

    /** Indica si el cliente adjuntó la captura de su constancia. */
    public boolean isTieneConstancia() {
        return (constancia != null && !constancia.isBlank()) || Boolean.TRUE.equals(conConstancia);
    }

    /** Lo usa el DAO al listar sin la imagen. */
    public void setConConstancia(Boolean conConstancia) { this.conConstancia = conConstancia; }

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public Integer getIdOperacion() { return idOperacion; }
    public void setIdOperacion(Integer idOperacion) { this.idOperacion = idOperacion; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }
    public String getCelularOrigen() { return celularOrigen; }
    public void setCelularOrigen(String celularOrigen) { this.celularOrigen = celularOrigen; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }
    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }
    public String getVerificadoPor() { return verificadoPor; }
    public void setVerificadoPor(String verificadoPor) { this.verificadoPor = verificadoPor; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getConstancia() { return constancia; }
    public void setConstancia(String constancia) { this.constancia = constancia; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
}
