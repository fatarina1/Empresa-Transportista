package ar.edu.tpi.seguimiento.repositories;

import ar.edu.tpi.seguimiento.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByDocumento(String documento);
    
    boolean existsByEmail(String email);
    
    boolean existsByDocumento(String documento);
}
