package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.clients.GeoApiClient;
import ar.edu.tpi.logistica.clients.SeguimientoServiceClient;
import ar.edu.tpi.logistica.config.MetricasLogistica;
import ar.edu.tpi.logistica.dto.*;
import ar.edu.tpi.logistica.entities.*;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.exceptions.ServicioExternoException;
import ar.edu.tpi.logistica.repositories.*;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CostoService {

    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;
    private final CamionRepository camionRepository;
    private final DepositoRepository depositoRepository;
    private final TarifaVolumenRepository tarifaVolumenRepository;
    private final ConfiguracionGeneralRepository configuracionGeneralRepository;
    private final UbicacionRepository ubicacionRepository;
    private final GeoApiClient geoApiClient;
    private final SeguimientoServiceClient seguimientoServiceClient;
    private final MetricasLogistica metricas;

    private static final String CLAVE_VALOR_LITRO_COMBUSTIBLE = "VALOR_LITRO_COMBUSTIBLE";
    private static final String CLAVE_CARGO_GESTION_POR_TRAMO = "CARGO_GESTION_POR_TRAMO";
    private static final BigDecimal VALOR_LITRO_DEFAULT = new BigDecimal("450.00");
    private static final BigDecimal CARGO_GESTION_DEFAULT = new BigDecimal("5000.00");

    /**
     * Calcula el costo real de una ruta completada
     */
    @Transactional(readOnly = true)
    public CostoRealDTO calcularCostoReal(Long rutaId) {
        String correlationId = MDC.get("correlationId");
        Timer.Sample sample = metricas.iniciarMedicionCostoReal();
        
        log.debug("[CID={}] Calculando costo real para ruta {}", correlationId, rutaId);

        try {
            Ruta ruta = rutaRepository.findById(rutaId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", rutaId));

            List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId);

            if (tramos.isEmpty()) {
                log.warn("[CID={}] La ruta {} no tiene tramos", correlationId, rutaId);
                throw new DatosInvalidosException("La ruta no tiene tramos");
            }

            // Validar que todos los tramos estén finalizados
            boolean todosFinalizados = tramos.stream()
                    .allMatch(t -> Tramo.EstadoTramo.FINALIZADO.equals(t.getEstado()));

            if (!todosFinalizados) {
                long tramosNoFinalizados = tramos.stream()
                        .filter(t -> !Tramo.EstadoTramo.FINALIZADO.equals(t.getEstado()))
                        .count();
                log.warn("[CID={}] La ruta {} tiene {} tramos no finalizados", correlationId, rutaId, tramosNoFinalizados);
                throw new DatosInvalidosException("No todos los tramos están finalizados");
            }

        BigDecimal valorLitroCombustible = obtenerValorLitroCombustible();
        BigDecimal cargoGestionPorTramo = obtenerCargoGestionPorTramo();

        BigDecimal costoTotalKm = BigDecimal.ZERO;
        BigDecimal costoTotalCombustible = BigDecimal.ZERO;
        BigDecimal costoTotalEstadias = BigDecimal.ZERO;
        BigDecimal costoTotalGestion = cargoGestionPorTramo.multiply(new BigDecimal(tramos.size()));

        List<CostoRealDTO.DetalleTramoDTO> detalles = new ArrayList<>();
        LocalDateTime inicioRuta = null;
        LocalDateTime finRuta = null;

        for (int i = 0; i < tramos.size(); i++) {
            Tramo tramo = tramos.get(i);
            
            if (tramo.getCamionId() == null) {
                throw new IllegalStateException("El tramo " + tramo.getId() + " no tiene camión asignado");
            }

            Camion camion = camionRepository.findById(tramo.getCamionId())
                    .orElseThrow(() -> new IllegalStateException("Camión no encontrado: " + tramo.getCamionId()));

            // Calcular costos del tramo
            Double distanciaKm = tramo.getDistanciaKm() != null ? tramo.getDistanciaKm() : 0.0;
            
            // Costo por km del camión
            BigDecimal costoKmTramo = BigDecimal.valueOf(camion.getCostoKmBase())
                    .multiply(BigDecimal.valueOf(distanciaKm))
                    .setScale(2, RoundingMode.HALF_UP);
            
            // Costo de combustible
            BigDecimal consumoLitros = BigDecimal.valueOf(camion.getConsumoPromedioLitrosKm())
                    .multiply(BigDecimal.valueOf(distanciaKm));
            BigDecimal costoCombustibleTramo = consumoLitros
                    .multiply(valorLitroCombustible)
                    .setScale(2, RoundingMode.HALF_UP);

            // Costo de estadía (si aplica)
            BigDecimal costoEstadiaTramo = BigDecimal.ZERO;
            Integer tiempoMinutos = 0;

            if (tramo.getFechaInicioReal() != null && tramo.getFechaFinReal() != null) {
                Duration duracion = Duration.between(tramo.getFechaInicioReal(), tramo.getFechaFinReal());
                tiempoMinutos = (int) duracion.toMinutes();

                if (inicioRuta == null) {
                    inicioRuta = tramo.getFechaInicioReal();
                }
                finRuta = tramo.getFechaFinReal();
            }

            // Si el tramo termina en un depósito y hay un tramo siguiente, calcular estadía
            if (tramo.getDestinoDepositoId() != null && i < tramos.size() - 1) {
                Tramo siguienteTramo = tramos.get(i + 1);
                Deposito deposito = depositoRepository.findById(tramo.getDestinoDepositoId())
                        .orElseThrow(() -> new IllegalStateException("Depósito no encontrado: " + tramo.getDestinoDepositoId()));
                
                if (tramo.getFechaFinReal() != null && siguienteTramo.getFechaInicioReal() != null) {
                    Duration estadia = Duration.between(
                            tramo.getFechaFinReal(),
                            siguienteTramo.getFechaInicioReal()
                    );
                    
                    long diasEstadia = estadia.toDays();
                    BigDecimal costoDiarioEstadia = BigDecimal.valueOf(deposito.getCostoEstadiaDiaria());
                    
                    costoEstadiaTramo = costoDiarioEstadia
                            .multiply(new BigDecimal(diasEstadia))
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    log.info("Estadía en depósito {}: {} días, costo: {}", 
                            deposito.getNombre(), diasEstadia, costoEstadiaTramo);
                }
            }

            costoTotalKm = costoTotalKm.add(costoKmTramo);
            costoTotalCombustible = costoTotalCombustible.add(costoCombustibleTramo);
            costoTotalEstadias = costoTotalEstadias.add(costoEstadiaTramo);

            // Obtener nombres de origen y destino
            String origen = obtenerNombreOrigen(tramo);
            String destino = obtenerNombreDestino(tramo);

            detalles.add(new CostoRealDTO.DetalleTramoDTO(
                    tramo.getId(),
                    origen,
                    destino,
                    BigDecimal.valueOf(distanciaKm),
                    costoKmTramo,
                    costoCombustibleTramo,
                    costoEstadiaTramo,
                    tiempoMinutos
            ));
        }

        BigDecimal costoTotal = costoTotalKm
                .add(costoTotalCombustible)
                .add(costoTotalEstadias)
                .add(costoTotalGestion)
                .setScale(2, RoundingMode.HALF_UP);

            Integer tiempoRealTotal = 0;
            if (inicioRuta != null && finRuta != null) {
                Duration duracionTotal = Duration.between(inicioRuta, finRuta);
                tiempoRealTotal = (int) duracionTotal.toMinutes();
            }

            log.info("[CID={}] Costo real calculado: Total={}, Km={}, Combustible={}, Estadías={}, Gestión={}", 
                    correlationId, costoTotal, costoTotalKm, costoTotalCombustible, costoTotalEstadias, costoTotalGestion);

            CostoRealDTO resultado = new CostoRealDTO(
                    costoTotal,
                    costoTotalKm,
                    costoTotalCombustible,
                    costoTotalEstadias,
                    costoTotalGestion,
                    tiempoRealTotal,
                    detalles
            );
            
            metricas.finalizarMedicionCostoReal(sample);
            return resultado;
            
        } catch (RecursoNoEncontradoException | DatosInvalidosException e) {
            metricas.incrementarErroresCalculoCosto();
            log.error("[CID={}] Error al calcular costo real de ruta {}: {}", correlationId, rutaId, e.getMessage());
            throw e;
        } catch (Exception e) {
            metricas.incrementarErroresCalculoCosto();
            log.error("[CID={}] Error inesperado al calcular costo real de ruta {}: {}", correlationId, rutaId, e.getMessage(), e);
            throw new RuntimeException("Error al calcular costo real", e);
        }
    }

    /**
     * Calcula una ruta tentativa con costos estimados
     */
    @Transactional(readOnly = true)
    public RutaTentativaDTO calcularRutaTentativa(CalcularRutaTentativaRequest request) {
        String correlationId = MDC.get("correlationId");
        Timer.Sample sample = metricas.iniciarMedicionRutaTentativa();
        
        log.debug("[CID={}] Calculando ruta tentativa de ubicación {} a {}", 
                correlationId, request.getUbicacionOrigenId(), request.getUbicacionDestinoId());

        try {
            Ubicacion origen = ubicacionRepository.findById(request.getUbicacionOrigenId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación origen", request.getUbicacionOrigenId()));

            Ubicacion destino = ubicacionRepository.findById(request.getUbicacionDestinoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación destino", request.getUbicacionDestinoId()));

            List<Deposito> depositosIntermedios = new ArrayList<>();
            if (request.getDepositosIntermediosIds() != null) {
                for (Long depositoId : request.getDepositosIntermediosIds()) {
                    Deposito deposito = depositoRepository.findById(depositoId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Depósito", depositoId));
                    depositosIntermedios.add(deposito);
                }
            }

            // Obtener camiones elegibles según capacidad
            List<Camion> camionesElegibles = camionRepository.findAll().stream()
                    .filter(c -> c.getCapacidadPesoKg() >= request.getPesoKg() 
                            && c.getCapacidadVolumenM3() >= request.getVolumenM3())
                    .collect(Collectors.toList());

            if (camionesElegibles.isEmpty()) {
                log.warn("[CID={}] No hay camiones con capacidad suficiente para peso={} kg y volumen={} m³", 
                        correlationId, request.getPesoKg(), request.getVolumenM3());
                throw new DatosInvalidosException("No hay camiones disponibles con capacidad suficiente");
            }
            
            log.debug("[CID={}] Se encontraron {} camiones elegibles", correlationId, camionesElegibles.size());

        // Calcular promedios de camiones elegibles
        double costoKmPromedio = camionesElegibles.stream()
                .mapToDouble(Camion::getCostoKmBase)
                .average()
                .orElse(0.0);

        double consumoPromedioLitrosKm = camionesElegibles.stream()
                .mapToDouble(Camion::getConsumoPromedioLitrosKm)
                .average()
                .orElse(0.0);

        BigDecimal valorLitroCombustible = obtenerValorLitroCombustible();
        BigDecimal cargoGestionPorTramo = obtenerCargoGestionPorTramo();

        // Obtener tarifa por volumen
        TarifaVolumen tarifa = tarifaVolumenRepository.findAll().stream()
                .filter(t -> {
                    BigDecimal volumen = BigDecimal.valueOf(request.getVolumenM3());
                    return volumen.compareTo(t.getVolumenMinM3()) >= 0 
                            && volumen.compareTo(t.getVolumenMaxM3()) <= 0;
                })
                .findFirst()
                .orElse(null);

        BigDecimal tarifaVolumenBase = tarifa != null ? tarifa.getCostoKmBase() : BigDecimal.ZERO;

        // Calcular tramos
        List<RutaTentativaDTO.TramoTentativoDTO> tramos = new ArrayList<>();
        BigDecimal costoTotalEstimado = BigDecimal.ZERO;
        Integer tiempoTotalEstimado = 0;
        BigDecimal distanciaTotal = BigDecimal.ZERO;
        int orden = 1;

        // Primer tramo: origen -> primer depósito o destino
        String coordOrigen = String.format("%s,%s", origen.getLatitud(), origen.getLongitud());
        String coordSiguiente;
        String nombreSiguiente;
        String tipoTramo;

        if (!depositosIntermedios.isEmpty()) {
            Deposito primerDeposito = depositosIntermedios.get(0);
            coordSiguiente = String.format("%s,%s", 
                    primerDeposito.getUbicacion().getLatitud(), 
                    primerDeposito.getUbicacion().getLongitud());
            nombreSiguiente = primerDeposito.getNombre();
            tipoTramo = "ORIGEN_DEPOSITO";
        } else {
            coordSiguiente = String.format("%s,%s", destino.getLatitud(), destino.getLongitud());
            nombreSiguiente = destino.getDireccion();
            tipoTramo = "ORIGEN_DESTINO";
        }

        DistanciaDTO distanciaPrimerTramo = geoApiClient.calcularDistancia(coordOrigen, coordSiguiente);
        if (distanciaPrimerTramo != null) {
            BigDecimal distanciaKm = BigDecimal.valueOf(distanciaPrimerTramo.getKilometros());
            BigDecimal costoTramo = calcularCostoTramo(distanciaKm.doubleValue(), costoKmPromedio, 
                    consumoPromedioLitrosKm, valorLitroCombustible, tarifaVolumenBase, cargoGestionPorTramo);

            Integer duracionMin = distanciaPrimerTramo.getDuracionMinutos() != null ? 
                    distanciaPrimerTramo.getDuracionMinutos() : 
                    (int)(distanciaPrimerTramo.getKilometros() * 1.5); // Estimación: 1.5 min por km

            tramos.add(new RutaTentativaDTO.TramoTentativoDTO(
                    orden++,
                    origen.getDireccion(),
                    nombreSiguiente,
                    distanciaKm,
                    duracionMin,
                    costoTramo,
                    tipoTramo
            ));

            costoTotalEstimado = costoTotalEstimado.add(costoTramo);
            tiempoTotalEstimado += duracionMin;
            distanciaTotal = distanciaTotal.add(distanciaKm);
        }

        // Tramos entre depósitos
        for (int i = 0; i < depositosIntermedios.size(); i++) {
            Deposito depositoActual = depositosIntermedios.get(i);
            String coordActual = String.format("%s,%s", 
                    depositoActual.getUbicacion().getLatitud(), 
                    depositoActual.getUbicacion().getLongitud());

            if (i < depositosIntermedios.size() - 1) {
                // Hay más depósitos
                Deposito depositoSiguiente = depositosIntermedios.get(i + 1);
                String coordSig = String.format("%s,%s", 
                        depositoSiguiente.getUbicacion().getLatitud(), 
                        depositoSiguiente.getUbicacion().getLongitud());

                DistanciaDTO dist = geoApiClient.calcularDistancia(coordActual, coordSig);
                if (dist != null) {
                    BigDecimal distanciaKm = BigDecimal.valueOf(dist.getKilometros());
                    BigDecimal costoTramo = calcularCostoTramo(distanciaKm.doubleValue(), costoKmPromedio, 
                            consumoPromedioLitrosKm, valorLitroCombustible, tarifaVolumenBase, cargoGestionPorTramo);

                    Integer duracionMin = dist.getDuracionMinutos() != null ? 
                            dist.getDuracionMinutos() : 
                            (int)(dist.getKilometros() * 1.5);

                    tramos.add(new RutaTentativaDTO.TramoTentativoDTO(
                            orden++,
                            depositoActual.getNombre(),
                            depositoSiguiente.getNombre(),
                            distanciaKm,
                            duracionMin,
                            costoTramo,
                            "DEPOSITO_DEPOSITO"
                    ));

                    costoTotalEstimado = costoTotalEstimado.add(costoTramo);
                    tiempoTotalEstimado += duracionMin;
                    distanciaTotal = distanciaTotal.add(distanciaKm);
                }
            } else {
                // Último depósito -> destino
                String coordDestino = String.format("%s,%s", destino.getLatitud(), destino.getLongitud());
                DistanciaDTO dist = geoApiClient.calcularDistancia(coordActual, coordDestino);
                if (dist != null) {
                    BigDecimal distanciaKm = BigDecimal.valueOf(dist.getKilometros());
                    BigDecimal costoTramo = calcularCostoTramo(distanciaKm.doubleValue(), costoKmPromedio, 
                            consumoPromedioLitrosKm, valorLitroCombustible, tarifaVolumenBase, cargoGestionPorTramo);

                    Integer duracionMin = dist.getDuracionMinutos() != null ? 
                            dist.getDuracionMinutos() : 
                            (int)(dist.getKilometros() * 1.5);

                    tramos.add(new RutaTentativaDTO.TramoTentativoDTO(
                            orden++,
                            depositoActual.getNombre(),
                            destino.getDireccion(),
                            distanciaKm,
                            duracionMin,
                            costoTramo,
                            "DEPOSITO_DESTINO"
                    ));

                    costoTotalEstimado = costoTotalEstimado.add(costoTramo);
                    tiempoTotalEstimado += duracionMin;
                    distanciaTotal = distanciaTotal.add(distanciaKm);
                }
            }
        }

            log.info("[CID={}] Ruta tentativa calculada: {} tramos, costo total estimado: {}, distancia total: {} km", 
                    correlationId, tramos.size(), costoTotalEstimado, distanciaTotal);

            RutaTentativaDTO resultado = new RutaTentativaDTO(
                    tramos,
                    costoTotalEstimado,
                    tiempoTotalEstimado,
                    tramos.size(),
                    depositosIntermedios.size(),
                    distanciaTotal
            );
            
            metricas.finalizarMedicionRutaTentativa(sample);
            return resultado;
            
        } catch (RecursoNoEncontradoException | DatosInvalidosException e) {
            log.error("[CID={}] Error al calcular ruta tentativa: {}", correlationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[CID={}] Error inesperado al calcular ruta tentativa: {}", correlationId, e.getMessage(), e);
            throw new ServicioExternoException("GeoAPI", "Error al calcular ruta tentativa: " + e.getMessage());
        }
    }

    private BigDecimal calcularCostoTramo(double distanciaKm, double costoKmPromedio,
                                          double consumoPromedioLitrosKm, BigDecimal valorLitroCombustible,
                                          BigDecimal tarifaVolumenBase, BigDecimal cargoGestion) {
        BigDecimal costoKm = BigDecimal.valueOf(distanciaKm * costoKmPromedio);
        BigDecimal costoCombustible = BigDecimal.valueOf(distanciaKm * consumoPromedioLitrosKm)
                .multiply(valorLitroCombustible);
        BigDecimal costoTarifaVolumen = BigDecimal.valueOf(distanciaKm).multiply(tarifaVolumenBase);
        
        return costoKm.add(costoCombustible).add(costoTarifaVolumen).add(cargoGestion)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String obtenerNombreOrigen(Tramo tramo) {
        if (tramo.getOrigenDepositoId() != null) {
            return depositoRepository.findById(tramo.getOrigenDepositoId())
                    .map(Deposito::getNombre)
                    .orElse("Depósito desconocido");
        } else {
            return String.format("Ubicación (%.4f, %.4f)", tramo.getOrigenLatitud(), tramo.getOrigenLongitud());
        }
    }

    private String obtenerNombreDestino(Tramo tramo) {
        if (tramo.getDestinoDepositoId() != null) {
            return depositoRepository.findById(tramo.getDestinoDepositoId())
                    .map(Deposito::getNombre)
                    .orElse("Depósito desconocido");
        } else {
            return String.format("Ubicación (%.4f, %.4f)", tramo.getDestinoLatitud(), tramo.getDestinoLongitud());
        }
    }

    private BigDecimal obtenerValorLitroCombustible() {
        return configuracionGeneralRepository.findByClave(CLAVE_VALOR_LITRO_COMBUSTIBLE)
                .map(config -> new BigDecimal(config.getValor()))
                .orElse(VALOR_LITRO_DEFAULT);
    }

    private BigDecimal obtenerCargoGestionPorTramo() {
        return configuracionGeneralRepository.findByClave(CLAVE_CARGO_GESTION_POR_TRAMO)
                .map(config -> new BigDecimal(config.getValor()))
                .orElse(CARGO_GESTION_DEFAULT);
    }
}

