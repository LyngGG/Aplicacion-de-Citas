package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> { }