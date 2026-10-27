package com.utp.sistemafinanciero.controller;

import com.utp.sistemafinanciero.archivos.Columna;
import com.utp.sistemafinanciero.archivos.DefinicionArchivo;
import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.dto.VentaReporteDTO;
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
import com.utp.sistemafinanciero.model.pagos.Cotizacion;
import com.utp.sistemafinanciero.model.pagos.Pago;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;
import com.utp.sistemafinanciero.model.seguridad.Auditoria;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.utp.sistemafinanciero.archivos.Columna.editable;
import static com.utp.sistemafinanciero.archivos.Columna.obligatoria;
import static com.utp.sistemafinanciero.archivos.Columna.salida;
import static com.utp.sistemafinanciero.archivos.TipoDato.FECHA;
import static com.utp.sistemafinanciero.archivos.TipoDato.NUMERO;
import static com.utp.sistemafinanciero.archivos.TipoDato.TEXTO;
import static com.utp.sistemafinanciero.web.AreaNegocio.ALMACEN;
import static com.utp.sistemafinanciero.web.AreaNegocio.CLIENTES;
import static com.utp.sistemafinanciero.web.AreaNegocio.DIRECCION_GERENCIA;
import static com.utp.sistemafinanciero.web.AreaNegocio.LOGISTICA;
import static com.utp.sistemafinanciero.web.AreaNegocio.MARKETING;
import static com.utp.sistemafinanciero.web.AreaNegocio.OPERACIONES;
import static com.utp.sistemafinanciero.web.AreaNegocio.PRODUCCION;
import static com.utp.sistemafinanciero.web.AreaNegocio.SEGURIDAD;
import static com.utp.sistemafinanciero.web.AreaNegocio.VENTAS;

/**
 * Catálogo de ficheros por área del negocio: qué recursos se exportan,
 * cuáles también se importan y con qué columnas.
 *
 * <table>
 *   <caption>Resumen</caption>
 *   <tr><th>Área</th><th>Exporta (xlsx · csv · json)</th><th>Importa (xlsx · csv)</th></tr>
 *   <tr><td>Marketing</td><td>prospectos, campañas</td><td>prospectos, campañas</td></tr>
 *   <tr><td>Ventas</td><td>ventas</td><td>—</td></tr>
 *   <tr><td>Logística</td><td>proveedores</td><td>proveedores</td></tr>
 *   <tr><td>Almacén</td><td>productos</td><td>productos</td></tr>
 *   <tr><td>Producción</td><td>líneas, órdenes</td><td>líneas</td></tr>
 *   <tr><td>Operaciones</td><td>operaciones, tipos de cambio, préstamos, empeños</td><td>—</td></tr>
 *   <tr><td>Dirección y Gerencia</td><td>expedientes</td><td>expedientes</td></tr>
 *   <tr><td>Clientes</td><td>clientes, servicios</td><td>clientes</td></tr>
 *   <tr><td>Seguridad</td><td>auditoría (solo ADMIN)</td><td>—</td></tr>
 * </table>
 *
 * <p>Regla de seguridad: los movimientos de dinero (operaciones, ventas,
 * préstamos, empeños, tipos de cambio) <b>nunca se importan</b>; solo se
 * registran desde el sistema, donde el servidor calcula los totales.
 * Tampoco se importan ids, fechas de registro ni estados de aprobación.</p>
 */
public final class CatalogoArchivos {

    private CatalogoArchivos() {
    }

