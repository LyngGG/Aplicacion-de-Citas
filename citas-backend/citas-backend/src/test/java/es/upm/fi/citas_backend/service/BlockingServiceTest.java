package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.MessageBroker;
import es.upm.fi.citas_backend.domain.Bloqueo;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.BloqueoResponseDto;
import es.upm.fi.citas_backend.exception.BloqueoYaExisteException;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.BloqueoRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BlockingService.
 * 
 * PATRÓN GRASP: EXPERTO + MEDIADOR
 * BlockingService es experto en reglas de bloqueo.
 * MessageBroker media para publicar eventos de bloqueo.
 * 
 * CU: Sistema de Bloqueos (previo a mensajes y matches)
 * 
 * Requisitos:
 * - Un usuario puede bloquear a otro (unidireccional)
 * - No se puede bloquear al mismo usuario
 * - No se puede crear bloqueo duplicado
 * - Bloqueo invalida matches activos
 */
@ExtendWith(MockitoExtension.class)
class BlockingServiceTest {

    @Mock
    private BloqueoRepository bloqueoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private MessageBroker messageBroker;

    @InjectMocks
    private BlockingService blockingService;

    /**
     * CU: Bloquear usuario correctamente
     * 
     * Validar que:
     * - Se crea bloqueo entre dos usuarios
     * - Se guarda en repositorio
     * - Se invalidan matches previos
     * - Se publica evento en broker
     * - Se retorna DTO con fecha de bloqueo
     */
    @Test
    void bloquearUsuario_crearBloqueoValido() {
        Long bloqueadorId = 1L;
        Long bloqueadoId = 2L;
        LocalDateTime ahora = LocalDateTime.now();

        Usuario bloqueador = Usuario.builder()
            .id(bloqueadorId)
            .email("bloqueador@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        Usuario bloqueado = Usuario.builder()
            .id(bloqueadoId)
            .email("bloqueado@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        // Mock: bloqueo no existe
        when(bloqueoRepository.existeBloqueo(bloqueadorId, bloqueadoId)).thenReturn(false);
        
        // Mock: usuarios existen
        when(usuarioRepository.findById(bloqueadorId)).thenReturn(Optional.of(bloqueador));
        when(usuarioRepository.findById(bloqueadoId)).thenReturn(Optional.of(bloqueado));
        
        // Mock: save asigna ID
        when(bloqueoRepository.save(any(Bloqueo.class))).thenAnswer(invocation -> {
            Bloqueo bloqueo = invocation.getArgument(0);
            bloqueo.setId(10L);
            return bloqueo;
        });

        // Ejecutar
        BloqueoResponseDto response = blockingService.bloquearUsuario(bloqueadorId, bloqueadoId);

        // Validar respuesta
        assertNotNull(response);
        assertEquals(10L, response.getBloqueoId());
        assertNotNull(response.getFechaBloqueo());

        // Validar que se guardó correctamente
        ArgumentCaptor<Bloqueo> captor = ArgumentCaptor.forClass(Bloqueo.class);
        verify(bloqueoRepository).save(captor.capture());
        
        Bloqueo saved = captor.getValue();
        assertEquals(bloqueador, saved.getBloqueador());
        assertEquals(bloqueado, saved.getBloqueado());
        assertNotNull(saved.getFechaBloqueo());

        // Validar que se invalidaron matches
        verify(matchService).invalidarMatchSiExiste(bloqueadorId, bloqueadoId);

        // Validar que se publicó evento
        verify(messageBroker).publicarBloqueo(bloqueadorId, bloqueadoId);
    }

    /**
     * Validar que se rechaza bloqueo duplicado
     * PATRÓN: IDEMPOTENCIA - No crear duplicados
     */
    @Test
    void bloquearUsuario_rechazaBloqueoYaExistente() {
        Long bloqueadorId = 1L;
        Long bloqueadoId = 2L;

        // Mock: bloqueo ya existe
        when(bloqueoRepository.existeBloqueo(bloqueadorId, bloqueadoId)).thenReturn(true);

        // Validar que lanza excepción
        assertThrows(BloqueoYaExisteException.class, () ->
            blockingService.bloquearUsuario(bloqueadorId, bloqueadoId));

        // Validar que NO se guardó nada
        verify(bloqueoRepository, never()).save(any());
        verify(matchService, never()).invalidarMatchSiExiste(anyLong(), anyLong());
        verify(messageBroker, never()).publicarBloqueo(anyLong(), anyLong());
    }

    /**
     * Validar que se rechaza bloqueo si bloqueador no existe
     */
    @Test
    void bloquearUsuario_lanzaSiBloqueadorNoExiste() {
        Long bloqueadorId = 404L;
        Long bloqueadoId = 2L;

        when(bloqueoRepository.existeBloqueo(bloqueadorId, bloqueadoId)).thenReturn(false);
        when(usuarioRepository.findById(bloqueadorId)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            blockingService.bloquearUsuario(bloqueadorId, bloqueadoId));
    }

    /**
     * Validar que se rechaza bloqueo si bloqueado no existe
     */
    @Test
    void bloquearUsuario_lanzaSiBloqueadoNoExiste() {
        Long bloqueadorId = 1L;
        Long bloqueadoId = 404L;

        Usuario bloqueador = Usuario.builder()
            .id(bloqueadorId)
            .email("bloqueador@test.com")
            .build();

        when(bloqueoRepository.existeBloqueo(bloqueadorId, bloqueadoId)).thenReturn(false);
        when(usuarioRepository.findById(bloqueadorId)).thenReturn(Optional.of(bloqueador));
        when(usuarioRepository.findById(bloqueadoId)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            blockingService.bloquearUsuario(bloqueadorId, bloqueadoId));
    }

    /**
     * Validar que bloqueo es UNIDIRECCIONAL
     * 
     * Si A bloquea a B:
     * - A no verá perfiles de B
     * - B PUEDE ver perfil de A (a menos que B bloquee a A)
     */
    @Test
    void bloquearUsuario_esUnidireccional() {
        Long usuarioA = 1L;
        Long usuarioB = 2L;

        Usuario userA = Usuario.builder().id(usuarioA).email("A@test.com").build();
        Usuario userB = Usuario.builder().id(usuarioB).email("B@test.com").build();

        // Mock genérico para save: asigna IDs secuenciales
        java.util.concurrent.atomic.AtomicLong idGenerator = new java.util.concurrent.atomic.AtomicLong(9L);
        when(bloqueoRepository.save(any(Bloqueo.class))).thenAnswer(inv -> {
            Bloqueo b = inv.getArgument(0);
            b.setId(idGenerator.incrementAndGet());
            return b;
        });

        // A bloquea a B
        when(bloqueoRepository.existeBloqueo(usuarioA, usuarioB)).thenReturn(false);
        when(usuarioRepository.findById(usuarioA)).thenReturn(Optional.of(userA));
        when(usuarioRepository.findById(usuarioB)).thenReturn(Optional.of(userB));

        blockingService.bloquearUsuario(usuarioA, usuarioB);

        // Ahora B intenta bloquear a A - debe permitirse (es un bloqueo diferente)
        when(bloqueoRepository.existeBloqueo(usuarioB, usuarioA)).thenReturn(false);

        assertDoesNotThrow(() -> blockingService.bloquearUsuario(usuarioB, usuarioA));

        // Validar: se crearon 2 bloqueos distintos
        verify(bloqueoRepository, times(2)).save(any(Bloqueo.class));
    }

    /**
     * Validar que bloqueo limpia matches activos
     * PATRÓN: Consistencia de estado
     */
    @Test
    void bloquearUsuario_invalidaMatchesActivos() {
        Long bloqueadorId = 1L;
        Long bloqueadoId = 2L;

        Usuario bloqueador = Usuario.builder().id(bloqueadorId).email("A@test.com").build();
        Usuario bloqueado = Usuario.builder().id(bloqueadoId).email("B@test.com").build();

        when(bloqueoRepository.existeBloqueo(bloqueadorId, bloqueadoId)).thenReturn(false);
        when(usuarioRepository.findById(bloqueadorId)).thenReturn(Optional.of(bloqueador));
        when(usuarioRepository.findById(bloqueadoId)).thenReturn(Optional.of(bloqueado));
        when(bloqueoRepository.save(any())).thenAnswer(inv -> {
            Bloqueo b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        blockingService.bloquearUsuario(bloqueadorId, bloqueadoId);

        // Validar que se invalidó match
        verify(matchService).invalidarMatchSiExiste(bloqueadorId, bloqueadoId);
    }
}
