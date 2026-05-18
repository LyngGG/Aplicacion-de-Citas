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
            "Madrid",
            java.util.List.of("viajes", "cine"),
            "foto-principal.jpg"
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
            req.getUbicacion(),
            req.getIntereses(),
            req.getFotos() != null && !req.getFotos().isEmpty() ? req.getFotos().get(0) : null
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarPerfil(@PathVariable Long usuarioId) {
        // TODO: Eliminar perfil del usuario
        return ResponseEntity.noContent().build();
    }
}
