package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.BloqueoRequestDto;
import es.upm.fi.citas_backend.dto.BloqueoResponseDto;
import es.upm.fi.citas_backend.service.BlockingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CU4 - Bloquear Usuario
 * 
 * Gestión de bloqueos entre usuarios.
 * 
 * CONTROLADOR: orquesta el flujo:
 * 1. POST /bloqueos - Crear bloqueo
 * 2. Delega validaciones y lógica en BlockingService
 * 3. Publica eventos asíncronos (MessageBroker)
 * 
 * BAJO ACOPLAMIENTO: No accede directamente a Bloqueo, delega en servicio.
 * ALTA COHESIÓN: BlockingService maneja toda la lógica de bloqueos.
 */
@RestController
@RequestMapping("/bloqueos")
@RequiredArgsConstructor
@Slf4j
public class BlockingController {

    private final BlockingService blockingService;

    /**
     * CU4 - POST /bloqueos
     * 
     * Bloquear un usuario.
     * 
     * Flujo:
     * 1. Valida que el bloqueo no exista (BlockingService)
     * 2. Obtiene usuarios (BlockingService)
     * 3. Crea bloqueo (BlockingService)
     * 4. Invalida matches activos (MatchService, delegado)
     * 5. Publica evento "UsuarioBloqueado" (MessageBroker)
     *    - Notifica asíncrono a ChatService, DiscoveryService, NotificationService
     * 
     * RESPUESTA MÍNIMA: bloqueoId y fechaBloqueo (VARIACIONES PROTEGIDAS).
     * 
     * @param req DTO con bloqueadorId y bloqueadoId
     * @return BloqueoResponseDto con detalles del bloqueo creado
     */
    @PostMapping
    public ResponseEntity<BloqueoResponseDto> bloquearUsuario(
            @Valid @RequestBody BloqueoRequestDto req) {

        log.info("[CU4] Bloqueando usuario → bloqueadorId={}, bloqueadoId={}", 
            req.getBloqueadorId(), req.getBloqueadoId());

        // CONTROLADOR: delega toda la lógica en BlockingService
        // BAJO ACOPLAMIENTO: no conoce detalles de cómo se valida o persiste
        BloqueoResponseDto response = blockingService.bloquearUsuario(
            req.getBloqueadorId(),
            req.getBloqueadoId()
        );

        log.info("[CU4] Bloqueo creado → bloqueoId={}, fechaBloqueo={}", 
            response.getBloqueoId(), response.getFechaBloqueo());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

