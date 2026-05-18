package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.MatchResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    @GetMapping
    public ResponseEntity<List<MatchResponseDto>> listarMatches(
            @RequestParam Long usuarioId) {
        // TODO: Obtener matches del usuario
        List<MatchResponseDto> matches = List.of(
            new MatchResponseDto(1L, usuarioId, 2L, LocalDateTime.now(), "ACTIVO"),
            new MatchResponseDto(2L, usuarioId, 3L, LocalDateTime.now(), "ACTIVO")
        );
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponseDto> obtenerMatch(@PathVariable Long matchId) {
        // TODO: Obtener match específico
        MatchResponseDto response = new MatchResponseDto(
            matchId,
            1L,
            2L,
            LocalDateTime.now(),
            "ACTIVO"
        );
        return ResponseEntity.ok(response);
    }
}
