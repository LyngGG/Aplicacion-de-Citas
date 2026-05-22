package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.dto.PerfilRequestDto;
import es.upm.fi.citas_backend.exception.PerfilNotFoundException;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.fake.FakeCacheService;
import es.upm.fi.citas_backend.repository.PerfilRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Servicio de gestión de perfiles de usuario.
 * 
 * EXPERTO: PerfilService conoce toda la lógica de perfil.
 * FABRICACIÓN PURA: Encapsula creación, actualización y eliminación.
 * INDIRECCIÓN: FakeCacheService aísla mecanismo de caché.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final FakeCacheService cache;

    private static final String CACHE_PREFIX = "perfil:";

    /**
     * Obtener perfil del usuario (con caché).
     * INDIRECCIÓN: CacheService evita acceso frecuente a BD.
     */
    @Transactional(readOnly = true)
    public Perfil obtenerPerfil(Long usuarioId) {
        String key = CACHE_PREFIX + usuarioId;
        return (Perfil) cache.get(key).orElseGet(() -> {
            log.debug("[PerfilService] Cache miss → cargando desde BD usuarioId={}", usuarioId);
            Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                    .orElseThrow(() -> new PerfilNotFoundException(usuarioId));
            cache.set(key, perfil);
            return perfil;
        });
    }

    /**
     * Obtener perfil como DTO (para respuestas).
     */
    @Transactional(readOnly = true)
    public PerfilDto obtenerPerfilDto(Long usuarioId) {
        Perfil perfil = obtenerPerfil(usuarioId);
        return convertirADto(perfil);
    }

    /**
     * Actualizar perfil del usuario.
     * CREADOR: PerfilService actualiza perfiles.
     */
    @Transactional
    public PerfilDto actualizarPerfil(Long usuarioId, PerfilRequestDto req) {

        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNotFoundException(usuarioId));

        // Actualizar campos
        perfil.setNombre(req.getNombre());
        perfil.setEdad(req.getEdad());
        perfil.setDescripcion(req.getDescripcion());
        perfil.setUbicacion(req.getUbicacion());
        perfil.setFotos(req.getFotos() != null ? req.getFotos() : List.of());
        perfil.setIntereses(req.getIntereses() != null ? req.getIntereses() : List.of());

        Perfil updated = perfilRepository.save(perfil);

        // Invalidar caché
        String cacheKey = CACHE_PREFIX + usuarioId;
        cache.evict(cacheKey);

        log.info("[PerfilService] Perfil actualizado → usuarioId={}, nombre={}", usuarioId, updated.getNombre());

        return convertirADto(updated);
    }

    /**
     * Crear perfil para un nuevo usuario.
     * FABRICACIÓN PURA: PerfilService crea perfiles.
     */
    @Transactional
    public PerfilDto crearPerfil(Long usuarioId, PerfilRequestDto req) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        // Verificar que no existe perfil
        if (perfilRepository.findByUsuarioId(usuarioId).isPresent()) {
            throw new IllegalArgumentException("El usuario ya tiene un perfil");
        }

        Perfil perfil = Perfil.builder()
                .usuario(usuario)
                .nombre(req.getNombre())
                .edad(req.getEdad())
                .descripcion(req.getDescripcion())
                .ubicacion(req.getUbicacion())
                .fotos(req.getFotos() != null ? req.getFotos() : List.of())
                .intereses(req.getIntereses() != null ? req.getIntereses() : List.of())
                .build();

        Perfil saved = perfilRepository.save(perfil);
        log.info("[PerfilService] Perfil creado → usuarioId={}, perfilId={}", usuarioId, saved.getId());

        return convertirADto(saved);
    }

    /**
     * Eliminar perfil del usuario (soft delete).
     */
    @Transactional
    public void eliminarPerfil(Long usuarioId) {

        Perfil perfil = perfilRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNotFoundException(usuarioId));

        perfilRepository.delete(perfil);

        // Invalidar caché
        String cacheKey = CACHE_PREFIX + usuarioId;
        cache.evict(cacheKey);

        log.info("[PerfilService] Perfil eliminado → usuarioId={}", usuarioId);
    }

    /**
     * Convertir Perfil a PerfilDto.
     */
    private PerfilDto convertirADto(Perfil perfil) {
        return new PerfilDto(
                perfil.getId(),
                perfil.getNombre(),
                perfil.getEdad(),
                perfil.getDescripcion(),
                perfil.getUbicacion(),
                perfil.getFotos() != null ? perfil.getFotos() : List.of(),
                perfil.getIntereses() != null ? perfil.getIntereses() : List.of());
    }
}