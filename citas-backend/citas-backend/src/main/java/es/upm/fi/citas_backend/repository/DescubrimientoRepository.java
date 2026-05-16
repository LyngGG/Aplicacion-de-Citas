package es.upm.fi.citas_backend.repository;

import es.upm.fi.citas_backend.domain.Descubrimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescubrimientoRepository extends JpaRepository<Descubrimiento, Long> { }