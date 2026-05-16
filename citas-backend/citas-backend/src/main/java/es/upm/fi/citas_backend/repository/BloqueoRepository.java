package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {

    @Query("""
        SELECT COUNT(b) > 0 FROM Bloqueo b
        WHERE b.bloqueador.id = :bloqueadorId
          AND b.bloqueado.id  = :bloqueadoId
        """)
    boolean existeBloqueo(
        @Param("bloqueadorId") Long bloqueadorId,
        @Param("bloqueadoId")  Long bloqueadoId
    );
}