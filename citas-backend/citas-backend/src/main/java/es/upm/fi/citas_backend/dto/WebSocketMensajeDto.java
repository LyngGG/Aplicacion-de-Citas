package es.upm.fi.citas_backend.dto;

import lombok.*;

/**
 * DTO para mensajes WebSocket en tiempo real (CU2).
 * Estructura simple para intercambio de datos en WebSocket.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMensajeDto {
    
    /**
     * ID del match al que pertenece el mensaje.
     */
    private Long matchId;
    
    /**
     * Texto del mensaje.
     */
    private String texto;
}
