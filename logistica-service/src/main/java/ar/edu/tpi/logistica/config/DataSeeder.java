package ar.edu.tpi.logistica.config;

import ar.edu.tpi.logistica.entities.*;
import ar.edu.tpi.logistica.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Configuración para cargar datos iniciales en la base de datos.
 * Se ejecuta automáticamente al iniciar la aplicación.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final CamionRepository camionRepository;
    private final DepositoRepository depositoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final TarifaVolumenRepository tarifaVolumenRepository;
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Solo cargar datos si la BD está vacía
            if (camionRepository.count() > 0) {
                log.info("Base de datos ya contiene datos. Saltando seeder.");
                return;
            }

            log.info("Iniciando carga de datos de prueba...");

            // ============================================
            // CAMIONES
            // ============================================
            Camion camion1 = new Camion();
            camion1.setDominio("AB123CD");
            camion1.setCapacidadPesoKg(10000.0);
            camion1.setCapacidadVolumenM3(50.0);
            camion1.setCostoKmBase(80.0);
            camion1.setConsumoPromedioLitrosKm(0.35);
            camion1.setDisponible(true);
            camion1.setCreatedAt(LocalDateTime.now());
            camionRepository.save(camion1);

            Camion camion2 = new Camion();
            camion2.setDominio("EF456GH");
            camion2.setCapacidadPesoKg(15000.0);
            camion2.setCapacidadVolumenM3(75.0);
            camion2.setCostoKmBase(120.0);
            camion2.setConsumoPromedioLitrosKm(0.45);
            camion2.setDisponible(true);
            camion2.setCreatedAt(LocalDateTime.now());
            camionRepository.save(camion2);

            Camion camion3 = new Camion();
            camion3.setDominio("IJ789KL");
            camion3.setCapacidadPesoKg(8000.0);
            camion3.setCapacidadVolumenM3(40.0);
            camion3.setCostoKmBase(60.0);
            camion3.setConsumoPromedioLitrosKm(0.30);
            camion3.setDisponible(false); // En uso
            camion3.setCreatedAt(LocalDateTime.now());
            camionRepository.save(camion3);

            log.info("✓ Cargados {} camiones", camionRepository.count());

            // ============================================
            // UBICACIONES
            // ============================================
            Ubicacion ubicCordoba = new Ubicacion();
            ubicCordoba.setNombre("Córdoba Capital");
            ubicCordoba.setDireccion("Av. Colón 1234, Córdoba");
            ubicCordoba.setLatitud(-31.4167);
            ubicCordoba.setLongitud(-64.1833);
            ubicCordoba.setCreatedAt(LocalDateTime.now());
            ubicacionRepository.save(ubicCordoba);

            Ubicacion ubicMendoza = new Ubicacion();
            ubicMendoza.setNombre("Mendoza Capital");
            ubicMendoza.setDireccion("San Martín 500, Mendoza");
            ubicMendoza.setLatitud(-32.8908);
            ubicMendoza.setLongitud(-68.8272);
            ubicMendoza.setCreatedAt(LocalDateTime.now());
            ubicacionRepository.save(ubicMendoza);

            Ubicacion ubicRosario = new Ubicacion();
            ubicRosario.setNombre("Rosario");
            ubicRosario.setDireccion("Pellegrini 1000, Rosario");
            ubicRosario.setLatitud(-32.9468);
            ubicRosario.setLongitud(-60.6393);
            ubicRosario.setCreatedAt(LocalDateTime.now());
            ubicacionRepository.save(ubicRosario);

            Ubicacion ubicBuenosAires = new Ubicacion();
            ubicBuenosAires.setNombre("Buenos Aires - Retiro");
            ubicBuenosAires.setDireccion("Av. Ramos Mejía 1302, CABA");
            ubicBuenosAires.setLatitud(-34.5875);
            ubicBuenosAires.setLongitud(-58.3736);
            ubicBuenosAires.setCreatedAt(LocalDateTime.now());
            ubicacionRepository.save(ubicBuenosAires);

            log.info("✓ Cargadas {} ubicaciones", ubicacionRepository.count());

            // ============================================
            // DEPÓSITOS
            // ============================================
            Deposito depositoCordoba = new Deposito();
            depositoCordoba.setNombre("Depósito Central Córdoba");
            depositoCordoba.setUbicacion(ubicCordoba);
            depositoCordoba.setCostoEstadiaDiaria(500.0);
            depositoCordoba.setActivo(true);
            depositoCordoba.setCreatedAt(LocalDateTime.now());
            depositoRepository.save(depositoCordoba);

            Deposito depositoMendoza = new Deposito();
            depositoMendoza.setNombre("Depósito Cuyo");
            depositoMendoza.setUbicacion(ubicMendoza);
            depositoMendoza.setCostoEstadiaDiaria(450.0);
            depositoMendoza.setActivo(true);
            depositoMendoza.setCreatedAt(LocalDateTime.now());
            depositoRepository.save(depositoMendoza);

            Deposito depositoRosario = new Deposito();
            depositoRosario.setNombre("Depósito Litoral");
            depositoRosario.setUbicacion(ubicRosario);
            depositoRosario.setCostoEstadiaDiaria(600.0);
            depositoRosario.setActivo(true);
            depositoRosario.setCreatedAt(LocalDateTime.now());
            depositoRepository.save(depositoRosario);

            log.info("✓ Cargados {} depósitos", depositoRepository.count());

            // ============================================
            // TARIFAS POR VOLUMEN
            // ============================================
            TarifaVolumen tarifa1 = new TarifaVolumen();
            tarifa1.setVolumenMinM3(BigDecimal.ZERO);
            tarifa1.setVolumenMaxM3(new BigDecimal("10"));
            tarifa1.setCostoKmBase(new BigDecimal("100.00"));
            tarifaVolumenRepository.save(tarifa1);

            TarifaVolumen tarifa2 = new TarifaVolumen();
            tarifa2.setVolumenMinM3(new BigDecimal("10"));
            tarifa2.setVolumenMaxM3(new BigDecimal("30"));
            tarifa2.setCostoKmBase(new BigDecimal("150.00"));
            tarifaVolumenRepository.save(tarifa2);

            TarifaVolumen tarifa3 = new TarifaVolumen();
            tarifa3.setVolumenMinM3(new BigDecimal("30"));
            tarifa3.setVolumenMaxM3(new BigDecimal("100"));
            tarifa3.setCostoKmBase(new BigDecimal("200.00"));
            tarifaVolumenRepository.save(tarifa3);

            log.info("✓ Cargadas {} tarifas", tarifaVolumenRepository.count());

            // ============================================
            // RUTA DE EJEMPLO
            // ============================================
            Ruta ruta1 = new Ruta();
            ruta1.setSolicitudId(1L);
            ruta1.setSolicitudExternaId(1001L);
            ruta1.setEstado(Ruta.EstadoRuta.PROPUESTA);
            ruta1.setCantidadTramos(1);
            ruta1.setCantidadDepositos(0);
            ruta1.setObservaciones("Ruta directa Córdoba - Mendoza");
            ruta1.setCreatedAt(LocalDateTime.now());
            rutaRepository.save(ruta1);

            // ============================================
            // TRAMO DE EJEMPLO
            // ============================================
            Tramo tramo1 = new Tramo();
            tramo1.setRuta(ruta1);
            tramo1.setOrden(1);
            tramo1.setOrigenLatitud(-31.4167);
            tramo1.setOrigenLongitud(-64.1833);
            tramo1.setDestinoLatitud(-32.8908);
            tramo1.setDestinoLongitud(-68.8272);
            tramo1.setTipo(Tramo.TipoTramo.ORIGEN_DESTINO);
            tramo1.setDistanciaKm(608.0);
            tramo1.setEstado(Tramo.EstadoTramo.ESTIMADO);
            tramo1.setCreatedAt(LocalDateTime.now());
            tramoRepository.save(tramo1);

            log.info("✓ Cargadas {} rutas con {} tramos", rutaRepository.count(), tramoRepository.count());

            log.info("========================================");
            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("========================================");
        };
    }
}
