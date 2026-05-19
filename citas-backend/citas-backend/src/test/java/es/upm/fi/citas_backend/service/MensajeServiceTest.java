package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.MessageBroker;
import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.domain.Mensaje;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.MensajeRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensajeServiceTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MessageBroker messageBroker;

    @InjectMocks
    private MensajeService mensajeService;

    @Test
    void crearMensaje_guardaMensajeYPublicaEvento() {
        Long remitenteId = 10L;
        Match match = Match.builder().id(5L).build();
        Usuario remitente = Usuario.builder()
            .id(remitenteId)
            .email("remitente@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        when(usuarioRepository.findById(remitenteId)).thenReturn(Optional.of(remitente));
        when(mensajeRepository.save(org.mockito.ArgumentMatchers.any(Mensaje.class)))
            .thenAnswer(invocation -> {
                Mensaje mensaje = invocation.getArgument(0);
                mensaje.setId(99L);
                return mensaje;
            });

        MensajeResponseDto response = mensajeService.crearMensaje(match, remitenteId, "hola");

        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(mensajeRepository).save(captor.capture());
        Mensaje guardado = captor.getValue();

        assertEquals(match, guardado.getMatch());
        assertEquals(remitente, guardado.getRemitente());
        assertEquals("hola", guardado.getTexto());
        assertNotNull(guardado.getTimestamp());
        assertFalse(guardado.isLeido());

        assertEquals(99L, response.getMensajeId());
        assertEquals("hola", response.getTexto());
        assertEquals(guardado.getTimestamp(), response.getTimestamp());
        assertFalse(response.isLeido());

        verify(messageBroker).publicar(match.getId(), remitenteId, 99L);
    }

    @Test
    void crearMensaje_lanzaSiUsuarioNoExiste() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            mensajeService.crearMensaje(Match.builder().id(1L).build(), 404L, "hola"));
    }
}
