package es.upm.fi.citas_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BloqueoRequestDto {

    @NotNull(message = "bloqueadorId es obligatorio")
    private Long bloqueadorId;

    @NotNull(message = "bloqueadoId es obligatorio")
    private Long bloqueadoId;
}