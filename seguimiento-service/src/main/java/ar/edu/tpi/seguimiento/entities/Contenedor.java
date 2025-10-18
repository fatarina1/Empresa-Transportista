package ar.edu.tpi.seguimiento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "contenedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80, nullable = false, unique = true)
    private String identificacion;

    @Column(name = "peso_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "volumen_m3", nullable = false, precision = 10, scale = 3)
    private BigDecimal volumenM3;

    @Column(name = "largo_m", precision = 10, scale = 2)
    private BigDecimal largoM;

    @Column(name = "ancho_m", precision = 10, scale = 2)
    private BigDecimal anchoM;

    @Column(name = "alto_m", precision = 10, scale = 2)
    private BigDecimal altoM;

    @Column(length = 240)
    private String descripcion;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Estado del contenedor se gestiona vía solicitud/flujo; no es un campo obligatorio en la DB esquema principal.
}
