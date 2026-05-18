package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.UsuarioLoginRequestDto;
import es.upm.fi.citas_backend.dto.UsuarioRegistroRequestDto;
import es.upm.fi.citas_backend.dto.UsuarioResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDto> registro(@Valid @RequestBody UsuarioRegistroRequestDto req) {
        // TODO: Implementar lógica de registro
        UsuarioResponseDto response = new UsuarioResponseDto(
            1L, 
            req.getEmail(), 
            "ACTIVO"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDto> login(@Valid @RequestBody UsuarioLoginRequestDto req) {
        // TODO: Validar credenciales (usuarioId + password)
        UsuarioResponseDto response = new UsuarioResponseDto(
            req.getUsuarioId(), 
            "usuario@test.com", 
            "ACTIVO"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponseDto> obtenerUsuario(@PathVariable Long usuarioId) {
        // TODO: Obtener usuario del repositorio
        UsuarioResponseDto response = new UsuarioResponseDto(
            usuarioId, 
            "usuario@test.com", 
            "ACTIVO"
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long usuarioId) {
        // TODO: Eliminar usuario del repositorio
        return ResponseEntity.noContent().build();
    }
}
