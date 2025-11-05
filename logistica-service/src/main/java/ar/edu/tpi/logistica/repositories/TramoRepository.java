package ar.edu.tpi.logistica.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.tpi.logistica.entities.Tramo;

public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByRutaIdOrderByOrdenAsc(Long rutaId);
    List<Tramo> findByCamionId(Long camionId);
    List<Tramo> findByEstado(Tramo.EstadoTramo estado);
}