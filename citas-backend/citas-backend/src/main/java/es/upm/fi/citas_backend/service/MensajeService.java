package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.broker.MessageBroker;
import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.domain.Mensaje;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.MensajeRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final MessageBroker     messageBroker;

    @Transactional
    public MensajeResponseDto crearMensaje(Match match, Long remitenteId, String texto) {

        Usuario remitente = usuarioRepository.findById(remitenteId)
            .orElseThrow(() -> new UsuarioNotFoundException(remitenteId));

        Mensaje mensaje = Mensaje.builder()
            .match(match)
            .remitente(remitente)
            .texto(texto)
            .timestamp(LocalDateTime.now())
            .leido(false)
            .build();

        Mensaje saved = mensajeRepository.save(mensaje);
        messageBroker.publicar(match.getId(), remitenteId, saved.getId());

        return new MensajeResponseDto(saved.getId(), saved.getTimestamp());
    }
}
