package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Perfil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByUsuarioId(Long usuarioId);

    @Query("""
        SELECT p FROM Perfil p
        WHERE p.usuario.id NOT IN :idsExcluidos
          AND p.usuario.id <> :usuarioId
          AND (:ubicacion IS NULL OR p.ubicacion = :ubicacion)
          AND (:edadMin IS NULL OR p.edad >= :edadMin)
          AND (:edadMax IS NULL OR p.edad <= :edadMax)
        ORDER BY p.id ASC
        """)
    List<Perfil> buscarCandidatos(
        @Param("usuarioId")    Long usuarioId,
        @Param("idsExcluidos") List<Long> idsExcluidos,
        @Param("ubicacion")    String ubicacion,
        @Param("edadMin")      Integer edadMin,
        @Param("edadMax")      Integer edadMax,
        Pageable pageable
    );
}