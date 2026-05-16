package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.events.MensajeNuevoEvent;
import es.upm.fi.citas_backend.broker.events.UsuarioBloqueadoEvent;
import es.upm.fi.citas_backend.fake.FakePushService;
import es.upm.fi.citas_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final WebSocketSessionManager sessionManager;
    private final FakePushService         fakePushService;

    @Async
    @EventListener
    public void onMensajeNuevo(MensajeNuevoEvent event) {
        log.info("[NotificationService] MensajeNuevo → matchId={} remitenteId={} mensajeId={}",
            event.getMatchId(), event.getRemitenteId(), event.getMensajeId());

        Long destinatarioId = fakePushService.resolverDestinatario(
            event.getMatchId(), event.getRemitenteId());

        if (sessionManager.estaConectado(destinatarioId)) {
            log.info("[NotificationService] Destinatario {} conectado → push omitida", destinatarioId);
        } else {
            String token   = fakePushService.resolverToken(destinatarioId);
            String payload = fakePushService.construirPayload(event.getMensajeId());
            fakePushService.enviarPush(token, payload);
        }
    }

    @Async
    @EventListener
    public void onUsuarioBloqueado(UsuarioBloqueadoEvent event) {
        log.info("[NotificationService] Cancelando notificaciones → bloqueador={} bloqueado={}",
            event.getBloqueadorId(), event.getBloqueadoId());
        log.info("[NotificationService] [FAKE] Notificaciones canceladas entre {} y {}",
            event.getBloqueadorId(), event.getBloqueadoId());
    }
}