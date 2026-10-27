package com.utp.sistemafinanciero.config;

import com.utp.sistemafinanciero.backup.EstrategiaRespaldo;
import com.utp.sistemafinanciero.backup.RespaldoJdbcSql;
import com.utp.sistemafinanciero.backup.RespaldoMemoriaJson;
import com.utp.sistemafinanciero.backup.RespaldoPgDump;
import com.utp.sistemafinanciero.backup.RespaldoService;
import com.utp.sistemafinanciero.dao.DaoFactory;
import com.utp.sistemafinanciero.dao.jdbc.JdbcDaoFactory;
import com.utp.sistemafinanciero.dao.memoria.DatosMasivos;
import com.utp.sistemafinanciero.dao.memoria.DatosPagos;
import com.utp.sistemafinanciero.dao.memoria.DatosSemilla;
import com.utp.sistemafinanciero.dao.memoria.MemoriaDaoFactory;
import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.LimitadorIntentos;
import com.utp.sistemafinanciero.security.PasswordHasher;
import com.utp.sistemafinanciero.security.Pbkdf2PasswordHasher;
import com.utp.sistemafinanciero.security.PoliticaAcceso;
import com.utp.sistemafinanciero.service.*;
import com.utp.sistemafinanciero.version.GitCli;
import com.utp.sistemafinanciero.version.VersionService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * <b>Raíz de composición</b> (Composition Root): el único lugar del
 * sistema donde se crean los objetos y se "conectan" entre sí
 * (inyección de dependencias manual, sin frameworks).
 *
 * <p>Los servicios reciben por constructor las interfaces que necesitan
 * (DAO, cifrado, sesiones). Aquí se decide qué implementación concreta
 * usar: PostgreSQL o memoria. Gracias a ello las pruebas unitarias
 * pueden armar el sistema completo sin base de datos.</p>
 */
public class Contenedor {

    // ---- Infraestructura ----
    public final DaoFactory bd;
    public final PasswordHasher hasher;
    public final GestorSesiones sesiones;
    public final LimitadorIntentos limitador;
    public final PoliticaAcceso politica;
    public final RespaldoService respaldos;
    /** Control de versiones: Git + VERSION + esquema de la base. */
    public final VersionService versiones;

    // ---- Servicios (lógica de negocio) ----
    public final AuthService auth;
    public final DashboardService dashboard;
    public final RolService roles;
    public final UsuarioService usuarios;
    public final ClienteService clientes;
    public final ServicioFinancieroService servicios;
    public final TipoCambioService tiposCambio;
    public final OperacionService operaciones;
    public final PrestamoService prestamos;
    public final EmpenoService empenos;
    public final ProductoService productos;
    public final ProveedorService proveedores;
    public final CampaniaService campanias;
    public final ProspectoService prospectos;
    public final VentaService ventas;
    public final ExpedienteService expedientes;
    public final ProduccionService produccion;
    public final SolicitudService solicitudes;
    public final PortalService portal;
    /** Pagos con Yape, Plin y Tunki (portal y caja) y billeteras de cobro. */
    public final PagoService pagos;
    /** Cotizaciones de préstamos, empeños y cambio de moneda. */
    public final CotizacionService cotizaciones;
    public final AuditoriaService auditoria;
    /** Importación y exportación de ficheros (CSV, Excel, JSON) de todas las áreas. */
    public final ArchivoService archivos;

