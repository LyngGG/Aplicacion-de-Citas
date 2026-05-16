package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.exception.MatchNoActivoException;
import es.upm.fi.citas_backend.exception.MatchNotFoundException;
import es.upm.fi.citas_backend.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public Match validarMatchActivo(Long matchId, Long remitenteId) {
        Match match = matchRepository.findById(matchId)
            .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (!match.isActivo() || !match.perteneceUsuario(remitenteId)) {
            throw new MatchNoActivoException(matchId, remitenteId);
        }
        return match;
    }

    @Transactional
    public void invalidarMatchSiExiste(Long bloqueadorId, Long bloqueadoId) {
        matchRepository.findMatchActivoEntreUsuarios(bloqueadorId, bloqueadoId)
            .ifPresentOrElse(match -> {
                match.setEstado(Match.EstadoMatch.INVALIDADO);
                matchRepository.save(match);
                log.info("[MatchService] Match {} invalidado por bloqueo", match.getId());
            }, () ->
                log.debug("[MatchService] No hay match activo entre {} y {}", bloqueadorId, bloqueadoId)
            );
    }
}
