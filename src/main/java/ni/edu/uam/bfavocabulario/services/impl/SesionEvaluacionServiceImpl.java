package ni.edu.uam.bfavocabulario.services.impl;

import ni.edu.uam.bfavocabulario.enums.EstadoSesion;
import ni.edu.uam.bfavocabulario.model.Opcion;
import ni.edu.uam.bfavocabulario.model.SesionEvaluacion;
import ni.edu.uam.bfavocabulario.services.exceptions.SesionFinalizadaException;
import ni.edu.uam.bfavocabulario.services.exceptions.TiempoAgotadoException;
import ni.edu.uam.bfavocabulario.services.interfaces.CorreccionService;
import ni.edu.uam.bfavocabulario.services.interfaces.SesionEvaluacionService;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class SesionEvaluacionServiceImpl implements SesionEvaluacionService {

    private static final int TIEMPO_LIMITE_MINUTOS = 5;

    private final CorreccionService correccionService = new CorreccionServiceImpl();

    @Override
    public SesionEvaluacion iniciarSesion(String nombreEvaluado) {
        if (nombreEvaluado == null || nombreEvaluado.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evaluado es obligatorio");
        }

        SesionEvaluacion sesionEvaluacion = new SesionEvaluacion();

        sesionEvaluacion.setNombreEvaluado(nombreEvaluado);
        sesionEvaluacion.setFechaInicio(LocalDateTime.now());
        sesionEvaluacion.setEstado(EstadoSesion.INICIADA);
        sesionEvaluacion.setPuntajeDirecto(0);
        sesionEvaluacion.setPercentil(0);

        return sesionEvaluacion;
    }

    @Override
    public void registrarRespuesta(SesionEvaluacion sesionEvaluacion, Opcion opcionSeleccionada) {
        validarSesion(sesionEvaluacion);

        if (tiempoAgotado(sesionEvaluacion)) {
            sesionEvaluacion.setFechaFin(LocalDateTime.now());
            sesionEvaluacion.setEstado(EstadoSesion.TIEMPO_AGOTADO);
            correccionService.corregirSesion(sesionEvaluacion);

            throw new TiempoAgotadoException("El tiempo de la evaluación se ha agotado");
        }

        if (opcionSeleccionada == null) {
            return;
        }

        if (sesionEvaluacion.getOpcionesSeleccionadas() == null) {
            sesionEvaluacion.setOpcionesSeleccionadas(new ArrayList<>());
        }

        sesionEvaluacion.getOpcionesSeleccionadas().add(opcionSeleccionada);
    }

    @Override
    public boolean tiempoAgotado(SesionEvaluacion sesionEvaluacion) {
        if (sesionEvaluacion == null || sesionEvaluacion.getFechaInicio() == null) {
            return false;
        }

        LocalDateTime tiempoLimite = sesionEvaluacion.getFechaInicio().plusMinutes(TIEMPO_LIMITE_MINUTOS);

        return LocalDateTime.now().isAfter(tiempoLimite);
    }

    @Override
    public int finalizarSesion(SesionEvaluacion sesionEvaluacion) {
        validarSesion(sesionEvaluacion);

        sesionEvaluacion.setFechaFin(LocalDateTime.now());
        sesionEvaluacion.setEstado(EstadoSesion.FINALIZADA);

        return correccionService.corregirSesion(sesionEvaluacion);
    }

    private void validarSesion(SesionEvaluacion sesionEvaluacion) {
        if (sesionEvaluacion == null) {
            throw new IllegalArgumentException("La sesión de evaluación no puede ser nula");
        }

        if (EstadoSesion.FINALIZADA.equals(sesionEvaluacion.getEstado())) {
            throw new SesionFinalizadaException("La sesión ya fue finalizada");
        }

        if (EstadoSesion.TIEMPO_AGOTADO.equals(sesionEvaluacion.getEstado())) {
            throw new TiempoAgotadoException("La sesión ya terminó por tiempo agotado");
        }
    }
}