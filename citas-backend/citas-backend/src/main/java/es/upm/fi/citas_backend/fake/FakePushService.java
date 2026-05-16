package es.upm.fi.citas_backend.fake;

import es.upm.fi.citas_backend.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FakePushService {

    private final MatchRepository matchRepository;

    public Long resolverDestinatario(Long matchId, Long remitenteId) {
        return matchRepository.findById(matchId).map(match -> {
            Long u1 = match.getUsuario1().getId();
            Long u2 = match.getUsuario2().getId();
            return u1.equals(remitenteId) ? u2 : u1;
        }).orElse(-1L);
    }

    public String resolverToken(Long usuarioId) {
        String token = "fake-device-token-usuario-" + usuarioId;
        log.debug("[FakePush] Token resuelto → usuarioId={} token={}", usuarioId, token);
        return token;
    }

    public String construirPayload(Long mensajeId) {
        return "{\"type\":\"NUEVO_MENSAJE\",\"mensajeId\":" + mensajeId + "}";
    }

    public void enviarPush(String token, String payload) {
        log.info("[FakePush] >>> PUSH ENVIADA (simulada) token={} payload={}", token, payload);
    }
}
