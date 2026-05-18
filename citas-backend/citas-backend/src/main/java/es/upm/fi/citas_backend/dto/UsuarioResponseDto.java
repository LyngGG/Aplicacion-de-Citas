package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponseDto {
    private Long   id;
    private String email;
    private String estado;
}
