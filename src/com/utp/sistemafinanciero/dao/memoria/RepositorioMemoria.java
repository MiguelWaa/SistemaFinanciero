package com.utp.sistemafinanciero.dao.memoria;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementación genérica (plantilla) de un DAO que guarda los registros
 * en memoria RAM. Reproduce el comportamiento de una tabla con clave
 * {@code SERIAL}: asigna ids correlativos, permite listar, buscar,
 * crear, actualizar y eliminar.
 *
 * <p>Se usa para las <b>pruebas unitarias (TDD)</b>, que así no dependen
 * de PostgreSQL, y para el <b>modo demostración</b>. Cada DAO concreto
 * (por ejemplo {@link ClienteDaoMemoria}) hereda de esta clase y solo
 * agrega sus consultas propias (principio DRY).</p>
 *
 * <p>Los objetos se guardan y devuelven como <i>copias</i>, igual que
 * ocurre con una base de datos real: modificar un objeto devuelto no
 * altera lo almacenado hasta llamar a {@link #actualizar}.</p>
 *
 * @param <T> entidad del modelo
 */
public class RepositorioMemoria<T> {

    private final Map<Integer, T> tabla = new ConcurrentSkipListMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(0);
    private final Function<T, Integer> obtenerId;
    private final BiConsumer<T, Integer> asignarId;
    private final boolean descendente;

    /**
     * @param obtenerId   función que lee la clave primaria (ej. {@code Cliente::getIdCliente})
     * @param asignarId   función que asigna la clave primaria (ej. {@code Cliente::setIdCliente})
     * @param descendente si {@code listar()} devuelve del más nuevo al más antiguo
     *                    (igual que el {@code ORDER BY ... DESC} del DAO JDBC)
     */
    protected RepositorioMemoria(Function<T, Integer> obtenerId, BiConsumer<T, Integer> asignarId,
                                 boolean descendente) {
        this.obtenerId = obtenerId;
        this.asignarId = asignarId;
        this.descendente = descendente;
    }

    public List<T> listar() {
        List<T> lista = new ArrayList<>();
        for (T t : tabla.values()) {
            lista.add(copiar(t));
        }
        if (descendente) {
            Collections.reverse(lista);
        }
        return lista;
    }

    public T buscar(int id) {
        T t = tabla.get(id);
        return t == null ? null : copiar(t);
    }

    public Integer crear(T entidad) {
        int id = secuencia.incrementAndGet();
        T copia = copiar(entidad);
        asignarId.accept(copia, id);
        tabla.put(id, copia);
        return id;
    }

    public int actualizar(T entidad) {
        Integer id = obtenerId.apply(entidad);
        if (id == null || !tabla.containsKey(id)) {
            return 0;
        }
        tabla.put(id, copiar(entidad));
        return 1;
    }

    public int eliminar(int id) {
        return tabla.remove(id) == null ? 0 : 1;
    }

    // ------------------------ apoyo para las subclases ------------------------

    /** Registros (copias) que cumplen una condición, en el orden de {@link #listar()}. */
    protected List<T> filtrar(Predicate<T> condicion) {
        List<T> salida = new ArrayList<>();
        for (T t : listar()) {
            if (condicion.test(t)) {
                salida.add(t);
            }
        }
        return salida;
    }

    /** Acceso directo (sin copiar) a los valores, para cálculos de solo lectura. */
    protected Iterable<T> valores() {
        return tabla.values();
    }

    /** Modifica en sitio los registros que cumplen la condición. Devuelve cuántos cambió. */
    protected int modificar(Predicate<T> condicion, java.util.function.Consumer<T> cambio) {
        int n = 0;
        for (T t : tabla.values()) {
            if (condicion.test(t)) {
                cambio.accept(t);
                n++;
            }
        }
        return n;
    }

    public int total() {
        return tabla.size();
    }

    /**
     * Copia superficial campo a campo (los campos son tipos inmutables:
     * String, Integer, BigDecimal, LocalDate...). Simula la "lectura de la
     * base de datos": cada consulta entrega un objeto nuevo.
     */
    @SuppressWarnings("unchecked")
    static <E> E copiar(E origen) {
        if (origen == null) return null;
        try {
            Class<?> clase = origen.getClass();
            E destino = (E) clase.getDeclaredConstructor().newInstance();
            for (Class<?> c = clase; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    f.set(destino, f.get(origen));
                }
            }
            return destino;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("No se pudo copiar " + origen.getClass().getSimpleName(), e);
        }
    }
}
