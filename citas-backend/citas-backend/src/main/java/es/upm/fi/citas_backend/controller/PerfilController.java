package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.dto.PerfilRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios/{usuarioId}/perfil")
@RequiredArgsConstructor
public class PerfilController {

    @GetMapping
    public ResponseEntity<PerfilDto> obtenerPerfil(@PathVariable Long usuarioId) {
        // TODO: Obtener perfil del usuario
        PerfilDto response = new PerfilDto(
            1L,
            "Nombre Perfil",
            25,
            "Descripción del perfil",
            "Madrid",
            java.util.List.of("foto-1.jpg", "foto-2.jpg"),
            java.util.List.of("viajes", "cine")
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<PerfilDto> actualizarPerfil(
            @PathVariable Long usuarioId,
            @Valid @RequestBody PerfilRequestDto req) {
        // TODO: Actualizar perfil del usuario
        PerfilDto response = new PerfilDto(
            1L,
            req.getNombre(),
            req.getEdad(),
            req.getDescripcion(),
            req.getUbicacion(),
            req.getFotos() != null ? req.getFotos() : java.util.List.of(),
            req.getIntereses() != null ? req.getIntereses() : java.util.List.of()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarPerfil(@PathVariable Long usuarioId) {
        // TODO: Eliminar perfil del usuario
        return ResponseEntity.noContent().build();
    }
}
