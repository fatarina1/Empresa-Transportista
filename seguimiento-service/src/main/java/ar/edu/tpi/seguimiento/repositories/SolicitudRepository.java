package ar.edu.tpi.seguimiento.repositories;

import ar.edu.tpi.seguimiento.entities.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    
    Optional<Solicitud> findByNumero(String numero);
    
    List<Solicitud> findByClienteId(Long clienteId);
    
    List<Solicitud> findByContenedorId(Long contenedorId);
    
    List<Solicitud> findByEstado(String estado);
    
    @Query("SELECT s FROM Solicitud s WHERE s.clienteId = ?1 AND s.estado = ?2")
    List<Solicitud> findByClienteIdAndEstado(Long clienteId, String estado);
    
    boolean existsByNumero(String numero);
}
