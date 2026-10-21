package com.utp.sistemafinanciero.model.seguridad;

import java.time.LocalDateTime;

/**
 * Entidad que representa la tabla {@code auditoria}: bitácora de las
 * acciones relevantes (inicios de sesión, altas, cambios, bajas,
 * respaldos y accesos denegados) para trazabilidad y control.
 *
 * Proyecto: Curso Integrador I Sistemas - Software (UTP)
 */
public class Auditoria {

    // Identificador (PK) -> columna: id_auditoria
    private Integer idAuditoria;
    // Fecha y hora del evento -> columna: fecha
    private LocalDateTime fecha;
    // Usuario o cliente que realizó la acción -> columna: usuario
    private String usuario;
    // Rol del usuario (ADMIN, CAJERO, CLIENTE...) -> columna: rol
    private String rol;
    // Dirección IP de origen -> columna: ip
    private String ip;
    // Acción (LOGIN_OK, LOGIN_FALLIDO, CREAR, ACTUALIZAR, ELIMINAR, ACCESO_DENEGADO, RESPALDO) -> columna: accion
    private String accion;
    // Método y recurso de la API -> columna: recurso
    private String recurso;
    // Código de respuesta HTTP -> columna: estado_http
    private Integer estadoHttp;
    // Detalle adicional -> columna: detalle
    private String detalle;

    public Auditoria() {
    }

    public Auditoria(String usuario, String rol, String ip, String accion,
                     String recurso, Integer estadoHttp, String detalle) {
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.rol = rol;
        this.ip = ip;
        this.accion = accion;
        this.recurso = recurso;
        this.estadoHttp = estadoHttp;
        this.detalle = detalle;
    }

    public Integer getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Integer idAuditoria) { this.idAuditoria = idAuditoria; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }
    public Integer getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(Integer estadoHttp) { this.estadoHttp = estadoHttp; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    @Override
    public String toString() {
        return "Auditoria{" + fecha + " " + usuario + " " + accion + " " + recurso + " " + estadoHttp + "}";
    }
}
