package ar.edu.tpi.seguimiento.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Métricas personalizadas para el servicio de seguimiento.
 * Registra contadores y temporizadores para operaciones clave del negocio.
 */
@Component
public class MetricasSeguimiento {

    private final Counter solicitudesCreadas;
    private final Counter solicitudesFinalizadas;
    private final Counter contenedoresCreados;
    private final Counter cambiosEstadoSolicitud;
    private final Timer tiempoProcesamientoSolicitud;

    public MetricasSeguimiento(MeterRegistry registry) {
        // Contadores de operaciones
        this.solicitudesCreadas = Counter.builder("seguimiento.solicitudes.creadas")
                .description("Número total de solicitudes creadas")
                .tag("servicio", "seguimiento")
                .register(registry);

        this.solicitudesFinalizadas = Counter.builder("seguimiento.solicitudes.finalizadas")
                .description("Número total de solicitudes finalizadas (ENTREGADA)")
                .tag("servicio", "seguimiento")
                .register(registry);

        this.contenedoresCreados = Counter.builder("seguimiento.contenedores.creados")
                .description("Número total de contenedores creados")
                .tag("servicio", "seguimiento")
                .register(registry);

        this.cambiosEstadoSolicitud = Counter.builder("seguimiento.cambios_estado")
                .description("Número total de cambios de estado en solicitudes")
                .tag("servicio", "seguimiento")
                .register(registry);

        // Temporizadores de rendimiento
        this.tiempoProcesamientoSolicitud = Timer.builder("seguimiento.tiempo.procesamiento_solicitud")
                .description("Tiempo de procesamiento de solicitud desde creación hasta finalización")
                .tag("operacion", "procesar_solicitud")
                .register(registry);
    }

    // Métodos para incrementar contadores
    public void incrementarSolicitudesCreadas() {
        solicitudesCreadas.increment();
    }

    public void incrementarSolicitudesFinalizadas() {
        solicitudesFinalizadas.increment();
    }

    public void incrementarContenedoresCreados() {
        contenedoresCreados.increment();
    }

    public void incrementarCambiosEstado() {
        cambiosEstadoSolicitud.increment();
    }

    // Métodos para registrar tiempos de ejecución
    public Timer.Sample iniciarMedicionProcesamientoSolicitud() {
        return Timer.start();
    }

    public void finalizarMedicionProcesamientoSolicitud(Timer.Sample sample) {
        sample.stop(tiempoProcesamientoSolicitud);
    }
}
