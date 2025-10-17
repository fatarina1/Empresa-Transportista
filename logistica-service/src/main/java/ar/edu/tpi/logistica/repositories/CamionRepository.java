package ar.edu.tpi.logistica.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.tpi.logistica.entities.Camion;

public interface CamionRepository extends JpaRepository<Camion, Long> {
    Camion findByDominio(String dominio);
    List<Camion> findByDisponibleTrue();
}
