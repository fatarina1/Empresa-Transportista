package ar.edu.tpi.logistica.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.tpi.logistica.entities.Ruta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    Ruta findBySolicitudId(Long solicitudId);
    Ruta findBySolicitudExternaId(Long solicitudExternaId);
    List<Ruta> findByEstado(Ruta.EstadoRuta estado);
}