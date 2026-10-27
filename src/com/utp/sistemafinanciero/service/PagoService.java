package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.BilleteraDao;
import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.dao.EmpenoDao;
import com.utp.sistemafinanciero.dao.OperacionDao;
import com.utp.sistemafinanciero.dao.PagoDao;
import com.utp.sistemafinanciero.dao.PrestamoDao;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.model.pagos.Billetera;
import com.utp.sistemafinanciero.model.pagos.Pago;
import com.utp.sistemafinanciero.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pagos con billeteras digitales (<b>Yape, Plin y Tunki</b>) y en caja.
 *
 * <h3>Flujo desde el Portal del Cliente</h3>
 * <ol>
 *   <li>El cliente elige qué paga (cuota de préstamo, rescate de empeño,
 *       cambio de moneda u otro), el monto y la billetera.</li>
 *   <li>El portal le muestra el QR de cobro de la entidad, el número,
 *       el titular, el monto exacto y un código de referencia.</li>
 *   <li>El cliente paga desde su app y registra el <b>número de
 *       operación</b> (y, si quiere, la captura de la constancia).</li>
 *   <li>El pago queda <b>POR_VERIFICAR</b> y aparece al instante en el
 *       módulo Pagos del sistema interno.</li>
 *   <li>El cajero lo compara con los movimientos de su billetera y lo
 *       <b>confirma</b> (se descuenta del saldo y se emite el comprobante)
 *       o lo <b>rechaza</b> indicando el motivo.</li>
 * </ol>
 *
 * <p>Reglas: el monto no puede superar lo que se debe; un mismo número de
 * operación no se puede registrar dos veces con la misma billetera (evita
 * reutilizar una constancia); solo se aceptan billeteras activas. Con
 * {@code pagos.confirmacion.automatica=true} el pago se confirma al
 * registrarlo (útil para demostraciones).</p>
 */
