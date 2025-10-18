package ar.edu.tpi.logistica.repositories;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.tpi.logistica.entities.TarifaVolumen;

public interface TarifaVolumenRepository extends JpaRepository<TarifaVolumen, Long> {
    List<TarifaVolumen> findByVolumenMinM3LessThanEqualAndVolumenMaxM3GreaterThanEqual(BigDecimal volumen, BigDecimal volumen2);
}