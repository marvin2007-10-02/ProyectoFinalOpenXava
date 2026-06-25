package ni.edu.uam.bfavocabulario.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.bfavocabulario.enums.EstadoSesion;
import ni.edu.uam.bfavocabulario.model.Opcion;
import ni.edu.uam.bfavocabulario.model.Pregunta;
import ni.edu.uam.bfavocabulario.model.SesionEvaluacion;
import ni.edu.uam.bfavocabulario.services.exceptions.SesionFinalizadaException;
import ni.edu.uam.bfavocabulario.services.exceptions.TiempoAgotadoException;
import ni.edu.uam.bfavocabulario.services.impl.SesionEvaluacionServiceImpl;
import ni.edu.uam.bfavocabulario.services.interfaces.SesionEvaluacionService;
import org.openxava.jpa.XPersistence;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BfaApiServlet extends HttpServlet {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int TIEMPO_LIMITE_SEGUNDOS = 300;

    private final SesionEvaluacionService sesionEvaluacionService = new SesionEvaluacionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("/test/vocabulario-a/preguntas".equals(request.getPathInfo())) {
            ejecutarConJpa(response, this::obtenerPreguntas);
            return;
        }

        responderError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint no encontrado");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();

        try {
            if ("/sesion/iniciar".equals(path)) {
                IniciarSesionRequest body = leerJson(request, IniciarSesionRequest.class);
                ejecutarConJpa(response, () -> iniciarSesion(body));
                return;
            }

            if ("/sesion/responder".equals(path)) {
                ResponderRequest body = leerJson(request, ResponderRequest.class);
                ejecutarConJpa(response, () -> registrarRespuesta(body));
                return;
            }

            if ("/sesion/finalizar".equals(path)) {
                FinalizarSesionRequest body = leerJson(request, FinalizarSesionRequest.class);
                ejecutarConJpa(response, () -> finalizarSesion(body));
                return;
            }

            responderError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint no encontrado");
        }
        catch (IOException ex) {
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, "El JSON enviado no es valido");
        }
    }

    private Map<String, Object> iniciarSesion(IniciarSesionRequest request) {
        if (request == null) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Los datos del evaluado son obligatorios");
        }

        SesionEvaluacion sesion = sesionEvaluacionService.iniciarSesion(
                request.nombreCompleto,
                request.edad,
                request.cedula,
                request.telefono,
                request.sexo,
                request.colegioProcedencia,
                request.procedencia
        );
        XPersistence.getManager().persist(sesion);
        XPersistence.getManager().flush();

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("sesionId", sesion.getId());
        respuesta.put("nombreEvaluado", sesion.getNombreEvaluado());
        respuesta.put("nombreCompleto", sesion.getNombreCompleto());
        respuesta.put("tiempoLimiteSegundos", TIEMPO_LIMITE_SEGUNDOS);
        return respuesta;
    }

    private Map<String, Object> obtenerPreguntas() {
        EntityManager manager = XPersistence.getManager();
        List<Pregunta> preguntas = manager.createQuery(
                "select distinct p from Pregunta p left join fetch p.opciones order by p.numero",
                Pregunta.class
        ).getResultList();

        List<Map<String, Object>> preguntasJson = new ArrayList<>();

        for (Pregunta pregunta : preguntas) {
            List<Opcion> opciones = pregunta.getOpciones() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(pregunta.getOpciones());
            opciones.sort(Comparator.comparing(opcion -> opcion.getLetra() == null ? "" : opcion.getLetra().name()));

            List<Map<String, Object>> opcionesJson = new ArrayList<>();
            for (Opcion opcion : opciones) {
                Map<String, Object> opcionJson = new LinkedHashMap<>();
                opcionJson.put("id", opcion.getId());
                opcionJson.put("letra", opcion.getLetra() == null ? "" : opcion.getLetra().name());
                opcionJson.put("texto", opcion.getTexto());
                opcionesJson.add(opcionJson);
            }

            Map<String, Object> preguntaJson = new LinkedHashMap<>();
            preguntaJson.put("id", pregunta.getId());
            preguntaJson.put("numero", pregunta.getNumero());
            preguntaJson.put("texto", pregunta.getTextoPrincipal());
            preguntaJson.put("opciones", opcionesJson);
            preguntasJson.add(preguntaJson);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("preguntas", preguntasJson);
        respuesta.put("total", preguntasJson.size());
        return respuesta;
    }

    private Map<String, Object> registrarRespuesta(ResponderRequest request) {
        validarId(request == null ? null : request.sesionId, "El id de sesion es obligatorio");
        validarId(request.opcionId, "El id de opcion es obligatorio");

        EntityManager manager = XPersistence.getManager();
        SesionEvaluacion sesion = manager.find(SesionEvaluacion.class, request.sesionId);
        if (sesion == null) {
            throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "La sesion no existe");
        }

        if (EstadoSesion.FINALIZADA.equals(sesion.getEstado())) {
            throw new ApiException(HttpServletResponse.SC_CONFLICT, "La sesion ya fue finalizada");
        }

        if (EstadoSesion.TIEMPO_AGOTADO.equals(sesion.getEstado()) || sesionEvaluacionService.tiempoAgotado(sesion)) {
            throw new ApiException(HttpServletResponse.SC_CONFLICT, "El tiempo limite se ha agotado. Finalice la prueba");
        }

        Opcion opcion = manager.find(Opcion.class, request.opcionId);
        if (opcion == null) {
            throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "La opcion no existe");
        }

        sesionEvaluacionService.registrarRespuesta(sesion, opcion);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("sesionId", sesion.getId());
        respuesta.put("estado", sesion.getEstado().name());
        respuesta.put("respuestasRegistradas", sesion.getOpcionesSeleccionadas().size());
        return respuesta;
    }

    private Map<String, Object> finalizarSesion(FinalizarSesionRequest request) {
        validarId(request == null ? null : request.sesionId, "El id de sesion es obligatorio");

        SesionEvaluacion sesion = XPersistence.getManager().find(SesionEvaluacion.class, request.sesionId);
        if (sesion == null) {
            throw new ApiException(HttpServletResponse.SC_NOT_FOUND, "La sesion no existe");
        }

        if (!EstadoSesion.FINALIZADA.equals(sesion.getEstado())) {
            sesionEvaluacionService.finalizarSesion(sesion);
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("puntajeDirecto", sesion.getPuntajeDirecto() == null ? 0 : sesion.getPuntajeDirecto());
        respuesta.put("percentil", sesion.getPercentil() == null ? 0 : sesion.getPercentil());
        respuesta.put("estado", sesion.getEstado().name());
        return respuesta;
    }

    private void ejecutarConJpa(HttpServletResponse response, JpaOperation operation) throws IOException {
        try {
            Object resultado = operation.execute();
            XPersistence.commit();
            responderJson(response, HttpServletResponse.SC_OK, resultado);
        }
        catch (ApiException ex) {
            XPersistence.rollback();
            responderError(response, ex.status, ex.getMessage());
        }
        catch (IllegalArgumentException ex) {
            XPersistence.rollback();
            responderError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
        catch (SesionFinalizadaException | TiempoAgotadoException ex) {
            XPersistence.rollback();
            responderError(response, HttpServletResponse.SC_CONFLICT, ex.getMessage());
        }
        catch (Exception ex) {
            XPersistence.rollback();
            getServletContext().log("Error procesando API BFA", ex);
            responderError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No fue posible procesar la solicitud");
        }
    }

    private <T> T leerJson(HttpServletRequest request, Class<T> tipo) throws IOException {
        return mapper.readValue(request.getInputStream(), tipo);
    }

    private void validarId(Long id, String mensaje) {
        if (id == null || id <= 0) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, mensaje);
        }
    }

    private void responderError(HttpServletResponse response, int status, String mensaje) throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("mensaje", mensaje);
        responderJson(response, status, error);
    }

    private void responderJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), body);
    }

    private interface JpaOperation {
        Object execute();
    }

    private static class ApiException extends RuntimeException {
        private final int status;

        private ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }

    public static class IniciarSesionRequest {
        public String nombreCompleto;
        public Integer edad;
        public String cedula;
        public String telefono;
        public String sexo;
        public String colegioProcedencia;
        public String procedencia;
    }

    public static class ResponderRequest {
        public Long sesionId;
        public Long opcionId;
    }

    public static class FinalizarSesionRequest {
        public Long sesionId;
    }
}
