package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.MessageBroker;
import es.upm.fi.citas_backend.domain.Bloqueo;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.BloqueoResponseDto;
import es.upm.fi.citas_backend.exception.BloqueoYaExisteException;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.BloqueoRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlockingService {

    private final BloqueoRepository bloqueoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MatchService      matchService;
    private final MessageBroker     messageBroker;

    @Transactional
    public BloqueoResponseDto bloquearUsuario(Long bloqueadorId, Long bloqueadoId) {

        if (bloqueoRepository.existeBloqueo(bloqueadorId, bloqueadoId)) {
            throw new BloqueoYaExisteException(bloqueadorId, bloqueadoId);
        }

        Usuario bloqueador = usuarioRepository.findById(bloqueadorId)
            .orElseThrow(() -> new UsuarioNotFoundException(bloqueadorId));
        Usuario bloqueado  = usuarioRepository.findById(bloqueadoId)
            .orElseThrow(() -> new UsuarioNotFoundException(bloqueadoId));

        Bloqueo bloqueo = Bloqueo.builder()
            .bloqueador(bloqueador)
            .bloqueado(bloqueado)
            .fechaBloqueo(LocalDateTime.now())
            .build();

        Bloqueo saved = bloqueoRepository.save(bloqueo);
        log.info("[BlockingService] Bloqueo creado → id={} bloqueador={} bloqueado={}",
            saved.getId(), bloqueadorId, bloqueadoId);

        matchService.invalidarMatchSiExiste(bloqueadorId, bloqueadoId);
        messageBroker.publicarBloqueo(bloqueadorId, bloqueadoId);

        return new BloqueoResponseDto(saved.getId(), saved.getFechaBloqueo());
    }
}