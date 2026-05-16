package es.upm.fi.citas_backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Component
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res,
                                   WebSocketHandler handler, Map<String, Object> attrs) {
        String userId = UriComponentsBuilder
            .fromUri(req.getURI())
            .build()
            .getQueryParams()
            .getFirst("userId");

        if (userId == null) {
            log.warn("[JwtInterceptor] Conexión rechazada: falta userId");
            return false;
        }

        attrs.put("usuarioId", Long.parseLong(userId));
        log.info("[JwtInterceptor] Handshake aceptado para usuarioId={}", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
                               WebSocketHandler handler, Exception ex) { }
}