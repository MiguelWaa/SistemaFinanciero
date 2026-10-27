package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.*;

/**
 * Familia de DAO en memoria (patrón Abstract Factory). Se usa en las
 * pruebas unitarias y en el modo demostración ({@code persistencia=memoria}).
 */
public class MemoriaDaoFactory implements DaoFactory {

    private final RolDao roles = new RolDaoMemoria();
    private final UsuarioDao usuarios = new UsuarioDaoMemoria();
    private final ClienteDao clientes = new ClienteDaoMemoria();
    private final AccesoClienteDao accesos = new AccesoClienteDaoMemoria();
    private final ServicioFinancieroDao servicios = new ServicioFinancieroDaoMemoria();
    private final TipoCambioDao tipos = new TipoCambioDaoMemoria();
    private final OperacionDao operaciones = new OperacionDaoMemoria(this);
    private final PrestamoDao prestamos = new PrestamoDaoMemoria();
    private final EmpenoDao empenos = new EmpenoDaoMemoria();
    private final ArticuloDao articulos = new ArticuloDaoMemoria();
    private final ProductoDao productos = new ProductoDaoMemoria();
    private final ProveedorDao proveedores = new ProveedorDaoMemoria();
    private final CampaniaDao campanias = new CampaniaDaoMemoria();
    private final ProspectoDao prospectos = new ProspectoDaoMemoria();
    private final VentaDao ventas = new VentaDaoMemoria(this);
    private final LineaProduccionDao lineas = new LineaProduccionDaoMemoria();
    private final OrdenProduccionDao ordenes = new OrdenProduccionDaoMemoria();
    private final ExpedienteAprobacionDao expedientes = new ExpedienteAprobacionDaoMemoria();
    private final AuditoriaDao auditoria = new AuditoriaDaoMemoria();
    private final VersionEsquemaDao versionesEsquema = new VersionEsquemaDaoMemoria();
    private final PagoDao pagos = new PagoDaoMemoria(this);
    private final BilleteraDao billeteras = new BilleteraDaoMemoria();
    private final CotizacionDao cotizaciones = new CotizacionDaoMemoria(this);

    @Override public String nombre() { return "Memoria (demostración)"; }
    @Override public RolDao roles() { return roles; }
    @Override public UsuarioDao usuarios() { return usuarios; }
    @Override public ClienteDao clientes() { return clientes; }
    @Override public AccesoClienteDao accesosCliente() { return accesos; }
    @Override public ServicioFinancieroDao servicios() { return servicios; }
    @Override public TipoCambioDao tiposCambio() { return tipos; }
    @Override public OperacionDao operaciones() { return operaciones; }
    @Override public PrestamoDao prestamos() { return prestamos; }
    @Override public EmpenoDao empenos() { return empenos; }
    @Override public ArticuloDao articulos() { return articulos; }
    @Override public ProductoDao productos() { return productos; }
    @Override public ProveedorDao proveedores() { return proveedores; }
    @Override public CampaniaDao campanias() { return campanias; }
    @Override public ProspectoDao prospectos() { return prospectos; }
    @Override public VentaDao ventas() { return ventas; }
    @Override public LineaProduccionDao lineasProduccion() { return lineas; }
    @Override public OrdenProduccionDao ordenesProduccion() { return ordenes; }
    @Override public ExpedienteAprobacionDao expedientes() { return expedientes; }
    @Override public AuditoriaDao auditoria() { return auditoria; }
    @Override public VersionEsquemaDao versionesEsquema() { return versionesEsquema; }
    @Override public PagoDao pagos() { return pagos; }
    @Override public BilleteraDao billeteras() { return billeteras; }
    @Override public CotizacionDao cotizaciones() { return cotizaciones; }
}
