package ni.edu.uam.bfavocabulario.services.interfaces;


import ni.edu.uam.bfavocabulario.model.Opcion;
import ni.edu.uam.bfavocabulario.model.SesionEvaluacion;

public interface SesionEvaluacionService {

    SesionEvaluacion iniciarSesion(String nombreEvaluado);

    void registrarRespuesta(SesionEvaluacion sesionEvaluacion, Opcion opcionSeleccionada);

    boolean tiempoAgotado(SesionEvaluacion sesionEvaluacion);

    int finalizarSesion(SesionEvaluacion sesionEvaluacion);
}
