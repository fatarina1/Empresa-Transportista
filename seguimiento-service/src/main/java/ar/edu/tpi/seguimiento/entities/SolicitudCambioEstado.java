package ar.edu.tpi.seguimiento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud_cambios_estado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus estado;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta")
    private LocalDateTime fechaHasta;

    @Column(length = 240)
    private String detalle;

    // Enum embedded here (no separate class) to represent request status values.
    public enum RequestStatus {
        BORRADOR,
        PROGRAMADA,
        EN_TRANSITO,
        ENTREGADA,
        CANCELADA
    }
}
