package es.upm.fi.citas_backend.broker.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MensajeNuevoEvent extends ApplicationEvent {

    private final Long matchId;
    private final Long remitenteId;
    private final Long mensajeId;

    public MensajeNuevoEvent(Object source, Long matchId, Long remitenteId, Long mensajeId) {
        super(source);
        this.matchId     = matchId;
        this.remitenteId = remitenteId;
        this.mensajeId   = mensajeId;
    }
}