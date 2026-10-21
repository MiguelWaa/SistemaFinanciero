package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ArticuloDao;
import com.utp.sistemafinanciero.model.operaciones.Articulo;

/** Implementación en memoria del {@link ArticuloDao} (pruebas y modo demostración). */
public class ArticuloDaoMemoria extends RepositorioMemoria<Articulo> implements ArticuloDao {

    public ArticuloDaoMemoria() {
        super(Articulo::getIdArticulo, Articulo::setIdArticulo, true);
    }
}
