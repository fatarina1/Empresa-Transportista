package ar.edu.tpi.logistica.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "origen_deposito_id")
    private Long origenDepositoId;

    @Column(name = "destino_deposito_id")
    private Long destinoDepositoId;

    @Column(name = "origen_latitud")
    private Double origenLatitud;

    @Column(name = "origen_longitud")
    private Double origenLongitud;

    @Column(name = "destino_latitud")
    private Double destinoLatitud;

    @Column(name = "destino_longitud")
    private Double destinoLongitud;

    @Column(name = "tipo", length = 40)
    private TipoTramo tipo;

    @Column(name = "estado", length = 30)
    private EstadoTramo estado;

    @Column(name = "distancia_km")
    private Double distanciaKm;

    @Column(name = "costo_aproximado")
    private Double costoAproximado;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "fecha_inicio_estimada")
    private LocalDateTime fechaInicioEstimada;

    @Column(name = "fecha_fin_estimada")
    private LocalDateTime fechaFinEstimada;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum TipoTramo {
        ORIGEN_DESTINO, ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO
    }

    public enum EstadoTramo {
        ESTIMADO, ASIGNADO, INICIADO, FINALIZADO
    }

}
