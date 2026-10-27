package com.utp.sistemafinanciero.service;

import com.utp.sistemafinanciero.dao.ClienteDao;
import com.utp.sistemafinanciero.dao.CotizacionDao;
import com.utp.sistemafinanciero.dao.TipoCambioDao;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.pagos.Cotizacion;
import com.utp.sistemafinanciero.util.Json;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Cotizaciones (proformas) de préstamos, empeños y cambio de moneda.
 *
 * <p>Usan exactamente las mismas fórmulas y tasas que el registro real
 * ({@link PrestamoService#calcular}, {@link PortalService#TASA_PRESTAMO},
 * {@link PortalService#TASA_EMPENO}), así lo cotizado coincide con lo que
 * luego se cobra. Cada cotización queda guardada con un número
 * correlativo y una vigencia de {@value #VIGENCIA_DIAS} días; mientras está
 * vigente, el cliente la puede convertir en solicitud.</p>
 */
public class CotizacionService {

    public static final int VIGENCIA_DIAS = 7;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("500000");

    private final CotizacionDao dao;
    private final ClienteDao clienteDao;
    private final TipoCambioDao tipoCambioDao;
    private final PortalService portal;

    public CotizacionService(CotizacionDao dao, ClienteDao clienteDao, TipoCambioDao tipoCambioDao,
                             PortalService portal) {
        this.dao = dao;
        this.clienteDao = clienteDao;
        this.tipoCambioDao = tipoCambioDao;
        this.portal = portal;
    }

    /** Calcula y guarda una cotización. {@code origen}: PORTAL o CAJA. */
    public Cotizacion cotizar(Integer idCliente, Map<String, Object> datos, String origen) {
        if (idCliente == null) {
            throw new ReglaNegocioException("Selecciona el cliente para la cotización.");
        }
        if (clienteDao.buscar(idCliente) == null) {
            throw new ReglaNegocioException("No existe el cliente indicado.");
        }
        String tipo = Json.str(datos, "tipo");
        tipo = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        BigDecimal monto = Json.decimalOrNull(datos, "monto");
        if (monto == null || monto.signum() <= 0) {
            throw new ReglaNegocioException("Ingresa un monto mayor a cero.");
        }
        if (monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new ReglaNegocioException("El monto supera el máximo que se puede cotizar en línea.");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);

        Cotizacion q = new Cotizacion();
        q.setIdCliente(idCliente);
        q.setTipo(tipo);
        q.setMonto(monto);
        q.setEstado(Cotizacion.VIGENTE);
        q.setFechaEmision(LocalDateTime.now().withNano(0));
        q.setFechaVencimiento(LocalDate.now().plusDays(VIGENCIA_DIAS));
        q.setOrigen(origen == null ? "PORTAL" : origen);

        switch (tipo) {
            case "PRESTAMO": {
                Integer plazo = Json.intOrNull(datos, "plazo");
                if (plazo == null || plazo < 1 || plazo > 60) {
                    throw new ReglaNegocioException("El plazo del préstamo debe estar entre 1 y 60 meses.");
                }
                BigDecimal[] r = PrestamoService.calcular(monto, PortalService.TASA_PRESTAMO, plazo);
                q.setMoneda("PEN");
                q.setPlazo(plazo);
                q.setUnidadPlazo("MESES");
                q.setTasa(PortalService.TASA_PRESTAMO);
                q.setCuota(r[0]);
                q.setTotalPagar(r[1]);
                q.setTotalInteres(r[1].subtract(monto));
                break;
            }
            case "EMPENO": {
                String desc = Json.str(datos, "descripcion");
                if (desc == null || desc.isBlank()) {
                    throw new ReglaNegocioException("Describe el artículo que vas a empeñar.");
                }
                Integer dias = Json.intOrNull(datos, "plazo");
                if (dias == null) dias = 30;
                if (dias < 7 || dias > 180) {
                    throw new ReglaNegocioException("El plazo del empeño debe estar entre 7 y 180 días.");
                }
                BigDecimal valor = Json.decimalOrNull(datos, "valorEstimado");
                if (valor != null && valor.signum() > 0 && monto.compareTo(valor) > 0) {
                    throw new ReglaNegocioException("El monto solicitado no puede superar el valor del artículo.");
                }
                BigDecimal interes = monto.multiply(PortalService.TASA_EMPENO).divide(CIEN, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(dias)).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
                q.setMoneda("PEN");
                q.setPlazo(dias);
                q.setUnidadPlazo("DIAS");
                q.setTasa(PortalService.TASA_EMPENO);
                q.setDescripcion(desc.trim().length() > 200 ? desc.trim().substring(0, 200) : desc.trim());
                q.setValorEstimado(valor);
                q.setTotalInteres(interes);
                q.setTotalPagar(monto.add(interes));
                break;
            }
            case "CAMBIO": {
                Integer idTc = Json.intOrNull(datos, "idTipoCambio");
                TipoCambio tc = idTc == null ? null : tipoCambioDao.buscar(idTc);
                if (tc == null) {
                    throw new ReglaNegocioException("Selecciona el tipo de cambio.");
                }
                q.setIdTipoCambio(idTc);
                q.setMoneda(tc.getMonedaOrigen());
                q.setMonedaRecibir(tc.getMonedaDestino());
                q.setTipoCambio(tc.getVenta());
                q.setMontoRecibir(monto.multiply(tc.getVenta()).setScale(2, RoundingMode.HALF_UP));
                break;
            }
            default:
                throw new ReglaNegocioException("Tipo de cotización no válido: use PRESTAMO, EMPENO o CAMBIO.");
        }
        q.setIdCotizacion(dao.crear(q));
        return dao.buscar(q.getIdCotizacion());
    }

    public List<Cotizacion> listar() {
        List<Cotizacion> l = dao.listar();
        l.forEach(CotizacionService::estadoVigente);
        return l;
    }

    public List<Cotizacion> delCliente(int idCliente) {
        List<Cotizacion> l = dao.listarPorCliente(idCliente);
        l.forEach(CotizacionService::estadoVigente);
        return l;
    }

    /** Cotización con su cronograma (para el personal). */
    public Map<String, Object> detalle(int idCotizacion) {
        return conCronograma(exigir(idCotizacion));
    }

    /** Cotización con su cronograma, solo si pertenece al cliente. */
    public Map<String, Object> detalleDelCliente(int idCliente, int idCotizacion) {
        Cotizacion q = exigir(idCotizacion);
        if (!Objects.equals(q.getIdCliente(), idCliente)) {
            throw new ReglaNegocioException("No existe la cotización indicada.");
        }
        return conCronograma(q);
    }

    /** Convierte una cotización vigente en solicitud (queda PENDIENTE en la bandeja). */
    public Map<String, Object> convertir(int idCliente, int idCotizacion) {
        Cotizacion q = exigir(idCotizacion);
        if (!Objects.equals(q.getIdCliente(), idCliente)) {
            throw new ReglaNegocioException("No existe la cotización indicada.");
        }
        if (!Cotizacion.VIGENTE.equals(q.getEstado())) {
            throw new ReglaNegocioException("La cotización " + q.getNumero() + " ya no está vigente ("
                    + q.getEstado() + "). Genera una nueva.");
        }
        Integer idOperacion;
        Object resultado;
        switch (q.getTipo()) {
            case "PRESTAMO": {
                Map<String, Object> r = portal.solicitarPrestamo(idCliente, q.getMonto(), q.getPlazo(), null);
                idOperacion = ((Operacion) r.get("operacion")).getIdOperacion();
                resultado = r;
                break;
            }
            case "EMPENO": {
                Map<String, Object> r = portal.solicitarEmpeno(idCliente, q.getDescripcion(), q.getValorEstimado(),
                        q.getMonto(), q.getPlazo());
                idOperacion = ((Operacion) r.get("operacion")).getIdOperacion();
                resultado = r;
                break;
            }
            default: {
                Operacion o = portal.solicitarCambio(idCliente, q.getIdTipoCambio(), q.getMonto());
                idOperacion = o.getIdOperacion();
                resultado = o;
            }
        }
        q.setEstado(Cotizacion.CONVERTIDA);
        q.setIdOperacion(idOperacion);
        dao.actualizar(q);
        return Json.map("cotizacion", dao.buscar(idCotizacion), "solicitud", resultado, "idOperacion", idOperacion,
                "mensaje", "Listo: la cotización " + q.getNumero() + " se envió como solicitud N° " + idOperacion + ".");
    }

    // ------------------------------------------------------------------

    private Cotizacion exigir(int idCotizacion) {
        Cotizacion q = dao.buscar(idCotizacion);
        if (q == null) {
            throw new ReglaNegocioException("No existe la cotización indicada.");
        }
        estadoVigente(q);
        return q;
    }

    /** Una cotización vigente cuya fecha ya pasó se muestra como VENCIDA. */
    private static void estadoVigente(Cotizacion q) {
        if (Cotizacion.VIGENTE.equals(q.getEstado()) && q.getFechaVencimiento() != null
                && q.getFechaVencimiento().isBefore(LocalDate.now())) {
            q.setEstado(Cotizacion.VENCIDA);
        }
    }

    /**
     * Cronograma de pagos del préstamo (interés simple, igual que el sistema):
     * cada cuota = capital fijo + interés fijo; el último saldo queda en cero.
     */
    static List<Map<String, Object>> cronograma(Cotizacion q) {
        List<Map<String, Object>> filas = new ArrayList<>();
        if (!"PRESTAMO".equals(q.getTipo()) || q.getPlazo() == null || q.getPlazo() <= 0) return filas;
        int n = q.getPlazo();
        BigDecimal capital = q.getMonto().divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal saldo = q.getTotalPagar();
        LocalDate base = q.getFechaEmision() == null ? LocalDate.now() : q.getFechaEmision().toLocalDate();
        for (int k = 1; k <= n; k++) {
            BigDecimal cuota = k < n ? q.getCuota() : saldo;
            saldo = saldo.subtract(cuota).max(BigDecimal.ZERO);
            BigDecimal cap = k < n ? capital : q.getMonto().subtract(capital.multiply(BigDecimal.valueOf(n - 1L)));
            filas.add(Json.map("numero", k, "fecha", base.plusMonths(k), "capital", cap,
                    "interes", cuota.subtract(cap).max(BigDecimal.ZERO),
                    "cuota", cuota, "saldo", saldo));
        }
        return filas;
    }

    private static Map<String, Object> conCronograma(Cotizacion q) {
        return Json.map("cotizacion", q, "cronograma", cronograma(q), "vigenciaDias", VIGENCIA_DIAS);
    }
}
