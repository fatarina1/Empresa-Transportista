package ar.edu.tpi.logistica.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rangos_volumen_km_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaVolumen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "volumen_min_m3", nullable = false)
    private BigDecimal volumenMinM3;

    @Column(name = "volumen_max_m3", nullable = false)
    private BigDecimal volumenMaxM3;

    @Column(name = "costo_km_base", nullable = false)
    private BigDecimal costoKmBase;
}
