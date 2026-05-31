package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    // TO-DO : se supone que hace validaciones, busca en la base de datos, etc.
}