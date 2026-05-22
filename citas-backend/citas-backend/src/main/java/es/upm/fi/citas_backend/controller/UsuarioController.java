package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.UsuarioLoginRequestDto;
import es.upm.fi.citas_backend.dto.UsuarioRegistroRequestDto;
import es.upm.fi.citas_backend.dto.UsuarioResponseDto;
import es.upm.fi.citas_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Usuarios
 * 
 * Gestión de autenticación y datos de usuario.
 * 
 * CONTROLADOR: orquesta flujos de usuario:
 * 1. Registro - Crear nuevo usuario
 * 2. Login - Autenticar usuario
 * 3. Obtener - Recuperar datos del usuario
 * 4. Eliminar - Desactivar usuario
 * 
 * BAJO ACOPLAMIENTO: Delega toda lógica en UsuarioService.
 * ALTA COHESIÓN: Responsabilidades claras por endpoint.
 */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * POST /usuarios/registro
     * 
     * Registrar un nuevo usuario en el sistema.
     * 
     * Flujo:
     * 1. Valida email no duplicado (UsuarioService)
     * 2. Encripta contraseña (PasswordEncoder, delegado en UsuarioService)
     * 3. Crea usuario (CREADOR: UsuarioService agrega usuarios)
     * 4. Persiste en BD (FABRICACIÓN PURA)
     * 
     * RESPUESTA MÍNIMA: usuarioId, email, estado (VARIACIONES PROTEGIDAS).
     * 
     * @param req DTO con email y password
     * @return UsuarioResponseDto con datos del usuario creado
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDto> registro(
            @Valid @RequestBody UsuarioRegistroRequestDto req) {

        log.info("[UsuarioController] Registro iniciado → email={}", req.getEmail());

        // CONTROLADOR delega toda la lógica en UsuarioService (experto)
        UsuarioResponseDto response = usuarioService.registrar(
            req.getEmail(),
            req.getPassword()
        );

        log.info("[UsuarioController] Registro completado → usuarioId={}, email={}", 
            response.getId(), response.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /usuarios/login
     * 
     * Autenticar usuario con credenciales.
     * 
     * Flujo:
     * 1. Valida que usuario existe (UsuarioService)
     * 2. Verifica contraseña (PasswordEncoder)
     * 3. Retorna datos del usuario autenticado
     * 
     * En una implementación real, aquí se generaría JWT.
     * Por ahora solo valida credenciales.
     * 
     * @param req DTO con usuarioId y password
     * @return UsuarioResponseDto si autenticación es exitosa
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDto> login(
            @Valid @RequestBody UsuarioLoginRequestDto req) {

        log.info("[UsuarioController] Login iniciado → usuarioId={}", req.getUsuarioId());

        // CONTROLADOR delega autenticación en UsuarioService (experto en validaciones)
        UsuarioResponseDto response = usuarioService.autenticar(
            req.getUsuarioId(),
            req.getPassword()
        );

        log.info("[UsuarioController] Login exitoso → usuarioId={}, email={}", 
            response.getId(), response.getEmail());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /usuarios/{usuarioId}
     * 
     * Obtener datos del usuario autenticado.
     * 
     * Flujo:
     * 1. Valida que usuario existe (UsuarioService)
     * 2. Retorna datos del usuario
     * 
     * EXPERTO: UsuarioService recupera usuario de BD.
     * 
     * @param usuarioId ID del usuario
     * @return UsuarioResponseDto con datos públicos del usuario
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<UsuarioResponseDto> obtenerUsuario(
            @PathVariable Long usuarioId) {

        log.info("[UsuarioController] Obteniendo usuario → usuarioId={}", usuarioId);

        // CONTROLADOR delega recuperación en UsuarioService
        UsuarioResponseDto response = usuarioService.obtenerUsuario(usuarioId);

        log.info("[UsuarioController] Usuario obtenido → email={}, estado={}", 
            response.getEmail(), response.getEstado());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /usuarios/{usuarioId}
     * 
     * Eliminar cuenta de usuario (desactivar).
     * 
     * Flujo:
     * 1. Valida que usuario existe (UsuarioService)
     * 2. Marca usuario como INACTIVO (no se borra físicamente)
     * 3. Limpia datos asociados si es necesario
     * 
     * EXPERTO: UsuarioService gestiona eliminación.
     * 
     * @param usuarioId ID del usuario a eliminar
     * @return 204 No Content
     */
    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long usuarioId) {

        log.info("[UsuarioController] Eliminando usuario → usuarioId={}", usuarioId);

        // CONTROLADOR delega eliminación en UsuarioService
        usuarioService.eliminarUsuario(usuarioId);

        log.info("[UsuarioController] Usuario eliminado → usuarioId={}", usuarioId);

        return ResponseEntity.noContent().build();
    }
}

