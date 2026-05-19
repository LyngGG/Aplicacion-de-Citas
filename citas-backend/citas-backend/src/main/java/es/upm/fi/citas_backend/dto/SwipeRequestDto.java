package es.upm.fi.citas_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SwipeRequestDto {

    @NotNull(message = "usuarioOrigen es obligatorio")
    private Long usuarioOrigen;

    @NotNull(message = "usuarioDestino es obligatorio")
    private Long usuarioDestino;

    @NotNull(message = "accion es obligatoria")
    private String accion;

    private LocalDateTime timestamp;
}
