package com.utp.sistemafinanciero.dao;

/**
 * Fábrica abstracta de DAO (patrón <b>Abstract Factory</b>).
 *
 * <p>Agrupa la creación de todos los DAO de una misma "familia" de
 * persistencia. El sistema trae dos familias intercambiables:</p>
 * <ul>
 *   <li>{@code dao.jdbc.JdbcDaoFactory} → PostgreSQL (producción).</li>
 *   <li>{@code dao.memoria.MemoriaDaoFactory} → memoria RAM (pruebas
 *       unitarias TDD y modo demostración sin base de datos).</li>
 * </ul>
 * <p>Cambiar de una a otra no exige modificar ningún servicio
 * (principio abierto/cerrado, OCP): basta con la propiedad
 * {@code persistencia} de {@code db.properties}.</p>
 */
public interface DaoFactory {

    /** Nombre legible de la familia de persistencia (para diagnóstico). */
    String nombre();

    RolDao roles();
    UsuarioDao usuarios();
    ClienteDao clientes();
    AccesoClienteDao accesosCliente();
    ServicioFinancieroDao servicios();
    TipoCambioDao tiposCambio();
    OperacionDao operaciones();
    PrestamoDao prestamos();
    EmpenoDao empenos();
    ArticuloDao articulos();
    ProductoDao productos();
    ProveedorDao proveedores();
    CampaniaDao campanias();
    ProspectoDao prospectos();
    VentaDao ventas();
    LineaProduccionDao lineasProduccion();
    OrdenProduccionDao ordenesProduccion();
    ExpedienteAprobacionDao expedientes();
    AuditoriaDao auditoria();
    /** Historial de versiones del esquema de la base (tabla version_esquema). */
    VersionEsquemaDao versionesEsquema();
    /** Pagos con billeteras digitales (Yape, Plin, Tunki) y en caja. */
    PagoDao pagos();
    /** Billeteras digitales de cobro de la entidad. */
    BilleteraDao billeteras();
    /** Cotizaciones de préstamos, empeños y cambio de moneda. */
    CotizacionDao cotizaciones();
}
