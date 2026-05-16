package es.upm.fi.citas_backend.broker;

import es.upm.fi.citas_backend.broker.events.MensajeNuevoEvent;
import es.upm.fi.citas_backend.broker.events.UsuarioBloqueadoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageBroker {

    private final ApplicationEventPublisher publisher;

    public void publicar(Long matchId, Long remitenteId, Long mensajeId) {
        publisher.publishEvent(new MensajeNuevoEvent(this, matchId, remitenteId, mensajeId));
    }

    public void publicarBloqueo(Long bloqueadorId, Long bloqueadoId) {
        publisher.publishEvent(new UsuarioBloqueadoEvent(this, bloqueadorId, bloqueadoId));
    }
}