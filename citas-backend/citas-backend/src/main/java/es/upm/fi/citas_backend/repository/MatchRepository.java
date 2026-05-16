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
            (m.usuario1.id = :u1 AND m.usuario2.id = :u2)
            OR
            (m.usuario1.id = :u2 AND m.usuario2.id = :u1)
          )
        """)
    Optional<Match> findMatchActivoEntreUsuarios(
        @Param("u1") Long u1,
        @Param("u2") Long u2
    );
}
