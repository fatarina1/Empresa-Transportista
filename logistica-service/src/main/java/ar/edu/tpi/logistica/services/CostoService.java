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
                log.debug("[CID={}] Tiempo real calculado: {} min (desde {} hasta {})", 
                        correlationId, tiempoRealTotal, inicioRuta, finRuta);
            } else {
                log.warn("[CID={}] No se pudo calcular tiempo real - inicioRuta: {}, finRuta: {}", 
                        correlationId, inicioRuta, finRuta);
            }

            log.info("[CID={}] Costo real calculado: Total={}, Km={}, Combustible={}, Estadías={}, Gestión={}, TiempoReal={}min", 
                    correlationId, costoTotal, costoTotalKm, costoTotalCombustible, costoTotalEstadias, costoTotalGestion, tiempoRealTotal);

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
     * Calcula 2 rutas alternativas con costos estimados:
     * 1. Ruta directa (origen → destino, sin depósitos intermedios)
     * 2. Ruta con depósitos intermedios (selección automática inteligente)
     */
    @Transactional(readOnly = true)
    public RutasAlternativasDTO calcularRutaTentativa(CalcularRutaTentativaRequest request) {
        String correlationId = MDC.get("correlationId");
        Timer.Sample sample = metricas.iniciarMedicionRutaTentativa();
        
        log.debug("[CID={}] Calculando rutas alternativas de ubicación {} a {}", 
                correlationId, request.getUbicacionOrigenId(), request.getUbicacionDestinoId());

        try {
            Ubicacion origen = ubicacionRepository.findById(request.getUbicacionOrigenId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación origen", request.getUbicacionOrigenId()));

            Ubicacion destino = ubicacionRepository.findById(request.getUbicacionDestinoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Ubicación destino", request.getUbicacionDestinoId()));

            // Obtener todos los depósitos activos
            List<Deposito> depositosDisponibles = depositoRepository.findAll().stream()
                    .filter(Deposito::getActivo)
                    .collect(Collectors.toList());

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

            List<RutaTentativaDTO> alternativas = new ArrayList<>();
            
            // ALTERNATIVA 1: Ruta directa (sin depósitos intermedios)
            log.debug("[CID={}] Calculando ruta directa (sin depósitos)", correlationId);
            RutaTentativaDTO rutaDirecta = calcularRutaConDepositos(
                    origen, destino, new ArrayList<>(), camionesElegibles, request, 
                    "Ruta directa sin paradas intermedias");
            alternativas.add(rutaDirecta);
            
            // ALTERNATIVA 2: Ruta con depósitos intermedios (selección automática)
            if (!depositosDisponibles.isEmpty()) {
                log.debug("[CID={}] Seleccionando depósitos intermedios de forma automática", correlationId);
                List<Deposito> depositosSeleccionados = seleccionarDepositosIntermedios(
                        origen, destino, depositosDisponibles);
                
                if (!depositosSeleccionados.isEmpty()) {
                    String descripcion = depositosSeleccionados.size() == 1 
                            ? "Ruta con parada en " + depositosSeleccionados.get(0).getNombre()
                            : "Ruta con paradas en " + depositosSeleccionados.stream()
                                    .map(Deposito::getNombre)
                                    .collect(Collectors.joining(", "));
                    
                    RutaTentativaDTO rutaConDepositos = calcularRutaConDepositos(
                            origen, destino, depositosSeleccionados, camionesElegibles, request, descripcion);
                    alternativas.add(rutaConDepositos);
                    
                    log.info("[CID={}] Depósitos seleccionados: {}", correlationId, 
                            depositosSeleccionados.stream().map(Deposito::getNombre).collect(Collectors.joining(", ")));
                } else {
                    log.warn("[CID={}] No se pudieron seleccionar depósitos intermedios", correlationId);
                }
            }
            
            log.info("[CID={}] Generadas {} rutas alternativas", correlationId, alternativas.size());
            
            RutasAlternativasDTO resultado = new RutasAlternativasDTO(
                    alternativas,
                    alternativas.size(),
                    alternativas.size() == 2 
                            ? "Ruta directa vs ruta con depósitos intermedios seleccionados automáticamente"
                            : "Solo ruta directa disponible (sin depósitos activos)"
            );
            
            metricas.finalizarMedicionRutaTentativa(sample);
            return resultado;
            
        } catch (RecursoNoEncontradoException | DatosInvalidosException e) {
            log.error("[CID={}] Error al calcular rutas alternativas: {}", correlationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[CID={}] Error inesperado al calcular rutas alternativas: {}", correlationId, e.getMessage(), e);
            throw new ServicioExternoException("GeoAPI", "Error al calcular rutas alternativas: " + e.getMessage());
        }
    }
    
    /**
     * Selecciona automáticamente depósitos intermedios óptimos entre origen y destino.
     * Estrategia: Selecciona el depósito más cercano al punto medio de la ruta.
     * Si hay muchos depósitos disponibles, puede seleccionar hasta 2 depósitos estratégicos.
     */
    private List<Deposito> seleccionarDepositosIntermedios(Ubicacion origen, Ubicacion destino, 
                                                            List<Deposito> depositosDisponibles) {
        List<Deposito> seleccionados = new ArrayList<>();
        
        if (depositosDisponibles.isEmpty()) {
            return seleccionados;
        }
        
        // Calcular punto medio entre origen y destino
        double latMedio = (origen.getLatitud() + destino.getLatitud()) / 2.0;
        double lngMedio = (origen.getLongitud() + destino.getLongitud()) / 2.0;
        
        // Encontrar el depósito más cercano al punto medio
        Deposito depositoMasCercano = depositosDisponibles.stream()
                .min((d1, d2) -> {
                    double dist1 = calcularDistanciaEuclidiana(
                            latMedio, lngMedio,
                            d1.getUbicacion().getLatitud(), d1.getUbicacion().getLongitud());
                    double dist2 = calcularDistanciaEuclidiana(
                            latMedio, lngMedio,
                            d2.getUbicacion().getLatitud(), d2.getUbicacion().getLongitud());
                    return Double.compare(dist1, dist2);
                })
                .orElse(null);
        
        if (depositoMasCercano != null) {
            seleccionados.add(depositoMasCercano);
            
            // Si hay más de 3 depósitos disponibles, intentar agregar un segundo depósito estratégico
            if (depositosDisponibles.size() > 3) {
                // Buscar un depósito entre origen y el primer depósito O entre el primer depósito y destino
                Deposito segundoDeposito = depositosDisponibles.stream()
                        .filter(d -> !d.equals(depositoMasCercano)) // No seleccionar el mismo
                        .min((d1, d2) -> {
                            // Calcular cercanía a la línea origen-destino
                            double dist1Linea = calcularDistanciaAPuntoEnLinea(
                                    d1.getUbicacion().getLatitud(), d1.getUbicacion().getLongitud(),
                                    origen.getLatitud(), origen.getLongitud(),
                                    destino.getLatitud(), destino.getLongitud());
                            double dist2Linea = calcularDistanciaAPuntoEnLinea(
                                    d2.getUbicacion().getLatitud(), d2.getUbicacion().getLongitud(),
                                    origen.getLatitud(), origen.getLongitud(),
                                    destino.getLatitud(), destino.getLongitud());
                            return Double.compare(dist1Linea, dist2Linea);
                        })
                        .orElse(null);
                
                if (segundoDeposito != null) {
                    // Ordenar los depósitos por cercanía al origen para mantener un orden lógico
                    double distOrigen1 = calcularDistanciaEuclidiana(
                            origen.getLatitud(), origen.getLongitud(),
                            depositoMasCercano.getUbicacion().getLatitud(), 
                            depositoMasCercano.getUbicacion().getLongitud());
                    double distOrigen2 = calcularDistanciaEuclidiana(
                            origen.getLatitud(), origen.getLongitud(),
                            segundoDeposito.getUbicacion().getLatitud(), 
                            segundoDeposito.getUbicacion().getLongitud());
                    
                    if (distOrigen2 < distOrigen1) {
                        // Insertar al principio si está más cerca del origen
                        seleccionados.add(0, segundoDeposito);
                    } else {
                        seleccionados.add(segundoDeposito);
                    }
                }
            }
        }
        
        return seleccionados;
    }
    
    /**
     * Calcula la distancia euclidiana entre dos puntos geográficos (aproximada)
     */
    private double calcularDistanciaEuclidiana(double lat1, double lng1, double lat2, double lng2) {
        double diffLat = lat2 - lat1;
        double diffLng = lng2 - lng1;
        return Math.sqrt(diffLat * diffLat + diffLng * diffLng);
    }
    
    /**
     * Calcula la distancia perpendicular de un punto a una línea (origen-destino)
     */
    private double calcularDistanciaAPuntoEnLinea(double puntoLat, double puntoLng,
                                                   double lineaLat1, double lineaLng1,
                                                   double lineaLat2, double lineaLng2) {
        // Fórmula de distancia de punto a línea
        double numerador = Math.abs(
                (lineaLat2 - lineaLat1) * puntoLng - 
                (lineaLng2 - lineaLng1) * puntoLat + 
                lineaLng2 * lineaLat1 - 
                lineaLat2 * lineaLng1);
        double denominador = Math.sqrt(
                Math.pow(lineaLat2 - lineaLat1, 2) + 
                Math.pow(lineaLng2 - lineaLng1, 2));
        
        return denominador == 0 ? Double.MAX_VALUE : numerador / denominador;
    }
    
    /**
     * Calcula una ruta específica con los depósitos dados
     */
    private RutaTentativaDTO calcularRutaConDepositos(Ubicacion origen, Ubicacion destino,
                                                      List<Deposito> depositosIntermedios,
                                                      List<Camion> camionesElegibles,
                                                      CalcularRutaTentativaRequest request,
                                                      String descripcion) {
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
        BigDecimal costoTotalEstadiasEstimado = BigDecimal.ZERO; // Nuevo: costo de estadías en depósitos
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

            // Obtener coordenadas de destino del tramo
            BigDecimal destinoLat, destinoLon;
            if (!depositosIntermedios.isEmpty()) {
                Deposito primerDeposito = depositosIntermedios.get(0);
                destinoLat = BigDecimal.valueOf(primerDeposito.getUbicacion().getLatitud());
                destinoLon = BigDecimal.valueOf(primerDeposito.getUbicacion().getLongitud());
            } else {
                destinoLat = BigDecimal.valueOf(destino.getLatitud());
                destinoLon = BigDecimal.valueOf(destino.getLongitud());
            }

            tramos.add(new RutaTentativaDTO.TramoTentativoDTO(
                    orden++,
                    origen.getDireccion(),
                    nombreSiguiente,
                    BigDecimal.valueOf(origen.getLatitud()),
                    BigDecimal.valueOf(origen.getLongitud()),
                    destinoLat,
                    destinoLon,
                    distanciaKm,
                    duracionMin,
                    costoTramo,
                    tipoTramo
            ));

            costoTotalEstimado = costoTotalEstimado.add(costoTramo);
            tiempoTotalEstimado += duracionMin;
            distanciaTotal = distanciaTotal.add(distanciaKm);
            
            // Agregar tiempo de estadía estimado si termina en depósito y hay más tramos
            if (!depositosIntermedios.isEmpty() && orden <= depositosIntermedios.size()) {
                int horasEstadiaEstimada = 24; // 1 día de estadía estimada
                tiempoTotalEstimado += (horasEstadiaEstimada * 60); // Convertir a minutos
                
                // Calcular costo de estadía
                Deposito depositoDestino = depositosIntermedios.get(orden - 2); // orden-1 para índice, -1 más porque ya incrementamos orden
                BigDecimal costoEstadia = BigDecimal.valueOf(depositoDestino.getCostoEstadiaDiaria())
                        .multiply(BigDecimal.valueOf(horasEstadiaEstimada / 24.0))
                        .setScale(2, RoundingMode.HALF_UP);
                
                costoTotalEstadiasEstimado = costoTotalEstadiasEstimado.add(costoEstadia);
            }
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
                            BigDecimal.valueOf(depositoActual.getUbicacion().getLatitud()),
                            BigDecimal.valueOf(depositoActual.getUbicacion().getLongitud()),
                            BigDecimal.valueOf(depositoSiguiente.getUbicacion().getLatitud()),
                            BigDecimal.valueOf(depositoSiguiente.getUbicacion().getLongitud()),
                            distanciaKm,
                            duracionMin,
                            costoTramo,
                            "DEPOSITO_DEPOSITO"
                    ));

                    costoTotalEstimado = costoTotalEstimado.add(costoTramo);
                    tiempoTotalEstimado += duracionMin;
                    distanciaTotal = distanciaTotal.add(distanciaKm);
                    
                    // Agregar tiempo y costo de estadía en el primer depósito
                    int horasEstadiaEstimada = 24; // 1 día de estadía estimada
                    tiempoTotalEstimado += (horasEstadiaEstimada * 60);
                    
                    BigDecimal costoEstadia = BigDecimal.valueOf(depositoActual.getCostoEstadiaDiaria());
                    costoTotalEstadiasEstimado = costoTotalEstadiasEstimado.add(costoEstadia);
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
                            BigDecimal.valueOf(depositoActual.getUbicacion().getLatitud()),
                            BigDecimal.valueOf(depositoActual.getUbicacion().getLongitud()),
                            BigDecimal.valueOf(destino.getLatitud()),
                            BigDecimal.valueOf(destino.getLongitud()),
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

        // Agregar costo de estadías al costo total
        costoTotalEstimado = costoTotalEstimado.add(costoTotalEstadiasEstimado);
        
        if (costoTotalEstadiasEstimado.compareTo(BigDecimal.ZERO) > 0) {
            log.info("[Ruta con {} depósitos] Costo total de estadías: ${} (24h por depósito)", 
                    depositosIntermedios.size(), costoTotalEstadiasEstimado);
        }

        return new RutaTentativaDTO(
                tramos,
                costoTotalEstimado,
                tiempoTotalEstimado,
                tramos.size(),
                depositosIntermedios.size(),
                distanciaTotal
        );
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

    /**
     * Calcula el costo aproximado de un tramo individual basado en estimaciones.
     * Se usa al asignar el camión al tramo.
     */
    public BigDecimal calcularCostoAproximadoTramo(Tramo tramo) {
        if (tramo.getCamionId() == null) {
            log.warn("No se puede calcular costo aproximado sin camión asignado");
            return BigDecimal.ZERO;
        }

        Camion camion = camionRepository.findById(tramo.getCamionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Camion", tramo.getCamionId()));

        BigDecimal valorLitro = obtenerValorLitroCombustible();
        BigDecimal cargoGestion = obtenerCargoGestionPorTramo();

        // Costo por distancia
        BigDecimal costoKm = BigDecimal.valueOf(tramo.getDistanciaKm() * camion.getCostoKmBase());

        // Costo de combustible
        BigDecimal costoCombustible = BigDecimal.valueOf(tramo.getDistanciaKm() * camion.getConsumoPromedioLitrosKm())
                .multiply(valorLitro);

        BigDecimal costoTotal = costoKm.add(costoCombustible).add(cargoGestion);

        // Si termina en depósito, agregar costo de estadía (24h)
        if (tramo.getDestinoDepositoId() != null) {
            Deposito deposito = depositoRepository.findById(tramo.getDestinoDepositoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Deposito", tramo.getDestinoDepositoId()));
            
            BigDecimal costoEstadia = BigDecimal.valueOf(deposito.getCostoEstadiaDiaria()); // 1 día
            costoTotal = costoTotal.add(costoEstadia);
            
            log.debug("Costo aproximado del tramo {} incluye estadía de 1 día en {}: ${}",
                    tramo.getId(), deposito.getNombre(), costoEstadia);
        }

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el costo real de un tramo individual después de finalizarlo.
     */
    public BigDecimal calcularCostoRealTramo(Tramo tramo) {
        if (tramo.getEstado() != Tramo.EstadoTramo.FINALIZADO) {
            log.warn("No se puede calcular costo real de tramo no finalizado");
            return BigDecimal.ZERO;
        }

        if (tramo.getCamionId() == null) {
            log.warn("No se puede calcular costo real sin camión asignado");
            return BigDecimal.ZERO;
        }

        Camion camion = camionRepository.findById(tramo.getCamionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Camion", tramo.getCamionId()));

        BigDecimal valorLitro = obtenerValorLitroCombustible();
        BigDecimal cargoGestion = obtenerCargoGestionPorTramo();

        // Costo por distancia
        BigDecimal costoKm = BigDecimal.valueOf(tramo.getDistanciaKm() * camion.getCostoKmBase());

        // Costo de combustible
        BigDecimal costoCombustible = BigDecimal.valueOf(tramo.getDistanciaKm() * camion.getConsumoPromedioLitrosKm())
                .multiply(valorLitro);

        BigDecimal costoTotal = costoKm.add(costoCombustible).add(cargoGestion);

        // Si termina en depósito, calcular estadía REAL
        if (tramo.getDestinoDepositoId() != null && tramo.getFechaFinReal() != null) {
            Deposito deposito = depositoRepository.findById(tramo.getDestinoDepositoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Deposito", tramo.getDestinoDepositoId()));

            // Buscar el siguiente tramo de la misma ruta
            List<Tramo> todosLosTramos = tramoRepository.findByRutaIdOrderByOrdenAsc(tramo.getRuta().getId());
            Tramo siguienteTramo = todosLosTramos.stream()
                    .filter(t -> t.getOrden() == tramo.getOrden() + 1)
                    .findFirst()
                    .orElse(null);

            if (siguienteTramo != null && siguienteTramo.getFechaInicioReal() != null) {
                // Calcular estadía real en días
                Duration duracionEstadia = Duration.between(tramo.getFechaFinReal(), siguienteTramo.getFechaInicioReal());
                long diasEstadia = duracionEstadia.toDays();
                if (diasEstadia == 0 && duracionEstadia.toHours() > 0) {
                    diasEstadia = 1; // Mínimo 1 día
                }

                BigDecimal costoEstadia = BigDecimal.valueOf(deposito.getCostoEstadiaDiaria() * diasEstadia);
                costoTotal = costoTotal.add(costoEstadia);

                log.info("Costo real del tramo {} incluye estadía de {} día(s) en {}: ${}",
                        tramo.getId(), diasEstadia, deposito.getNombre(), costoEstadia);
            } else {
                // Si no hay siguiente tramo o no ha iniciado, usar 1 día por defecto
                BigDecimal costoEstadia = BigDecimal.valueOf(deposito.getCostoEstadiaDiaria());
                costoTotal = costoTotal.add(costoEstadia);

                log.debug("Costo real del tramo {} incluye estadía estimada de 1 día en {}: ${}",
                        tramo.getId(), deposito.getNombre(), costoEstadia);
            }
        }

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }
}
