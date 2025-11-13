package ar.edu.tpi.logistica.repositories;

import ar.edu.tpi.logistica.entities.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
    
    List<Ubicacion> findByTipo(String tipo);
    
    Ubicacion findByNombre(String nombre);
}
