package ar.edu.tpi.logistica.repositories;

import ar.edu.tpi.logistica.entities.ConfiguracionGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionGeneralRepository extends JpaRepository<ConfiguracionGeneral, Long> {
    Optional<ConfiguracionGeneral> findByClave(String clave);
}
