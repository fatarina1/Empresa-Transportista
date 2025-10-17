package ar.edu.tpi.logistica.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "depositos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120, nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @Column(name = "costo_estadia_diaria", nullable = false)
    private Double costoEstadiaDiaria;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
