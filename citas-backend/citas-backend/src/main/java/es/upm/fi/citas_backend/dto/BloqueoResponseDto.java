package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BloqueoResponseDto {
    private Long          bloqueoId;
    private LocalDateTime fechaBloqueo;
}