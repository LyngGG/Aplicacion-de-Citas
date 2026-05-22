package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.Match;
import es.upm.fi.citas_backend.domain.Mensaje;
import es.upm.fi.citas_backend.dto.EnviarMensajeRequestDto;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.repository.MensajeRepository;
import es.upm.fi.citas_backend.service.MatchService;
import es.upm.fi.citas_backend.service.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matches/{matchId}/mensajes")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MatchService matchService;
    private final MensajeService mensajeService;
    private final MensajeRepository mensajeRepository;

    /**
     * CU1 - GET /matches/{matchId}/mensajes
     * 
     * Obtener todos los mensajes de un match ordenados por timestamp ascendente.
     * 
     * CONTROLADOR: delega en MatchService para validación.
     * BAJO ACOPLAMIENTO: no accede directamente a Match.
     * EXPERTO: MensajeRepository conoce cómo recuperar mensajes del match.
     * 
     * @param matchId ID del match
     * @param usuarioId ID del usuario que solicita (para validar pertenencia)
     * @return Lista de mensajes con formato MensajeResponseDto
     */
    @GetMapping
    public ResponseEntity<List<MensajeResponseDto>> obtenerMensajes(
            @PathVariable Long matchId,
            @RequestParam Long usuarioId) {

        log.info("[CU1-GET] Obteniendo mensajes → matchId={}, usuarioId={}", matchId, usuarioId);

        // Validar que el match existe y el usuario pertenece a él
        // BAJO ACOPLAMIENTO: MatchService es experto en validaciones de Match
        Match match = matchService.validarMatchActivo(matchId, usuarioId);

        // FABRICACIÓN PURA: MensajeRepository aísla acceso a BD
        List<Mensaje> mensajes = mensajeRepository.findByMatchIdOrderByTimestampAsc(matchId);

        // Convertir a DTOs
        List<MensajeResponseDto> respuesta = mensajes.stream()
            .map(m -> new MensajeResponseDto(
                m.getId(),
                m.getTexto(),
                m.getTimestamp(),
                m.isLeido()
            ))
            .collect(Collectors.toList());

        log.info("[CU1-GET] {} mensajes retornados", respuesta.size());
        return ResponseEntity.ok(respuesta);
    }

    /**
     * CU1 - POST /matches/{matchId}/mensajes
     * 
     * Enviar un nuevo mensaje en un match.
     * 
     * CONTROLADOR: orquesta el flujo:
     * 1. Valida match activo (MatchService)
     * 2. Crea el mensaje (MensajeService)
     * 3. Publica evento asíncrono (MessageBroker, manejado en MensajeService)
     * 
     * RESPUESTA MÍNIMA: solo datos esenciales del mensaje (VARIACIONES PROTEGIDAS).
     * 
     * @param matchId ID del match
     * @param req DTO con remitenteId y texto del mensaje
     * @return Mensaje creado con timestamp
     */
    @PostMapping
    public ResponseEntity<MensajeResponseDto> enviarMensaje(
            @PathVariable Long matchId,
            @Valid @RequestBody EnviarMensajeRequestDto req) {

        log.info("[CU1-POST] Enviando mensaje → matchId={}, remitenteId={}, texto={}", 
            matchId, req.getRemitenteId(), req.getTexto());

        // Paso 1: Validar match activo
        // BAJO ACOPLAMIENTO: MatchService valida sin exponer Match
        Match match = matchService.validarMatchActivo(matchId, req.getRemitenteId());

        // Paso 2: Crear y persistir mensaje
        // CONTROLADOR delega en MensajeService (experto en creación)
        // El servicio también publica evento "MensajeNuevo" (Broker)
        MensajeResponseDto response = mensajeService.crearMensaje(
            match,
            req.getRemitenteId(),
            req.getTexto()
        );

        log.info("[CU1-POST] Mensaje creado → mensajeId={}, timestamp={}", 
            response.getMensajeId(), response.getTimestamp());

        // Paso 3: Responder con 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
