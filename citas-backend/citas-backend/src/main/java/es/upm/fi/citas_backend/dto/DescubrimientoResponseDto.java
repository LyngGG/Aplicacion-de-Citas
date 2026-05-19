package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class DescubrimientoResponseDto {
    private LocalDateTime   fechaConsulta;
    private List<PerfilDto> resultados;
}