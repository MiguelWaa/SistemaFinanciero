package com.utp.sistemafinanciero.model.pagos;

import java.time.LocalDateTime;

/**
 * Billetera digital con la que la entidad recibe pagos (Yape, Plin,
 * Tunki). Tabla {@code billetera}.
 *
 * <p>El administrador registra el número de celular, el titular y sube la
 * imagen del QR de cobro que le da su propia app. Esa imagen es la que
 * el cliente escanea en el Portal; si no se sube, el portal muestra un
 * QR de respaldo con el número y el monto.</p>
 */
public class Billetera {

    // PK -> columna: id_billetera
    private Integer idBilletera;
    // YAPE, PLIN o TUNKI -> columna: codigo
    private String codigo;
    // Nombre visible -> columna: nombre
    private String nombre;
    // Celular asociado a la billetera -> columna: numero_celular
    private String numeroCelular;
    // Titular que verá el cliente al pagar -> columna: titular
    private String titular;
    // Imagen del QR de cobro (data URL) -> columna: qr_imagen
    private String qrImagen;
    // Indicaciones para el cliente -> columna: instrucciones
    private String instrucciones;
    // Si se muestra en el portal -> columna: activo
    private Boolean activo;
    // Última modificación -> columna: fecha_actualizacion
    private LocalDateTime fechaActualizacion;

    public Billetera() {
    }

    public Billetera(Integer idBilletera, String codigo, String nombre, String numeroCelular, String titular,
                     String qrImagen, String instrucciones, Boolean activo, LocalDateTime fechaActualizacion) {
        this.idBilletera = idBilletera;
        this.codigo = codigo;
        this.nombre = nombre;
        this.numeroCelular = numeroCelular;
        this.titular = titular;
        this.qrImagen = qrImagen;
        this.instrucciones = instrucciones;
        this.activo = activo;
        this.fechaActualizacion = fechaActualizacion;
    }

    /** Indica si el administrador ya subió el QR oficial de la billetera. */
    public boolean isTieneQr() {
        return qrImagen != null && !qrImagen.isBlank();
    }

    public Integer getIdBilletera() { return idBilletera; }
    public void setIdBilletera(Integer idBilletera) { this.idBilletera = idBilletera; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNumeroCelular() { return numeroCelular; }
    public void setNumeroCelular(String numeroCelular) { this.numeroCelular = numeroCelular; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public String getQrImagen() { return qrImagen; }
    public void setQrImagen(String qrImagen) { this.qrImagen = qrImagen; }
    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
