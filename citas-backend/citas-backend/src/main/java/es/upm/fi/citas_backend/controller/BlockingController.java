package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.BloqueoRequestDto;
import es.upm.fi.citas_backend.dto.BloqueoResponseDto;
import es.upm.fi.citas_backend.service.BlockingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bloqueos")
@RequiredArgsConstructor
public class BlockingController {

    private final BlockingService blockingService;

    @PostMapping
    public ResponseEntity<BloqueoResponseDto> bloquearUsuario(
            @Valid @RequestBody BloqueoRequestDto req) {

        Long bloqueoId = blockingService.bloquearUsuario(req.getBloqueadorId(), req.getBloqueadoId());
        return ResponseEntity.ok(new BloqueoResponseDto(bloqueoId, "Bloqueo realizado correctamente"));
    }
}

