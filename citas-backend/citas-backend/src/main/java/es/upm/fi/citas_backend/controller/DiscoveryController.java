package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.dto.DescubrimientoResponseDto;
import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.service.DescubrimientoService;
import es.upm.fi.citas_backend.service.PerfilService;
import es.upm.fi.citas_backend.service.SwipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/descubrimiento")
@RequiredArgsConstructor
public class DiscoveryController {

    private final PerfilService         perfilService;
    private final SwipeService          swipeService;
    private final DescubrimientoService descubrimientoService;

    @GetMapping
    public ResponseEntity<DescubrimientoResponseDto> descubrirPerfiles(
            @RequestParam Long    usuarioId,
            @RequestParam(defaultValue = "0")  int pagina,
            @RequestParam(defaultValue = "20") int limite) {

        Perfil perfilContexto   = perfilService.obtenerPerfil(usuarioId);
        List<Long> idsExcluidos = swipeService.obtenerInteraccionesRealizadas(usuarioId);
        DescubrimientoResponseDto resultados = descubrimientoService
            .buscarCandidatos(perfilContexto, idsExcluidos, pagina, limite);

        return ResponseEntity.ok(resultados);
    }
}