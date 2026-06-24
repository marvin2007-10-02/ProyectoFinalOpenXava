package ni.edu.uam.bfavocabulario.services.impl;

import ni.edu.uam.bfavocabulario.model.Opcion;
import ni.edu.uam.bfavocabulario.model.SesionEvaluacion;
import ni.edu.uam.bfavocabulario.services.interfaces.CorreccionService;

public class CorreccionServiceImpl implements CorreccionService {

    @Override
    public int calcularAciertos(SesionEvaluacion sesionEvaluacion) {
        if (sesionEvaluacion == null || sesionEvaluacion.getOpcionesSeleccionadas() == null) {
            return 0;
        }

        int aciertos = 0;

        for (Opcion opcion : sesionEvaluacion.getOpcionesSeleccionadas()) {
            if (opcion != null && opcion.isEsCorrecta()) {
                aciertos++;
            }
        }

        return aciertos;
    }

    @Override
    public int corregirSesion(SesionEvaluacion sesionEvaluacion) {
        if (sesionEvaluacion == null) {
            return 0;
        }

        int aciertos = calcularAciertos(sesionEvaluacion);

        sesionEvaluacion.setPuntajeDirecto(aciertos);

        return aciertos;
    }
}