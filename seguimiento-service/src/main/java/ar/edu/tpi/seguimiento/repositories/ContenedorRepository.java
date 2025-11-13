package ar.edu.tpi.seguimiento.repositories;

import ar.edu.tpi.seguimiento.entities.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    
    Optional<Contenedor> findByIdentificacion(String identificacion);
    
    List<Contenedor> findByClienteId(Long clienteId);
    
    boolean existsByIdentificacion(String identificacion);
}
