package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.clients.GeoApiClient;
import ar.edu.tpi.logistica.clients.SeguimientoServiceClient;
import ar.edu.tpi.logistica.config.MetricasLogistica;
import ar.edu.tpi.logistica.dto.CostoRealDTO;
import ar.edu.tpi.logistica.dto.DistanciaDTO;
import ar.edu.tpi.logistica.dto.TramoDTO;
import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.entities.Ruta;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.exceptions.ReglaNegocioException;
import ar.edu.tpi.logistica.exceptions.ServicioExternoException;
import ar.edu.tpi.logistica.repositories.CamionRepository;
import ar.edu.tpi.logistica.repositories.RutaRepository;
import ar.edu.tpi.logistica.repositories.TramoRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TramoService {

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private GeoApiClient geoApiClient;

    @Autowired
    private CostoService costoService;

    @Autowired
    private SeguimientoServiceClient seguimientoServiceClient;

    @Autowired
    private MetricasLogistica metricas;

    public List<TramoDTO> obtenerTodos() {
        return tramoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<TramoDTO> obtenerPorId(Long id) {
        return tramoRepository.findById(id)
                .map(this::convertirADTO);
    }

    public List<TramoDTO> obtenerPorRuta(Long rutaId) {
        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId);
        return tramos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Alias para compatibilidad con controller
    public List<TramoDTO> obtenerPorRutaId(Long rutaId) {
        return obtenerPorRuta(rutaId);
    }

    public List<TramoDTO> obtenerPorCamion(Long camionId) {
        List<Tramo> tramos = tramoRepository.findByCamionId(camionId);
        return tramos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // Alias para compatibilidad con controller
    public List<TramoDTO> obtenerPorCamionId(Long camionId) {
        return obtenerPorCamion(camionId);
    }

    public List<TramoDTO> obtenerPorEstado(String estado) {
        try {
            Tramo.EstadoTramo estadoEnum = Tramo.EstadoTramo.valueOf(estado.toUpperCase());
            List<Tramo> tramos = tramoRepository.findByEstado(estadoEnum);
            return tramos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + estado);
        }
    }

    // Sobrecarga para aceptar enum directamente desde controller
    public List<TramoDTO> obtenerPorEstado(Tramo.EstadoTramo estado) {
        List<Tramo> tramos = tramoRepository.findByEstado(estado);
        return tramos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TramoDTO crear(TramoDTO tramoDTO) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Creando tramo para ruta: {}", correlationId, tramoDTO.getRutaId());
        
        // Validar que la ruta existe
        Ruta ruta = rutaRepository.findById(tramoDTO.getRutaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", tramoDTO.getRutaId()));

        Tramo tramo = convertirAEntidad(tramoDTO);
        tramo.setRuta(ruta);
        tramo.setCreatedAt(LocalDateTime.now());
        tramo.setEstado(Tramo.EstadoTramo.ESTIMADO);

        // Calcular distancia si tenemos coordenadas
        if (tramo.getOrigenLatitud() != null && tramo.getOrigenLongitud() != null &&
            tramo.getDestinoLatitud() != null && tramo.getDestinoLongitud() != null) {
            try {
                // Formatear coordenadas como strings para el servicio GeoAPI
                String origen = tramo.getOrigenLatitud() + "," + tramo.getOrigenLongitud();
                String destino = tramo.getDestinoLatitud() + "," + tramo.getDestinoLongitud();
                
                DistanciaDTO distancia = geoApiClient.calcularDistancia(origen, destino);
                
                if (distancia != null) {
                    tramo.setDistanciaKm(distancia.getKilometros());
                    log.debug("[CID={}] Distancia calculada: {} km ({})", correlationId, distancia.getKilometros(), distancia.getDuracionTexto());
                }
            } catch (Exception e) {
                log.warn("[CID={}] No se pudo calcular distancia con GeoAPI: {}", correlationId, e.getMessage());
                throw new ServicioExternoException("GeoAPI", "Error al calcular distancia: " + e.getMessage());
            }
        }

        Tramo guardado = tramoRepository.save(tramo);
        log.info("[CID={}] Tramo creado exitosamente con ID: {}", correlationId, guardado.getId());
        return convertirADTO(guardado);
    }

    @Transactional
    public TramoDTO actualizar(Long id, TramoDTO tramoDTO) {
        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        tramo.setOrden(tramoDTO.getOrden());
        tramo.setDistanciaKm(tramoDTO.getDistanciaKm());
        tramo.setCostoAproximado(tramoDTO.getCostoAproximado() != null ? tramoDTO.getCostoAproximado().doubleValue() : null);
        tramo.setCostoReal(tramoDTO.getCostoReal() != null ? tramoDTO.getCostoReal().doubleValue() : null);
        tramo.setFechaInicioEstimada(tramoDTO.getFechaInicioEstimada());
        tramo.setFechaFinEstimada(tramoDTO.getFechaFinEstimada());

        if (tramoDTO.getTipo() != null) {
            tramo.setTipo(Tramo.TipoTramo.valueOf(tramoDTO.getTipo()));
        }

        Tramo actualizado = tramoRepository.save(tramo);
        log.info("Tramo actualizado: {}", id);
        return convertirADTO(actualizado);
    }

    @Transactional
    public TramoDTO asignarCamion(Long tramoId, Long camionId) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Asignando camión {} al tramo {}", correlationId, camionId, tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tramo", tramoId));

        // Verificar que el camión existe
        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Camión", camionId));

        // Validar que el camión está disponible
        if (!camion.getDisponible()) {
            log.warn("[CID={}] Intento de asignar camión no disponible: {}", correlationId, camion.getDominio());
            throw new ReglaNegocioException("El camión " + camion.getDominio() + " no está disponible");
        }

        // 🔥 VALIDACIÓN DE CAPACIDAD: Obtener el contenedor de la solicitud
        Ruta ruta = tramo.getRuta();
        if (ruta != null && ruta.getSolicitudExternaId() != null) {
            try {
                SeguimientoServiceClient.ContenedorDetalleDTO contenedor = 
                        seguimientoServiceClient.obtenerContenedorPorSolicitudId(ruta.getSolicitudExternaId());
                
                if (contenedor != null) {
                    // Validar peso
                    if (camion.getCapacidadPesoKg() < contenedor.getPesoKg().doubleValue()) {
                        String mensaje = String.format(
                                "El camión no tiene capacidad de peso suficiente. Requerido: %.2f kg, Disponible: %.2f kg",
                                contenedor.getPesoKg(), camion.getCapacidadPesoKg());
                        log.warn("[CID={}] {}", correlationId, mensaje);
                        throw new ReglaNegocioException(mensaje);
                    }
                    
                    // Validar volumen
                    if (camion.getCapacidadVolumenM3() < contenedor.getVolumenM3().doubleValue()) {
                        String mensaje = String.format(
                                "El camión no tiene capacidad de volumen suficiente. Requerido: %.2f m³, Disponible: %.2f m³",
                                contenedor.getVolumenM3(), camion.getCapacidadVolumenM3());
                        log.warn("[CID={}] {}", correlationId, mensaje);
                        throw new ReglaNegocioException(mensaje);
                    }
                    
                    log.debug("[CID={}] Validación de capacidad OK - Camión: {} | Contenedor peso: {} kg, volumen: {} m³",
                            correlationId, camion.getDominio(), contenedor.getPesoKg(), contenedor.getVolumenM3());
                }
            } catch (ReglaNegocioException e) {
                throw e; // Re-lanzar excepciones de regla de negocio
            } catch (Exception e) {
                log.warn("[CID={}] No se pudo validar capacidad del contenedor: {}", correlationId, e.getMessage());
                throw new ServicioExternoException("seguimiento-service", "Error al validar capacidad: " + e.getMessage());
            }
        }

        // 🔥 VALIDACIÓN DE DISPONIBILIDAD: Verificar que no esté en otro tramo iniciado
        List<Tramo> tramosEnCurso = tramoRepository.findByCamionId(camionId).stream()
                .filter(t -> t.getEstado() == Tramo.EstadoTramo.INICIADO)
                .collect(Collectors.toList());
        
        if (!tramosEnCurso.isEmpty()) {
            String mensaje = String.format(
                    "El camión %s ya está asignado a otro tramo en curso (ID: %d)",
                    camion.getDominio(), tramosEnCurso.get(0).getId());
            log.warn("[CID={}] {}", correlationId, mensaje);
            throw new ReglaNegocioException(mensaje);
        }

        // Asignar camión
        tramo.setCamionId(camionId);
        tramo.setEstado(Tramo.EstadoTramo.ASIGNADO);

        // Marcar camión como no disponible
        camion.setDisponible(false);
        camionRepository.save(camion);

        Tramo actualizado = tramoRepository.save(tramo);
        log.info("[CID={}] Camión {} asignado exitosamente al tramo {}", correlationId, camion.getDominio(), tramoId);
        
        return convertirADTO(actualizado);
    }

    @Transactional
    public TramoDTO iniciarTramo(Long tramoId) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Iniciando tramo: {}", correlationId, tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tramo", tramoId));

        // Validar que el tramo tiene camión asignado
        if (tramo.getCamionId() == null) {
            log.warn("[CID={}] Intento de iniciar tramo sin camión asignado: {}", correlationId, tramoId);
            throw new DatosInvalidosException("El tramo no tiene camión asignado");
        }

        // Validar que el tramo está en estado ASIGNADO
        if (tramo.getEstado() != Tramo.EstadoTramo.ASIGNADO) {
            log.warn("[CID={}] Intento de iniciar tramo en estado incorrecto: {} (estado actual: {})", 
                    correlationId, tramoId, tramo.getEstado());
            throw new DatosInvalidosException("El tramo debe estar en estado ASIGNADO para poder iniciarse");
        }

        // Registrar inicio
        tramo.setFechaInicioReal(LocalDateTime.now());
        tramo.setEstado(Tramo.EstadoTramo.INICIADO);

        Tramo actualizado = tramoRepository.save(tramo);
        metricas.incrementarTramosIniciados();
        
        log.info("[CID={}] Tramo {} iniciado exitosamente a las {}", correlationId, tramoId, tramo.getFechaInicioReal());

        // 🔥 ACTUALIZAR ESTADO DE SOLICITUD: Si es el primer tramo, cambiar a EN_TRANSITO
        Ruta ruta = tramo.getRuta();
        if (ruta != null && ruta.getSolicitudExternaId() != null) {
            List<Tramo> todosLosTramos = tramoRepository.findByRutaIdOrderByOrdenAsc(ruta.getId());
            Tramo primerTramo = todosLosTramos.isEmpty() ? null : todosLosTramos.get(0);
            
            if (primerTramo != null && primerTramo.getId().equals(tramoId)) {
                seguimientoServiceClient.actualizarEstadoSolicitud(ruta.getSolicitudExternaId(), "EN_TRANSITO");
                log.info("Solicitud {} actualizada a EN_TRANSITO", ruta.getSolicitudExternaId());
            }
        }

        return convertirADTO(actualizado);
    }

    @Transactional
    public TramoDTO finalizarTramo(Long tramoId) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Finalizando tramo: {}", correlationId, tramoId);
        
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tramo", tramoId));

        // Validar que el tramo está en estado INICIADO
        if (tramo.getEstado() != Tramo.EstadoTramo.INICIADO) {
            log.warn("[CID={}] Intento de finalizar tramo en estado incorrecto: {} (estado actual: {})", 
                    correlationId, tramoId, tramo.getEstado());
            throw new DatosInvalidosException("El tramo debe estar en estado INICIADO para poder finalizarse");
        }

        // Validar que tiene fecha de inicio
        if (tramo.getFechaInicioReal() == null) {
            log.warn("[CID={}] Intento de finalizar tramo sin fecha de inicio: {}", correlationId, tramoId);
            throw new DatosInvalidosException("El tramo no tiene fecha de inicio registrada");
        }

        // Registrar fin
        LocalDateTime fechaFin = LocalDateTime.now();
        tramo.setFechaFinReal(fechaFin);
        tramo.setEstado(Tramo.EstadoTramo.FINALIZADO);

        // Calcular duración real (en minutos)
        long duracionMinutos = ChronoUnit.MINUTES.between(tramo.getFechaInicioReal(), fechaFin);
        log.debug("[CID={}] Tramo {} finalizado - Duración: {} minutos", correlationId, tramoId, duracionMinutos);

        // Liberar camión
        if (tramo.getCamionId() != null) {
            camionRepository.findById(tramo.getCamionId()).ifPresent(camion -> {
                camion.setDisponible(true);
                camionRepository.save(camion);
                log.debug("[CID={}] Camión {} liberado", correlationId, camion.getDominio());
            });
        }

        Tramo actualizado = tramoRepository.save(tramo);
        metricas.incrementarTramosFinalizados();

        // 🔥 CALCULAR COSTOS REALES: Si es el último tramo de la ruta
        Ruta ruta = tramo.getRuta();
        if (ruta != null && ruta.getSolicitudExternaId() != null) {
            List<Tramo> todosLosTramos = tramoRepository.findByRutaIdOrderByOrdenAsc(ruta.getId());
            Tramo ultimoTramo = todosLosTramos.isEmpty() ? null : todosLosTramos.get(todosLosTramos.size() - 1);
            
            if (ultimoTramo != null && ultimoTramo.getId().equals(tramoId)) {
                try {
                    log.info("Calculando costo real de la ruta {} (último tramo finalizado)", ruta.getId());
                    CostoRealDTO costoReal = costoService.calcularCostoReal(ruta.getId());
                    
                    // Actualizar ruta con costo final
                    ruta.setCostoFinal(costoReal.getCostoTotal().doubleValue());
                    rutaRepository.save(ruta);
                    
                    // Actualizar solicitud con costo final y tiempo real
                    seguimientoServiceClient.actualizarCostosYTiempo(
                            ruta.getSolicitudExternaId(),
                            costoReal.getCostoTotal(),
                            costoReal.getTiempoRealMinutos()
                    );
                    
                    // Actualizar estado de solicitud a ENTREGADA
                    seguimientoServiceClient.actualizarEstadoSolicitud(ruta.getSolicitudExternaId(), "ENTREGADA");
                    
                    log.info("✅ Ruta finalizada - Costo real: {} | Tiempo: {} min | Solicitud actualizada a ENTREGADA",
                            costoReal.getCostoTotal(), costoReal.getTiempoRealMinutos());
                    
                } catch (Exception e) {
                    log.error("Error al calcular costo real de la ruta {}: {}", ruta.getId(), e.getMessage(), e);
                    // No lanzamos excepción para no bloquear la finalización del tramo
                }
            }
        }
        
        return convertirADTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        // Si el tramo tiene camión asignado y está en ejecución, liberar el camión
        if (tramo.getCamionId() != null && tramo.getEstado() != Tramo.EstadoTramo.FINALIZADO) {
            camionRepository.findById(tramo.getCamionId()).ifPresent(camion -> {
                camion.setDisponible(true);
                camionRepository.save(camion);
            });
        }

        tramoRepository.deleteById(id);
        log.info("Tramo eliminado: {}", id);
    }

    private TramoDTO convertirADTO(Tramo tramo) {
        TramoDTO dto = new TramoDTO();
        dto.setId(tramo.getId());
        dto.setRutaId(tramo.getRuta().getId());
        dto.setOrden(tramo.getOrden());
        dto.setOrigenDepositoId(tramo.getOrigenDepositoId());
        dto.setDestinoDepositoId(tramo.getDestinoDepositoId());
        dto.setOrigenLatitud(tramo.getOrigenLatitud());
        dto.setOrigenLongitud(tramo.getOrigenLongitud());
        dto.setDestinoLatitud(tramo.getDestinoLatitud());
        dto.setDestinoLongitud(tramo.getDestinoLongitud());
        dto.setTipo(tramo.getTipo() != null ? tramo.getTipo().name() : null);
        dto.setEstado(tramo.getEstado() != null ? tramo.getEstado().name() : null);
        dto.setDistanciaKm(tramo.getDistanciaKm());
        dto.setCostoAproximado(tramo.getCostoAproximado() != null ? BigDecimal.valueOf(tramo.getCostoAproximado()) : null);
        dto.setCostoReal(tramo.getCostoReal() != null ? BigDecimal.valueOf(tramo.getCostoReal()) : null);
        dto.setFechaInicioEstimada(tramo.getFechaInicioEstimada());
        dto.setFechaFinEstimada(tramo.getFechaFinEstimada());
        dto.setFechaInicioReal(tramo.getFechaInicioReal());
        dto.setFechaFinReal(tramo.getFechaFinReal());
        dto.setCamionId(tramo.getCamionId());
        dto.setCreatedAt(tramo.getCreatedAt());
        return dto;
    }

    private Tramo convertirAEntidad(TramoDTO dto) {
        Tramo tramo = new Tramo();
        tramo.setId(dto.getId());
        tramo.setOrden(dto.getOrden());
        tramo.setOrigenDepositoId(dto.getOrigenDepositoId());
        tramo.setDestinoDepositoId(dto.getDestinoDepositoId());
        tramo.setOrigenLatitud(dto.getOrigenLatitud());
        tramo.setOrigenLongitud(dto.getOrigenLongitud());
        tramo.setDestinoLatitud(dto.getDestinoLatitud());
        tramo.setDestinoLongitud(dto.getDestinoLongitud());
        if (dto.getTipo() != null) {
            tramo.setTipo(Tramo.TipoTramo.valueOf(dto.getTipo()));
        }
        if (dto.getEstado() != null) {
            tramo.setEstado(Tramo.EstadoTramo.valueOf(dto.getEstado()));
        }
        tramo.setDistanciaKm(dto.getDistanciaKm());
        tramo.setCostoAproximado(dto.getCostoAproximado() != null ? dto.getCostoAproximado().doubleValue() : null);
        tramo.setCostoReal(dto.getCostoReal() != null ? dto.getCostoReal().doubleValue() : null);
        tramo.setFechaInicioEstimada(dto.getFechaInicioEstimada());
        tramo.setFechaFinEstimada(dto.getFechaFinEstimada());
        tramo.setFechaInicioReal(dto.getFechaInicioReal());
        tramo.setFechaFinReal(dto.getFechaFinReal());
        tramo.setCamionId(dto.getCamionId());
        return tramo;
    }
}
