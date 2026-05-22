package es.upm.fi.citas_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(
    name = "matches",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_match_usuarios",
        columnNames = {"usuario1_id", "usuario2_id"}
    )
)
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

    public Match(Usuario usuario1, Usuario usuario2, LocalDateTime fechaCreacion, EstadoMatch estado) {
        // Normalizar: usuario1.id < usuario2.id para evitar duplicados (1,2) vs (2,1)
        if (usuario1.getId() < usuario2.getId()) {
            this.usuario1 = usuario1;
            this.usuario2 = usuario2;
        } else {
            this.usuario1 = usuario2;
            this.usuario2 = usuario1;
        }
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.mensajes = new ArrayList<>();
    }

    public boolean isActivo() {
        return EstadoMatch.ACTIVO.equals(this.estado);
    }

    public boolean perteneceUsuario(Long usuarioId) {
        return usuario1.getId().equals(usuarioId)
            || usuario2.getId().equals(usuarioId);
    }
}