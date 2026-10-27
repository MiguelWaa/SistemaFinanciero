package com.utp.sistemafinanciero.dao.jdbc;

import com.utp.sistemafinanciero.dao.*;

/**
 * Familia de DAO para PostgreSQL (JDBC). Cada DAO es sin estado, por lo
 * que se crea una única instancia de cada uno (se reutiliza en toda la
 * aplicación).
 */
public class JdbcDaoFactory implements DaoFactory {

    private final RolDao roles = new RolDaoJdbc();
    private final UsuarioDao usuarios = new UsuarioDaoJdbc();
    private final ClienteDao clientes = new ClienteDaoJdbc();
    private final AccesoClienteDao accesos = new AccesoClienteDaoJdbc();
    private final ServicioFinancieroDao servicios = new ServicioFinancieroDaoJdbc();
    private final TipoCambioDao tipos = new TipoCambioDaoJdbc();
    private final OperacionDao operaciones = new OperacionDaoJdbc();
    private final PrestamoDao prestamos = new PrestamoDaoJdbc();
    private final EmpenoDao empenos = new EmpenoDaoJdbc();
    private final ArticuloDao articulos = new ArticuloDaoJdbc();
    private final ProductoDao productos = new ProductoDaoJdbc();
    private final ProveedorDao proveedores = new ProveedorDaoJdbc();
    private final CampaniaDao campanias = new CampaniaDaoJdbc();
    private final ProspectoDao prospectos = new ProspectoDaoJdbc();
    private final VentaDao ventas = new VentaDaoJdbc();
    private final LineaProduccionDao lineas = new LineaProduccionDaoJdbc();
    private final OrdenProduccionDao ordenes = new OrdenProduccionDaoJdbc();
    private final ExpedienteAprobacionDao expedientes = new ExpedienteAprobacionDaoJdbc();
    private final AuditoriaDao auditoria = new AuditoriaDaoJdbc();
    private final VersionEsquemaDao versionesEsquema = new VersionEsquemaDaoJdbc();
    private final PagoDao pagos = new PagoDaoJdbc();
    private final BilleteraDao billeteras = new BilleteraDaoJdbc();
    private final CotizacionDao cotizaciones = new CotizacionDaoJdbc();

    @Override public String nombre() { return "PostgreSQL (JDBC)"; }
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
