package es.upm.fi.citas_backend.exception;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(Long matchId) {
        super("Match no encontrado: " + matchId);
    }
}