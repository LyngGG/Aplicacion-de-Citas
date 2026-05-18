package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.exception.MatchNoActivoException;
import es.upm.fi.citas_backend.exception.MatchNotFoundException;
import es.upm.fi.citas_backend.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    void validarMatchActivo_devuelveMatchCuandoEsValido() {
        Match match = buildMatchActivo(1L, 10L, 20L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        Match resultado = matchService.validarMatchActivo(1L, 10L);

        assertSame(match, resultado);
    }

    @Test
    void validarMatchActivo_lanzaSiNoExiste() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MatchNotFoundException.class, () -> matchService.validarMatchActivo(99L, 10L));
    }

    @Test
    void validarMatchActivo_lanzaSiUsuarioNoPertenece() {
        Match match = buildMatchActivo(2L, 10L, 20L);

        when(matchRepository.findById(2L)).thenReturn(Optional.of(match));

        assertThrows(MatchNoActivoException.class, () -> matchService.validarMatchActivo(2L, 999L));
    }

    @Test
    void invalidarMatchSiExiste_actualizaEstadoYGuarda() {
        Match match = buildMatchActivo(3L, 10L, 20L);

        when(matchRepository.findMatchActivoEntreUsuarios(10L, 20L))
            .thenReturn(Optional.of(match));

        matchService.invalidarMatchSiExiste(10L, 20L);

        assertEquals(Match.EstadoMatch.INVALIDADO, match.getEstado());
        verify(matchRepository).save(match);
    }

    @Test
    void invalidarMatchSiExiste_noHaceNadaSiNoHayMatch() {
        when(matchRepository.findMatchActivoEntreUsuarios(10L, 20L))
            .thenReturn(Optional.empty());

        matchService.invalidarMatchSiExiste(10L, 20L);

        verify(matchRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Match buildMatchActivo(Long matchId, Long usuario1Id, Long usuario2Id) {
        Usuario usuario1 = Usuario.builder()
            .id(usuario1Id)
            .email("u" + usuario1Id + "@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        Usuario usuario2 = Usuario.builder()
            .id(usuario2Id)
            .email("u" + usuario2Id + "@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        return Match.builder()
            .id(matchId)
            .usuario1(usuario1)
            .usuario2(usuario2)
            .estado(Match.EstadoMatch.ACTIVO)
            .build();
    }
}
