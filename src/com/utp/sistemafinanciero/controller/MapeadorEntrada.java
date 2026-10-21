package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.model.almacen.Producto;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;
import com.utp.sistemafinanciero.model.logistica.Proveedor;
import com.utp.sistemafinanciero.model.marketing.Campania;
import com.utp.sistemafinanciero.model.marketing.Prospecto;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.model.ventas.Venta;
import com.utp.sistemafinanciero.service.ReglaNegocioException;
import com.utp.sistemafinanciero.util.Json;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Mapeador de entrada: convierte el cuerpo JSON de una petición en la
 * entidad del modelo correspondiente. Separa la traducción de datos de
 * los controladores (principio de responsabilidad única, SRP).
 *
 * <p>Solo copia los campos que el usuario puede enviar: los campos que
 * calcula el servidor (fechas de registro, cuotas, saldos, ids) se
 * ignoran aunque lleguen en el JSON (protección contra
 * <i>mass assignment</i>).</p>
 */
public final class MapeadorEntrada {

    private MapeadorEntrada() {
    }

    public static Rol aRol(Map<String, Object> b) {
        Rol r = new Rol();
        r.setNombre(Json.str(b, "nombre"));
        r.setDescripcion(Json.str(b, "descripcion"));
        return r;
    }

    public static Usuario aUsuario(Map<String, Object> b) {
        Usuario u = new Usuario();
        u.setNombre(Json.str(b, "nombre"));
        u.setApellido(Json.str(b, "apellido"));
        u.setEmail(Json.str(b, "email"));
        u.setPassword(Json.str(b, "password"));
        u.setIdRol(Json.intOrNull(b, "idRol"));
        u.setEstado(Json.str(b, "estado"));
        return u;
    }

    public static Cliente aCliente(Map<String, Object> b) {
        Cliente c = new Cliente();
        c.setTipoDocumento(Json.str(b, "tipoDocumento"));
        c.setDocumento(Json.str(b, "documento"));
        c.setNombres(Json.str(b, "nombres"));
        c.setApellidos(Json.str(b, "apellidos"));
        c.setFechaNacimiento(fecha(b, "fechaNacimiento"));
        c.setTelefono(Json.str(b, "telefono"));
        c.setCorreo(Json.str(b, "correo"));
        c.setDireccion(Json.str(b, "direccion"));
        c.setEstado(Json.str(b, "estado"));
        return c;
    }

    public static ServicioFinanciero aServicio(Map<String, Object> b) {
        ServicioFinanciero s = new ServicioFinanciero();
        s.setNombre(Json.str(b, "nombre"));
        s.setTipoServicio(Json.str(b, "tipoServicio"));
        s.setDescripcion(Json.str(b, "descripcion"));
        s.setEstado(Json.str(b, "estado"));
        return s;
    }

    public static TipoCambio aTipoCambio(Map<String, Object> b) {
        TipoCambio t = new TipoCambio();
        t.setMonedaOrigen(Json.str(b, "monedaOrigen"));
        t.setMonedaDestino(Json.str(b, "monedaDestino"));
        t.setCompra(Json.decimalOrNull(b, "compra"));
        t.setVenta(Json.decimalOrNull(b, "venta"));
        t.setFechaInicio(fecha(b, "fechaInicio"));
        t.setEstado(Json.str(b, "estado"));
        return t;
    }

    public static Operacion aOperacion(Map<String, Object> b) {
        Operacion o = new Operacion();
        o.setIdCliente(Json.intOrNull(b, "idCliente"));
        o.setIdTipoCambio(Json.intOrNull(b, "idTipoCambio"));
        o.setTipoOperacion(Json.str(b, "tipoOperacion"));
        o.setMonedaOrigen(Json.str(b, "monedaOrigen"));
        o.setMonedaDestino(Json.str(b, "monedaDestino"));
        o.setMonto(Json.decimalOrNull(b, "monto"));
        o.setTipoCambio(Json.decimalOrNull(b, "tipoCambio"));
        // totalEntregar y totalRecibir NO se aceptan del cliente: los calcula el servidor.
        o.setEstado(Json.str(b, "estado"));
        return o;
    }

    public static Prestamo aPrestamo(Map<String, Object> b) {
        Prestamo p = new Prestamo();
        p.setIdOperacion(Json.intOrNull(b, "idOperacion"));
        p.setMontoPrestamo(Json.decimalOrNull(b, "montoPrestamo"));
        p.setTasaInteres(Json.decimalOrNull(b, "tasaInteres"));
        p.setPlazoMeses(Json.intOrNull(b, "plazoMeses"));
        p.setFechaInicio(fecha(b, "fechaInicio"));
        p.setEstado(Json.str(b, "estado"));
        return p;
    }

