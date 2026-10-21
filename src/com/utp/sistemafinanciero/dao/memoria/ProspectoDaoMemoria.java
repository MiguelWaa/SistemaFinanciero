package com.utp.sistemafinanciero.dao.memoria;

import com.utp.sistemafinanciero.dao.ProspectoDao;
import com.utp.sistemafinanciero.model.marketing.Prospecto;

/** Implementación en memoria del {@link ProspectoDao} (pruebas y modo demostración). */
public class ProspectoDaoMemoria extends RepositorioMemoria<Prospecto> implements ProspectoDao {

    public ProspectoDaoMemoria() {
        super(Prospecto::getIdProspecto, Prospecto::setIdProspecto, true);
    }
}
