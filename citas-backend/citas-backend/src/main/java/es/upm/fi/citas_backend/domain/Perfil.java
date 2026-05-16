package es.upm.fi.citas_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "perfiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    private Integer edad;
    private String  descripcion;
    private String  ubicacion;

    @ElementCollection
    @CollectionTable(name = "perfil_intereses", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "interes")
    private List<String> intereses;

    @ElementCollection
    @CollectionTable(name = "perfil_fotos", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "url_foto")
    private List<String> fotos;
}