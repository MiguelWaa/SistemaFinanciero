package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.DaoFactory;
import com.utp.sistemafinanciero.model.almacen.Producto;
import com.utp.sistemafinanciero.model.clientes.Cliente;
import com.utp.sistemafinanciero.model.clientes.ServicioFinanciero;
import com.utp.sistemafinanciero.model.direccion.ExpedienteAprobacion;
import com.utp.sistemafinanciero.model.logistica.Proveedor;
import com.utp.sistemafinanciero.model.marketing.Campania;
import com.utp.sistemafinanciero.model.marketing.Prospecto;
import com.utp.sistemafinanciero.model.operaciones.Articulo;
import com.utp.sistemafinanciero.model.operaciones.Empeno;
import com.utp.sistemafinanciero.model.operaciones.Operacion;
import com.utp.sistemafinanciero.model.operaciones.Prestamo;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;
import com.utp.sistemafinanciero.model.produccion.LineaProduccion;
import com.utp.sistemafinanciero.model.produccion.OrdenProduccion;
import com.utp.sistemafinanciero.model.seguridad.Rol;
import com.utp.sistemafinanciero.model.seguridad.Usuario;
import com.utp.sistemafinanciero.model.ventas.Venta;
import com.utp.sistemafinanciero.security.PasswordHasher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Datos de demostración masivos para el modo memoria: completa cada módulo
 * hasta {@value #OBJETIVO} registros (300 operaciones: 100 cambios, 100
 * préstamos y 100 empeños), igual que {@code db/datos_100_registros.sql}
 * hace en PostgreSQL.
 *
 * <p>Las dos versiones salen del mismo generador
 * ({@code scripts/datos/generar_datos_100.py}), así que muestran los mismos
 * datos. El archivo {@code semilla_100.tsv} guarda fechas relativas a hoy
 * ({@code D-12} = hace 12 días; {@code T-3-5-20} = hace 3 días, 5 h, 20 min)
 * y claves foráneas por clave natural (documento, código, serie...).</p>
 *
 * <p>Regla: cada módulo recibe {@code OBJETIVO - registros_actuales} filas,
 * por lo que se puede llamar varias veces sin duplicar. Se invoca solo en el
 * modo demostración; las pruebas usan {@link DatosSemilla} (pocas filas).</p>
 */
public final class DatosMasivos {

    public static final int OBJETIVO = 100;
    private static final String RECURSO = "semilla_100.tsv";
    private static final String CLAVE_DEMO = "sigsf123";

    private DatosMasivos() {
    }

    private static BigDecimal d(String v) { return v == null ? null : new BigDecimal(v); }
    private static Integer i(String v) { return v == null ? null : Integer.valueOf(v); }

    /** Fecha relativa: {@code D-12} / {@code D+5}. */
    static LocalDate fecha(String v, LocalDate hoy) {
        return v == null ? null : hoy.plusDays(Long.parseLong(v.substring(1)));
    }

    /** Instante relativo: {@code T-dias-horas-minutos} (hacia atrás). */
    static LocalDateTime instante(String v, LocalDateTime ahora) {
        if (v == null) {
            return null;
        }
        String[] p = v.split("-");
        return ahora.minusDays(Long.parseLong(p[1])).minusHours(Long.parseLong(p[2])).minusMinutes(Long.parseLong(p[3]));
    }

    /** Lee el TSV: sección ({@code @nombre}) → filas (columnas separadas por tabulador; {@code \N} = null). */
    static Map<String, List<String[]>> leer() {
        Map<String, List<String[]>> secciones = new LinkedHashMap<>();
        try (InputStream in = DatosMasivos.class.getResourceAsStream(RECURSO)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso " + RECURSO);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            List<String[]> actual = null;
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank() || linea.startsWith("#")) {
                    continue;
                }
                if (linea.startsWith("@")) {
                    actual = new ArrayList<>();
                    secciones.put(linea.substring(1), actual);
                    continue;
                }
                String[] c = linea.split("\t", -1);
                for (int k = 0; k < c.length; k++) {
                    if (c[k].equals("\\N")) {
                        c[k] = null;
                    }
                }
                actual.add(c);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return secciones;
    }

    /** Número de filas que faltan y que hay que insertar. */
    private static int faltan(int existentes) {
        return Math.max(0, OBJETIVO - existentes);
    }

    private static long contar(DaoFactory bd, String tipo) {
        return bd.operaciones().listar().stream().filter(o -> tipo.equals(o.getTipoOperacion())).count();
    }

