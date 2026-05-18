package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificacionResponseDto {
    private Long            id;
    private String          tipo;
    private String          contenido;
    private LocalDateTime   timestamp;
    private boolean         leida;
}
