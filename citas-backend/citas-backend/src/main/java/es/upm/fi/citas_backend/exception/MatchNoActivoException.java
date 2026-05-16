package es.upm.fi.citas_backend.exception;

public class MatchNoActivoException extends RuntimeException {
    public MatchNoActivoException(Long matchId, Long usuarioId) {
        super("Match " + matchId + " no está activo o el usuario " + usuarioId + " no pertenece a él");
    }
}