    public static void cargar(DaoFactory bd, PasswordHasher hasher) {
        Map<String, List<String[]>> t = leer();
        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        String hashDemo = hasher.hash(CLAVE_DEMO);          // un solo hash: la clave de todos los usuarios generados

        // ---- Seguridad ----
        Map<String, Integer> rolId = new HashMap<>();
        bd.roles().listar().forEach(r -> rolId.put(r.getNombre(), r.getIdRol()));
        int n = faltan(bd.roles().listar().size());
        for (String[] f : t.get("rol")) {
            if (n == 0) break;
            if (rolId.containsKey(f[0])) continue;
            rolId.put(f[0], bd.roles().crear(new Rol(null, f[0], f[1])));
            n--;
        }
        n = faltan(bd.usuarios().listar().size());
        for (String[] f : t.get("usuario")) {
            if (n == 0) break;
            Integer rol = rolId.get(f[4]);
            if (rol == null) continue;
            bd.usuarios().crear(new Usuario(null, f[0], f[1], f[2], hashDemo, rol, f[5], instante(f[6], ahora)));
            n--;
        }
        n = faltan(bd.servicios().listar().size());
        Map<String, Integer> servicioId = new HashMap<>();
        bd.servicios().listar().forEach(s -> servicioId.put(s.getNombre(), s.getIdServicio()));
        for (String[] f : t.get("servicio")) {
            if (n == 0) break;
            servicioId.put(f[0], bd.servicios().crear(new ServicioFinanciero(null, f[0], f[1], f[2], f[3])));
            n--;
        }

        // ---- Clientes (con acceso al portal: clave inicial = documento) ----
        Map<String, Integer> clienteId = new HashMap<>();
        bd.clientes().listar().forEach(c -> clienteId.put(c.getDocumento(), c.getIdCliente()));
        n = faltan(bd.clientes().listar().size());
        for (String[] f : t.get("cliente")) {
            if (n == 0) break;
            if (clienteId.containsKey(f[1])) continue;
            int id = bd.clientes().crear(new Cliente(null, f[0], f[1], f[2], f[3], fecha(f[4], hoy), f[5], f[6], f[7], f[8], instante(f[9], ahora)));
            clienteId.put(f[1], id);
            bd.accesosCliente().guardar(id, hasher.hash(f[1]));
            n--;
        }

        // ---- Operaciones financieras ----
        n = faltan(bd.tiposCambio().listar().size());
        for (String[] f : t.get("tipo_cambio")) {
            if (n == 0) break;
            bd.tiposCambio().crear(new TipoCambio(null, f[0], f[1], d(f[2]), d(f[3]), fecha(f[4], hoy), f[5]));
            n--;
        }
        Map<String, Integer> tcActivo = new HashMap<>();
        for (TipoCambio tc : bd.tiposCambio().listar()) {
            if ("ACTIVO".equals(tc.getEstado())) tcActivo.putIfAbsent(tc.getMonedaOrigen() + ">" + tc.getMonedaDestino(), tc.getIdTipoCambio());
        }
        List<Integer> opCambio = operaciones(bd, t.get("operacion_cambio"), "CAMBIO", clienteId, tcActivo, ahora);
        List<Integer> opPrestamo = operaciones(bd, t.get("operacion_prestamo"), "PRESTAMO", clienteId, tcActivo, ahora);
        List<Integer> opEmpeno = operaciones(bd, t.get("operacion_empeno"), "EMPENO", clienteId, tcActivo, ahora);

        n = faltan(bd.prestamos().listar().size());
        List<String[]> filas = t.get("prestamo");
        for (int k = 0; k < filas.size() && n > 0; k++) {
            String[] f = filas.get(k);
            Integer op = k < opPrestamo.size() ? opPrestamo.get(k) : null;
            if (op == null) continue;
            bd.prestamos().crear(new Prestamo(null, op, d(f[0]), d(f[1]), i(f[2]), d(f[3]), fecha(f[4], hoy), fecha(f[5], hoy), d(f[6]), f[7]));
            n--;
        }
        Map<String, Integer> articuloId = new HashMap<>();
        bd.articulos().listar().forEach(a -> articuloId.put(a.getNumeroSerie(), a.getIdArticulo()));
        n = faltan(bd.articulos().listar().size());
        for (String[] f : t.get("articulo")) {
            if (n == 0) break;
            Integer cli = clienteId.get(f[0]);
            if (cli == null || articuloId.containsKey(f[5])) continue;
            articuloId.put(f[5], bd.articulos().crear(new Articulo(null, cli, f[1], f[2], f[3], f[4], f[5], f[6], d(f[7]), fecha(f[8], hoy), f[9])));
            n--;
        }
        n = faltan(bd.empenos().listar().size());
        filas = t.get("empeno");
        for (int k = 0; k < filas.size() && n > 0; k++) {
            String[] f = filas.get(k);
            Integer op = k < opEmpeno.size() ? opEmpeno.get(k) : null;
            Integer art = articuloId.get(f[0]);
            if (op == null || art == null) continue;
            bd.empenos().crear(new Empeno(null, op, art, d(f[1]), d(f[2]), i(f[3]), fecha(f[4], hoy), fecha(f[5], hoy), d(f[6]), f[7]));
            n--;
        }

        // ---- Ventas ----
        n = faltan(bd.ventas().listar().size());
        for (String[] f : t.get("venta")) {
            if (n == 0) break;
            Integer cli = clienteId.get(f[0]);
            Integer srv = servicioId.get(f[1]);
            if (cli == null || srv == null) continue;
            bd.ventas().crear(new Venta(null, cli, srv, instante(f[2], ahora), d(f[3]), d(f[4]), f[5]));
            n--;
        }

        // ---- Almacén, logística y marketing ----
        Map<String, Integer> productoId = new HashMap<>();
        bd.productos().listar().forEach(p -> productoId.put(p.getCodigo(), p.getIdProducto()));
        n = faltan(bd.productos().listar().size());
        for (String[] f : t.get("producto")) {
            if (n == 0) break;
            if (productoId.containsKey(f[0])) continue;
            productoId.put(f[0], bd.productos().crear(new Producto(null, f[0], f[1], f[2], f[3], f[4], i(f[5]), i(f[6]), f[7])));
            n--;
        }
        n = faltan(bd.proveedores().listar().size());
        for (String[] f : t.get("proveedor")) {
            if (n == 0) break;
            bd.proveedores().crear(new Proveedor(null, f[0], f[1], f[2], f[3], f[4], f[5], f[6]));
            n--;
        }
        n = faltan(bd.prospectos().listar().size());
        for (String[] f : t.get("prospecto")) {
            if (n == 0) break;
            bd.prospectos().crear(new Prospecto(null, f[0], f[1], f[2], f[3], f[4], f[5], f[6], instante(f[7], ahora)));
            n--;
        }
        n = faltan(bd.campanias().listar().size());
        for (String[] f : t.get("campania")) {
            if (n == 0) break;
            bd.campanias().crear(new Campania(null, f[0], f[1], fecha(f[2], hoy), fecha(f[3], hoy), d(f[4]), f[5], f[6]));
            n--;
        }

        // ---- Producción ----
        Map<String, Integer> lineaId = new HashMap<>();
        bd.lineasProduccion().listar().forEach(l -> lineaId.put(l.getCodigo(), l.getIdLinea()));
        n = faltan(bd.lineasProduccion().listar().size());
        for (String[] f : t.get("linea")) {
            if (n == 0) break;
            lineaId.put(f[0], bd.lineasProduccion().crear(new LineaProduccion(null, f[0], f[1], f[2], f[3])));
            n--;
        }
        n = faltan(bd.ordenesProduccion().listar().size());
        for (String[] f : t.get("orden")) {
            if (n == 0) break;
            Integer prod = productoId.get(f[0]);
            Integer lin = lineaId.get(f[1]);
            if (prod == null || lin == null) continue;
            bd.ordenesProduccion().crear(new OrdenProduccion(null, prod, lin, i(f[2]), fecha(f[3], hoy), fecha(f[4], hoy), fecha(f[5], hoy), d(f[6]), f[7], f[8]));
            n--;
        }

        // ---- Dirección y Gerencia ----
        n = faltan(bd.expedientes().listar().size());
        for (String[] f : t.get("expediente")) {
            if (n == 0) break;
            bd.expedientes().crear(new ExpedienteAprobacion(null, f[0], f[1], f[2], d(f[3]), f[4], f[5], f[6], instante(f[7], ahora), instante(f[8], ahora)));
            n--;
        }
    }

    /**
     * Inserta las operaciones de un tipo hasta llegar a {@value #OBJETIVO} de ese tipo.
     * Devuelve, por posición en el TSV, el id insertado (o null si esa fila no se insertó),
     * para que préstamos y empeños se enlacen con su operación.
     */
    private static List<Integer> operaciones(DaoFactory bd, List<String[]> filas, String tipo, Map<String, Integer> clienteId,
                                             Map<String, Integer> tcActivo, LocalDateTime ahora) {
        List<Integer> ids = new ArrayList<>();
        int n = faltan((int) contar(bd, tipo));
        for (String[] f : filas) {
            Integer cli = clienteId.get(f[0]);
            if (n == 0 || cli == null) {
                ids.add(null);
                continue;
            }
            Integer tc = "CAMBIO".equals(tipo) ? tcActivo.get(f[2] + ">" + f[3]) : null;
            ids.add(bd.operaciones().crear(new Operacion(null, cli, tc, f[1], f[2], f[3], d(f[4]), d(f[5]), d(f[6]), d(f[7]), f[8], instante(f[9], ahora))));
            n--;
        }
        return ids;
    }
}
