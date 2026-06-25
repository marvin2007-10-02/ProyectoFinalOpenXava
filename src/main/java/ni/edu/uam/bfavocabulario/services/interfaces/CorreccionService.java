package ni.edu.uam.bfavocabulario.services.interfaces;

import ni.edu.uam.bfavocabulario.model.Opcion;
import ni.edu.uam.bfavocabulario.model.SesionEvaluacion;

public interface CorreccionService {

    int calcularAciertos(SesionEvaluacion sesionEvaluacion);

    int corregirSesion(SesionEvaluacion sesionEvaluacion);

    boolean opcionEsCorrecta(Opcion opcion);
}