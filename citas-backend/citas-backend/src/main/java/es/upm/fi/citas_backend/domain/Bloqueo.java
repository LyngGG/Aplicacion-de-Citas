package es.upm.fi.citas_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "bloqueos",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bloqueador_id", "bloqueado_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bloqueador_id")
    private Usuario bloqueador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bloqueado_id")
    private Usuario bloqueado;

    @Column(nullable = false)
    private LocalDateTime fechaBloqueo;
}
