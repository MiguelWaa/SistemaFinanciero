package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.CampaniaDao;
import com.utp.sistemafinanciero.model.marketing.Campania;

/** Implementación en memoria del {@link CampaniaDao} (pruebas y modo demostración). */
public class CampaniaDaoMemoria extends RepositorioMemoria<Campania> implements CampaniaDao {

    public CampaniaDaoMemoria() {
        super(Campania::getIdCampania, Campania::setIdCampania, true);
    }
}
