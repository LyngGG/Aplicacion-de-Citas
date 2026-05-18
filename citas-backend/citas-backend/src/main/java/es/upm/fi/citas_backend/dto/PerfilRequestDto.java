package es.upm.fi.citas_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRequestDto {

    @NotBlank(message = "nombre es obligatorio")
    private String nombre;

    @NotNull(message = "edad es obligatoria")
    private Integer edad;

    private String ubicacion;
    private List<String> intereses;
    private List<String> fotos;
}
