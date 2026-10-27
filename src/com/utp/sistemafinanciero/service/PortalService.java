package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dto.ClientePublicoDTO;
import com.utp.sistemafinanciero.dto.LoginDTO;
import com.utp.sistemafinanciero.security.GestorSesiones;
import com.utp.sistemafinanciero.security.LimitadorIntentos;
import com.utp.sistemafinanciero.security.PasswordHasher;
import com.utp.sistemafinanciero.security.Sesion;
import com.utp.sistemafinanciero.dao.AccesoClienteDao;
import com.utp.sistemafinanciero.dao.ArticuloDao;
import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.dao.OperacionDao;
import com.utp.sistemafinanciero.dao.TipoCambioDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.operaciones.Articulo;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.util.Json;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Lógica del <b>Portal del Cliente</b>: la parte pública del sistema donde
 * la propia persona crea su cuenta, inicia sesión y solicita sus trámites
 * (cambio de moneda, préstamo y empeño).
 *
 * <p>Las solicitudes se registran con estado <b>PENDIENTE</b> para que el
 * personal las revise desde el sistema interno.</p>
 */
public class PortalService {

    private static final String CREDENCIALES_INVALIDAS = "Documento o contraseña incorrectos.";

    /** Tasa mensual (%) que la entidad aplica a los préstamos pedidos desde el portal. */
    public static final BigDecimal TASA_PRESTAMO = new BigDecimal("15");
    /** Tasa mensual (%) que la entidad aplica a los empeños pedidos desde el portal. */
    public static final BigDecimal TASA_EMPENO = new BigDecimal("10");

    private final ClienteDao clienteDao;
    private final AccesoClienteDao accesoDao;
    private final OperacionDao operacionDao;
    private final ArticuloDao articuloDao;
    private final TipoCambioDao tipoCambioDao;
    private final PrestamoService prestamoService;
    private final EmpenoService empenoService;
    private final PasswordHasher hasher;
    private final GestorSesiones sesiones;
    private final LimitadorIntentos limitador;

    /** Todas las dependencias se inyectan por constructor (DIP). */
    public PortalService(ClienteDao clienteDao, AccesoClienteDao accesoDao, OperacionDao operacionDao,
                         ArticuloDao articuloDao, TipoCambioDao tipoCambioDao,
                         PrestamoService prestamoService, EmpenoService empenoService,
                         PasswordHasher hasher, GestorSesiones sesiones, LimitadorIntentos limitador) {
        this.clienteDao = clienteDao;
        this.accesoDao = accesoDao;
        this.operacionDao = operacionDao;
        this.articuloDao = articuloDao;
        this.tipoCambioDao = tipoCambioDao;
        this.prestamoService = prestamoService;
        this.empenoService = empenoService;
        this.hasher = hasher;
        this.sesiones = sesiones;
        this.limitador = limitador;
    }

    // ------------------------- Cuenta -------------------------

