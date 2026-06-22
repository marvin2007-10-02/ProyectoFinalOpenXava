package ni.edu.uam.bfavocabulario.services.exceptions;

public class SesionFinalizadaException extends RuntimeException {

    public SesionFinalizadaException(String message) {
        super(message);
    }
}