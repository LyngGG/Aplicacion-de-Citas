package es.upm.fi.citas_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "swipes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccionSwipe accion;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public enum AccionSwipe { ACEPTADO, RECHAZADO }
}