public class PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoService.class);

    public static final Set<String> BILLETERAS = Set.of("YAPE", "PLIN", "TUNKI");
    public static final Set<String> METODOS_CAJA = Set.of("YAPE", "PLIN", "TUNKI", "EFECTIVO", "TRANSFERENCIA");
    public static final String CUOTA_PRESTAMO = "CUOTA_PRESTAMO";
    public static final String RESCATE_EMPENO = "RESCATE_EMPENO";
    public static final String CAMBIO_MONEDA = "CAMBIO_MONEDA";
    public static final String OTRO = "OTRO";
    private static final Set<String> CONCEPTOS = Set.of(CUOTA_PRESTAMO, RESCATE_EMPENO, CAMBIO_MONEDA, OTRO);

    private static final Pattern NUMERO_OPERACION = Pattern.compile("^[A-Za-z0-9-]{4,30}$");
    private static final Pattern CELULAR = Pattern.compile("^9\\d{8}$");
    private static final Pattern IMAGEN = Pattern.compile("^data:image/(png|jpeg|jpg|webp);base64,[A-Za-z0-9+/=]+$");
    /** Tamaño máximo de una imagen (QR o constancia) como data URL: ≈ 500 KB. */
    private static final int MAX_IMAGEN = 700_000;

    private final PagoDao dao;
    private final BilleteraDao billeteraDao;
    private final ClienteDao clienteDao;
    private final OperacionDao operacionDao;
    private final PrestamoDao prestamoDao;
    private final EmpenoDao empenoDao;
    private final boolean confirmacionAutomatica;
    private final BigDecimal montoMaximo;

    public PagoService(PagoDao dao, BilleteraDao billeteraDao, ClienteDao clienteDao, OperacionDao operacionDao,
                       PrestamoDao prestamoDao, EmpenoDao empenoDao, boolean confirmacionAutomatica,
                       BigDecimal montoMaximo) {
        this.dao = dao;
        this.billeteraDao = billeteraDao;
        this.clienteDao = clienteDao;
        this.operacionDao = operacionDao;
        this.prestamoDao = prestamoDao;
        this.empenoDao = empenoDao;
        this.confirmacionAutomatica = confirmacionAutomatica;
        this.montoMaximo = montoMaximo == null ? new BigDecimal("10000") : montoMaximo;
    }

    // =====================================================================
    //  Billeteras (Yape, Plin, Tunki)
    // =====================================================================

    public List<Billetera> billeteras() {
        return billeteraDao.listar();
    }

    /** Billeteras que el cliente puede usar (activas y con número). */
    public List<Billetera> billeterasActivas() {
        List<Billetera> salida = new ArrayList<>();
        for (Billetera b : billeteraDao.listar()) {
            if (Boolean.TRUE.equals(b.getActivo()) && b.getNumeroCelular() != null && !b.getNumeroCelular().isBlank()) {
                salida.add(b);
            }
        }
        return salida;
    }

    /** El ADMIN actualiza número, titular, QR, indicaciones y si está activa. */
    public Billetera actualizarBilletera(int idBilletera, Map<String, Object> datos) {
        Billetera b = billeteraDao.buscar(idBilletera);
        if (b == null) {
            throw new ReglaNegocioException("No existe la billetera con id " + idBilletera + ".");
        }
        String numero = Json.str(datos, "numeroCelular");
        if (numero != null) {
            numero = numero.replaceAll("\\s", "");
            if (!numero.isEmpty() && !CELULAR.matcher(numero).matches()) {
                throw new ReglaNegocioException("El celular de la billetera debe tener 9 dígitos y empezar con 9.");
            }
            b.setNumeroCelular(numero.isEmpty() ? null : numero);
        }
        String titular = Json.str(datos, "titular");
        if (titular != null) b.setTitular(recortar(titular, 120));
        String instr = Json.str(datos, "instrucciones");
        if (instr != null) b.setInstrucciones(recortar(instr, 300));
        if (datos.containsKey("qrImagen")) {
            String qr = Json.str(datos, "qrImagen");
            if (qr == null || qr.isBlank()) {
                b.setQrImagen(null);
            } else {
                validarImagen(qr, "La imagen del QR");
                b.setQrImagen(qr);
            }
        }
        Object activo = datos.get("activo");
        if (activo != null) b.setActivo(Boolean.parseBoolean(String.valueOf(activo)));
        if (Boolean.TRUE.equals(b.getActivo()) && (b.getNumeroCelular() == null || b.getNumeroCelular().isBlank())) {
            throw new ReglaNegocioException("Para activar " + b.getNombre() + " registre primero el número de celular.");
        }
        b.setFechaActualizacion(LocalDateTime.now());
        billeteraDao.actualizar(b);
        return b;
    }

    // =====================================================================
    //  Portal del Cliente
    // =====================================================================

    /**
     * Lo que el cliente puede pagar ahora: cuotas de préstamos activos,
     * rescate de empeños activos y cambios de moneda en soles pendientes.
     * El máximo descuenta los pagos que aún están por verificar.
     */
    public List<Map<String, Object>> pagables(int idCliente) {
        exigirCliente(idCliente);
        List<Pago> previos = dao.listarPorCliente(idCliente);
        List<Map<String, Object>> salida = new ArrayList<>();
        for (Operacion o : operacionDao.listarPorCliente(idCliente)) {
            String tipo = tipo(o);
            if (tipo.contains("PRESTAMO")) {
                Prestamo p = prestamoDao.buscarPorOperacion(o.getIdOperacion());
                if (p == null || !"ACTIVO".equalsIgnoreCase(p.getEstado()) || !positivo(p.getSaldoPendiente())) continue;
                BigDecimal disponible = p.getSaldoPendiente().subtract(enVerificacion(previos, o.getIdOperacion()));
                if (!positivo(disponible)) continue;
                BigDecimal sugerido = p.getCuotaMensual() == null ? disponible : p.getCuotaMensual().min(disponible);
                salida.add(pagable(o, CUOTA_PRESTAMO, "Cuota de préstamo N° " + o.getIdOperacion(),
                        "Cuota S/ " + texto(p.getCuotaMensual()) + " · saldo S/ " + texto(p.getSaldoPendiente())
                                + (p.getFechaVencimiento() == null ? "" : " · vence " + p.getFechaVencimiento()),
                        sugerido, disponible, p.getSaldoPendiente()));
            } else if (tipo.contains("EMPENO")) {
                Empeno e = empenoDao.buscarPorOperacion(o.getIdOperacion());
                if (e == null || !"ACTIVO".equalsIgnoreCase(e.getEstado()) || !positivo(e.getSaldoPendiente())) continue;
                BigDecimal disponible = e.getSaldoPendiente().subtract(enVerificacion(previos, o.getIdOperacion()));
                if (!positivo(disponible)) continue;
                salida.add(pagable(o, RESCATE_EMPENO, "Rescate de empeño N° " + o.getIdOperacion(),
                        "Saldo S/ " + texto(e.getSaldoPendiente())
                                + (e.getFechaVencimiento() == null ? "" : " · vence " + e.getFechaVencimiento()),
                        disponible, disponible, e.getSaldoPendiente()));
            } else if (tipo.contains("CAMBIO") && "PENDIENTE".equalsIgnoreCase(o.getEstado())
                    && "PEN".equalsIgnoreCase(o.getMonedaOrigen())) {
                boolean yaPagado = previos.stream().anyMatch(x -> Objects.equals(x.getIdOperacion(), o.getIdOperacion())
                        && !Pago.RECHAZADO.equals(x.getEstado()));
                BigDecimal total = o.getTotalEntregar() != null ? o.getTotalEntregar() : o.getMonto();
                if (yaPagado || !positivo(total)) continue;
                salida.add(pagable(o, CAMBIO_MONEDA, "Cambio de moneda N° " + o.getIdOperacion(),
                        "Entregas S/ " + texto(total) + " · recibes " + texto(o.getTotalRecibir()) + " "
                                + (o.getMonedaDestino() == null ? "" : o.getMonedaDestino()),
                        total, total, total));
            }
        }
        return salida;
    }

    /** Registra el pago que el cliente hizo con Yape, Plin o Tunki desde el portal. */
    public Pago registrarDesdePortal(int idCliente, Map<String, Object> datos) {
        exigirCliente(idCliente);
        String metodo = mayus(Json.str(datos, "metodo"));
        if (metodo == null || !BILLETERAS.contains(metodo)) {
            throw new ReglaNegocioException("Elige con qué billetera pagaste: Yape, Plin o Tunki.");
        }
        Billetera b = billeteraDao.buscarPorCodigo(metodo);
        if (b == null || !Boolean.TRUE.equals(b.getActivo())) {
            throw new ReglaNegocioException("Por ahora no recibimos pagos con " + metodo + ". Elige otra billetera.");
        }
        Pago p = armarPago(idCliente, datos, metodo, true);
        p.setOrigen("PORTAL");
        if (confirmacionAutomatica) {
            p.setEstado(Pago.CONFIRMADO);
            p.setFechaVerificacion(LocalDateTime.now());
            p.setVerificadoPor("confirmación automática");
        } else {
            p.setEstado(Pago.POR_VERIFICAR);
        }
        Integer id = dao.crear(p);
        p.setIdPago(id);
        if (Pago.CONFIRMADO.equals(p.getEstado())) {
            aplicarEfectos(p);
        }
        LOG.info("Pago {} registrado desde el portal: cliente={} metodo={} monto={} estado={}",
                p.getCodigo(), idCliente, metodo, p.getMonto(), p.getEstado());
        return dao.buscar(id);
    }

    public List<Pago> delCliente(int idCliente) {
        exigirCliente(idCliente);
        return dao.listarPorCliente(idCliente);
    }

    /** Pago de un cliente (para su comprobante); no deja ver pagos ajenos. */
    public Pago delCliente(int idCliente, int idPago) {
        Pago p = dao.buscar(idPago);
        if (p == null || !Objects.equals(p.getIdCliente(), idCliente)) {
            throw new ReglaNegocioException("No existe el pago indicado.");
        }
        return p;
    }

    /** Estado de cuenta del cliente: deudas vigentes, pagos y totales. */
    public Map<String, Object> estadoCuenta(int idCliente) {
        Cliente c = exigirCliente(idCliente);
        List<Map<String, Object>> deudas = new ArrayList<>();
        BigDecimal deudaTotal = BigDecimal.ZERO;
        for (Operacion o : operacionDao.listarPorCliente(idCliente)) {
            String tipo = tipo(o);
            if (tipo.contains("PRESTAMO")) {
                Prestamo p = prestamoDao.buscarPorOperacion(o.getIdOperacion());
                if (p == null) continue;
                deudas.add(Json.map("idOperacion", o.getIdOperacion(), "tipo", "Préstamo",
                        "monto", p.getMontoPrestamo(), "cuota", p.getCuotaMensual(), "plazo", p.getPlazoMeses() + " meses",
                        "vencimiento", p.getFechaVencimiento(), "saldo", p.getSaldoPendiente(), "estado", p.getEstado()));
                if ("ACTIVO".equalsIgnoreCase(p.getEstado()) && p.getSaldoPendiente() != null) {
                    deudaTotal = deudaTotal.add(p.getSaldoPendiente());
                }
            } else if (tipo.contains("EMPENO")) {
                Empeno e = empenoDao.buscarPorOperacion(o.getIdOperacion());
                if (e == null) continue;
                deudas.add(Json.map("idOperacion", o.getIdOperacion(), "tipo", "Empeño",
                        "monto", e.getMontoPrestamo(), "cuota", null, "plazo", e.getPlazoDias() + " días",
                        "vencimiento", e.getFechaVencimiento(), "saldo", e.getSaldoPendiente(), "estado", e.getEstado()));
                if ("ACTIVO".equalsIgnoreCase(e.getEstado()) && e.getSaldoPendiente() != null) {
                    deudaTotal = deudaTotal.add(e.getSaldoPendiente());
                }
            }
        }
        List<Pago> pagos = dao.listarPorCliente(idCliente);
        BigDecimal pagado = BigDecimal.ZERO;
        BigDecimal porVerificar = BigDecimal.ZERO;
        for (Pago p : pagos) {
            if (Pago.CONFIRMADO.equals(p.getEstado())) pagado = pagado.add(p.getMonto());
            if (Pago.POR_VERIFICAR.equals(p.getEstado())) porVerificar = porVerificar.add(p.getMonto());
        }
        return Json.map(
                "cliente", Json.map("idCliente", c.getIdCliente(), "nombre", nombre(c),
                        "documento", (c.getTipoDocumento() == null ? "" : c.getTipoDocumento() + " ") + c.getDocumento(),
                        "telefono", c.getTelefono(), "correo", c.getCorreo(), "direccion", c.getDireccion()),
                "deudas", deudas,
                "pagos", pagos,
                "deudaTotal", deudaTotal,
                "totalPagado", pagado,
                "porVerificar", porVerificar,
                "fecha", LocalDateTime.now().withNano(0));
    }

    // =====================================================================
    //  Sistema interno (cajero / supervisor / administrador)
    // =====================================================================

    public List<Pago> listar(String estado) {
        if (estado == null || estado.isBlank()) {
            return dao.listar();
        }
        return dao.listarPorEstado(estado.trim().toUpperCase(Locale.ROOT));
    }

    public Pago buscar(int idPago) {
        Pago p = dao.buscar(idPago);
        if (p == null) {
            throw new ReglaNegocioException("No existe el pago con id " + idPago + ".");
        }
        return p;
    }

    /** Imagen de la constancia que adjuntó el cliente. */
    public Map<String, Object> constancia(int idPago) {
        buscar(idPago);
        return Json.map("idPago", idPago, "imagen", dao.constancia(idPago));
    }

    /** El personal registra un pago recibido en ventanilla (queda confirmado). */
    public Pago registrarEnCaja(Map<String, Object> datos, String usuario) {
        Integer idCliente = Json.intOrNull(datos, "idCliente");
        if (idCliente == null) {
            throw new ReglaNegocioException("Selecciona el cliente que paga.");
        }
        exigirCliente(idCliente);
        String metodo = mayus(Json.str(datos, "metodo"));
        if (metodo == null || !METODOS_CAJA.contains(metodo)) {
            throw new ReglaNegocioException("Método de pago no válido. Use Yape, Plin, Tunki, efectivo o transferencia.");
        }
        Pago p = armarPago(idCliente, datos, metodo, BILLETERAS.contains(metodo));
        p.setOrigen("CAJA");
        p.setEstado(Pago.CONFIRMADO);
        p.setFechaVerificacion(LocalDateTime.now());
        p.setVerificadoPor(usuario);
        Integer id = dao.crear(p);
        p.setIdPago(id);
        aplicarEfectos(p);
        LOG.info("Pago {} registrado en caja por {}: cliente={} metodo={} monto={}",
                p.getCodigo(), usuario, idCliente, metodo, p.getMonto());
        return dao.buscar(id);
    }

    /** Confirma un pago por verificar: descuenta el saldo y emite el comprobante. */
    public Pago confirmar(int idPago, String usuario) {
        Pago p = exigirPorVerificar(idPago);
        p.setEstado(Pago.CONFIRMADO);
        p.setFechaVerificacion(LocalDateTime.now());
        p.setVerificadoPor(usuario);
        dao.actualizar(p);
        aplicarEfectos(p);
        LOG.info("Pago {} confirmado por {}", p.getCodigo(), usuario);
        return dao.buscar(idPago);
    }

    /** Rechaza un pago por verificar (por ejemplo, el dinero no llegó). */
    public Pago rechazar(int idPago, String motivo, String usuario) {
        Pago p = exigirPorVerificar(idPago);
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("Indica el motivo del rechazo para informar al cliente.");
        }
        p.setEstado(Pago.RECHAZADO);
        p.setFechaVerificacion(LocalDateTime.now());
        p.setVerificadoPor(usuario);
        p.setObservacion(recortar(motivo, 200));
        dao.actualizar(p);
        LOG.info("Pago {} rechazado por {}: {}", p.getCodigo(), usuario, motivo);
        return dao.buscar(idPago);
    }

    /** Indicadores para el módulo Pagos y los reportes. */
    public Map<String, Object> resumen() {
        List<Pago> todos = dao.listar();
        LocalDate hoy = LocalDate.now();
        YearMonth mes = YearMonth.now();
        int porVerificar = 0, confirmadosHoy = 0, rechazados = 0, confirmados = 0;
        BigDecimal montoPorVerificar = BigDecimal.ZERO, cobradoHoy = BigDecimal.ZERO, cobradoMes = BigDecimal.ZERO,
                cobradoTotal = BigDecimal.ZERO;
        Map<String, Map<String, Object>> porMetodo = new LinkedHashMap<>();
        for (String m : List.of("YAPE", "PLIN", "TUNKI", "EFECTIVO", "TRANSFERENCIA")) {
            porMetodo.put(m, Json.map("metodo", m, "cantidad", 0, "monto", BigDecimal.ZERO));
        }
        Map<String, Map<String, Object>> porConcepto = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal[]> porDia = new LinkedHashMap<>();
        for (int i = 13; i >= 0; i--) {
            porDia.put(hoy.minusDays(i), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (Pago p : todos) {
            BigDecimal monto = p.getMonto() == null ? BigDecimal.ZERO : p.getMonto();
            if (Pago.POR_VERIFICAR.equals(p.getEstado())) {
                porVerificar++;
                montoPorVerificar = montoPorVerificar.add(monto);
            } else if (Pago.RECHAZADO.equals(p.getEstado())) {
                rechazados++;
            } else if (Pago.CONFIRMADO.equals(p.getEstado())) {
                confirmados++;
                cobradoTotal = cobradoTotal.add(monto);
                LocalDate f = p.getFechaPago() == null ? null : p.getFechaPago().toLocalDate();
                if (hoy.equals(f)) {
                    confirmadosHoy++;
                    cobradoHoy = cobradoHoy.add(monto);
                }
                if (f != null && YearMonth.from(f).equals(mes)) cobradoMes = cobradoMes.add(monto);
                if (f != null && porDia.containsKey(f)) {
                    BigDecimal[] d = porDia.get(f);
                    d[0] = d[0].add(monto);
                    d[1] = d[1].add(BigDecimal.ONE);
                }
                Map<String, Object> m = porMetodo.computeIfAbsent(p.getMetodo(),
                        k -> Json.map("metodo", k, "cantidad", 0, "monto", BigDecimal.ZERO));
                m.put("cantidad", (Integer) m.get("cantidad") + 1);
                m.put("monto", ((BigDecimal) m.get("monto")).add(monto));
                Map<String, Object> c = porConcepto.computeIfAbsent(p.getConcepto(),
                        k -> Json.map("concepto", k, "cantidad", 0, "monto", BigDecimal.ZERO));
                c.put("cantidad", (Integer) c.get("cantidad") + 1);
                c.put("monto", ((BigDecimal) c.get("monto")).add(monto));
            }
        }
        List<Map<String, Object>> dias = new ArrayList<>();
        porDia.forEach((f, v) -> dias.add(Json.map("fecha", f, "monto", v[0], "cantidad", v[1].intValue())));
        return Json.map(
                "total", todos.size(),
                "porVerificar", porVerificar,
                "montoPorVerificar", montoPorVerificar,
                "confirmados", confirmados,
                "confirmadosHoy", confirmadosHoy,
                "rechazados", rechazados,
                "cobradoHoy", cobradoHoy,
                "cobradoMes", cobradoMes,
                "cobradoTotal", cobradoTotal,
                "porMetodo", new ArrayList<>(porMetodo.values()),
                "porConcepto", new ArrayList<>(porConcepto.values()),
                "porDia", dias,
                "confirmacionAutomatica", confirmacionAutomatica);
    }

    // =====================================================================
    //  Apoyo
    // =====================================================================

    /** Valida los datos comunes y arma el pago (todavía sin guardar). */
    private Pago armarPago(int idCliente, Map<String, Object> datos, String metodo, boolean exigeNumeroOperacion) {
        BigDecimal monto = Json.decimalOrNull(datos, "monto");
        if (monto == null || monto.signum() <= 0) {
            throw new ReglaNegocioException("Ingresa un monto mayor a cero.");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
        if (monto.compareTo(montoMaximo) > 0) {
            throw new ReglaNegocioException("El monto supera el máximo permitido por pago (S/ " + texto(montoMaximo) + ").");
        }
        String concepto = mayus(Json.str(datos, "concepto"));
        if (concepto == null) concepto = OTRO;
        if (!CONCEPTOS.contains(concepto)) {
            throw new ReglaNegocioException("Concepto de pago no válido.");
        }
        Integer idOperacion = Json.intOrNull(datos, "idOperacion");
        String descripcion = recortar(Json.str(datos, "descripcion"), 200);

        if (!OTRO.equals(concepto)) {
            if (idOperacion == null) {
                throw new ReglaNegocioException("Selecciona el trámite que estás pagando.");
            }
            Operacion o = operacionDao.buscar(idOperacion);
            if (o == null || !Objects.equals(o.getIdCliente(), idCliente)) {
                throw new ReglaNegocioException("El trámite seleccionado no pertenece al cliente.");
            }
            BigDecimal maximo = maximoPagable(o, concepto, idCliente);
            if (monto.compareTo(maximo) > 0) {
                throw new ReglaNegocioException("El monto supera lo pendiente de pago (S/ " + texto(maximo) + ").");
            }
            if (descripcion == null || descripcion.isBlank()) {
                descripcion = describir(concepto) + " N° " + idOperacion;
            }
        } else {
            idOperacion = null;
            if (descripcion == null || descripcion.isBlank()) {
                throw new ReglaNegocioException("Describe brevemente qué estás pagando.");
            }
        }

        String numero = Json.str(datos, "numeroOperacion");
        numero = numero == null ? null : numero.trim().replaceAll("\\s", "");
        if (numero != null && numero.isEmpty()) numero = null;
        if (exigeNumeroOperacion && numero == null) {
            throw new ReglaNegocioException("Ingresa el número de operación que te muestra tu app después de pagar.");
        }
        if (numero != null) {
            if (!NUMERO_OPERACION.matcher(numero).matches()) {
                throw new ReglaNegocioException("El número de operación solo admite letras, números y guiones (4 a 30).");
            }
            if (dao.existeNumeroOperacion(metodo, numero)) {
                throw new ReglaNegocioException("Ese número de operación de " + metodo + " ya fue registrado.");
            }
        }
        String celular = Json.str(datos, "celularOrigen");
        celular = celular == null ? null : celular.replaceAll("\\s", "");
        if (celular != null && !celular.isEmpty() && !CELULAR.matcher(celular).matches()) {
            throw new ReglaNegocioException("El celular debe tener 9 dígitos y empezar con 9.");
        }
        String constancia = Json.str(datos, "constancia");
        if (constancia != null && !constancia.isBlank()) {
            validarImagen(constancia, "La captura de la constancia");
        } else {
            constancia = null;
        }

        Pago p = new Pago();
        p.setIdCliente(idCliente);
        p.setIdOperacion(idOperacion);
        p.setConcepto(concepto);
        p.setDescripcion(descripcion);
        p.setMetodo(metodo);
        p.setMonto(monto);
        p.setMoneda("PEN");
        p.setNumeroOperacion(numero);
        p.setCelularOrigen(celular == null || celular.isEmpty() ? null : celular);
        p.setReferencia(referencia(idCliente, idOperacion));
        p.setConstancia(constancia);
        p.setFechaPago(LocalDateTime.now());
        return p;
    }

    /** Código que el cliente pone en el mensaje del pago para identificarlo (C12-OP45). */
    public static String referencia(int idCliente, Integer idOperacion) {
        return "C" + idCliente + (idOperacion == null ? "" : "-OP" + idOperacion);
    }

    /** Lo máximo que se puede pagar de un trámite, descontando lo que ya está por verificar. */
    private BigDecimal maximoPagable(Operacion o, String concepto, int idCliente) {
        String tipo = tipo(o);
        List<Pago> previos = dao.listarPorCliente(idCliente);
        switch (concepto) {
            case CUOTA_PRESTAMO: {
                Prestamo p = tipo.contains("PRESTAMO") ? prestamoDao.buscarPorOperacion(o.getIdOperacion()) : null;
                if (p == null || !"ACTIVO".equalsIgnoreCase(p.getEstado())) {
                    throw new ReglaNegocioException("El préstamo no está activo (estado: "
                            + (p == null ? "sin préstamo" : p.getEstado()) + ").");
                }
                return nz(p.getSaldoPendiente()).subtract(enVerificacion(previos, o.getIdOperacion())).max(BigDecimal.ZERO);
            }
            case RESCATE_EMPENO: {
                Empeno e = tipo.contains("EMPENO") ? empenoDao.buscarPorOperacion(o.getIdOperacion()) : null;
                if (e == null || !"ACTIVO".equalsIgnoreCase(e.getEstado())) {
                    throw new ReglaNegocioException("El empeño no está activo (estado: "
                            + (e == null ? "sin empeño" : e.getEstado()) + ").");
                }
                return nz(e.getSaldoPendiente()).subtract(enVerificacion(previos, o.getIdOperacion())).max(BigDecimal.ZERO);
            }
            case CAMBIO_MONEDA: {
                if (!tipo.contains("CAMBIO")) {
                    throw new ReglaNegocioException("El trámite seleccionado no es un cambio de moneda.");
                }
                boolean yaPagado = previos.stream().anyMatch(x -> Objects.equals(x.getIdOperacion(), o.getIdOperacion())
                        && !Pago.RECHAZADO.equals(x.getEstado()));
                if (yaPagado) {
                    throw new ReglaNegocioException("Ese cambio de moneda ya tiene un pago registrado.");
                }
                return nz(o.getTotalEntregar() != null ? o.getTotalEntregar() : o.getMonto());
            }
            default:
                return montoMaximo;
        }
    }

    /** Efectos de un pago confirmado sobre el trámite: baja el saldo y lo cierra si llega a cero. */
    private void aplicarEfectos(Pago p) {
        if (p.getIdOperacion() == null) return;
        if (CUOTA_PRESTAMO.equals(p.getConcepto())) {
            Prestamo pr = prestamoDao.buscarPorOperacion(p.getIdOperacion());
            if (pr == null) return;
            BigDecimal saldo = nz(pr.getSaldoPendiente()).subtract(p.getMonto()).max(BigDecimal.ZERO);
            pr.setSaldoPendiente(saldo);
            if (saldo.signum() == 0) pr.setEstado("CANCELADO");
            prestamoDao.actualizar(pr);
        } else if (RESCATE_EMPENO.equals(p.getConcepto())) {
            Empeno e = empenoDao.buscarPorOperacion(p.getIdOperacion());
            if (e == null) return;
            BigDecimal saldo = nz(e.getSaldoPendiente()).subtract(p.getMonto()).max(BigDecimal.ZERO);
            e.setSaldoPendiente(saldo);
            if (saldo.signum() == 0) e.setEstado("RECUPERADO");
            empenoDao.actualizar(e);
        }
        // CAMBIO_MONEDA: el pago queda registrado; la entrega de la divisa se aprueba en Solicitudes.
    }

    private Pago exigirPorVerificar(int idPago) {
        Pago p = buscar(idPago);
        if (!Pago.POR_VERIFICAR.equals(p.getEstado())) {
            throw new ReglaNegocioException("El pago " + p.getCodigo() + " ya fue atendido (estado " + p.getEstado() + ").");
        }
        return p;
    }

    private Cliente exigirCliente(int idCliente) {
        Cliente c = clienteDao.buscar(idCliente);
        if (c == null) {
            throw new ReglaNegocioException("No existe el cliente indicado.");
        }
        return c;
    }

    private static void validarImagen(String dataUrl, String que) {
        if (dataUrl.length() > MAX_IMAGEN) {
            throw new ReglaNegocioException(que + " es muy pesada (máximo 500 KB).");
        }
        if (!IMAGEN.matcher(dataUrl).matches()) {
            throw new ReglaNegocioException(que + " debe ser una imagen PNG, JPG o WEBP.");
        }
    }

    private static BigDecimal enVerificacion(List<Pago> pagos, Integer idOperacion) {
        BigDecimal s = BigDecimal.ZERO;
        for (Pago p : pagos) {
            if (Objects.equals(p.getIdOperacion(), idOperacion) && Pago.POR_VERIFICAR.equals(p.getEstado())) {
                s = s.add(nz(p.getMonto()));
            }
        }
        return s;
    }

    private static Map<String, Object> pagable(Operacion o, String concepto, String titulo, String detalle,
                                               BigDecimal sugerido, BigDecimal maximo, BigDecimal saldo) {
        return Json.map("idOperacion", o.getIdOperacion(), "concepto", concepto, "titulo", titulo,
                "detalle", detalle, "montoSugerido", sugerido.setScale(2, RoundingMode.HALF_UP),
                "montoMaximo", maximo.setScale(2, RoundingMode.HALF_UP), "saldo", saldo);
    }

    private static String describir(String concepto) {
        switch (concepto) {
            case CUOTA_PRESTAMO: return "Pago de cuota de préstamo";
            case RESCATE_EMPENO: return "Pago para rescate de empeño";
            case CAMBIO_MONEDA: return "Pago de cambio de moneda";
            default: return "Pago";
        }
    }

    private static String tipo(Operacion o) {
        return o.getTipoOperacion() == null ? "" : o.getTipoOperacion().toUpperCase(Locale.ROOT);
    }

    private static boolean positivo(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String texto(BigDecimal v) {
        return v == null ? "0.00" : v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String mayus(String s) {
        return s == null || s.isBlank() ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static String recortar(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }

    private static String nombre(Cliente c) {
        return ((c.getNombres() == null ? "" : c.getNombres()) + " "
                + (c.getApellidos() == null ? "" : c.getApellidos())).trim();
    }
}
