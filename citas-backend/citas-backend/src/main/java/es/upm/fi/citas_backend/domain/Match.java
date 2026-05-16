package es.upm.fi.citas_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario1_id")
    private Usuario usuario1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario2_id")
    private Usuario usuario2;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatch estado;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<Mensaje> mensajes;

    public enum EstadoMatch { ACTIVO, INVALIDADO }

    public boolean isActivo() {
        return EstadoMatch.ACTIVO.equals(this.estado);
    }

    public boolean perteneceUsuario(Long usuarioId) {
        return usuario1.getId().equals(usuarioId)
            || usuario2.getId().equals(usuarioId);
    }
}