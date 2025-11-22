package ar.edu.tpi.logistica.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estado", length = 30)
    private EstadoRuta estado;

    @Column(name = "cantidad_tramos")
    private Integer cantidadTramos = 0;

    @Column(name = "cantidad_depositos")
    private Integer cantidadDepositos = 0;

    @Column(name = "costo_final")
    private Double costoFinal;

    @Column(length = 400)
    private String observaciones;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum EstadoRuta {
        PROPUESTA, ASIGNADA, EN_EJECUCION, FINALIZADA, CANCELADA
    }
}
