package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Descubrimiento;
import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.DescubrimientoResponseDto;
import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.DescubrimientoRepository;
import es.upm.fi.citas_backend.repository.PerfilRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DescubrimientoService {

    private final PerfilRepository         perfilRepository;
    private final DescubrimientoRepository descubrimientoRepository;
    private final UsuarioRepository        usuarioRepository;

    @Transactional
    public DescubrimientoResponseDto buscarCandidatos(
            Perfil perfilContexto,
            List<Long> idsExcluidos,
            int pagina,
            int limite) {

        List<Long> excluidos = new ArrayList<>(idsExcluidos);
        excluidos.add(perfilContexto.getUsuario().getId());

        List<Perfil> candidatos = perfilRepository.buscarCandidatos(
            perfilContexto.getUsuario().getId(),
            excluidos.isEmpty() ? List.of(-1L) : excluidos,
            perfilContexto.getUbicacion(),
            calcularEdadMin(perfilContexto.getEdad()),
            calcularEdadMax(perfilContexto.getEdad()),
            PageRequest.of(pagina, limite)
        );

        List<Perfil> diversificados = aplicarDiversidadYFrescura(candidatos);
        LocalDateTime fechaConsulta = registrarDescubrimiento(perfilContexto.getUsuario().getId(), diversificados);
        List<PerfilDto> resultados = componerRespuestaMinima(diversificados);
        return new DescubrimientoResponseDto(fechaConsulta, resultados);
    }

    private List<Perfil> aplicarDiversidadYFrescura(List<Perfil> perfiles) {
        List<Perfil> resultado = new ArrayList<>(perfiles);
        Collections.shuffle(resultado);
        log.debug("[DescubrimientoService] Diversidad aplicada → {} candidatos", resultado.size());
        return resultado;
    }

    private LocalDateTime registrarDescubrimiento(Long usuarioId, List<Perfil> perfiles) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        LocalDateTime ahora = LocalDateTime.now();
        List<Descubrimiento> registros = perfiles.stream()
            .map(p -> Descubrimiento.builder()
                .usuario(usuario)
                .perfil(p)
                .fechaConsulta(ahora)
                .build())
            .collect(Collectors.toList());

        descubrimientoRepository.saveAll(registros);
        return ahora;
    }

    private List<PerfilDto> componerRespuestaMinima(List<Perfil> perfiles) {
        return perfiles.stream()
            .map(p -> new PerfilDto(
                p.getId(),
                p.getNombre(),
                p.getEdad(),
                p.getDescripcion(),
                p.getUbicacion(),
                p.getFotos(),
                p.getIntereses()
            ))
            .collect(Collectors.toList());
    }

    private Integer calcularEdadMin(Integer edad) {
        return edad != null ? Math.max(18, edad - 10) : 18;
    }

    private Integer calcularEdadMax(Integer edad) {
        return edad != null ? edad + 10 : 99;
    }
}