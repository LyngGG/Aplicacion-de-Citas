package es.upm.fi.citas_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EnviarMensajeRequestDto {

    @NotNull(message = "remitenteId es obligatorio")
    private Long remitenteId;

    @NotBlank(message = "El texto no puede estar vacío")
    private String texto;
}