    public Contenedor(DaoFactory bd, PasswordHasher hasher, GestorSesiones sesiones,
                      LimitadorIntentos limitador, List<EstrategiaRespaldo> estrategiasRespaldo,
                      Path carpetaRespaldos, int retenerRespaldos) {
        this.bd = bd;
        this.hasher = hasher;
        this.sesiones = sesiones;
        this.limitador = limitador;
        this.politica = new PoliticaAcceso();
        this.respaldos = new RespaldoService(estrategiasRespaldo, carpetaRespaldos, retenerRespaldos);

        this.auth = new AuthService(bd.usuarios(), bd.roles(), hasher, sesiones, limitador);
        this.dashboard = new DashboardService(bd);
        this.roles = new RolService(bd.roles());
        this.usuarios = new UsuarioService(bd.usuarios(), hasher);
        this.clientes = new ClienteService(bd.clientes());
        this.servicios = new ServicioFinancieroService(bd.servicios());
        this.tiposCambio = new TipoCambioService(bd.tiposCambio());
        this.operaciones = new OperacionService(bd.operaciones(), bd.tiposCambio());
        this.prestamos = new PrestamoService(bd.prestamos());
        this.empenos = new EmpenoService(bd.empenos());
        this.productos = new ProductoService(bd.productos());
        this.proveedores = new ProveedorService(bd.proveedores());
        this.campanias = new CampaniaService(bd.campanias());
        this.prospectos = new ProspectoService(bd.prospectos());
        this.ventas = new VentaService(bd.ventas());
        this.expedientes = new ExpedienteService(bd.expedientes());
        this.produccion = new ProduccionService(bd.lineasProduccion(), bd.ordenesProduccion(), bd.productos());
        this.solicitudes = new SolicitudService(bd.operaciones(), bd.prestamos(), bd.empenos());
        this.portal = new PortalService(bd.clientes(), bd.accesosCliente(), bd.operaciones(),
                bd.articulos(), bd.tiposCambio(), prestamos, empenos, hasher, sesiones, limitador);
        this.pagos = new PagoService(bd.pagos(), bd.billeteras(), bd.clientes(), bd.operaciones(),
                bd.prestamos(), bd.empenos(), Configuracion.pagosConfirmacionAutomatica(),
                Configuracion.pagosMontoMaximo());
        this.cotizaciones = new CotizacionService(bd.cotizaciones(), bd.clientes(), bd.tiposCambio(), portal);
        this.auditoria = new AuditoriaService(bd.auditoria());
        this.archivos = new ArchivoService(Configuracion.importacionMaxFilas());
        Path raiz = Paths.get("").toAbsolutePath();
        this.versiones = new VersionService(new GitCli(raiz), bd.versionesEsquema(), raiz);
    }

    /** Arma el sistema según {@code db.properties} (PostgreSQL o memoria). */
    public static Contenedor desdeConfiguracion() {
        PasswordHasher hasher = new Pbkdf2PasswordHasher(Configuracion.pbkdf2Iteraciones());
        GestorSesiones sesiones = new GestorSesiones(Configuracion.sesionMinutos());
        LimitadorIntentos limitador = new LimitadorIntentos(Configuracion.intentosMaximos(),
                Configuracion.bloqueoMinutos());
        Path carpeta = Paths.get(Configuracion.backupDir());

        if ("memoria".equals(Configuracion.persistencia())) {
            MemoriaDaoFactory bd = new MemoriaDaoFactory();
            DatosSemilla.cargar(bd, hasher);
            boolean masivos = !"false".equalsIgnoreCase(Configuracion.get("memoria.datos.masivos", "true"));
            if (masivos) {
                DatosMasivos.cargar(bd, hasher);   // 100 registros por módulo (solo modo demostración)
            }
            Contenedor c = new Contenedor(bd, hasher, sesiones, limitador,
                    List.of(new RespaldoMemoriaJson(bd)), carpeta, Configuracion.backupRetener());
            if (masivos) {
                DatosPagos.cargar(c, bd);          // pagos con QR y cotizaciones de demostración
            }
            return c;
        }
        DaoFactory bd = new JdbcDaoFactory();
        List<EstrategiaRespaldo> estrategias = List.of(
                new RespaldoPgDump(Configuracion.backupPgDump(), Configuracion.dbHost(), Configuracion.dbPort(),
                        Configuracion.dbName(), Configuracion.dbUser(), Configuracion.dbPassword()),
                new RespaldoJdbcSql());
        return new Contenedor(bd, hasher, sesiones, limitador, estrategias, carpeta, Configuracion.backupRetener());
    }

    /**
     * Sistema completo en memoria con datos de ejemplo, para las pruebas
     * automatizadas (usa menos iteraciones PBKDF2 para que sean rápidas).
     */
    public static Contenedor enMemoria(Path carpetaRespaldos) {
        PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);
        MemoriaDaoFactory bd = new MemoriaDaoFactory();
        DatosSemilla.cargar(bd, hasher);
        return new Contenedor(bd, hasher, new GestorSesiones(30), new LimitadorIntentos(5, 5),
                List.of(new RespaldoMemoriaJson(bd)), carpetaRespaldos, 5);
    }
}
