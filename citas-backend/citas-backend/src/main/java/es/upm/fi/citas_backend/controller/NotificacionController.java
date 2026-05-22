package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.NotificacionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/usuarios/{usuarioId}/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final es.upm.fi.citas_backend.service.NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificacionResponseDto>> obtenerNotificaciones(@PathVariable Long usuarioId) {
        List<NotificacionResponseDto> notificaciones = notificationService.obtenerNotificaciones(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }
}
