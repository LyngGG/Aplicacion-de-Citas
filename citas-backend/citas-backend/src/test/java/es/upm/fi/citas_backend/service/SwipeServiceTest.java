package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Swipe;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.SwipeRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

    @Mock
    private SwipeRepository swipeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private SwipeService swipeService;

    @Test
    void obtenerInteraccionesRealizadas_devuelveIds() {
        Long usuarioId = 1L;
        List<Long> esperados = List.of(2L, 3L, 4L);

        when(swipeRepository.findIdsInteractuadosByUsuarioId(usuarioId)).thenReturn(esperados);

        List<Long> resultado = swipeService.obtenerInteraccionesRealizadas(usuarioId);

        assertEquals(esperados, resultado);
        verify(swipeRepository).findIdsInteractuadosByUsuarioId(usuarioId);
    }

    @Test
    void crearSwipe_aceptadoConReciprocidad_creaMatch() {
        Long remitenteId = 1L;
        Long destinatarioId = 2L;

        Usuario remitente = Usuario.builder().id(remitenteId).email("a@test.com").passwordHash("hash").estado(Usuario.EstadoUsuario.ACTIVO).build();
        Usuario destinatario = Usuario.builder().id(destinatarioId).email("b@test.com").passwordHash("hash").estado(Usuario.EstadoUsuario.ACTIVO).build();

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));

        Swipe reciprocal = Swipe.builder()
            .remitente(destinatario)
            .destinatario(remitente)
            .accion(Swipe.AccionSwipe.ACEPTADO)
            .timestamp(LocalDateTime.now())
            .build();

        when(swipeRepository.findByRemitenteIdAndDestinatarioId(destinatarioId, remitenteId))
            .thenReturn(Optional.of(reciprocal));

        boolean matchCreado = swipeService.crearSwipe(remitenteId, destinatarioId, Swipe.AccionSwipe.ACEPTADO);

        assertTrue(matchCreado);
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService).crearMatch(remitente, destinatario);
    }

    @Test
    void crearSwipe_aceptadoSinReciprocidad_noCreaMatch() {
        Long remitenteId = 1L;
        Long destinatarioId = 2L;

        Usuario remitente = Usuario.builder().id(remitenteId).email("a@test.com").passwordHash("hash").estado(Usuario.EstadoUsuario.ACTIVO).build();
        Usuario destinatario = Usuario.builder().id(destinatarioId).email("b@test.com").passwordHash("hash").estado(Usuario.EstadoUsuario.ACTIVO).build();

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));
        when(swipeRepository.findByRemitenteIdAndDestinatarioId(destinatarioId, remitenteId))
            .thenReturn(Optional.empty());

        boolean matchCreado = swipeService.crearSwipe(remitenteId, destinatarioId, Swipe.AccionSwipe.ACEPTADO);

        assertFalse(matchCreado);
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService, never()).crearMatch(any(Usuario.class), any(Usuario.class));
    }

    @Test
    void crearSwipe_rechazado_noBuscaReciprocidad() {
        Long remitenteId = 1L;
        Long destinatarioId = 2L;

        Usuario remitente = Usuario.builder().id(remitenteId).email("a@test.com").passwordHash("hash").estado(Usuario.EstadoUsuario.ACTIVO).build();
        Usuario destinatario = Usuario.builder().id(destinatarioId).email("b@test.com").passwordHash("hash").estado(Usuario.EstadoUsuario.ACTIVO).build();

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));

        boolean matchCreado = swipeService.crearSwipe(remitenteId, destinatarioId, Swipe.AccionSwipe.RECHAZADO);

        assertFalse(matchCreado);
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService, never()).crearMatch(any(Usuario.class), any(Usuario.class));
        verify(swipeRepository, never()).findByRemitenteIdAndDestinatarioId(destinatarioId, remitenteId);
    }

    @Test
    void crearSwipe_lanzaSiUsuarioNoExiste() {
        Long remitenteId = 404L;
        Long destinatarioId = 2L;

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            swipeService.crearSwipe(remitenteId, destinatarioId, Swipe.AccionSwipe.ACEPTADO));
    }

    @Test
    void crearSwipe_lanzaSiDestinatarioNoExiste() {
        Long remitenteId = 1L;
        Long destinatarioId = 404L;

        Usuario remitente = Usuario.builder()
            .id(remitenteId)
            .email("a@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            swipeService.crearSwipe(remitenteId, destinatarioId, Swipe.AccionSwipe.ACEPTADO));
    }

    @Test
    void crearSwipe_aceptadoConReciprocidadNoAceptada_noCreaMatch() {
        Long remitenteId = 1L;
        Long destinatarioId = 2L;

        Usuario remitente = Usuario.builder()
            .id(remitenteId)
            .email("a@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();
        Usuario destinatario = Usuario.builder()
            .id(destinatarioId)
            .email("b@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
        when(usuarioRepository.findById(destinatarioId)).thenReturn(Optional.of(destinatario));

        Swipe reciprocal = Swipe.builder()
            .remitente(destinatario)
            .destinatario(remitente)
            .accion(Swipe.AccionSwipe.RECHAZADO)
            .timestamp(LocalDateTime.now())
            .build();

        when(swipeRepository.findByRemitenteIdAndDestinatarioId(destinatarioId, remitenteId))
            .thenReturn(Optional.of(reciprocal));

        boolean matchCreado = swipeService.crearSwipe(remitenteId, destinatarioId, Swipe.AccionSwipe.ACEPTADO);

        assertFalse(matchCreado);
        verify(swipeRepository).save(any(Swipe.class));
        verify(matchService, never()).crearMatch(any(Usuario.class), any(Usuario.class));
    }
}
