package es.upm.fi.citas_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class PerfilDto {
    private Long         id;
    private String       nombre;
    private Integer      edad;
    private String       ubicacion;
    private List<String> intereses;
    private String       fotoPrincipal;
}