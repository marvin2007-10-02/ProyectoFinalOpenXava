package ni.edu.uam.bfavocabulario.services.impl;

import ni.edu.uam.bfavocabulario.model.NormaNacionalBFA;
import ni.edu.uam.bfavocabulario.services.interfaces.IInterpretacion;
import org.openxava.jpa.XPersistence;

import javax.persistence.TypedQuery;
import java.util.List;

public class InterpretacionService implements IInterpretacion {

    @Override
    public Integer obtenerPercentil(String subtest, Integer puntajeDirecto) {

        if (subtest == null || subtest.isBlank()) {
            return 0;
        }

        if (puntajeDirecto == null || puntajeDirecto < 0) {
            return 0;
        }

        TypedQuery<NormaNacionalBFA> query = XPersistence.getManager().createQuery(
                "select n from NormaNacionalBFA n where n.subtest = :subtest and " +
                        ":puntajeDirecto between n.puntajeMinimo and n.puntajeMaximo",
                NormaNacionalBFA.class
        );
        query.setParameter("subtest", subtest);
        query.setParameter("puntajeDirecto", puntajeDirecto);

        List<NormaNacionalBFA> normas = query.getResultList();

        if (normas.isEmpty()) {
            return 0;
        }
        return normas.get(0).getPercentil();
    }
}
