package es.upm.fi.citas_backend.websocket;

import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.dto.WsMensajeRequestDto;
import es.upm.fi.citas_backend.service.MatchService;
import es.upm.fi.citas_backend.service.MensajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final MatchService            matchService;
    private final MensajeService          mensajeService;
    private final ObjectMapper            objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long usuarioId = extraerUsuarioId(session);
        sessionManager.registrarSesion(usuarioId, session);
        sendAck(session, "conexión establecida para usuarioId=" + usuarioId);
        log.info("[ChatWSHandler] Conexión establecida → usuarioId={}", usuarioId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long usuarioId = extraerUsuarioId(session);
        sessionManager.eliminarSesion(usuarioId);
        log.info("[ChatWSHandler] Conexión cerrada → usuarioId={} status={}", usuarioId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long remitenteId = extraerUsuarioId(session);

        WsMensajeRequestDto req = objectMapper.readValue(message.getPayload(), WsMensajeRequestDto.class);
        Match match = matchService.validarMatchActivo(req.getMatchId(), remitenteId);
        MensajeResponseDto resp = mensajeService.crearMensaje(match, remitenteId, req.getTexto());

        Long destinatarioId = resolverDestinatario(match, remitenteId);
        if (sessionManager.estaConectado(destinatarioId)) {
            sessionManager.enviarAlDestinatario(destinatarioId, objectMapper.writeValueAsString(resp));
        }

        sendAck(session, objectMapper.writeValueAsString(resp));
    }

    private Long extraerUsuarioId(WebSocketSession session) {
        return (Long) session.getAttributes().get("usuarioId");
    }

    private Long resolverDestinatario(Match match, Long remitenteId) {
        return match.getUsuario1().getId().equals(remitenteId)
            ? match.getUsuario2().getId()
            : match.getUsuario1().getId();
    }

    private void sendAck(WebSocketSession session, String texto) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage("{\"ack\":\"" + texto + "\"}"));
            }
        } catch (Exception e) {
            log.error("[ChatWSHandler] Error al enviar ACK: {}", e.getMessage());
        }
    }
}
