package es.upm.fi.citas_backend.exception;


public class BloqueoYaExisteException extends RuntimeException {
    public BloqueoYaExisteException(Long bloqueadorId, Long bloqueadoId) {
        super("Ya existe un bloqueo de " + bloqueadorId + " sobre " + bloqueadoId);
    }
}
