package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.events.MensajeNuevoEvent;
import es.upm.fi.citas_backend.broker.events.UsuarioBloqueadoEvent;
import es.upm.fi.citas_backend.dto.NotificacionResponseDto;
import es.upm.fi.citas_backend.fake.FakePushService;
import es.upm.fi.citas_backend.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para NotificationService.
 * 
 * PATRÓN GRASP: MEDIADOR
 * NotificationService media entre eventos de dominio y canales de notificación.
 * 
 * CU1: Enviar Mensaje Asíncrono (notificación)
 * CU2: WebSocket (entrega en tiempo real o fallback a push)
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private FakePushService fakePushService;

    @InjectMocks
    private NotificationService notificationService;

    /**
     * CU1: MensajeNuevo - Usuario destinatario NO conectado
     * 
     * Validar que:
     * - Se registra notificación localmente
     * - Se envía push notification al usuario
     * - Se usa WebSocketSessionManager para verificar conexión
     */
    @Test
    void onMensajeNuevo_enviaNotificacionPush() {
        Long matchId = 5L;
        Long remitenteId = 1L;
        Long destinatarioId = 2L;
        Long mensajeId = 100L;

        MensajeNuevoEvent event = new MensajeNuevoEvent(matchId, remitenteId, mensajeId, mensajeId);

        // Mock: destinatario NO está conectado a WebSocket
        when(sessionManager.estaConectado(destinatarioId)).thenReturn(false);
        
        // Mock: resolver el destinatario del match (anyLong para aceptar cualquier matchId)
        when(fakePushService.resolverDestinatario(anyLong(), anyLong()))
            .thenReturn(destinatarioId);
        
        // Mock: obtener token y payload
        when(fakePushService.resolverToken(destinatarioId)).thenReturn("token_user2");
        when(fakePushService.construirPayload(mensajeId)).thenReturn("payload_json");

        // Ejecutar
        notificationService.onMensajeNuevo(event);

        // Validar que se registró localmente
        List<NotificacionResponseDto> notificaciones = notificationService.obtenerNotificaciones(destinatarioId);
        assertEquals(1, notificaciones.size());
        
        NotificacionResponseDto notif = notificaciones.get(0);
        assertEquals("NUEVO_MENSAJE", notif.getTipo());
        assertFalse(notif.isLeida());
        assertTrue(notif.getContenido().contains("100")); // mensajeId

        // Validar que se envió push
        verify(fakePushService).enviarPush("token_user2", "payload_json");
    }

    /**
     * CU2: MensajeNuevo - Usuario destinatario CONECTADO a WebSocket
     * 
     * Validar que:
     * - Si destinatario está conectado, NO se envía push
     * - Se registra notificación (entrega será por WebSocket)
     * - Optimización: evita envío redundante
     */
    @Test
    void onMensajeNuevo_OmitePushSiEstaConectado() {
        Long matchId = 5L;
        Long remitenteId = 1L;
        Long destinatarioId = 2L;
        Long mensajeId = 101L;

        MensajeNuevoEvent event = new MensajeNuevoEvent(matchId, remitenteId, mensajeId, mensajeId);

        // Mock: destinatario SÍ está conectado a WebSocket
        when(sessionManager.estaConectado(destinatarioId)).thenReturn(true);
        when(fakePushService.resolverDestinatario(anyLong(), anyLong()))
            .thenReturn(destinatarioId);

        // Ejecutar
        notificationService.onMensajeNuevo(event);

        // Validar que se registró localmente
        List<NotificacionResponseDto> notificaciones = notificationService.obtenerNotificaciones(destinatarioId);
        assertEquals(1, notificaciones.size());

        // Validar que NO se envió push (usuario conectado)
        verify(fakePushService, never()).enviarPush(anyString(), anyString());
    }

    /**
     * Validar múltiples notificaciones en orden FIFO (más reciente primero)
     * 
     * PATRÓN: ConcurrentHashMap + CopyOnWriteArrayList
     * Permite concurrencia sin race conditions
     */
    @Test
    void obtenerNotificaciones_mantieneOrdenMultiples() {
        Long destinatarioId = 2L;

        // Mock genérico: cualquier valor de matchId/remitenteId
        when(sessionManager.estaConectado(destinatarioId)).thenReturn(false);
        when(fakePushService.resolverDestinatario(anyLong(), anyLong())).thenReturn(destinatarioId);
        when(fakePushService.resolverToken(destinatarioId)).thenReturn("token");
        when(fakePushService.construirPayload(anyLong())).thenReturn("payload");
        notificationService.onMensajeNuevo(new MensajeNuevoEvent(5L, 1L, 100L, 100L));

        // Evento 2
        notificationService.onMensajeNuevo(new MensajeNuevoEvent(5L, 1L, 101L, 101L));

        // Evento 3
        notificationService.onMensajeNuevo(new MensajeNuevoEvent(5L, 1L, 102L, 102L));

        // Obtener notificaciones
        List<NotificacionResponseDto> notificaciones = notificationService.obtenerNotificaciones(destinatarioId);

        // Validar: más reciente primero (LIFO internamente)
        assertEquals(3, notificaciones.size());
        assertTrue(notificaciones.get(0).getContenido().contains("102")); // Última agregada
        assertTrue(notificaciones.get(1).getContenido().contains("101"));
        assertTrue(notificaciones.get(2).getContenido().contains("100"));
    }

    /**
     * CU: Usuario Bloqueado - Cancelar notificaciones
     * 
     * Validar que:
     * - Cuando un usuario bloquea a otro, se notifica
     * - Sistema invalida canales de comunicación activos
     */
    @Test
    void onUsuarioBloqueado_cancelalNotificaciones() {
        Long bloqueadorId = 1L;
        Long bloqueadoId = 2L;

        UsuarioBloqueadoEvent event = new UsuarioBloqueadoEvent(bloqueadorId, bloqueadoId, bloqueadoId);

        // Ejecutar - no debe lanzar excepción
        assertDoesNotThrow(() -> notificationService.onUsuarioBloqueado(event));
        
        // En este caso la implementación es principalmente logging
        // Se valida que el evento se procesa correctamente sin errores
    }

    /**
     * Validar que destinatarioId nulo/inválido no causa exception
     * PATRÓN: ROBUSTEZ - Manejo graceful de valores inválidos
     */
    @Test
    void onMensajeNuevo_manejaDestinatarioNulo() {
        Long matchId = 5L;
        Long remitenteId = 1L;
        Long mensajeId = 100L;

        MensajeNuevoEvent event = new MensajeNuevoEvent(matchId, remitenteId, mensajeId, mensajeId);

        // Mock: resolverDestinatario retorna null (caso edge)
        when(fakePushService.resolverDestinatario(anyLong(), anyLong())).thenReturn(null);

        // Ejecutar - no debe lanzar excepción
        assertDoesNotThrow(() -> notificationService.onMensajeNuevo(event));

        // Validar que no se envió push a nulo
        verify(fakePushService, never()).enviarPush(anyString(), anyString());
    }
}
