package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.dto.PerfilRequestDto;
import es.upm.fi.citas_backend.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Perfiles
 * 
 * Gestión de perfiles de usuario.
 * 
 * CONTROLADOR: orquesta flujos de perfil:
 * 1. GET - Obtener perfil del usuario
 * 2. POST - Crear perfil (opcional)
 * 3. PUT - Actualizar perfil
 * 4. DELETE - Eliminar perfil
 * 
 * BAJO ACOPLAMIENTO: Delega toda lógica en PerfilService.
 * ALTA COHESIÓN: Responsabilidades claras por endpoint.
 */
@RestController
@RequestMapping("/usuarios/{usuarioId}/perfil")
@RequiredArgsConstructor
@Slf4j
public class PerfilController {

    private final PerfilService perfilService;

    /**
     * GET /usuarios/{usuarioId}/perfil
     * 
     * Obtener perfil del usuario.
     * 
     * Flujo:
     * 1. Valida usuario existe (PerfilService)
     * 2. Obtiene perfil con caché (INDIRECCIÓN: CacheService)
     * 3. Convierte a DTO (VARIACIONES PROTEGIDAS)
     * 
     * @param usuarioId ID del usuario
     * @return PerfilDto con datos del perfil
     */
    @GetMapping
    public ResponseEntity<PerfilDto> obtenerPerfil(
            @PathVariable Long usuarioId) {

        log.info("[PerfilController] Obteniendo perfil → usuarioId={}", usuarioId);

        // CONTROLADOR delega en PerfilService (experto)
        PerfilDto response = perfilService.obtenerPerfilDto(usuarioId);

        log.info("[PerfilController] Perfil obtenido → nombre={}, edad={}", 
            response.getNombre(), response.getEdad());

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /usuarios/{usuarioId}/perfil
     * 
     * Actualizar perfil del usuario.
     * 
     * Flujo:
     * 1. Valida usuario y perfil existen (PerfilService)
     * 2. Actualiza campos del perfil (PerfilService)
     * 3. Invalida caché (FakeCacheService)
     * 4. Retorna perfil actualizado
     * 
     * RESPUESTA MÍNIMA: datos públicos del perfil (VARIACIONES PROTEGIDAS).
     * 
     * @param usuarioId ID del usuario
     * @param req DTO con datos a actualizar
     * @return PerfilDto actualizado
     */
    @PutMapping
    public ResponseEntity<PerfilDto> actualizarPerfil(
            @PathVariable Long usuarioId,
            @Valid @RequestBody PerfilRequestDto req) {

        log.info("[PerfilController] Actualizando perfil → usuarioId={}, nombre={}", 
            usuarioId, req.getNombre());

        // CONTROLADOR delega actualización en PerfilService
        PerfilDto response = perfilService.actualizarPerfil(usuarioId, req);

        log.info("[PerfilController] Perfil actualizado → usuarioId={}", usuarioId);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /usuarios/{usuarioId}/perfil
     * 
     * Crear perfil para un usuario (opcional, puede no ser necesario si se crea en registro).
     * 
     * Flujo:
     * 1. Valida usuario existe (PerfilService)
     * 2. Valida que no exista perfil previo
     * 3. Crea perfil (CREADOR: PerfilService agrega perfiles)
     * 4. Retorna 201 CREATED
     * 
     * @param usuarioId ID del usuario
     * @param req DTO con datos del perfil
     * @return PerfilDto del perfil creado
     */
    @PostMapping
    public ResponseEntity<PerfilDto> crearPerfil(
            @PathVariable Long usuarioId,
            @Valid @RequestBody PerfilRequestDto req) {

        log.info("[PerfilController] Creando perfil → usuarioId={}, nombre={}", 
            usuarioId, req.getNombre());

        // CONTROLADOR delega creación en PerfilService (experto)
        PerfilDto response = perfilService.crearPerfil(usuarioId, req);

        log.info("[PerfilController] Perfil creado → usuarioId={}, perfilId={}", 
            usuarioId, response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /usuarios/{usuarioId}/perfil
     * 
     * Eliminar perfil del usuario.
     * 
     * Flujo:
     * 1. Valida perfil existe (PerfilService)
     * 2. Elimina perfil (soft delete)
     * 3. Invalida caché
     * 4. Retorna 204 NO CONTENT
     * 
     * @param usuarioId ID del usuario
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> eliminarPerfil(
            @PathVariable Long usuarioId) {

        log.info("[PerfilController] Eliminando perfil → usuarioId={}", usuarioId);

        // CONTROLADOR delega eliminación en PerfilService
        perfilService.eliminarPerfil(usuarioId);

        log.info("[PerfilController] Perfil eliminado → usuarioId={}", usuarioId);

        return ResponseEntity.noContent().build();
    }
}