    public static Empeno aEmpeno(Map<String, Object> b) {
        Empeno e = new Empeno();
        e.setIdOperacion(Json.intOrNull(b, "idOperacion"));
        e.setIdArticulo(Json.intOrNull(b, "idArticulo"));
        e.setMontoPrestamo(Json.decimalOrNull(b, "montoPrestamo"));
        e.setTasaInteres(Json.decimalOrNull(b, "tasaInteres"));
        e.setPlazoDias(Json.intOrNull(b, "plazoDias"));
        e.setFechaEmpeno(fecha(b, "fechaEmpeno"));
        e.setEstado(Json.str(b, "estado"));
        return e;
    }

    public static Producto aProducto(Map<String, Object> b) {
        Producto p = new Producto();
        p.setCodigo(Json.str(b, "codigo"));
        p.setNombre(Json.str(b, "nombre"));
        p.setDescripcion(Json.str(b, "descripcion"));
        p.setCategoria(Json.str(b, "categoria"));
        p.setUnidadMedida(Json.str(b, "unidadMedida"));
        p.setStockActual(Json.intOrNull(b, "stockActual"));
        p.setStockMinimo(Json.intOrNull(b, "stockMinimo"));
        p.setEstado(Json.str(b, "estado"));
        return p;
    }

    public static Proveedor aProveedor(Map<String, Object> b) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(Json.str(b, "razonSocial"));
        p.setRuc(Json.str(b, "ruc"));
        p.setContacto(Json.str(b, "contacto"));
        p.setTelefono(Json.str(b, "telefono"));
        p.setCorreo(Json.str(b, "correo"));
        p.setDireccion(Json.str(b, "direccion"));
        p.setEstado(Json.str(b, "estado"));
        return p;
    }

    public static Campania aCampania(Map<String, Object> b) {
        Campania c = new Campania();
        c.setNombre(Json.str(b, "nombre"));
        c.setDescripcion(Json.str(b, "descripcion"));
        c.setFechaInicio(fecha(b, "fechaInicio"));
        c.setFechaFin(fecha(b, "fechaFin"));
        c.setPresupuesto(Json.decimalOrNull(b, "presupuesto"));
        c.setCanal(Json.str(b, "canal"));
        c.setEstado(Json.str(b, "estado"));
        return c;
    }

    public static Prospecto aProspecto(Map<String, Object> b) {
        Prospecto p = new Prospecto();
        p.setNombres(Json.str(b, "nombres"));
        p.setApellidos(Json.str(b, "apellidos"));
        p.setDocumento(Json.str(b, "documento"));
        p.setTelefono(Json.str(b, "telefono"));
        p.setCorreo(Json.str(b, "correo"));
        p.setOrigen(Json.str(b, "origen"));
        p.setEstado(Json.str(b, "estado"));
        return p;
    }

    public static Venta aVenta(Map<String, Object> b) {
        Venta v = new Venta();
        v.setIdCliente(Json.intOrNull(b, "idCliente"));
        v.setIdServicio(Json.intOrNull(b, "idServicio"));
        v.setMonto(Json.decimalOrNull(b, "monto"));
        v.setImporte(Json.decimalOrNull(b, "importe"));
        v.setEstado(Json.str(b, "estado"));
        return v;
    }

    public static ExpedienteAprobacion aExpediente(Map<String, Object> b) {
        ExpedienteAprobacion e = new ExpedienteAprobacion();
        e.setCodigo(Json.str(b, "codigo"));
        e.setArea(Json.str(b, "area"));
        e.setSolicitante(Json.str(b, "solicitante"));
        e.setMonto(Json.decimalOrNull(b, "monto"));
        e.setImpacto(Json.str(b, "impacto"));
        e.setDescripcion(Json.str(b, "descripcion"));
        e.setEstado(Json.str(b, "estado"));
        return e;
    }

    public static LineaProduccion aLinea(Map<String, Object> b) {
        LineaProduccion l = new LineaProduccion();
        l.setCodigo(Json.str(b, "codigo"));
        l.setNombre(Json.str(b, "nombre"));
        l.setDescripcion(Json.str(b, "descripcion"));
        l.setEstado(Json.str(b, "estado"));
        return l;
    }

    public static OrdenProduccion aOrden(Map<String, Object> b) {
        OrdenProduccion o = new OrdenProduccion();
        o.setIdProducto(Json.intOrNull(b, "idProducto"));
        o.setIdLinea(Json.intOrNull(b, "idLinea"));
        o.setCantidad(Json.intOrNull(b, "cantidad"));
        o.setFechaInicio(fecha(b, "fechaInicio"));
        o.setFechaFinEstimada(fecha(b, "fechaFinEstimada"));
        o.setAvance(Json.decimalOrNull(b, "avance"));
        o.setEstado(Json.str(b, "estado"));
        o.setObservaciones(Json.str(b, "observaciones"));
        return o;
    }

    /** Convierte "YYYY-MM-DD" (o ISO con hora) a LocalDate, o null. */
    static LocalDate fecha(Map<String, Object> b, String clave) {
        String v = Json.str(b, clave);
        if (v == null || v.isBlank()) return null;
        try {
            return LocalDate.parse(v.length() >= 10 ? v.substring(0, 10) : v);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La fecha '" + v + "' no es válida (use AAAA-MM-DD).");
        }
    }
}
