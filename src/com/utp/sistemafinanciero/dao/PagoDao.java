package com.utp.sistemafinanciero.dao;

import com.utp.sistemafinanciero.model.pagos.Pago;

import java.util.List;

/**
 * Contrato DAO para la tabla {@code pago} (pagos con Yape, Plin, Tunki o en caja).
 *
 * <p>Los pagos no se eliminan: se confirman o se rechazan, para conservar
 * la trazabilidad del dinero. Los listados incluyen el nombre y el
 * documento del cliente y <b>no</b> traen la imagen de la constancia
 * (se pide aparte con {@link #constancia(int)} para no cargar de más).</p>
 */
public interface PagoDao extends LecturaDao<Pago> {

    /** Inserta el pago y devuelve su id. */
    Integer crear(Pago pago);

    /** Actualiza estado, verificación y observación del pago. */
    int actualizar(Pago pago);

    List<Pago> listarPorCliente(int idCliente);

    /** Pagos en un estado (POR_VERIFICAR, CONFIRMADO, RECHAZADO). */
    List<Pago> listarPorEstado(String estado);

    /** ¿Ya se registró ese número de operación con esa billetera? (evita reutilizar una constancia). */
    boolean existeNumeroOperacion(String metodo, String numeroOperacion);

    /** Imagen (data URL) de la constancia adjunta, o null. */
    String constancia(int idPago);
}
