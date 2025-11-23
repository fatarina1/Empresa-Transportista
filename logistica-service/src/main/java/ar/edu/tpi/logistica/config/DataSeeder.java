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
    private final ConfiguracionGeneralRepository configuracionGeneralRepository;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Eliminar todos los datos antes de cargar
            log.info("Forzando recarga de datos de prueba: eliminando datos existentes...");
            tramoRepository.deleteAll();
            rutaRepository.deleteAll();
            tarifaVolumenRepository.deleteAll();
            depositoRepository.deleteAll();
            ubicacionRepository.deleteAll();
            camionRepository.deleteAll();
            configuracionGeneralRepository.deleteAll();

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
                camion1.setTransportistaId(1L); // Asociar transportista de prueba
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
            // CONFIGURACIÓN GENERAL
            // ============================================
            ConfiguracionGeneral valorCombustible = new ConfiguracionGeneral();
            valorCombustible.setClave("VALOR_LITRO_COMBUSTIBLE");
            valorCombustible.setValor("450.00");
            valorCombustible.setDescripcion("Valor en pesos del litro de combustible");
            valorCombustible.setUpdatedAt(LocalDateTime.now());
            configuracionGeneralRepository.save(valorCombustible);

            ConfiguracionGeneral cargoGestion = new ConfiguracionGeneral();
            cargoGestion.setClave("CARGO_GESTION_POR_TRAMO");
            cargoGestion.setValor("5000.00");
            cargoGestion.setDescripcion("Cargo fijo de gestión por cada tramo de la ruta");
            cargoGestion.setUpdatedAt(LocalDateTime.now());
            configuracionGeneralRepository.save(cargoGestion);

            log.info("✓ Cargada configuración general");

            // ============================================
            // RUTAS Y TRAMOS DE EJEMPLO - ESCENARIOS REALISTAS
            // ============================================
            
            LocalDateTime now = LocalDateTime.now();
            
            // ESCENARIO 1: Ruta con contenedor EN DEPÓSITO (para probar RF5)
            // Solicitud ID=1, Contenedor ID=1 está esperando en Depósito Rosario
            Ruta ruta1 = new Ruta();
            ruta1.setEstado(Ruta.EstadoRuta.EN_EJECUCION);
            ruta1.setCantidadTramos(2);
            ruta1.setCantidadDepositos(1);
            ruta1.setObservaciones("Ruta Córdoba → Rosario → Buenos Aires (Contenedor esperando en Rosario)");
            ruta1.setCreatedAt(now.minusDays(5));
            rutaRepository.save(ruta1);

            // Tramo 1: Córdoba → Rosario (FINALIZADO hace 3 días)
            Tramo tramo1_1 = new Tramo();
            tramo1_1.setRuta(ruta1);
            tramo1_1.setOrden(1);
            // origenDepositoId = null porque sale del ORIGEN del cliente (Córdoba), no de un depósito
            tramo1_1.setDestinoDepositoId(depositoRosario.getId());
            tramo1_1.setOrigenLatitud(ubicCordoba.getLatitud());
            tramo1_1.setOrigenLongitud(ubicCordoba.getLongitud());
            tramo1_1.setDestinoLatitud(ubicRosario.getLatitud());
            tramo1_1.setDestinoLongitud(ubicRosario.getLongitud());
            tramo1_1.setTipo(Tramo.TipoTramo.ORIGEN_DEPOSITO);
            tramo1_1.setDistanciaKm(320.0);
            tramo1_1.setEstado(Tramo.EstadoTramo.FINALIZADO);
            tramo1_1.setCamionId(camion1.getId());
            tramo1_1.setFechaInicioEstimada(now.minusDays(4));
            tramo1_1.setFechaFinEstimada(now.minusDays(4).plusHours(6)); // 6 horas estimadas
            tramo1_1.setFechaInicioReal(now.minusDays(4));
            tramo1_1.setFechaFinReal(now.minusDays(3).plusHours(18)); // Llegó hace 3 días
            tramo1_1.setCreatedAt(now.minusDays(5));
            tramoRepository.save(tramo1_1);

            // Tramo 2: Rosario → Buenos Aires (ASIGNADO, aún no inicia)
            // Incluye 24h de estadía en depósito antes de iniciar
            Tramo tramo1_2 = new Tramo();
            tramo1_2.setRuta(ruta1);
            tramo1_2.setOrden(2);
            tramo1_2.setOrigenDepositoId(depositoRosario.getId());
            tramo1_2.setOrigenLatitud(ubicRosario.getLatitud());
            tramo1_2.setOrigenLongitud(ubicRosario.getLongitud());
            tramo1_2.setDestinoLatitud(ubicBuenosAires.getLatitud());
            tramo1_2.setDestinoLongitud(ubicBuenosAires.getLongitud());
            tramo1_2.setTipo(Tramo.TipoTramo.DEPOSITO_DESTINO);
            tramo1_2.setDistanciaKm(300.0);
            tramo1_2.setEstado(Tramo.EstadoTramo.ASIGNADO);
            tramo1_2.setCamionId(camion2.getId());
            // Inicio estimado: 24h después del fin del tramo anterior (estadía)
            tramo1_2.setFechaInicioEstimada(now.minusDays(4).plusHours(30)); // 6h viaje + 24h estadía
            tramo1_2.setFechaFinEstimada(now.minusDays(4).plusHours(35)); // + 5h de viaje
            tramo1_2.setCreatedAt(now.minusDays(5));
            tramoRepository.save(tramo1_2);

            log.info("✅ Escenario 1: Contenedor ID=1 esperando en Depósito Rosario (3 días)");

            // ESCENARIO 2: Otra ruta con contenedor EN DEPÓSITO (hace 7 días)
            // Solicitud ID=2, Contenedor ID=2 esperando en Depósito Mendoza
            Ruta ruta2 = new Ruta();
            ruta2.setEstado(Ruta.EstadoRuta.EN_EJECUCION);
            ruta2.setCantidadTramos(2);
            ruta2.setCantidadDepositos(1);
            ruta2.setObservaciones("Ruta Buenos Aires → Mendoza → Córdoba (Contenedor esperando en Mendoza)");
            ruta2.setCreatedAt(now.minusDays(10));
            rutaRepository.save(ruta2);

            // Tramo 2.1: Buenos Aires → Mendoza (FINALIZADO hace 7 días)
            Tramo tramo2_1 = new Tramo();
            tramo2_1.setRuta(ruta2);
            tramo2_1.setOrden(1);
            // origenDepositoId = null porque sale del ORIGEN del cliente (Buenos Aires), no de un depósito
            tramo2_1.setDestinoDepositoId(depositoMendoza.getId());
            tramo2_1.setOrigenLatitud(ubicBuenosAires.getLatitud());
            tramo2_1.setOrigenLongitud(ubicBuenosAires.getLongitud());
            tramo2_1.setDestinoLatitud(ubicMendoza.getLatitud());
            tramo2_1.setDestinoLongitud(ubicMendoza.getLongitud());
            tramo2_1.setTipo(Tramo.TipoTramo.ORIGEN_DEPOSITO);
            tramo2_1.setDistanciaKm(1050.0);
            tramo2_1.setEstado(Tramo.EstadoTramo.FINALIZADO);
            tramo2_1.setCamionId(camion2.getId());
            tramo2_1.setFechaInicioEstimada(now.minusDays(9));
            tramo2_1.setFechaFinEstimada(now.minusDays(9).plusHours(15)); // 15h estimadas
            tramo2_1.setFechaInicioReal(now.minusDays(9));
            tramo2_1.setFechaFinReal(now.minusDays(7)); // Llegó hace 7 días
            tramo2_1.setCreatedAt(now.minusDays(10));
            tramoRepository.save(tramo2_1);

            // Tramo 2.2: Mendoza → Córdoba (ASIGNADO, esperando)
            // Incluye 24h de estadía en depósito antes de iniciar
            Tramo tramo2_2 = new Tramo();
            tramo2_2.setRuta(ruta2);
            tramo2_2.setOrden(2);
            tramo2_2.setOrigenDepositoId(depositoMendoza.getId());
            tramo2_2.setOrigenLatitud(ubicMendoza.getLatitud());
            tramo2_2.setOrigenLongitud(ubicMendoza.getLongitud());
            tramo2_2.setDestinoLatitud(ubicCordoba.getLatitud());
            tramo2_2.setDestinoLongitud(ubicCordoba.getLongitud());
            tramo2_2.setTipo(Tramo.TipoTramo.DEPOSITO_DESTINO);
            tramo2_2.setDistanciaKm(608.0);
            tramo2_2.setEstado(Tramo.EstadoTramo.ASIGNADO);
            // Inicio estimado: 24h después del fin del tramo anterior (estadía)
            tramo2_2.setFechaInicioEstimada(now.minusDays(9).plusHours(39)); // 15h viaje + 24h estadía
            tramo2_2.setFechaFinEstimada(now.minusDays(9).plusHours(49)); // + 10h de viaje
            tramo2_2.setCreatedAt(now.minusDays(10));
            tramoRepository.save(tramo2_2);

            log.info("✅ Escenario 2: Contenedor ID=2 esperando en Depósito Mendoza (7 días)");

            // ESCENARIO 3: Ruta simple directa (para testing básico)
            Ruta ruta3 = new Ruta();
            ruta3.setEstado(Ruta.EstadoRuta.PROPUESTA);
            ruta3.setCantidadTramos(1);
            ruta3.setCantidadDepositos(0);
            ruta3.setObservaciones("Ruta directa Córdoba - Mendoza (sin depósitos)");
            ruta3.setCreatedAt(now.minusDays(1));
            rutaRepository.save(ruta3);

            Tramo tramo3_1 = new Tramo();
            tramo3_1.setRuta(ruta3);
            tramo3_1.setOrden(1);
            tramo3_1.setOrigenLatitud(ubicCordoba.getLatitud());
            tramo3_1.setOrigenLongitud(ubicCordoba.getLongitud());
            tramo3_1.setDestinoLatitud(ubicMendoza.getLatitud());
            tramo3_1.setDestinoLongitud(ubicMendoza.getLongitud());
            tramo3_1.setTipo(Tramo.TipoTramo.ORIGEN_DESTINO);
            tramo3_1.setDistanciaKm(608.0);
            tramo3_1.setEstado(Tramo.EstadoTramo.ESTIMADO);
            tramo3_1.setFechaInicioEstimada(now.plusHours(2));
            tramo3_1.setFechaFinEstimada(now.plusHours(12)); // 10h estimadas
            tramo3_1.setCreatedAt(now.minusDays(1));
            tramoRepository.save(tramo3_1);

            // ============================================
            // RUTAS COMPLETADAS (para probar RF8 - Costo Real)
            // ============================================

            // RUTA 4: COMPLETADA - Solicitud 4 (entregada hace 2 días, eficiente)
            Ruta ruta4 = new Ruta();
            ruta4.setEstado(Ruta.EstadoRuta.FINALIZADA);
            ruta4.setCantidadTramos(1);
            ruta4.setCantidadDepositos(0);
            ruta4.setCostoFinal(48500.00); // Costo real calculado
            ruta4.setObservaciones("Ruta completada exitosamente - Córdoba → Buenos Aires");
            ruta4.setCreatedAt(now.minusDays(12));
            rutaRepository.save(ruta4);

            Tramo tramo4_1 = new Tramo();
            tramo4_1.setRuta(ruta4);
            tramo4_1.setOrden(1);
            tramo4_1.setOrigenLatitud(ubicCordoba.getLatitud());
            tramo4_1.setOrigenLongitud(ubicCordoba.getLongitud());
            tramo4_1.setDestinoLatitud(ubicBuenosAires.getLatitud());
            tramo4_1.setDestinoLongitud(ubicBuenosAires.getLongitud());
            tramo4_1.setTipo(Tramo.TipoTramo.ORIGEN_DESTINO);
            tramo4_1.setDistanciaKm(710.0);
            tramo4_1.setEstado(Tramo.EstadoTramo.FINALIZADO);
            tramo4_1.setCamionId(camion1.getId());
            tramo4_1.setCostoAproximado(52000.0);
            tramo4_1.setCostoReal(48500.0); // Mejor que estimado
            tramo4_1.setFechaInicioEstimada(now.minusDays(12));
            tramo4_1.setFechaFinEstimada(now.minusDays(11).plusHours(12));
            tramo4_1.setFechaInicioReal(now.minusDays(12));
            tramo4_1.setFechaFinReal(now.minusDays(11).plusHours(11).plusMinutes(20)); // 11h 20min
            tramo4_1.setCreatedAt(now.minusDays(12));
            tramoRepository.save(tramo4_1);

            // RUTA 5: COMPLETADA - Solicitud 5 (entregada hace 5 días, con demoras)
            Ruta ruta5 = new Ruta();
            ruta5.setEstado(Ruta.EstadoRuta.FINALIZADA);
            ruta5.setCantidadTramos(2);
            ruta5.setCantidadDepositos(1);
            ruta5.setCostoFinal(108500.00); // Costo real con penalización
            ruta5.setObservaciones("Ruta completada con demoras - Buenos Aires → Córdoba → Mendoza");
            ruta5.setCreatedAt(now.minusDays(18));
            rutaRepository.save(ruta5);

            Tramo tramo5_1 = new Tramo();
            tramo5_1.setRuta(ruta5);
            tramo5_1.setOrden(1);
            tramo5_1.setDestinoDepositoId(depositoCordoba.getId());
            tramo5_1.setOrigenLatitud(ubicBuenosAires.getLatitud());
            tramo5_1.setOrigenLongitud(ubicBuenosAires.getLongitud());
            tramo5_1.setDestinoLatitud(ubicCordoba.getLatitud());
            tramo5_1.setDestinoLongitud(ubicCordoba.getLongitud());
            tramo5_1.setTipo(Tramo.TipoTramo.ORIGEN_DEPOSITO);
            tramo5_1.setDistanciaKm(710.0);
            tramo5_1.setEstado(Tramo.EstadoTramo.FINALIZADO);
            tramo5_1.setCamionId(camion2.getId());
            tramo5_1.setCostoAproximado(48000.0);
            tramo5_1.setCostoReal(52000.0);
            tramo5_1.setFechaInicioEstimada(now.minusDays(18));
            tramo5_1.setFechaFinEstimada(now.minusDays(17).plusHours(12));
            tramo5_1.setFechaInicioReal(now.minusDays(18));
            tramo5_1.setFechaFinReal(now.minusDays(17).plusHours(14)); // 14h (2h más lento)
            tramo5_1.setCreatedAt(now.minusDays(18));
            tramoRepository.save(tramo5_1);

            Tramo tramo5_2 = new Tramo();
            tramo5_2.setRuta(ruta5);
            tramo5_2.setOrden(2);
            tramo5_2.setOrigenDepositoId(depositoCordoba.getId());
            tramo5_2.setOrigenLatitud(ubicCordoba.getLatitud());
            tramo5_2.setOrigenLongitud(ubicCordoba.getLongitud());
            tramo5_2.setDestinoLatitud(ubicMendoza.getLatitud());
            tramo5_2.setDestinoLongitud(ubicMendoza.getLongitud());
            tramo5_2.setTipo(Tramo.TipoTramo.DEPOSITO_DESTINO);
            tramo5_2.setDistanciaKm(608.0);
            tramo5_2.setEstado(Tramo.EstadoTramo.FINALIZADO);
            tramo5_2.setCamionId(camion2.getId());
            tramo5_2.setCostoAproximado(47000.0);
            tramo5_2.setCostoReal(56500.0); // Más caro por demoras
            tramo5_2.setFechaInicioEstimada(now.minusDays(17).plusHours(12));
            tramo5_2.setFechaFinEstimada(now.minusDays(17).plusHours(20));
            tramo5_2.setFechaInicioReal(now.minusDays(16).plusHours(14)); // Inicia 1 día después (24h de estadía)
            tramo5_2.setFechaFinReal(now.minusDays(15).plusHours(23)); // 9h + 1 día
            tramo5_2.setCreatedAt(now.minusDays(18));
            tramoRepository.save(tramo5_2);

            // RUTA 6: COMPLETADA - Solicitud 6 (entregada hace 7 días, perfecta)
            Ruta ruta6 = new Ruta();
            ruta6.setEstado(Ruta.EstadoRuta.FINALIZADA);
            ruta6.setCantidadTramos(1);
            ruta6.setCantidadDepositos(0);
            ruta6.setCostoFinal(42000.00); // Exacto como estimado
            ruta6.setObservaciones("Ruta completada perfectamente - Córdoba → Mendoza");
            ruta6.setCreatedAt(now.minusDays(8));
            rutaRepository.save(ruta6);

            Tramo tramo6_1 = new Tramo();
            tramo6_1.setRuta(ruta6);
            tramo6_1.setOrden(1);
            tramo6_1.setOrigenLatitud(ubicCordoba.getLatitud());
            tramo6_1.setOrigenLongitud(ubicCordoba.getLongitud());
            tramo6_1.setDestinoLatitud(ubicMendoza.getLatitud());
            tramo6_1.setDestinoLongitud(ubicMendoza.getLongitud());
            tramo6_1.setTipo(Tramo.TipoTramo.ORIGEN_DESTINO);
            tramo6_1.setDistanciaKm(608.0);
            tramo6_1.setEstado(Tramo.EstadoTramo.FINALIZADO);
            tramo6_1.setCamionId(camion3.getId());
            tramo6_1.setCostoAproximado(42000.0);
            tramo6_1.setCostoReal(42000.0); // Perfecto
            tramo6_1.setFechaInicioEstimada(now.minusDays(8));
            tramo6_1.setFechaFinEstimada(now.minusDays(8).plusHours(10));
            tramo6_1.setFechaInicioReal(now.minusDays(8));
            tramo6_1.setFechaFinReal(now.minusDays(8).plusHours(10)); // Exacto 10h
            tramo6_1.setCreatedAt(now.minusDays(8));
            tramoRepository.save(tramo6_1);

            log.info("✓ Cargadas {} rutas con {} tramos", rutaRepository.count(), tramoRepository.count());
            log.info("📊 RESUMEN DE DATOS:");
            log.info("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  🚚 Camiones: {}", camionRepository.count());
            log.info("  📍 Ubicaciones: {}", ubicacionRepository.count());
            log.info("  🏢 Depósitos: {}", depositoRepository.count());
            log.info("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  📦 RUTAS EN PROGRESO:");
            log.info("     • Ruta 1: Contenedor esperando en Depósito Rosario (3 días)");
            log.info("     • Ruta 2: Contenedor esperando en Depósito Mendoza (7 días)");
            log.info("     • Ruta 3: Ruta directa en planificación");
            log.info("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log.info("  ✅ RUTAS COMPLETADAS (para probar RF8 - Costo Real):");
            log.info("     • Ruta 4: Finalizada hace 2 días - Costo $48,500 (eficiente)");
            log.info("     • Ruta 5: Finalizada hace 5 días - Costo $108,500 (con demoras)");
            log.info("     • Ruta 6: Finalizada hace 7 días - Costo $42,000 (perfecta)");
            log.info("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            log.info("========================================");
            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("   Usar GET /rutas/4/costo-real");
            log.info("   Usar GET /rutas/5/costo-real");
            log.info("   Usar GET /rutas/6/costo-real");
            log.info("========================================");
        };
    }
}
