package ar.edu.tpi.logistica.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Métricas personalizadas para el servicio de logística.
 * Registra contadores y temporizadores para operaciones clave del negocio.
 */
@Component
public class MetricasLogistica {

    private final Counter rutasCreadas;
    private final Counter rutasFinalizadas;
    private final Counter tramosIniciados;
    private final Counter tramosFinalizados;
    private final Counter erroresCalculoCosto;
    private final Timer tiempoCalculoRutaTentativa;
    private final Timer tiempoCalculoCostoReal;

    public MetricasLogistica(MeterRegistry registry) {
        // Contadores de operaciones
        this.rutasCreadas = Counter.builder("logistica.rutas.creadas")
                .description("Número total de rutas creadas")
                .tag("servicio", "logistica")
                .register(registry);

        this.rutasFinalizadas = Counter.builder("logistica.rutas.finalizadas")
                .description("Número total de rutas finalizadas (ENTREGADA)")
                .tag("servicio", "logistica")
                .register(registry);

        this.tramosIniciados = Counter.builder("logistica.tramos.iniciados")
                .description("Número total de tramos iniciados")
                .tag("servicio", "logistica")
                .register(registry);

        this.tramosFinalizados = Counter.builder("logistica.tramos.finalizados")
                .description("Número total de tramos finalizados")
                .tag("servicio", "logistica")
                .register(registry);

        this.erroresCalculoCosto = Counter.builder("logistica.errores.calculo_costo")
                .description("Número total de errores al calcular costos")
                .tag("servicio", "logistica")
                .register(registry);

        // Temporizadores de rendimiento
        this.tiempoCalculoRutaTentativa = Timer.builder("logistica.tiempo.ruta_tentativa")
                .description("Tiempo de cálculo de ruta tentativa")
                .tag("operacion", "calcular_tentativa")
                .register(registry);

        this.tiempoCalculoCostoReal = Timer.builder("logistica.tiempo.costo_real")
                .description("Tiempo de cálculo de costo real")
                .tag("operacion", "calcular_costo_real")
                .register(registry);
    }

    // Métodos para incrementar contadores
    public void incrementarRutasCreadas() {
        rutasCreadas.increment();
    }

    public void incrementarRutasFinalizadas() {
        rutasFinalizadas.increment();
    }

    public void incrementarTramosIniciados() {
        tramosIniciados.increment();
    }

    public void incrementarTramosFinalizados() {
        tramosFinalizados.increment();
    }

    public void incrementarErroresCalculoCosto() {
        erroresCalculoCosto.increment();
    }

    // Métodos para registrar tiempos de ejecución
    public Timer.Sample iniciarMedicionRutaTentativa() {
        return Timer.start();
    }

    public void finalizarMedicionRutaTentativa(Timer.Sample sample) {
        sample.stop(tiempoCalculoRutaTentativa);
    }

    public Timer.Sample iniciarMedicionCostoReal() {
        return Timer.start();
    }

    public void finalizarMedicionCostoReal(Timer.Sample sample) {
        sample.stop(tiempoCalculoCostoReal);
    }
}
