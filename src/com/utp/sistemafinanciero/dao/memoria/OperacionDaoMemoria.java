package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.OperacionDao;
import com.utp.sistemafinanciero.dto.SolicitudDTO;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.operaciones.Articulo;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementación en memoria del {@link OperacionDao}. La consulta de
 * solicitudes reproduce en Java el {@code LEFT JOIN} del DAO JDBC.
 */
public class OperacionDaoMemoria extends RepositorioMemoria<Operacion> implements OperacionDao {

    private final MemoriaDaoFactory bd;

    public OperacionDaoMemoria(MemoriaDaoFactory bd) {
        super(Operacion::getIdOperacion, Operacion::setIdOperacion, true);
        this.bd = bd;
    }

    @Override
    public List<Operacion> listarPorCliente(int idCliente) {
        return filtrar(o -> Objects.equals(o.getIdCliente(), idCliente));
    }

    @Override
    public int actualizarEstado(int idOperacion, String estado) {
        return modificar(o -> o.getIdOperacion() == idOperacion, o -> o.setEstado(estado));
    }

    @Override
    public int contarHoy() {
        LocalDate hoy = LocalDate.now();
        return filtrar(o -> o.getFechaOperacion() != null
                && o.getFechaOperacion().toLocalDate().equals(hoy)).size();
    }

    @Override
    public BigDecimal totalMonto() {
        YearMonth mes = YearMonth.now();
        BigDecimal total = BigDecimal.ZERO;
        for (Operacion o : valores()) {
            if (o.getFechaOperacion() != null && YearMonth.from(o.getFechaOperacion()).equals(mes)
                    && o.getMonto() != null) {
                total = total.add(o.getMonto());
            }
        }
        return total;
    }

    @Override
    public List<SolicitudDTO> solicitudes(String estado) {
        List<SolicitudDTO> salida = new ArrayList<>();
        for (Operacion o : listar()) {
            if (estado != null && !estado.equals(o.getEstado())) continue;
            SolicitudDTO d = new SolicitudDTO();
            d.setIdOperacion(o.getIdOperacion());
            d.setIdCliente(o.getIdCliente());
            d.setTipoOperacion(o.getTipoOperacion());
            d.setMonto(o.getMonto());
            d.setTotalRecibir(o.getTotalRecibir());
            d.setMonedaOrigen(o.getMonedaOrigen());
            d.setMonedaDestino(o.getMonedaDestino());
            d.setEstado(o.getEstado());
            d.setFechaOperacion(o.getFechaOperacion());
            Cliente c = o.getIdCliente() == null ? null : bd.clientes().buscar(o.getIdCliente());
            d.setCliente(c == null ? "" : (c.getNombres() + " " + (c.getApellidos() == null ? "" : c.getApellidos())).trim());
            d.setDocumento(c == null || c.getDocumento() == null ? "" : c.getDocumento());
            d.setTelefono(c == null || c.getTelefono() == null ? "" : c.getTelefono());
            d.setCorreo(c == null || c.getCorreo() == null ? "" : c.getCorreo());
            Prestamo p = ((PrestamoDaoMemoria) bd.prestamos()).porOperacion(o.getIdOperacion());
            if (p != null) {
                d.setCuotaMensual(p.getCuotaMensual());
                d.setPlazoMeses(p.getPlazoMeses());
                d.setTasaInteres(p.getTasaInteres());
            }
            Empeno e = ((EmpenoDaoMemoria) bd.empenos()).porOperacion(o.getIdOperacion());
            if (e != null) {
                d.setPlazoDias(e.getPlazoDias());
                Articulo a = e.getIdArticulo() == null ? null : bd.articulos().buscar(e.getIdArticulo());
                d.setArticulo(a == null ? null : a.getDescripcion());
            }
            salida.add(d);
        }
        return salida;
    }
}
