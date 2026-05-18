package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.dto.EnviarMensajeRequestDto;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.service.MatchService;
import es.upm.fi.citas_backend.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/matches/{matchId}/mensajes")
@RequiredArgsConstructor
public class ChatController {

    private final MatchService   matchService;
    private final MensajeService mensajeService;

    @GetMapping
    public ResponseEntity<List<Object>> obtenerMensajes(@PathVariable Long matchId) {
        // TODO: Obtener mensajes del match
        List<Object> mensajes = List.of();
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping
    public ResponseEntity<MensajeResponseDto> enviarMensaje(
            @PathVariable Long matchId,
            @Valid @RequestBody EnviarMensajeRequestDto req) {

        Match match = matchService.validarMatchActivo(matchId, req.getRemitenteId());
        MensajeResponseDto response = mensajeService.crearMensaje(match, req.getRemitenteId(), req.getTexto());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
