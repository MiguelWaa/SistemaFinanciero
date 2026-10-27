package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.config.Contenedor;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.pagos.Pago;
import com.utp.sistemafinanciero.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Pagos con QR y cotizaciones de demostración para el modo memoria.
 *
 * <p>No escribe los datos "a mano": usa los mismos servicios que el
 * portal y la caja ({@code PagoService}, {@code CotizacionService}), así
 * los saldos de préstamos y empeños quedan coherentes con lo pagado.
 * Es determinista (semilla fija) y se reparte en los últimos 14 días.</p>
 */
public final class DatosPagos {

    private static final Logger LOG = LoggerFactory.getLogger(DatosPagos.class);
    private static final String CAJERO = "lucia.ramos@sigsf.com";

    private DatosPagos() {
    }

    public static void cargar(Contenedor c, MemoriaDaoFactory bd) {
        Random r = new Random(20261026L);
        String[] metodos = {"YAPE", "YAPE", "YAPE", "YAPE", "PLIN", "PLIN", "PLIN", "TUNKI", "TUNKI", "EFECTIVO"};
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        int pagos = 0;
        int numero = 10_000_000 + r.nextInt(80_000_000);
        for (Cliente cli : bd.clientes().listar()) {
            if (pagos >= 64) break;
            if (cli.getIdCliente() <= 4) continue; // los clientes de ejemplo quedan con deuda para probar el portal
            List<Map<String, Object>> pendientes;
            try {
                pendientes = c.pagos.pagables(cli.getIdCliente());
            } catch (RuntimeException e) {
                continue;
            }
            for (Map<String, Object> deuda : pendientes) {
                if (pagos >= 64 || r.nextInt(100) < 35) continue;
                String metodo = metodos[r.nextInt(metodos.length)];
                BigDecimal monto = (BigDecimal) deuda.get("montoSugerido");
                Map<String, Object> datos = Json.map(
                        "idOperacion", deuda.get("idOperacion"), "concepto", deuda.get("concepto"),
                        "monto", monto, "metodo", metodo, "numeroOperacion", String.valueOf(numero += 7 + r.nextInt(900)),
                        "celularOrigen", cli.getTelefono() != null && cli.getTelefono().matches("^9\\d{8}$")
                                ? cli.getTelefono() : null,
                        "idCliente", cli.getIdCliente());
                try {
                    Pago p;
                    if ("EFECTIVO".equals(metodo)) {
                        datos.remove("numeroOperacion");
                        p = c.pagos.registrarEnCaja(datos, CAJERO);
                    } else {
                        p = c.pagos.registrarDesdePortal(cli.getIdCliente(), datos);
                        int dado = r.nextInt(100);
                        if (dado < 72) {
                            p = c.pagos.confirmar(p.getIdPago(), CAJERO);
                        } else if (dado < 80) {
                            p = c.pagos.rechazar(p.getIdPago(), "No se encontró el abono en la billetera.", CAJERO);
                        } // el resto queda POR_VERIFICAR para la bandeja
                    }
                    int dias = Pago.POR_VERIFICAR.equals(p.getEstado()) ? 0 : r.nextInt(14);
                    ((PagoDaoMemoria) bd.pagos()).fijarFecha(p.getIdPago(),
                            ahora.minusDays(dias).minusMinutes(r.nextInt(8 * 60)));
                    pagos++;
                } catch (RuntimeException e) {
                    LOG.debug("Pago de demostración omitido: {}", e.getMessage());
                }
            }
        }

        // Cotizaciones de demostración (préstamo, empeño y cambio)
        List<Cliente> clientes = bd.clientes().listar();
        List<TipoCambio> tipos = bd.tiposCambio().listar();
        int cotizaciones = 0;
        for (int i = 0; i < 18 && !clientes.isEmpty(); i++) {
            Cliente cli = clientes.get(r.nextInt(clientes.size()));
            Map<String, Object> datos;
            switch (i % 3) {
                case 0:
                    datos = Json.map("tipo", "PRESTAMO", "monto", BigDecimal.valueOf(500 + r.nextInt(40) * 250L),
                            "plazo", new int[]{6, 12, 18, 24}[r.nextInt(4)]);
                    break;
                case 1:
                    datos = Json.map("tipo", "EMPENO", "monto", BigDecimal.valueOf(200 + r.nextInt(30) * 50L),
                            "valorEstimado", BigDecimal.valueOf(3000), "plazo", 30,
                            "descripcion", new String[]{"Laptop Lenovo IdeaPad", "Cadena de oro 18k", "Celular Samsung A54",
                                    "Televisor LG 50\"", "Reloj Casio Edifice"}[r.nextInt(5)]);
                    break;
                default:
                    if (tipos.isEmpty()) continue;
                    datos = Json.map("tipo", "CAMBIO", "monto", BigDecimal.valueOf(100 + r.nextInt(20) * 50L),
                            "idTipoCambio", tipos.get(r.nextInt(tipos.size())).getIdTipoCambio());
            }
            try {
                c.cotizaciones.cotizar(cli.getIdCliente(), datos, i % 4 == 0 ? "CAJA" : "PORTAL");
                cotizaciones++;
            } catch (RuntimeException e) {
                LOG.debug("Cotización de demostración omitida: {}", e.getMessage());
            }
        }
        LOG.info("Datos de demostración de pagos: {} pagos y {} cotizaciones.", pagos, cotizaciones);
    }
}
