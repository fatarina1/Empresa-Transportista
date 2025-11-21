package ar.edu.tpi.seguimiento.repositories;

import ar.edu.tpi.seguimiento.entities.SolicitudCambioEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudCambioEstadoRepository extends JpaRepository<SolicitudCambioEstado, Long> {
    
    /**
     * Obtiene todos los cambios de estado de una solicitud ordenados por fecha
     */
    List<SolicitudCambioEstado> findBySolicitudIdOrderByFechaDesdeDesc(Long solicitudId);
    
    /**
     * Obtiene el último cambio de estado activo de una solicitud (fecha_hasta es null)
     */
    @Query("SELECT c FROM SolicitudCambioEstado c WHERE c.solicitudId = :solicitudId AND c.fechaHasta IS NULL")
    Optional<SolicitudCambioEstado> findEstadoActual(@Param("solicitudId") Long solicitudId);
    
    /**
     * Verifica si existe un cambio de estado activo
     */
    boolean existsBySolicitudIdAndFechaHastaIsNull(Long solicitudId);
}
