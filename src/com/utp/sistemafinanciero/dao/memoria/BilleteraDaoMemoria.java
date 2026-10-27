package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.BilleteraDao;
import com.utp.sistemafinanciero.model.pagos.Billetera;

/** Implementación en memoria del {@link BilleteraDao}. */
public class BilleteraDaoMemoria extends RepositorioMemoria<Billetera> implements BilleteraDao {

    public BilleteraDaoMemoria() {
        super(Billetera::getIdBilletera, Billetera::setIdBilletera, false);
    }

    @Override
    public Billetera buscarPorCodigo(String codigo) {
        return filtrar(b -> b.getCodigo() != null && b.getCodigo().equalsIgnoreCase(codigo))
                .stream().findFirst().orElse(null);
    }
}
