package es.upm.fi.citas_backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    private final Map<Long, WebSocketSession> sesiones = new ConcurrentHashMap<>();

    public void registrarSesion(Long usuarioId, WebSocketSession sesion) {
        sesiones.put(usuarioId, sesion);
        log.info("[WSManager] Sesión registrada → usuarioId={}", usuarioId);
    }

    public void eliminarSesion(Long usuarioId) {
        sesiones.remove(usuarioId);
        log.info("[WSManager] Sesión eliminada → usuarioId={}", usuarioId);
    }

    public boolean estaConectado(Long usuarioId) {
        WebSocketSession s = sesiones.get(usuarioId);
        return s != null && s.isOpen();
    }

    public boolean enviarAlDestinatario(Long destinatarioId, String mensajeJson) {
        WebSocketSession s = sesiones.get(destinatarioId);
        if (s == null || !s.isOpen()) {
            log.warn("[WSManager] Sin sesión activa para usuarioId={}", destinatarioId);
            return false;
        }
        try {
            synchronized (s) {
                s.sendMessage(new TextMessage(mensajeJson));
            }
            log.info("[WSManager] Mensaje entregado en tiempo real → destinatarioId={}", destinatarioId);
            return true;
        } catch (IOException e) {
            log.error("[WSManager] Error al enviar a usuarioId={}: {}", destinatarioId, e.getMessage());
            return false;
        }
    }
}