    public static Map<String, DefinicionArchivo<?>> crear(Contenedor c) {
        Map<String, DefinicionArchivo<?>> m = new LinkedHashMap<>();

        // ---------------- Marketing ----------------
        agregar(m, DefinicionArchivo.<Prospecto>exportarEImportar("prospectos", MARKETING, "Prospectos", List.of(
                salida("idProspecto", "N°", NUMERO, Prospecto::getIdProspecto),
                obligatoria("nombres", "Nombres", TEXTO, Prospecto::getNombres, "Luis"),
                editable("apellidos", "Apellidos", TEXTO, Prospecto::getApellidos, "Gonzales Quispe"),
                editable("documento", "Documento", TEXTO, Prospecto::getDocumento, "70011122"),
                editable("telefono", "Teléfono", TEXTO, Prospecto::getTelefono, "987000111"),
                editable("correo", "Correo", TEXTO, Prospecto::getCorreo, "luis@correo.com"),
                editable("origen", "Origen", TEXTO, Prospecto::getOrigen, "FERIA"),
                editable("estado", "Estado", TEXTO, Prospecto::getEstado, "NUEVO"),
                salida("fechaRegistro", "Registrado", TEXTO, Prospecto::getFechaRegistro)),
                c.prospectos::listar, MapeadorEntrada::aProspecto, c.prospectos::crear));

        agregar(m, DefinicionArchivo.<Campania>exportarEImportar("campanias", MARKETING, "Campañas", List.of(
                salida("idCampania", "N°", NUMERO, Campania::getIdCampania),
                obligatoria("nombre", "Nombre", TEXTO, Campania::getNombre, "Cambio sin comisión"),
                editable("descripcion", "Descripción", TEXTO, Campania::getDescripcion, "Captar clientes nuevos"),
                editable("canal", "Canal", TEXTO, Campania::getCanal, "REDES"),
                editable("presupuesto", "Presupuesto", NUMERO, Campania::getPresupuesto, "1500.00"),
                editable("fechaInicio", "Fecha de inicio", FECHA, Campania::getFechaInicio, "2026-11-01"),
                editable("fechaFin", "Fecha de fin", FECHA, Campania::getFechaFin, "2026-11-30"),
                editable("estado", "Estado", TEXTO, Campania::getEstado, "PLANIFICADA")),
                c.campanias::listar, MapeadorEntrada::aCampania, c.campanias::crear));

        // ---------------- Ventas (solo exportar) ----------------
        agregar(m, DefinicionArchivo.<VentaReporteDTO>soloExportar("ventas", VENTAS, "Ventas", List.of(
                salida("idVenta", "N° Venta", NUMERO, VentaReporteDTO::getIdVenta),
                salida("cliente", "Cliente", TEXTO, VentaReporteDTO::getCliente),
                salida("servicio", "Servicio", TEXTO, VentaReporteDTO::getServicio),
                salida("fecha", "Fecha", TEXTO, VentaReporteDTO::getFecha),
                salida("importe", "Importe (S/)", NUMERO, VentaReporteDTO::getImporte),
                salida("estado", "Estado", TEXTO, VentaReporteDTO::getEstado)),
                () -> c.bd.ventas().reporte()));

        // ---------------- Logística ----------------
        agregar(m, DefinicionArchivo.<Proveedor>exportarEImportar("proveedores", LOGISTICA, "Proveedores", List.of(
                salida("idProveedor", "N°", NUMERO, Proveedor::getIdProveedor),
                obligatoria("razonSocial", "Razón social", TEXTO, Proveedor::getRazonSocial, "Seguridad Andina SAC"),
                editable("ruc", "RUC", TEXTO, Proveedor::getRuc, "20123456789"),
                editable("contacto", "Contacto", TEXTO, Proveedor::getContacto, "Ana Torres"),
                editable("telefono", "Teléfono", TEXTO, Proveedor::getTelefono, "014445566"),
                editable("correo", "Correo", TEXTO, Proveedor::getCorreo, "ventas@andina.pe"),
                editable("direccion", "Dirección", TEXTO, Proveedor::getDireccion, "Av. Arequipa 123, Lima"),
                editable("estado", "Estado", TEXTO, Proveedor::getEstado, "ACTIVO")),
                c.proveedores::listar, MapeadorEntrada::aProveedor, c.proveedores::crear));

        // ---------------- Almacén ----------------
        agregar(m, DefinicionArchivo.<Producto>exportarEImportar("productos", ALMACEN, "Productos", List.of(
                salida("idProducto", "N°", NUMERO, Producto::getIdProducto),
                editable("codigo", "Código", TEXTO, Producto::getCodigo, "INS-010"),
                obligatoria("nombre", "Nombre", TEXTO, Producto::getNombre, "Papel bond A4"),
                editable("descripcion", "Descripción", TEXTO, Producto::getDescripcion, "Paquete x 500 hojas"),
                editable("categoria", "Categoría", TEXTO, Producto::getCategoria, "INSUMO"),
                editable("unidadMedida", "Unidad de medida", TEXTO, Producto::getUnidadMedida, "PAQUETE"),
                editable("stockActual", "Stock actual", NUMERO, Producto::getStockActual, "40"),
                editable("stockMinimo", "Stock mínimo", NUMERO, Producto::getStockMinimo, "10"),
                editable("estado", "Estado", TEXTO, Producto::getEstado, "ACTIVO")),
                c.productos::listar, MapeadorEntrada::aProducto, c.productos::crear));

        // ---------------- Producción ----------------
        agregar(m, DefinicionArchivo.<LineaProduccion>exportarEImportar("lineas", PRODUCCION, "Líneas", List.of(
                salida("idLinea", "N°", NUMERO, LineaProduccion::getIdLinea),
                editable("codigo", "Código", TEXTO, LineaProduccion::getCodigo, "LIN-03"),
                obligatoria("nombre", "Nombre", TEXTO, LineaProduccion::getNombre, "Tasación de joyas"),
                editable("descripcion", "Descripción", TEXTO, LineaProduccion::getDescripcion, "Evaluación de garantías"),
                editable("estado", "Estado", TEXTO, LineaProduccion::getEstado, "ACTIVA")),
                c.produccion::listarLineas, MapeadorEntrada::aLinea, c.produccion::crearLinea));

        agregar(m, DefinicionArchivo.<OrdenProduccion>soloExportar("ordenes", PRODUCCION, "Órdenes", List.of(
                salida("idOrden", "N°", NUMERO, OrdenProduccion::getIdOrden),
                salida("idProducto", "Producto (N°)", NUMERO, OrdenProduccion::getIdProducto),
                salida("idLinea", "Línea (N°)", NUMERO, OrdenProduccion::getIdLinea),
                salida("cantidad", "Cantidad", NUMERO, OrdenProduccion::getCantidad),
                salida("fechaInicio", "Inicio", TEXTO, OrdenProduccion::getFechaInicio),
                salida("fechaFinEstimada", "Fin estimado", TEXTO, OrdenProduccion::getFechaFinEstimada),
                salida("fechaFinReal", "Fin real", TEXTO, OrdenProduccion::getFechaFinReal),
                salida("avance", "Avance (%)", NUMERO, OrdenProduccion::getAvance),
                salida("estado", "Estado", TEXTO, OrdenProduccion::getEstado),
                salida("observaciones", "Observaciones", TEXTO, OrdenProduccion::getObservaciones)),
                c.produccion::listarOrdenes));

        // ---------------- Operaciones (solo exportar: dinero) ----------------
        agregar(m, DefinicionArchivo.<Operacion>soloExportar("operaciones", OPERACIONES, "Operaciones", List.of(
                salida("idOperacion", "N°", NUMERO, Operacion::getIdOperacion),
                salida("idCliente", "Cliente (N°)", NUMERO, Operacion::getIdCliente),
                salida("tipoOperacion", "Tipo", TEXTO, Operacion::getTipoOperacion),
                salida("monedaOrigen", "Moneda origen", TEXTO, Operacion::getMonedaOrigen),
                salida("monedaDestino", "Moneda destino", TEXTO, Operacion::getMonedaDestino),
                salida("monto", "Monto", NUMERO, Operacion::getMonto),
                salida("tipoCambio", "Tipo de cambio", NUMERO, Operacion::getTipoCambio),
                salida("totalRecibir", "Total a recibir", NUMERO, Operacion::getTotalRecibir),
                salida("estado", "Estado", TEXTO, Operacion::getEstado),
                salida("fechaOperacion", "Fecha", TEXTO, Operacion::getFechaOperacion)),
                c.operaciones::listar));

        agregar(m, DefinicionArchivo.<TipoCambio>soloExportar("tipos-cambio", OPERACIONES, "Tipos de cambio", List.of(
                salida("idTipoCambio", "N°", NUMERO, TipoCambio::getIdTipoCambio),
                salida("monedaOrigen", "Moneda origen", TEXTO, TipoCambio::getMonedaOrigen),
                salida("monedaDestino", "Moneda destino", TEXTO, TipoCambio::getMonedaDestino),
                salida("compra", "Compra", NUMERO, TipoCambio::getCompra),
                salida("venta", "Venta", NUMERO, TipoCambio::getVenta),
                salida("fechaInicio", "Vigente desde", TEXTO, TipoCambio::getFechaInicio),
                salida("estado", "Estado", TEXTO, TipoCambio::getEstado)),
                c.tiposCambio::listar));

        agregar(m, DefinicionArchivo.<Prestamo>soloExportar("prestamos", OPERACIONES, "Préstamos", List.of(
                salida("idPrestamo", "N°", NUMERO, Prestamo::getIdPrestamo),
                salida("idOperacion", "Operación (N°)", NUMERO, Prestamo::getIdOperacion),
                salida("montoPrestamo", "Monto", NUMERO, Prestamo::getMontoPrestamo),
                salida("tasaInteres", "Tasa (%)", NUMERO, Prestamo::getTasaInteres),
                salida("plazoMeses", "Plazo (meses)", NUMERO, Prestamo::getPlazoMeses),
                salida("cuotaMensual", "Cuota mensual", NUMERO, Prestamo::getCuotaMensual),
                salida("fechaInicio", "Inicio", TEXTO, Prestamo::getFechaInicio),
                salida("fechaVencimiento", "Vencimiento", TEXTO, Prestamo::getFechaVencimiento),
                salida("saldoPendiente", "Saldo pendiente", NUMERO, Prestamo::getSaldoPendiente),
                salida("estado", "Estado", TEXTO, Prestamo::getEstado)),
                c.prestamos::listar));

        agregar(m, DefinicionArchivo.<Empeno>soloExportar("empenos", OPERACIONES, "Empeños", List.of(
                salida("idEmpeno", "N°", NUMERO, Empeno::getIdEmpeno),
                salida("idOperacion", "Operación (N°)", NUMERO, Empeno::getIdOperacion),
                salida("idArticulo", "Artículo (N°)", NUMERO, Empeno::getIdArticulo),
                salida("montoPrestamo", "Monto", NUMERO, Empeno::getMontoPrestamo),
                salida("tasaInteres", "Tasa (%)", NUMERO, Empeno::getTasaInteres),
                salida("plazoDias", "Plazo (días)", NUMERO, Empeno::getPlazoDias),
                salida("fechaEmpeno", "Fecha", TEXTO, Empeno::getFechaEmpeno),
                salida("fechaVencimiento", "Vencimiento", TEXTO, Empeno::getFechaVencimiento),
                salida("saldoPendiente", "Saldo pendiente", NUMERO, Empeno::getSaldoPendiente),
                salida("estado", "Estado", TEXTO, Empeno::getEstado)),
                c.empenos::listar));

        // ---------------- Pagos con Yape, Plin, Tunki y caja (solo exportar: dinero) ----------------
        agregar(m, DefinicionArchivo.<Pago>soloExportar("pagos", OPERACIONES, "Pagos", List.of(
                salida("codigo", "Código", TEXTO, Pago::getCodigo),
                salida("fechaPago", "Fecha", TEXTO, Pago::getFechaPago),
                salida("cliente", "Cliente", TEXTO, Pago::getCliente),
                salida("documento", "Documento", TEXTO, Pago::getDocumento),
                salida("concepto", "Concepto", TEXTO, Pago::getConcepto),
                salida("descripcion", "Descripción", TEXTO, Pago::getDescripcion),
                salida("idOperacion", "Trámite (N°)", NUMERO, Pago::getIdOperacion),
                salida("metodo", "Método", TEXTO, Pago::getMetodo),
                salida("numeroOperacion", "N° de operación", TEXTO, Pago::getNumeroOperacion),
                salida("celularOrigen", "Celular", TEXTO, Pago::getCelularOrigen),
                salida("referencia", "Referencia", TEXTO, Pago::getReferencia),
                salida("monto", "Monto (S/)", NUMERO, Pago::getMonto),
                salida("origen", "Origen", TEXTO, Pago::getOrigen),
                salida("estado", "Estado", TEXTO, Pago::getEstado),
                salida("numeroComprobante", "Comprobante", TEXTO, Pago::getNumeroComprobante),
                salida("verificadoPor", "Verificado por", TEXTO, Pago::getVerificadoPor),
                salida("fechaVerificacion", "Fecha de verificación", TEXTO, Pago::getFechaVerificacion),
                salida("observacion", "Observación", TEXTO, Pago::getObservacion)),
                () -> c.pagos.listar(null)));

        agregar(m, DefinicionArchivo.<Cotizacion>soloExportar("cotizaciones", OPERACIONES, "Cotizaciones", List.of(
                salida("numero", "Número", TEXTO, Cotizacion::getNumero),
                salida("fechaEmision", "Emitida", TEXTO, Cotizacion::getFechaEmision),
                salida("fechaVencimiento", "Vigente hasta", TEXTO, Cotizacion::getFechaVencimiento),
                salida("cliente", "Cliente", TEXTO, Cotizacion::getCliente),
                salida("documento", "Documento", TEXTO, Cotizacion::getDocumento),
                salida("tipo", "Tipo", TEXTO, Cotizacion::getTipo),
                salida("monto", "Monto", NUMERO, Cotizacion::getMonto),
                salida("moneda", "Moneda", TEXTO, Cotizacion::getMoneda),
                salida("plazo", "Plazo", NUMERO, Cotizacion::getPlazo),
                salida("unidadPlazo", "Unidad", TEXTO, Cotizacion::getUnidadPlazo),
                salida("tasa", "Tasa (%)", NUMERO, Cotizacion::getTasa),
                salida("cuota", "Cuota", NUMERO, Cotizacion::getCuota),
                salida("totalInteres", "Interés", NUMERO, Cotizacion::getTotalInteres),
                salida("totalPagar", "Total a pagar", NUMERO, Cotizacion::getTotalPagar),
                salida("montoRecibir", "Recibe (cambio)", NUMERO, Cotizacion::getMontoRecibir),
                salida("estado", "Estado", TEXTO, Cotizacion::getEstado),
                salida("idOperacion", "Solicitud (N°)", NUMERO, Cotizacion::getIdOperacion),
                salida("origen", "Origen", TEXTO, Cotizacion::getOrigen)),
                c.cotizaciones::listar));

        // ---------------- Dirección y Gerencia ----------------
        // El estado NO se importa: todo expediente importado entra PENDIENTE y se resuelve en el sistema.
        agregar(m, DefinicionArchivo.<ExpedienteAprobacion>exportarEImportar("expedientes", DIRECCION_GERENCIA,
                "Expedientes", List.of(
                salida("idExpediente", "N°", NUMERO, ExpedienteAprobacion::getIdExpediente),
                obligatoria("codigo", "Código", TEXTO, ExpedienteAprobacion::getCodigo, "EXP-2026-050"),
                editable("area", "Área", TEXTO, ExpedienteAprobacion::getArea, "OPERACIONES"),
                editable("solicitante", "Solicitante", TEXTO, ExpedienteAprobacion::getSolicitante, "Jefatura de caja"),
                editable("monto", "Monto", NUMERO, ExpedienteAprobacion::getMonto, "25000.00"),
                editable("impacto", "Impacto", TEXTO, ExpedienteAprobacion::getImpacto, "MEDIO"),
                editable("descripcion", "Descripción", TEXTO, ExpedienteAprobacion::getDescripcion,
                        "Ampliar fondo de caja"),
                salida("estado", "Estado", TEXTO, ExpedienteAprobacion::getEstado),
                salida("fechaSolicitud", "Solicitado", TEXTO, ExpedienteAprobacion::getFechaSolicitud),
                salida("fechaResolucion", "Resuelto", TEXTO, ExpedienteAprobacion::getFechaResolucion)),
                c.expedientes::listar, MapeadorEntrada::aExpediente, c.expedientes::crear));

        // ---------------- Clientes y servicios ----------------
        agregar(m, DefinicionArchivo.<Cliente>exportarEImportar("clientes", CLIENTES, "Clientes", List.of(
                salida("idCliente", "N°", NUMERO, Cliente::getIdCliente),
                editable("tipoDocumento", "Tipo de documento", TEXTO, Cliente::getTipoDocumento, "DNI"),
                obligatoria("documento", "Documento", TEXTO, Cliente::getDocumento, "70123456"),
                obligatoria("nombres", "Nombres", TEXTO, Cliente::getNombres, "María"),
                editable("apellidos", "Apellidos", TEXTO, Cliente::getApellidos, "Quispe Mamani"),
                editable("fechaNacimiento", "Fecha de nacimiento", FECHA, Cliente::getFechaNacimiento, "1995-04-18"),
                editable("telefono", "Teléfono", TEXTO, Cliente::getTelefono, "951222333"),
                editable("correo", "Correo", TEXTO, Cliente::getCorreo, "maria@correo.com"),
                editable("direccion", "Dirección", TEXTO, Cliente::getDireccion, "Jr. Lima 450, Juliaca"),
                editable("estado", "Estado", TEXTO, Cliente::getEstado, "ACTIVO"),
                salida("fechaRegistro", "Registrado", TEXTO, Cliente::getFechaRegistro)),
                c.clientes::listar, MapeadorEntrada::aCliente, c.clientes::crear));

        agregar(m, DefinicionArchivo.<ServicioFinanciero>soloExportar("servicios", CLIENTES, "Servicios", List.of(
                salida("idServicio", "N°", NUMERO, ServicioFinanciero::getIdServicio),
                salida("nombre", "Servicio", TEXTO, ServicioFinanciero::getNombre),
                salida("tipoServicio", "Tipo", TEXTO, ServicioFinanciero::getTipoServicio),
                salida("descripcion", "Descripción", TEXTO, ServicioFinanciero::getDescripcion),
                salida("estado", "Estado", TEXTO, ServicioFinanciero::getEstado)),
                c.servicios::listar));

        // ---------------- Seguridad (módulo auditoría: solo ADMIN por PoliticaAcceso) ----------------
        agregar(m, DefinicionArchivo.<Auditoria>soloExportar("auditoria", SEGURIDAD, "Auditoría", List.of(
                salida("idAuditoria", "N°", NUMERO, Auditoria::getIdAuditoria),
                salida("fecha", "Fecha", TEXTO, Auditoria::getFecha),
                salida("usuario", "Usuario", TEXTO, Auditoria::getUsuario),
                salida("rol", "Rol", TEXTO, Auditoria::getRol),
                salida("ip", "IP", TEXTO, Auditoria::getIp),
                salida("accion", "Acción", TEXTO, Auditoria::getAccion),
                salida("recurso", "Recurso", TEXTO, Auditoria::getRecurso),
                salida("estadoHttp", "HTTP", NUMERO, Auditoria::getEstadoHttp),
                salida("detalle", "Detalle", TEXTO, Auditoria::getDetalle)),
                () -> c.auditoria.recientes(500)));

        return m;
    }

    private static void agregar(Map<String, DefinicionArchivo<?>> m, DefinicionArchivo<?> d) {
        m.put(d.recurso(), d);
    }
}
