package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.events.UsuarioBloqueadoEvent;
import es.upm.fi.citas_backend.fake.FakeCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscoveryService {

    private final FakeCacheService cache;

    @Async
    @EventListener
    public void onUsuarioBloqueado(UsuarioBloqueadoEvent event) {
        log.info("[DiscoveryService] Excluyendo del índice → bloqueador={} bloqueado={}",
            event.getBloqueadorId(), event.getBloqueadoId());

        cache.evict("perfil:" + event.getBloqueadorId());
        cache.evict("perfil:" + event.getBloqueadoId());

        log.info("[DiscoveryService] [FAKE] Índice actualizado → usuarios {} y {} excluidos mutuamente",
            event.getBloqueadorId(), event.getBloqueadoId());
    }
}