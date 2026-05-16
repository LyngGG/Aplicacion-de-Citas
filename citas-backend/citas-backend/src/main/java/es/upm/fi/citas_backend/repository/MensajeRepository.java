package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByMatchIdOrderByTimestampAsc(Long matchId);
}