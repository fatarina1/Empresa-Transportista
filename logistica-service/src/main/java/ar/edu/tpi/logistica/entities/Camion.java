package ar.edu.tpi.logistica.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String dominio;

    @Column(name = "capacidad_peso_kg", nullable = false)
    private Double capacidadPesoKg;

    @Column(name = "capacidad_volumen_m3", nullable = false)
    private Double capacidadVolumenM3;

    @Column(name = "costo_km_base", nullable = false)
    private Double costoKmBase;

    @Column(name = "consumo_promedio_l_km", nullable = false)
    private Double consumoPromedioLitrosKm;

    @Column(nullable = false)
    private Boolean disponible;

    @Column(name = "transportista_id")
    private Long transportistaId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
