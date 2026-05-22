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

    private final es.upm.fi.citas_backend.service.MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchResponseDto>> listarMatches(
            @RequestParam Long usuarioId) {
        List<MatchResponseDto> matches = matchService.listarMatches(usuarioId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponseDto> obtenerMatch(
            @PathVariable Long matchId,
            @RequestParam Long usuarioId) {
        MatchResponseDto response = matchService.obtenerMatchDto(matchId, usuarioId);
        return ResponseEntity.ok(response);
    }
}
