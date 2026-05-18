package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.repository.SwipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwipeServiceTest {

    @Mock
    private SwipeRepository swipeRepository;

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
}
