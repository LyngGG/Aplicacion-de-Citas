package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
        SELECT m FROM Match m
        WHERE m.estado = 'ACTIVO'
          AND (
            (m.usuario1.id = :u1_id AND m.usuario2.id = :u2_id)
            OR
            (m.usuario1.id = :u2_id AND m.usuario2.id = :u1_id)
          )
        """)
    Optional<Match> findMatchActivoEntreUsuarios(
        @Param("u1_id") Long u1_id,
        @Param("u2_id") Long u2_id
    );

    @Query("SELECT m FROM Match m WHERE m.usuario1.id = :usuarioId OR m.usuario2.id = :usuarioId")
    java.util.List<Match> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
