package ar.edu.tpi.logistica.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.tpi.logistica.entities.Deposito;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    Deposito findByNombre(String nombre);
}