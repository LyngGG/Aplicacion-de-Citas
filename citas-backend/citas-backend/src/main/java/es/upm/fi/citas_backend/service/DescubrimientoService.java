package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Descubrimiento;
import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.domain.Usuario;
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
    public List<PerfilDto> buscarCandidatos(
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
        registrarDescubrimiento(perfilContexto.getUsuario().getId(), diversificados);
        return componerRespuestaMinima(diversificados);
    }

    private List<Perfil> aplicarDiversidadYFrescura(List<Perfil> perfiles) {
        List<Perfil> resultado = new ArrayList<>(perfiles);
        Collections.shuffle(resultado);
        log.debug("[DescubrimientoService] Diversidad aplicada → {} candidatos", resultado.size());
        return resultado;
    }

    private void registrarDescubrimiento(Long usuarioId, List<Perfil> perfiles) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        List<Descubrimiento> registros = perfiles.stream()
            .map(p -> Descubrimiento.builder()
                .usuario(usuario)
                .perfil(p)
                .fechaConsulta(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());

        descubrimientoRepository.saveAll(registros);
    }

    private List<PerfilDto> componerRespuestaMinima(List<Perfil> perfiles) {
        return perfiles.stream()
            .map(p -> new PerfilDto(
                p.getId(),
                p.getNombre(),
                p.getEdad(),
                p.getUbicacion(),
                p.getIntereses(),
                p.getFotos() != null && !p.getFotos().isEmpty() ? p.getFotos().get(0) : null
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