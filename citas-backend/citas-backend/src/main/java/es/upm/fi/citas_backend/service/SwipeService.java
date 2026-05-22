package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Swipe;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.SwipeRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeRepository swipeRepository;
    private final UsuarioRepository usuarioRepository;
    private final MatchService matchService;

    public List<Long> obtenerInteraccionesRealizadas(Long usuarioId) {
        return swipeRepository.findIdsInteractuadosByUsuarioId(usuarioId);
    }

    @Transactional
    public boolean crearSwipe(Long remitenteId, Long destinatarioId, Swipe.AccionSwipe accion) {
        Usuario remitente = usuarioRepository.findById(remitenteId)
            .orElseThrow(() -> new UsuarioNotFoundException(remitenteId));
        Usuario destinatario = usuarioRepository.findById(destinatarioId)
            .orElseThrow(() -> new UsuarioNotFoundException(destinatarioId));

        Swipe swipe = Swipe.builder()
            .remitente(remitente)
            .destinatario(destinatario)
            .accion(accion)
            .timestamp(LocalDateTime.now())
            .build();
        
        swipeRepository.save(swipe);
        log.info("[SwipeService] Swipe creado → remitenteId={}, destinatarioId={}, accion={}", remitenteId, destinatarioId, accion);

        if (accion == Swipe.AccionSwipe.ACEPTADO) {
            Optional<Swipe> reciprocal = swipeRepository.findByRemitenteIdAndDestinatarioId(destinatarioId, remitenteId);
            if (reciprocal.isPresent() && reciprocal.get().getAccion() == Swipe.AccionSwipe.ACEPTADO) {
                log.info("[SwipeService] Reciprocidad encontrada → Creando match entre {} y {}", remitenteId, destinatarioId);
                matchService.crearMatch(remitente, destinatario);
                return true;
            }
        }
        return false;
    }
}