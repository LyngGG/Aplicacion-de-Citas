package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.events.UsuarioBloqueadoEvent;
import es.upm.fi.citas_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final WebSocketSessionManager sessionManager;

    @Async
    @EventListener
    public void onUsuarioBloqueado(UsuarioBloqueadoEvent event) {
        log.info("[ChatService] Bloqueando mensajes → bloqueador={} bloqueado={}",
            event.getBloqueadorId(), event.getBloqueadoId());

        if (sessionManager.estaConectado(event.getBloqueadoId())) {
            sessionManager.eliminarSesion(event.getBloqueadoId());
            log.info("[ChatService] Sesión WS cerrada para usuarioId={}", event.getBloqueadoId());
        }

        log.info("[ChatService] [FAKE] Mensajes bloqueados entre {} y {}",
            event.getBloqueadorId(), event.getBloqueadoId());
    }
}
