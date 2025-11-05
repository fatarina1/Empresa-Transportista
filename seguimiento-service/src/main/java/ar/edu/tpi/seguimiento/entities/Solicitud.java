package ar.edu.tpi.seguimiento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.BigDecimal;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, nullable = false, unique = true)
    private String numero;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "contenedor_id", nullable = false)
    private Long contenedorId;

    // `Ubicacion` is owned by the logistica microservice. To avoid duplicating
    // entity classes across microservices, we store foreign keys here and
    // resolve the full `Ubicacion` via the logistica service API when needed.
    @Column(name = "origen_id", nullable = false)
    private Long origenId;

    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "costo_estimado", precision = 14, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "tiempo_estimado_min")
    private Integer tiempoEstimadoMin;

    @Column(name = "costo_final", precision = 14, scale = 2)
    private BigDecimal costoFinal;

    @Column(name = "tiempo_real_min")
    private Integer tiempoRealMin;

    @Column(name = "observaciones", length = 400)
    private String observaciones;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    // Origen (dirección + lat/long)
    @Column(name = "origen_direccion", length = 240, nullable = false)
    private String origenDireccion;


    @Column(name = "origen_latitud", precision = 9, scale = 6, nullable = false)
    private BigDecimal origenLatitud;

    @Column(name = "origen_longitud", precision = 9, scale = 6, nullable = false)
    private BigDecimal origenLongitud;

    // Destino (dirección + lat/long)
    @Column(name = "destino_direccion", length = 240, nullable = false)
    private String destinoDireccion;

    @Column(name = "destino_latitud", precision = 9, scale = 6, nullable = false)
    private BigDecimal destinoLatitud;

    @Column(name = "destino_longitud", precision = 9, scale = 6, nullable = false)
    private BigDecimal destinoLongitud;
}
