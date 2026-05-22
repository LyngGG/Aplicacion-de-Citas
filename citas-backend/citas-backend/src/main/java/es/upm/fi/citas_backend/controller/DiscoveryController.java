package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.dto.DescubrimientoResponseDto;
import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.service.DescubrimientoService;
import es.upm.fi.citas_backend.service.PerfilService;
import es.upm.fi.citas_backend.service.SwipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * CU3 - Descubrimiento de Perfiles
 * 
 * Búsqueda de candidatos con algoritmo de relevancia.
 * 
 * CONTROLADOR: orquesta el flujo:
 * 1. Obtiene perfil del usuario (PerfilService con caché)
 * 2. Obtiene IDs de interacciones realizadas (SwipeService)
 * 3. Busca candidatos con filtros (DescubrimientoService)
 * 
 * BAJO ACOPLAMIENTO: Delega en servicios especializados.
 * INDIRECCIÓN: SearchDatabase aísla cambios en motor de búsqueda.
 * VARIACIONES PROTEGIDAS: Respuesta mínima protege cambios internos.
 */
@RestController
@RequestMapping("/descubrimiento")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {

    private final PerfilService         perfilService;
    private final SwipeService          swipeService;
    private final DescubrimientoService descubrimientoService;

    /**
     * CU3 - GET /descubrimiento
     * 
     * Descubrir perfiles compatibles.
     * 
     * Flujo:
     * 1. Obtiene perfil del usuario (PerfilService)
     *    - INDIRECCIÓN: CacheService evita acceso frecuente a BD
     * 2. Obtiene IDs de usuarios con los que ya ha interactuado (SwipeService)
     * 3. Construye filtros: edad, ubicación, intereses (DescubrimientoService)
     * 4. Busca en SearchDatabase con INDIRECCIÓN (permite cambiar motor)
     * 5. Aplica algoritmo de diversidad y frescura
     * 6. Registra descubrimiento (para analytics)
     * 
     * @param usuarioId ID del usuario que busca
     * @param pagina Número de página (default: 0)
     * @param limite Cantidad de resultados (default: 20)
     * @return DescubrimientoResponseDto con fechaConsulta y resultados
     */
    @GetMapping
    public ResponseEntity<DescubrimientoResponseDto> descubrirPerfiles(
            @RequestParam Long usuarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int limite) {

        log.info("[CU3] Descubriendo perfiles → usuarioId={}, pagina={}, limite={}", 
            usuarioId, pagina, limite);

        // Paso 1: Obtener perfil del usuario (BAJO ACOPLAMIENTO)
        // INDIRECCIÓN: PerfilService usa CacheService para evitar acceso repetido a BD
        Perfil perfilContexto = perfilService.obtenerPerfil(usuarioId);
        log.debug("[CU3] Perfil obtenido → edad={}, ubicacion={}, intereses={}", 
            perfilContexto.getEdad(), perfilContexto.getUbicacion(), 
            perfilContexto.getIntereses().size());

        // Paso 2: Obtener IDs de interacciones realizadas
        // EXPERTO: SwipeService conoce el historial de swipes del usuario
        List<Long> idsExcluidos = swipeService.obtenerInteraccionesRealizadas(usuarioId);
        log.debug("[CU3] Interacciones realizadas → cantidad={}", idsExcluidos.size());

        // Paso 3-6: Buscar candidatos
        // FABRICACIÓN PURA + ALTA COHESIÓN: DescubrimientoService encapsula búsqueda
        // INDIRECCIÓN: SearchDatabase permite cambiar motor de búsqueda sin impacto
        DescubrimientoResponseDto resultados = descubrimientoService
            .buscarCandidatos(perfilContexto, idsExcluidos, pagina, limite);

        log.info("[CU3] Búsqueda completada → resultados={}, fechaConsulta={}", 
            resultados.getResultados().size(), resultados.getFechaConsulta());

        // VARIACIONES PROTEGIDAS: respuesta mínima con datos esenciales
        return ResponseEntity.ok(resultados);
    }
}