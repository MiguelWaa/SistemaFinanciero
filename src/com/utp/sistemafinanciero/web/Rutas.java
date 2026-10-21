package com.utp.sistemafinanciero.web;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.controller.*;
import com.utp.sistemafinanciero.security.FiltroSeguridad;

import java.util.List;

/**
 * Registro central de la API: instala la cadena de seguridad, la
 * auditoría y los controladores de cada módulo del negocio.
 */
public final class Rutas {

    private Rutas() {
    }

    public static void registrar(ApiServer app, Contenedor c) {
        // 1) Seguridad por defecto: toda ruta /api pasa por autenticación y autorización.
        app.usarFiltro(new FiltroSeguridad(c.sesiones, c.politica));
        // 2) Auditoría de cada petición relevante (Observer).
        app.observar(c.auditoria);

        // 3) Controladores (uno por módulo del negocio).
        List<Controlador> controladores = List.of(
                new SistemaController(c.dashboard, c.bd),
                new AuthController(c.auth),
                new SeguridadController(c.usuarios, c.roles, c.auditoria, c.politica, c.sesiones),
                new ClienteController(c.clientes, c.servicios),
                new OperacionController(c.operaciones, c.tiposCambio, c.prestamos, c.empenos),
                new VentaController(c.ventas, c.bd.ventas()),
                new MarketingController(c.campanias, c.prospectos),
                new AlmacenController(c.productos),
                new LogisticaController(c.proveedores),
                new ProduccionController(c.produccion),
                new DireccionController(c.expedientes),
                new SolicitudController(c.solicitudes),
                new PortalController(c.portal),
                new RespaldoController(c.respaldos),
                // Importación / exportación de ficheros de todas las áreas (CSV, Excel, JSON)
                new ArchivoController(c.archivos, CatalogoArchivos.crear(c)));
        for (Controlador ctrl : controladores) {
            ctrl.registrar(app);
        }
    }
}
