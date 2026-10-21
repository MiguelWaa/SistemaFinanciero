package com.utp.sistemafinanciero.dto;

import com.utp.sistemafinanciero.model.clientes.Cliente;

/**
 * DTO con los datos del cliente que se envían al Portal del Cliente
 * (sin credenciales). Cuando es la respuesta de un inicio de sesión o
 * registro, lleva además el token de la sesión del cliente.
 */
public class ClientePublicoDTO {

    private final Integer idCliente;
    private final String tipoDocumento;
    private final String documento;
    private final String nombres;
    private final String apellidos;
    private final String telefono;
    private final String correo;
    private final String direccion;
    private final String estado;
    private final String token;

    private ClientePublicoDTO(Cliente c, String token) {
        this.idCliente = c.getIdCliente();
        this.tipoDocumento = c.getTipoDocumento();
        this.documento = c.getDocumento();
        this.nombres = c.getNombres();
        this.apellidos = c.getApellidos();
        this.telefono = c.getTelefono();
        this.correo = c.getCorreo();
        this.direccion = c.getDireccion();
        this.estado = c.getEstado();
        this.token = token;
    }

    public static ClientePublicoDTO de(Cliente c) {
        return new ClientePublicoDTO(c, null);
    }

    public static ClientePublicoDTO conToken(Cliente c, String token) {
        return new ClientePublicoDTO(c, token);
    }

    public Integer getIdCliente() { return idCliente; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getDocumento() { return documento; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
    public String getEstado() { return estado; }
    public String getToken() { return token; }
}
