package es.upm.fi.citas_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WsMensajeRequestDto {
    private Long   matchId;
    private String texto;
}