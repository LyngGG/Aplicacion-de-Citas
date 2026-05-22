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

    @Transactional
    public Match crearMatch(es.upm.fi.citas_backend.domain.Usuario usuario1, es.upm.fi.citas_backend.domain.Usuario usuario2) {
        Match match = new Match(usuario1, usuario2, java.time.LocalDateTime.now(), Match.EstadoMatch.ACTIVO);
        Match saved = matchRepository.save(match);
        log.info("[MatchService] Match creado → id={} entre usuarios {} y {}", saved.getId(), usuario1.getId(), usuario2.getId());
        return saved;
    }
    @Transactional(readOnly = true)
    public java.util.List<es.upm.fi.citas_backend.dto.MatchResponseDto> listarMatches(Long usuarioId) {
        return matchRepository.findByUsuarioId(usuarioId).stream()
            .map(m -> new es.upm.fi.citas_backend.dto.MatchResponseDto(
                m.getId(),
                m.getUsuario1().getId(),
                m.getUsuario2().getId(),
                m.getFechaCreacion(),
                m.getEstado().name()
            ))
            .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public es.upm.fi.citas_backend.dto.MatchResponseDto obtenerMatchDto(Long matchId, Long usuarioId) {
        Match match = validarMatchActivo(matchId, usuarioId);
        return new es.upm.fi.citas_backend.dto.MatchResponseDto(
            match.getId(),
            match.getUsuario1().getId(),
            match.getUsuario2().getId(),
            match.getFechaCreacion(),
            match.getEstado().name()
        );
    }
}
