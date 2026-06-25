package ni.edu.uam.bfavocabulario.services.impl;

import ni.edu.uam.bfavocabulario.enums.EstadoSesion;
import ni.edu.uam.bfavocabulario.model.Opcion;
import ni.edu.uam.bfavocabulario.model.SesionEvaluacion;
import ni.edu.uam.bfavocabulario.services.exceptions.SesionFinalizadaException;
import ni.edu.uam.bfavocabulario.services.exceptions.TiempoAgotadoException;
import ni.edu.uam.bfavocabulario.services.interfaces.CorreccionService;
import ni.edu.uam.bfavocabulario.services.interfaces.IInterpretacion;
import ni.edu.uam.bfavocabulario.services.interfaces.SesionEvaluacionService;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class SesionEvaluacionServiceImpl implements SesionEvaluacionService {

    private static final int TIEMPO_LIMITE_MINUTOS = 5;
    private static final String SUBTEST_VOCABULARIO_A = "VOC1";

    private final CorreccionService correccionService = new CorreccionServiceImpl();
    private final IInterpretacion interpretacionService = new InterpretacionService();

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
    public SesionEvaluacion iniciarSesion(
            String nombreCompleto,
            Integer edad,
            String cedula,
            String telefono,
            String sexo,
            String colegioProcedencia,
            String procedencia
    ) {
        String nombreCompletoValidado = validarTexto(nombreCompleto, "El nombre completo es obligatorio");
        Integer edadValidada = validarEdad(edad);
        String cedulaValidada = validarTexto(cedula, "La cedula es obligatoria");
        String telefonoValidado = validarTexto(telefono, "El telefono es obligatorio");
        String sexoValidado = validarTexto(sexo, "El sexo es obligatorio");
        String colegioValidado = validarTexto(colegioProcedencia, "El colegio de procedencia es obligatorio");
        String procedenciaValidada = validarTexto(procedencia, "La procedencia es obligatoria");

        SesionEvaluacion sesionEvaluacion = iniciarSesion(nombreCompletoValidado);
        sesionEvaluacion.setNombreCompleto(nombreCompletoValidado);
        sesionEvaluacion.setEdad(edadValidada);
        sesionEvaluacion.setCedula(cedulaValidada);
        sesionEvaluacion.setTelefono(telefonoValidado);
        sesionEvaluacion.setSexo(sexoValidado);
        sesionEvaluacion.setColegioProcedencia(colegioValidado);
        sesionEvaluacion.setProcedencia(procedenciaValidada);

        return sesionEvaluacion;
    }

    @Override
    public void registrarRespuesta(SesionEvaluacion sesionEvaluacion, Opcion opcionSeleccionada) {
        validarSesion(sesionEvaluacion);

        if (tiempoAgotado(sesionEvaluacion)) {
            sesionEvaluacion.setFechaFin(LocalDateTime.now());
            sesionEvaluacion.setEstado(EstadoSesion.TIEMPO_AGOTADO);
            corregirEInterpretar(sesionEvaluacion);

            throw new TiempoAgotadoException("El tiempo de la evaluacion se ha agotado");
        }

        if (opcionSeleccionada == null) {
            return;
        }

        if (sesionEvaluacion.getOpcionesSeleccionadas() == null) {
            sesionEvaluacion.setOpcionesSeleccionadas(new ArrayList<>());
        }

        reemplazarRespuestaAnterior(sesionEvaluacion, opcionSeleccionada);
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

        return corregirEInterpretar(sesionEvaluacion);
    }

    private void reemplazarRespuestaAnterior(SesionEvaluacion sesionEvaluacion, Opcion opcionSeleccionada) {
        Long preguntaId = obtenerPreguntaId(opcionSeleccionada);

        if (preguntaId != null) {
            sesionEvaluacion.getOpcionesSeleccionadas().removeIf(
                    opcion -> preguntaId.equals(obtenerPreguntaId(opcion))
            );
        }

        sesionEvaluacion.getOpcionesSeleccionadas().add(opcionSeleccionada);
    }

    private Long obtenerPreguntaId(Opcion opcion) {
        if (opcion == null || opcion.getPregunta() == null) {
            return null;
        }

        return opcion.getPregunta().getId();
    }

    private int corregirEInterpretar(SesionEvaluacion sesionEvaluacion) {
        int puntajeDirecto = correccionService.corregirSesion(sesionEvaluacion);
        Integer percentil = interpretacionService.obtenerPercentil(SUBTEST_VOCABULARIO_A, puntajeDirecto);
        sesionEvaluacion.setPercentil(percentil == null ? 0 : percentil);
        return puntajeDirecto;
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }

        return valor.trim();
    }

    private Integer validarEdad(Integer edad) {
        if (edad == null || edad <= 0) {
            throw new IllegalArgumentException("La edad es obligatoria y debe ser numerica");
        }

        return edad;
    }

    private void validarSesion(SesionEvaluacion sesionEvaluacion) {
        if (sesionEvaluacion == null) {
            throw new IllegalArgumentException("La sesion de evaluacion no puede ser nula");
        }

        if (EstadoSesion.FINALIZADA.equals(sesionEvaluacion.getEstado())) {
            throw new SesionFinalizadaException("La sesion ya fue finalizada");
        }

        if (EstadoSesion.TIEMPO_AGOTADO.equals(sesionEvaluacion.getEstado())) {
            throw new TiempoAgotadoException("La sesion ya termino por tiempo agotado");
        }
    }
}
