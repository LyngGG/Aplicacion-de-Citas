package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.exception.PerfilNotFoundException;
import es.upm.fi.citas_backend.fake.FakeCacheService;
import es.upm.fi.citas_backend.repository.PerfilRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final FakeCacheService cache;

    private static final String CACHE_PREFIX = "perfil:";

    @SuppressWarnings("unchecked")
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
}