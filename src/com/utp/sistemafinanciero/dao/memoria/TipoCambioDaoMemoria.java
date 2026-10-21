package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.TipoCambioDao;
import com.utp.sistemafinanciero.model.operaciones.TipoCambio;

import java.util.Comparator;

/** Implementación en memoria del {@link TipoCambioDao}. */
public class TipoCambioDaoMemoria extends RepositorioMemoria<TipoCambio> implements TipoCambioDao {

    public TipoCambioDaoMemoria() {
        super(TipoCambio::getIdTipoCambio, TipoCambio::setIdTipoCambio, true);
    }

    @Override
    public TipoCambio vigente(String origen, String destino) {
        return filtrar(t -> t.getMonedaOrigen().equals(origen) && t.getMonedaDestino().equals(destino)
                && "ACTIVO".equals(t.getEstado()))
                .stream()
                .max(Comparator.comparing(TipoCambio::getFechaInicio))
                .orElse(null);
    }
}
