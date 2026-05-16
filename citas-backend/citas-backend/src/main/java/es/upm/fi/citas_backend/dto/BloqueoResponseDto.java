package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BloqueoResponseDto {
    private Long   bloqueoId;
    private String mensaje;
}