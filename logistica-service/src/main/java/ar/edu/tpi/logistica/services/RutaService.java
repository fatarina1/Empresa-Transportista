package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.clients.SeguimientoServiceClient;
import ar.edu.tpi.logistica.config.MetricasLogistica;
import ar.edu.tpi.logistica.dto.*;
import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.entities.Ruta;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.entities.Ubicacion;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.repositories.RutaRepository;
import ar.edu.tpi.logistica.repositories.TramoRepository;
import ar.edu.tpi.logistica.repositories.UbicacionRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private MetricasLogistica metricas;

    @Autowired
    private SeguimientoServiceClient seguimientoServiceClient;

    public List<RutaDTO> obtenerTodas() {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Obteniendo todas las rutas", correlationId);
        
        List<RutaDTO> rutas = rutaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        
        log.debug("[CID={}] Se encontraron {} rutas", correlationId, rutas.size());
        return rutas;
    }

    public Optional<RutaDTO> obtenerPorId(Long id) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Buscando ruta con ID: {}", correlationId, id);
        
        Optional<RutaDTO> resultado = rutaRepository.findById(id)
                .map(this::convertirADTO);
        
        if (resultado.isPresent()) {
            log.debug("[CID={}] Ruta encontrada con ID: {}", correlationId, id);
        } else {
            log.debug("[CID={}] No se encontró ruta con ID: {}", correlationId, id);
        }
        
        return resultado;
    }

    /**
     * Crea una ruta completa con todos sus tramos a partir de una ruta tentativa seleccionada.
     * También vincula la ruta con la solicitud correspondiente.
     * 
     * @param solicitudId ID de la solicitud en seguimiento-service
     * @param rutaTentativa Ruta tentativa seleccionada (de las alternativas calculadas)
     * @param observaciones Notas adicionales
     * @return RutaDTO con los tramos creados
     */
    @Transactional
    public RutaDTO crearRutaConTramos(Long solicitudId, RutaTentativaDTO rutaTentativa, String observaciones) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Creando ruta para solicitud {} con {} tramos", 
                correlationId, solicitudId, rutaTentativa.getCantidadTramos());

        if (rutaTentativa.getTramos() == null || rutaTentativa.getTramos().isEmpty()) {
            throw new DatosInvalidosException("La ruta tentativa debe tener al menos un tramo");
        }

        // 1. Crear la ruta
        Ruta ruta = new Ruta();
        
        // Si la ruta se crea para una solicitud específica, está ASIGNADA directamente
        // Si NO tiene solicitudId, es una ruta PROPUESTA (exploración, "qué pasaría si")
        ruta.setEstado(solicitudId != null ? Ruta.EstadoRuta.ASIGNADA : Ruta.EstadoRuta.PROPUESTA);
        
        ruta.setCantidadTramos(rutaTentativa.getCantidadTramos());
        ruta.setCantidadDepositos(rutaTentativa.getCantidadDepositos());
        ruta.setObservaciones(observaciones);
        ruta.setCreatedAt(LocalDateTime.now());
        
        Ruta rutaGuardada = rutaRepository.save(ruta);
        log.debug("[CID={}] Ruta creada con ID: {}", correlationId, rutaGuardada.getId());

        // 2. Crear los tramos asociados con fechas estimadas
        List<Tramo> tramosCreados = new ArrayList<>();
        LocalDateTime fechaActual = LocalDateTime.now();
        LocalDateTime fechaInicioTramo = fechaActual.plusHours(2); // La ruta inicia 2 horas después de ser creada
        
        for (int i = 0; i < rutaTentativa.getTramos().size(); i++) {
            RutaTentativaDTO.TramoTentativoDTO tramoTentativo = rutaTentativa.getTramos().get(i);
            
            Tramo tramo = new Tramo();
            tramo.setRuta(rutaGuardada); // Relación ManyToOne
            tramo.setOrden(tramoTentativo.getOrden());
            tramo.setDistanciaKm(tramoTentativo.getDistanciaKm().doubleValue());
            tramo.setCostoAproximado(tramoTentativo.getCostoEstimado().doubleValue());
            tramo.setEstado(Tramo.EstadoTramo.ESTIMADO);
            
            // Mapear tipo de tramo
            if (tramoTentativo.getTipo() != null) {
                tramo.setTipo(Tramo.TipoTramo.valueOf(tramoTentativo.getTipo()));
            }
            
            // Calcular fechas estimadas basadas en la duración del tramo
            tramo.setFechaInicioEstimada(fechaInicioTramo);
            
            Integer duracionMinutos = tramoTentativo.getTiempoEstimadoMinutos() != null 
                    ? tramoTentativo.getTiempoEstimadoMinutos() 
                    : (int)(tramoTentativo.getDistanciaKm().doubleValue() * 1.5); // Fallback: 1.5 min/km
            
            LocalDateTime fechaFinTramo = fechaInicioTramo.plusMinutes(duracionMinutos);
            tramo.setFechaFinEstimada(fechaFinTramo);
            
            // Guardar coordenadas de origen y destino
            if (tramoTentativo.getOrigenLatitud() != null && tramoTentativo.getOrigenLongitud() != null) {
                tramo.setOrigenLatitud(tramoTentativo.getOrigenLatitud().doubleValue());
                tramo.setOrigenLongitud(tramoTentativo.getOrigenLongitud().doubleValue());
            }
            
            if (tramoTentativo.getDestinoLatitud() != null && tramoTentativo.getDestinoLongitud() != null) {
                tramo.setDestinoLatitud(tramoTentativo.getDestinoLatitud().doubleValue());
                tramo.setDestinoLongitud(tramoTentativo.getDestinoLongitud().doubleValue());
            }
            
            tramo.setCreatedAt(LocalDateTime.now());
            
            Tramo tramoGuardado = tramoRepository.save(tramo);
            tramosCreados.add(tramoGuardado);
            
            log.debug("[CID={}] Tramo {} creado: {} → {} ({} km, {} min estimados, inicio: {}, fin: {})", 
                    correlationId, tramoGuardado.getOrden(), 
                    tramoTentativo.getOrigenNombre(), tramoTentativo.getDestinoNombre(),
                    tramoTentativo.getDistanciaKm(), duracionMinutos,
                    fechaInicioTramo, fechaFinTramo);
            
            // Calcular cuándo inicia el siguiente tramo (considerando estadía en depósitos)
            fechaInicioTramo = fechaFinTramo;
            
            // Si este tramo termina en un depósito Y hay un tramo siguiente, agregar tiempo de estadía estimado
            boolean terminaEnDeposito = "ORIGEN_DEPOSITO".equals(tramoTentativo.getTipo()) 
                    || "DEPOSITO_DEPOSITO".equals(tramoTentativo.getTipo());
            boolean hayTramoSiguiente = i < rutaTentativa.getTramos().size() - 1;
            
            if (terminaEnDeposito && hayTramoSiguiente) {
                // Tiempo de estadía estimado: 24 horas por defecto (puede configurarse)
                int horasEstadiaEstimada = 24;
                fechaInicioTramo = fechaInicioTramo.plusHours(horasEstadiaEstimada);
                
                log.debug("[CID={}] Tramo {} termina en depósito - Agregando {} horas de estadía estimada. Siguiente tramo inicia: {}", 
                        correlationId, tramoGuardado.getOrden(), horasEstadiaEstimada, fechaInicioTramo);
            }
        }

        metricas.incrementarRutasCreadas();
        
        log.info("[CID={}] Ruta {} creada con {} tramos exitosamente", 
                correlationId, rutaGuardada.getId(), tramosCreados.size());

        // 3. Vincular la ruta con la solicitud en seguimiento-service y actualizar costos/tiempos estimados
        if (solicitudId != null) {
            try {
                seguimientoServiceClient.asignarRutaASolicitud(solicitudId, rutaGuardada.getId());
                log.info("[CID={}] Solicitud {} vinculada con ruta {}", 
                        correlationId, solicitudId, rutaGuardada.getId());
                
                // Actualizar costos y tiempos estimados en la solicitud
                try {
                    seguimientoServiceClient.actualizarCostosYTiempoEstimados(
                            solicitudId, 
                            rutaTentativa.getCostoEstimadoTotal(), 
                            rutaTentativa.getTiempoEstimadoTotalMinutos());
                    log.info("[CID={}] Costos y tiempos estimados actualizados en solicitud {}: ${} - {} min", 
                            correlationId, solicitudId, 
                            rutaTentativa.getCostoEstimadoTotal(), 
                            rutaTentativa.getTiempoEstimadoTotalMinutos());
                } catch (Exception e) {
                    log.warn("[CID={}] Error al actualizar costos/tiempos en solicitud {}: {}", 
                            correlationId, solicitudId, e.getMessage());
                }
            } catch (Exception e) {
                log.error("[CID={}] Error al vincular solicitud {} con ruta {}: {}", 
                        correlationId, solicitudId, rutaGuardada.getId(), e.getMessage());
                // No lanzamos excepción para no romper la transacción
                // La ruta se creó correctamente, el vínculo se puede hacer manualmente después
            }
        } else {
            log.warn("[CID={}] Ruta {} creada sin solicitudId - No se pudo vincular con solicitud", 
                    correlationId, rutaGuardada.getId());
        }

        // 4. Retornar la ruta con sus tramos
        RutaDTO rutaDTO = convertirADTO(rutaGuardada);
        rutaDTO.setTramos(tramosCreados.stream()
                .map(this::convertirTramoADTO)
                .collect(Collectors.toList()));
        
        return rutaDTO;
    }

    private TramoDTO convertirTramoADTO(Tramo tramo) {
        TramoDTO dto = new TramoDTO();
        dto.setId(tramo.getId());
        dto.setRutaId(tramo.getRuta() != null ? tramo.getRuta().getId() : null);
        dto.setOrden(tramo.getOrden());
        dto.setOrigenDepositoId(tramo.getOrigenDepositoId());
        dto.setDestinoDepositoId(tramo.getDestinoDepositoId());
        dto.setOrigenLatitud(tramo.getOrigenLatitud());
        dto.setOrigenLongitud(tramo.getOrigenLongitud());
        dto.setDestinoLatitud(tramo.getDestinoLatitud());
        dto.setDestinoLongitud(tramo.getDestinoLongitud());
        dto.setCamionId(tramo.getCamionId());
        dto.setEstado(tramo.getEstado() != null ? tramo.getEstado().name() : null);
        dto.setTipo(tramo.getTipo() != null ? tramo.getTipo().name() : null);
        dto.setDistanciaKm(tramo.getDistanciaKm());
        dto.setCostoAproximado(tramo.getCostoAproximado() != null ? BigDecimal.valueOf(tramo.getCostoAproximado()) : null);
        dto.setCostoReal(tramo.getCostoReal() != null ? BigDecimal.valueOf(tramo.getCostoReal()) : null);
        dto.setFechaInicioEstimada(tramo.getFechaInicioEstimada());
        dto.setFechaFinEstimada(tramo.getFechaFinEstimada());
        dto.setFechaInicioReal(tramo.getFechaInicioReal());
        dto.setFechaFinReal(tramo.getFechaFinReal());
        dto.setCreatedAt(tramo.getCreatedAt());
        return dto;
    }

    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Cambiando estado de ruta {} a: {}", correlationId, id, nuevoEstado);
        
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", id));

        try {
            Ruta.EstadoRuta estado = Ruta.EstadoRuta.valueOf(nuevoEstado.toUpperCase());
            Ruta.EstadoRuta estadoAnterior = ruta.getEstado();
            ruta.setEstado(estado);
            rutaRepository.save(ruta);
            
            // Incrementar métrica si se finaliza la ruta
            if (estado == Ruta.EstadoRuta.FINALIZADA) {
                metricas.incrementarRutasFinalizadas();
                log.info("[CID={}] Ruta {} finalizada (estado: {} -> {})", correlationId, id, estadoAnterior, estado);
            } else {
                log.info("[CID={}] Estado de ruta {} cambiado de {} a {}", correlationId, id, estadoAnterior, estado);
            }
        } catch (IllegalArgumentException e) {
            log.warn("[CID={}] Estado inválido proporcionado: {}", correlationId, nuevoEstado);
            throw new DatosInvalidosException("Estado inválido: " + nuevoEstado);
        }
    }

    /**
     * Obtiene información de tracking en tiempo real para una ruta.
     * Calcula progreso, ubicación actual, ETA y estado de cada tramo.
     */
    public TrackingInfoDTO obtenerTrackingInfo(Long rutaId) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Obteniendo tracking info para ruta: {}", correlationId, rutaId);

        // Buscar la ruta
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", rutaId));

        // Buscar todos los tramos de la ruta ordenados por orden
        List<Tramo> tramos = tramoRepository.findAll().stream()
                .filter(t -> t.getRuta() != null && t.getRuta().getId().equals(rutaId))
                .sorted((t1, t2) -> Integer.compare(t1.getOrden(), t2.getOrden()))
                .collect(Collectors.toList());

        if (tramos.isEmpty()) {
            log.warn("[CID={}] Ruta {} no tiene tramos asignados", correlationId, rutaId);
        }

        TrackingInfoDTO trackingInfo = new TrackingInfoDTO();
        trackingInfo.setRutaId(rutaId);
        trackingInfo.setEstadoRuta(ruta.getEstado() != null ? ruta.getEstado().name() : "PROPUESTA");

        // Determinar ubicación actual (última ubicación de destino de tramos finalizados)
        UbicacionDTO ubicacionActual = null;
        Tramo tramoActual = tramos.stream()
                .filter(t -> t.getEstado() == Tramo.EstadoTramo.INICIADO)
                .findFirst()
                .orElse(null);

        if (tramoActual != null) {
            // Si hay un tramo en curso, la ubicación actual es su origen
            ubicacionActual = crearUbicacionDesdeTramo(tramoActual, true);
        } else {
            // Si no hay tramo en curso, buscar el último tramo finalizado
            Optional<Tramo> ultimoFinalizado = tramos.stream()
                    .filter(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO)
                    .max((t1, t2) -> t1.getOrden().compareTo(t2.getOrden()));
            
            if (ultimoFinalizado.isPresent()) {
                ubicacionActual = crearUbicacionDesdeTramo(ultimoFinalizado.get(), false);
            } else if (!tramos.isEmpty()) {
                // Si ningún tramo ha iniciado, ubicación es el origen del primer tramo
                ubicacionActual = crearUbicacionDesdeTramo(tramos.get(0), true);
            }
        }
        trackingInfo.setUbicacionActual(ubicacionActual);

        // Convertir tramos a DTO de tracking
        List<TramoDTO> tramosTracking = tramos.stream()
                .map(this::convertirTramoADTO)
                .collect(Collectors.toList());
        trackingInfo.setTramos(tramosTracking);

        log.info("[CID={}] Tracking info obtenido: ruta={}, tramos={}, ubicacionActual={}", 
                correlationId, rutaId, tramos.size(), 
                ubicacionActual != null ? ubicacionActual.getDireccion() : "N/A");

        return trackingInfo;
    }

    /**
     * Crea un DTO de ubicación desde las coordenadas del tramo.
     * Si el tramo tiene un depósito, intenta obtener la ubicación real de la BD.
     * @param tramo Tramo del cual extraer las coordenadas
     * @param esOrigen true para usar origen, false para usar destino
     */
    private UbicacionDTO crearUbicacionDesdeTramo(Tramo tramo, boolean esOrigen) {
        if (tramo == null) return null;
        
        UbicacionDTO dto = new UbicacionDTO();
        Long depositoId = esOrigen ? tramo.getOrigenDepositoId() : tramo.getDestinoDepositoId();
        
        // Si hay depositoId, intentar obtener la ubicación real de la BD
        if (depositoId != null) {
            Optional<Ubicacion> ubicacionOpt = ubicacionRepository.findById(depositoId);
            if (ubicacionOpt.isPresent()) {
                Ubicacion u = ubicacionOpt.get();
                dto.setId(u.getId());
                dto.setNombre(u.getNombre());
                dto.setDireccion(u.getDireccion());
                dto.setLatitud(u.getLatitud());
                dto.setLongitud(u.getLongitud());
                dto.setTipo(u.getTipo());
                return dto;
            }
        }
        
        // Si no hay depósito o no se encontró, usar coordenadas del tramo
        if (esOrigen) {
            dto.setLatitud(tramo.getOrigenLatitud());
            dto.setLongitud(tramo.getOrigenLongitud());
            if (depositoId != null) {
                dto.setId(depositoId);
                dto.setDireccion("Depósito ID: " + depositoId);
                dto.setTipo("DEPOSITO");
            } else {
                dto.setDireccion(String.format("Lat: %.6f, Lon: %.6f", 
                    tramo.getOrigenLatitud(), tramo.getOrigenLongitud()));
            }
        } else {
            dto.setLatitud(tramo.getDestinoLatitud());
            dto.setLongitud(tramo.getDestinoLongitud());
            if (depositoId != null) {
                dto.setId(depositoId);
                dto.setDireccion("Depósito ID: " + depositoId);
                dto.setTipo("DEPOSITO");
            } else {
                dto.setDireccion(String.format("Lat: %.6f, Lon: %.6f", 
                    tramo.getDestinoLatitud(), tramo.getDestinoLongitud()));
            }
        }
        
        return dto;
    }

    private RutaDTO convertirADTO(Ruta ruta) {
        RutaDTO dto = new RutaDTO();
        dto.setId(ruta.getId());
        dto.setEstado(ruta.getEstado() != null ? ruta.getEstado().name() : null);
        dto.setCantidadTramos(ruta.getCantidadTramos());
        dto.setCantidadDepositos(ruta.getCantidadDepositos());
        dto.setObservaciones(ruta.getObservaciones());
        dto.setCreatedAt(ruta.getCreatedAt());
        return dto;
    }
}
