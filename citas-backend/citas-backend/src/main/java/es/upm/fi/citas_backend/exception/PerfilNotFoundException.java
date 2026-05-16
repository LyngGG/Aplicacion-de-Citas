package es.upm.fi.citas_backend.exception;


public class PerfilNotFoundException extends RuntimeException {
    public PerfilNotFoundException(Long usuarioId) {
        super("Perfil no encontrado para usuarioId: " + usuarioId);
    }
}