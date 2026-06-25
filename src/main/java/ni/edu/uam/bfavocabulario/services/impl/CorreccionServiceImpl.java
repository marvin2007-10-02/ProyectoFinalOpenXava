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
            if (opcionEsCorrecta(opcion)) {
                aciertos++;
            }
        }

        return aciertos;
    }

    @Override
    public int corregirSesion(SesionEvaluacion sesionEvaluacion) {

        int aciertos = calcularAciertos(sesionEvaluacion);

        if (sesionEvaluacion != null) {
            sesionEvaluacion.setPuntajeDirecto(aciertos);
        }

        return aciertos;
    }

    @Override
    public boolean opcionEsCorrecta(Opcion opcion) {

        if (opcion == null) {
            return false;
        }

        return opcion.verificarCorrecta();
    }
}