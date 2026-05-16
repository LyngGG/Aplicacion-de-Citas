package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {

    @Query("SELECT s.destinatario.id FROM Swipe s WHERE s.remitente.id = :usuarioId")
    List<Long> findIdsInteractuadosByUsuarioId(@Param("usuarioId") Long usuarioId);
}