package com.utp.sistemafinanciero.archivos;

import com.utp.sistemafinanciero.web.AreaNegocio;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describe qué se puede exportar e importar de un recurso de la API
 * (por ejemplo {@code clientes}): sus columnas, de dónde salen los datos
 * y, si admite importación, cómo se convierte cada fila en entidad y qué
 * servicio la registra.
 *
 * <p>La importación nunca escribe directamente en la base: cada fila pasa
 * por el mismo {@code MapeadorEntrada} y el mismo servicio que usa la
 * pantalla, por lo que se aplican las mismas reglas de negocio,
 * validaciones y protección contra <i>mass assignment</i>.</p>
 *
 * @param <T> tipo de cada fila
 */
public final class DefinicionArchivo<T> {

    private final String recurso;
    private final AreaNegocio area;
    private final String hoja;
    private final List<Columna<T>> columnas;
    private final Supplier<List<T>> datos;
    private final Function<Map<String, Object>, T> mapeador;
    private final Consumer<T> registrar;

    private DefinicionArchivo(String recurso, AreaNegocio area, String hoja, List<Columna<T>> columnas,
                              Supplier<List<T>> datos, Function<Map<String, Object>, T> mapeador,
                              Consumer<T> registrar) {
        this.recurso = recurso;
        this.area = area;
        this.hoja = hoja;
        this.columnas = List.copyOf(columnas);
        this.datos = datos;
        this.mapeador = mapeador;
        this.registrar = registrar;
    }

    /** Recurso que solo se exporta (por ejemplo, operaciones financieras: nunca se importan). */
    public static <T> DefinicionArchivo<T> soloExportar(String recurso, AreaNegocio area, String hoja,
                                                        List<Columna<T>> columnas, Supplier<List<T>> datos) {
        return new DefinicionArchivo<>(recurso, area, hoja, columnas, datos, null, null);
    }

    /** Recurso que se exporta y se importa. */
    public static <T> DefinicionArchivo<T> exportarEImportar(String recurso, AreaNegocio area, String hoja,
                                                             List<Columna<T>> columnas, Supplier<List<T>> datos,
                                                             Function<Map<String, Object>, T> mapeador,
                                                             Consumer<T> registrar) {
        return new DefinicionArchivo<>(recurso, area, hoja, columnas, datos, mapeador, registrar);
    }

    public String recurso() { return recurso; }

    public AreaNegocio area() { return area; }

    public String hoja() { return hoja; }

    public List<Columna<T>> columnas() { return columnas; }

    public List<T> datos() { return datos.get(); }

    public boolean importable() { return mapeador != null && registrar != null; }

    /** Convierte una fila leída del archivo en entidad (usa el MapeadorEntrada). */
    public T mapear(Map<String, Object> fila) { return mapeador.apply(fila); }

    /** Registra la entidad con el servicio del área (aplica las reglas de negocio). */
    public void registrar(T entidad) { registrar.accept(entidad); }
}
