package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MatchResponseDto {
    private Long            id;
    private Long            usuario1Id;
    private Long            usuario2Id;
    private LocalDateTime   fechaCreacion;
    private String          estado;
}
