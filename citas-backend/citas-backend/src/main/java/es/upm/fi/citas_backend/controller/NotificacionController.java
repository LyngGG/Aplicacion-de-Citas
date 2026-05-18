package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.NotificacionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/usuarios/{usuarioId}/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    @GetMapping
    public ResponseEntity<List<NotificacionResponseDto>> obtenerNotificaciones(@PathVariable Long usuarioId) {
        // TODO: Obtener notificaciones del usuario (solo lectura, se generan de forma asíncrona)
        List<NotificacionResponseDto> notificaciones = List.of(
            new NotificacionResponseDto(1L, "LIKE", "Te ha gustado", LocalDateTime.now(), false),
            new NotificacionResponseDto(2L, "MATCH", "Nuevo match", LocalDateTime.now(), false)
        );
        return ResponseEntity.ok(notificaciones);
    }
}
