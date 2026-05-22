package es.upm.fi.citas_backend.config;

import es.upm.fi.citas_backend.controller.WebSocketChatController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuración de WebSocket para CU2 (Conversación en tiempo real).
 * PATRÓN: Configura el endpoint /ws/chat para conexiones WebSocket autenticadas.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketChatController webSocketChatController;

    /**
     * Registra el handler WebSocket en el endpoint /ws/chat.
     * Los clientes se conectan: ws://localhost:8080/ws/chat
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketChatController, "/ws/chat")
            .setAllowedOrigins("*");  // En producción, especificar origen exacto
    }
}