    /** Registra una nueva cuenta de cliente en el portal. */
    public ClientePublicoDTO registrar(Cliente c, String password) {
        if (c.getDocumento() == null || c.getDocumento().isBlank()) {
            throw new ReglaNegocioException("El número de documento es obligatorio.");
        }
        if (c.getNombres() == null || c.getNombres().isBlank()) {
            throw new ReglaNegocioException("Los nombres son obligatorios.");
        }
        if (password == null || password.length() < 6) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 6 caracteres.");
        }
        if (clienteDao.buscarPorDocumento(c.getDocumento()) != null) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese documento. Inicia sesión.");
        }
        if (c.getTipoDocumento() == null || c.getTipoDocumento().isBlank()) {
            c.setTipoDocumento("DNI");
        }
        c.setEstado("ACTIVO");
        c.setFechaRegistro(LocalDateTime.now());

        Integer id = clienteDao.crear(c);
        c.setIdCliente(id);
        accesoDao.guardar(id, hasher.hash(password));
        // Tras registrarse, el cliente entra directamente con su sesión.
        Sesion sesion = sesiones.abrir(Sesion.Tipo.CLIENTE, id, c.getDocumento(), "CLIENTE");
        return ClientePublicoDTO.conToken(c, sesion.getToken());
    }

    /** Inicia sesión con documento y contraseña; devuelve los datos y el token. */
    public ClientePublicoDTO login(LoginDTO datos) {
        if (datos.incompleto()) {
            throw new ReglaNegocioException("Ingresa tu documento y tu contraseña.");
        }
        String documento = datos.getUsuario();
        limitador.verificar("cliente:" + documento);
        Cliente c = clienteDao.buscarPorDocumento(documento);
        String guardada = c == null ? null : accesoDao.passwordDe(c.getIdCliente());
        if (c == null || guardada == null || !hasher.verificar(datos.password(), guardada)) {
            boolean bloqueada = limitador.registrarFallo("cliente:" + documento);
            throw new ReglaNegocioException(bloqueada
                    ? "Demasiados intentos fallidos. Tu acceso quedó bloqueado unos minutos."
                    : CREDENCIALES_INVALIDAS);
        }
        if (c.getEstado() != null && !"ACTIVO".equalsIgnoreCase(c.getEstado())) {
            throw new ReglaNegocioException("Tu cuenta no está activa. Comunícate con la entidad.");
        }
        limitador.registrarExito("cliente:" + documento);
        if (hasher.requiereActualizar(guardada)) {
            accesoDao.guardar(c.getIdCliente(), hasher.hash(datos.password()));
        }
        Sesion sesion = sesiones.abrir(Sesion.Tipo.CLIENTE, c.getIdCliente(), c.getDocumento(), "CLIENTE");
        return ClientePublicoDTO.conToken(c, sesion.getToken());
    }

    /** Actualiza los datos de contacto que el cliente puede editar. */
    public ClientePublicoDTO actualizarDatos(int idCliente, Map<String, Object> datos) {
        Cliente c = exigirCliente(idCliente);
        String tel = Json.str(datos, "telefono");
        String correo = Json.str(datos, "correo");
        String dir = Json.str(datos, "direccion");
        if (tel != null && !tel.isBlank()) c.setTelefono(tel);
        if (correo != null && !correo.isBlank()) c.setCorreo(correo);
        if (dir != null && !dir.isBlank()) c.setDireccion(dir);
        if (correo != null && !correo.isBlank() && !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ReglaNegocioException("El correo no tiene un formato válido.");
        }
        clienteDao.actualizar(c);
        return ClientePublicoDTO.de(c);
    }

    // ------------------------ Trámites ------------------------

    /** Solicitud de cambio de moneda. */
    public Operacion solicitarCambio(int idCliente, Integer idTipoCambio, BigDecimal monto) {
        exigirCliente(idCliente);
        if (monto == null || monto.signum() <= 0) {
            throw new ReglaNegocioException("Ingresa un monto mayor a cero.");
        }
        if (idTipoCambio == null) {
            throw new ReglaNegocioException("Selecciona el tipo de cambio.");
        }
        TipoCambio tc = tipoCambioDao.buscar(idTipoCambio);
        if (tc == null) {
            throw new ReglaNegocioException("El tipo de cambio seleccionado no existe.");
        }
        BigDecimal cambio = tc.getVenta();
        Operacion o = new Operacion();
        o.setIdCliente(idCliente);
        o.setIdTipoCambio(idTipoCambio);
        o.setTipoOperacion("CAMBIO");
        o.setMonedaOrigen(tc.getMonedaOrigen());
        o.setMonedaDestino(tc.getMonedaDestino());
        o.setMonto(monto);
        o.setTipoCambio(cambio);
        o.setTotalEntregar(monto);
        o.setTotalRecibir(monto.multiply(cambio).setScale(2, RoundingMode.HALF_UP));
        o.setEstado("PENDIENTE");
        o.setFechaOperacion(LocalDateTime.now());
        o.setIdOperacion(operacionDao.crear(o));
        return o;
    }

    /** Solicitud de préstamo: crea la operación y el préstamo con su cuota. */
    public Map<String, Object> solicitarPrestamo(int idCliente, BigDecimal montoPrestamo,
                                                 Integer plazoMeses, BigDecimal tasaInteres) {
        exigirCliente(idCliente);
        if (montoPrestamo == null || montoPrestamo.signum() <= 0) {
            throw new ReglaNegocioException("Ingresa el monto que necesitas.");
        }
        if (plazoMeses == null || plazoMeses <= 0) {
            throw new ReglaNegocioException("Indica en cuántos meses lo pagarás.");
        }
        Operacion o = new Operacion();
        o.setIdCliente(idCliente);
        o.setTipoOperacion("PRESTAMO");
        o.setMonedaOrigen("PEN");
        o.setMonedaDestino("PEN");
        o.setMonto(montoPrestamo);
        o.setEstado("PENDIENTE");
        o.setFechaOperacion(LocalDateTime.now());
        Integer idOperacion = operacionDao.crear(o);
        o.setIdOperacion(idOperacion);

        Prestamo p = new Prestamo();
        p.setIdOperacion(idOperacion);
        p.setMontoPrestamo(montoPrestamo);
        p.setTasaInteres(tasaInteres == null ? TASA_PRESTAMO : tasaInteres);
        p.setPlazoMeses(plazoMeses);
        p.setFechaInicio(LocalDate.now());
        p.setEstado("PENDIENTE");
        Prestamo creado = prestamoService.crear(p);

        return Json.map(
                "operacion", o,
                "prestamo", creado,
                "mensaje", "Solicitud de préstamo enviada. Está pendiente de evaluación.");
    }

    /** Solicitud de empeño: registra el artículo y el empeño asociado. */
    public Map<String, Object> solicitarEmpeno(int idCliente, String descripcionArticulo,
                                               BigDecimal valorEstimado, BigDecimal montoPrestamo,
                                               Integer plazoDias) {
        exigirCliente(idCliente);
        if (descripcionArticulo == null || descripcionArticulo.isBlank()) {
            throw new ReglaNegocioException("Describe el artículo que vas a empeñar.");
        }
        if (montoPrestamo == null || montoPrestamo.signum() <= 0) {
            throw new ReglaNegocioException("Ingresa el monto que solicitas.");
        }
        if (plazoDias == null || plazoDias <= 0) {
            plazoDias = 30;
        }

        Articulo a = new Articulo();
        a.setIdCliente(idCliente);
        a.setDescripcion(descripcionArticulo);
        a.setCategoria("GENERAL");
        a.setEstadoFisico("POR EVALUAR");
        a.setValorEstimado(valorEstimado);
        a.setFechaRegistro(LocalDate.now());
        a.setEstado("ACTIVO");
        Integer idArticulo = articuloDao.crear(a);
        a.setIdArticulo(idArticulo);

        Operacion o = new Operacion();
        o.setIdCliente(idCliente);
        o.setTipoOperacion("EMPENO");
        o.setMonedaOrigen("PEN");
        o.setMonedaDestino("PEN");
        o.setMonto(montoPrestamo);
        o.setEstado("PENDIENTE");
        o.setFechaOperacion(LocalDateTime.now());
        Integer idOperacion = operacionDao.crear(o);
        o.setIdOperacion(idOperacion);

        Empeno e = new Empeno();
        e.setIdOperacion(idOperacion);
        e.setIdArticulo(idArticulo);
        e.setMontoPrestamo(montoPrestamo);
        e.setTasaInteres(TASA_EMPENO);
        e.setPlazoDias(plazoDias);
        e.setFechaEmpeno(LocalDate.now());
        e.setEstado("PENDIENTE");
        Empeno creado = empenoService.crear(e);

        return Json.map(
                "operacion", o,
                "empeno", creado,
                "articulo", a,
                "mensaje", "Solicitud de empeño enviada. Lleva tu artículo para la tasación.");
    }

    /** Historial de trámites del cliente. */
    public List<Operacion> misOperaciones(int idCliente) {
        exigirCliente(idCliente);
        return operacionDao.listarPorCliente(idCliente);
    }

    // ------------------------ Apoyo ------------------------

    private Cliente exigirCliente(int idCliente) {
        Cliente c = clienteDao.buscar(idCliente);
        if (c == null) {
            throw new ReglaNegocioException("La sesión no es válida. Vuelve a iniciar sesión.");
        }
        return c;
    }
}
