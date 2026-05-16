package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MensajeResponseDto {
    private Long          mensajeId;
    private LocalDateTime timestamp;
}
