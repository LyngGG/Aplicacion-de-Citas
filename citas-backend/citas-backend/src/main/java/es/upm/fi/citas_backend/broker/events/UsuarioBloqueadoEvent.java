package es.upm.fi.citas_backend.broker.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UsuarioBloqueadoEvent extends ApplicationEvent {

    private final Long bloqueadorId;
    private final Long bloqueadoId;

    public UsuarioBloqueadoEvent(Object source, Long bloqueadorId, Long bloqueadoId) {
        super(source);
        this.bloqueadorId = bloqueadorId;
        this.bloqueadoId  = bloqueadoId;
    }
}