package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.dto.WebSocketMensajeDto;
import es.upm.fi.citas_backend.service.MatchService;
import es.upm.fi.citas_backend.service.MensajeService;
import es.upm.fi.citas_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * CU2 - Iniciar Conversación con Match Activo
 * 
 * CONTROLADOR: delega en servicios manteniendo ALTA COHESIÓN.
 * Maneja:
 * 1. Registro de sesiones WebSocket
 * 2. Validación de match activo
 * 3. Creación de mensajes
 * 4. Entrega en tiempo real
 * 5. Publicación de eventos asíncronos
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketChatController extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final MatchService matchService;
    private final MensajeService mensajeService;
    private final ObjectMapper objectMapper;

    /**
     * CU2 - Paso 1: Conectar (autenticado con JWT).
     * FABRICACIÓN PURA: gestión de sesiones WebSocket.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession sesion) throws Exception {
        // Extraer usuarioId del principal (JWT)
        Long usuarioId = extractUsuarioIdFromSession(sesion);
        
        if (usuarioId != null) {
            sessionManager.registrarSesion(usuarioId, sesion);
            log.info("[CU2] Conexión WebSocket establecida → usuarioId={}", usuarioId);
        } else {
            sesion.close(CloseStatus.POLICY_VIOLATION);
            log.warn("[CU2] Conexión rechazada: usuario no autenticado");
        }
    }

    /**
     * CU2 - Pasos 2-5: Enviar mensaje en tiempo real.
     * 
     * Flujo:
     * 1. Validar match activo (MatchService)
     * 2. Crear mensaje (MensajeService)
     * 3. Si destinatario conectado → entregar en tiempo real (WebSocketSessionManager)
     * 4. Publicar evento asíncrono (Broker)
     */
    @Override
    public void handleTextMessage(WebSocketSession sesion, TextMessage mensaje) throws Exception {
        try {
            // Parsear el mensaje WebSocket
            WebSocketMensajeDto dto = objectMapper.readValue(
                mensaje.getPayload(),
                WebSocketMensajeDto.class
            );

            Long remitenteId = extractUsuarioIdFromSession(sesion);
            Long matchId = dto.getMatchId();
            String texto = dto.getTexto();

            log.info("[CU2] Mensaje recibido → matchId={}, remitenteId={}, texto={}", 
                matchId, remitenteId, texto);

            // PASO 2: Validar match activo
            // BAJO ACOPLAMIENTO: MatchService es experto en validaciones de Match
            Match match = matchService.validarMatchActivo(matchId, remitenteId);

            // PASO 3: Crear y persistir mensaje
            // CONTROLADOR delega en MensajeService
            MensajeResponseDto mensajeCreado = mensajeService.crearMensaje(
                match,
                remitenteId,
                texto
            );

            // Determinar el destinatario
            Long destinatarioId = match.getUsuario1().getId().equals(remitenteId)
                ? match.getUsuario2().getId()
                : match.getUsuario1().getId();

            // PASO 4: Entregar en tiempo real si el destinatario está conectado
            // EXPERTO: WebSocketSessionManager conoce las sesiones activas
            boolean entregado = sessionManager.enviarAlDestinatario(
                destinatarioId,
                objectMapper.writeValueAsString(mensajeCreado)
            );

            if (entregado) {
                log.info("[CU2] Mensaje entregado en tiempo real → destinatarioId={}", destinatarioId);
                // PASO 5: Publicar evento "MensajeNuevo" (asíncrono)
                // El Broker notificará a NotificationService si es necesario
                // Esto se maneja en MensajeService.crearMensaje()
            } else {
                log.info("[CU2] Destinatario desconectado, se usará push notification");
                // El Broker (en MensajeService) enviará notificación push
            }

            // ACK al remitente
            sesion.sendMessage(new TextMessage(
                objectMapper.writeValueAsString(mensajeCreado)
            ));

        } catch (Exception e) {
            log.error("[CU2] Error procesando mensaje: {}", e.getMessage());
            sesion.sendMessage(new TextMessage("{\"error\": \"" + e.getMessage() + "\"}"));
        }
    }

    /**
     * Cerrar sesión cuando el usuario se desconecta.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) throws Exception {
        Long usuarioId = extractUsuarioIdFromSession(sesion);
        if (usuarioId != null) {
            sessionManager.eliminarSesion(usuarioId);
            log.info("[CU2] Conexión cerrada → usuarioId={}", usuarioId);
        }
    }

    /**
     * Extraer usuarioId del principal autenticado (JWT).
     * En una implementación real, esto vendría del token JWT.
     */
    private Long extractUsuarioIdFromSession(WebSocketSession sesion) {
        try {
            Object principal = sesion.getPrincipal();
            if (principal != null) {
                // En Spring Security, principal.getName() contiene el usuarioId
                String usuarioIdStr = principal.toString();
                return Long.parseLong(usuarioIdStr);
            }
        } catch (Exception e) {
            log.warn("[CU2] No se pudo extraer usuarioId: {}", e.getMessage());
        }
        return null;
    }
}
