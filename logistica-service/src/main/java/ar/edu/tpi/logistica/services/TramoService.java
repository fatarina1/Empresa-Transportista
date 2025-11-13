package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.clients.GeoApiClient;
import ar.edu.tpi.logistica.dto.DistanciaDTO;
import ar.edu.tpi.logistica.dto.TramoDTO;
import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.entities.Ruta;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.repositories.CamionRepository;
import ar.edu.tpi.logistica.repositories.RutaRepository;
import ar.edu.tpi.logistica.repositories.TramoRepository;
import lombok.extern.slf4j.Slf4j;
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
    private ar.edu.tpi.logistica.clients.SeguimientoClient seguimientoClient;

    @Autowired
    private ar.edu.tpi.logistica.services.CostoService costoService;

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
        // Validar que la ruta existe
        Ruta ruta = rutaRepository.findById(tramoDTO.getRutaId())
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

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
                    log.info("Distancia calculada para tramo: {} km ({})", distancia.getKilometros(), distancia.getDuracionTexto());
                }
            } catch (Exception e) {
                log.warn("No se pudo calcular distancia automáticamente: {}", e.getMessage());
            }
        }

        Tramo guardado = tramoRepository.save(tramo);
        log.info("Tramo creado: {}", guardado.getId());
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
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        // Verificar que el camión existe
        Camion camion = camionRepository.findById(camionId)
                .orElseThrow(() -> new IllegalArgumentException("Camión no encontrado"));

        // Validar que el camión está disponible
        if (!camion.getDisponible()) {
            throw new IllegalArgumentException("El camión no está disponible");
        }

        // VALIDACIÓN ADICIONAL: verificar que el camión soporte el contenedor (peso y volumen)
        try {
            Ruta ruta = tramo.getRuta();
            if (ruta == null || ruta.getSolicitudExternaId() == null) {
                throw new IllegalArgumentException("La ruta no tiene una solicitud externa asociada para validar el contenedor");
            }

            Long solicitudExternaId = ruta.getSolicitudExternaId();
            ar.edu.tpi.logistica.dto.ContenedorRemotoDTO contenedor = seguimientoClient.obtenerContenedorDeSolicitud(solicitudExternaId);

            if (contenedor == null) {
                throw new IllegalArgumentException("No se pudo obtener información del contenedor desde el servicio de seguimiento");
            }

            if (contenedor.getPesoKg() != null && camion.getCapacidadPesoKg() != null) {
                double pesoKg = contenedor.getPesoKg().doubleValue();
                if (pesoKg > camion.getCapacidadPesoKg()) {
                    throw new IllegalArgumentException("Camión no tiene capacidad de peso suficiente");
                }
            }

            if (contenedor.getVolumenM3() != null && camion.getCapacidadVolumenM3() != null) {
                double volumenM3 = contenedor.getVolumenM3().doubleValue();
                if (volumenM3 > camion.getCapacidadVolumenM3()) {
                    throw new IllegalArgumentException("Camión no tiene capacidad de volumen suficiente");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("No se pudo validar capacidad del camión contra el contenedor: {}", e.getMessage());
            // En caso de error de comunicación con seguimiento, defendemos la operación y evitamos asignar por seguridad
            throw new IllegalArgumentException("No se pudo validar la capacidad del camión: " + e.getMessage());
        }

        // Asignar camión
        tramo.setCamionId(camionId);
        tramo.setEstado(Tramo.EstadoTramo.ASIGNADO);

        // Marcar camión como no disponible
        camion.setDisponible(false);
        camionRepository.save(camion);

        Tramo actualizado = tramoRepository.save(tramo);
        log.info("Camión {} asignado al tramo {}", camionId, tramoId);
        
        return convertirADTO(actualizado);
    }

    @Transactional
    public TramoDTO iniciarTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        // Validar que el tramo tiene camión asignado
        if (tramo.getCamionId() == null) {
            throw new IllegalArgumentException("El tramo no tiene camión asignado");
        }

        // Validar que el tramo está en estado ASIGNADO
        if (tramo.getEstado() != Tramo.EstadoTramo.ASIGNADO) {
            throw new IllegalArgumentException("El tramo debe estar en estado ASIGNADO para poder iniciarse");
        }

        // Registrar inicio
        tramo.setFechaInicioReal(LocalDateTime.now());
        tramo.setEstado(Tramo.EstadoTramo.INICIADO);

        Tramo actualizado = tramoRepository.save(tramo);
        log.info("Tramo {} iniciado a las {}", tramoId, tramo.getFechaInicioReal());

        return convertirADTO(actualizado);
    }

    @Transactional
    public TramoDTO finalizarTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        // Validar que el tramo está en estado INICIADO
        if (tramo.getEstado() != Tramo.EstadoTramo.INICIADO) {
            throw new IllegalArgumentException("El tramo debe estar en estado INICIADO para poder finalizarse");
        }

        // Validar que tiene fecha de inicio
        if (tramo.getFechaInicioReal() == null) {
            throw new IllegalArgumentException("El tramo no tiene fecha de inicio registrada");
        }

        // Registrar fin
        LocalDateTime fechaFin = LocalDateTime.now();
        tramo.setFechaFinReal(fechaFin);
        tramo.setEstado(Tramo.EstadoTramo.FINALIZADO);

        // Calcular duración real (en minutos)
        long duracionMinutos = ChronoUnit.MINUTES.between(tramo.getFechaInicioReal(), fechaFin);
        log.info("Tramo {} finalizado. Duración: {} minutos", tramoId, duracionMinutos);

        // Calcular costo real si tiene camión asignado
        if (tramo.getCamionId() != null) {
            camionRepository.findById(tramo.getCamionId()).ifPresent(camion -> {
                try {
                    java.math.BigDecimal costoReal = costoService.calcularCostoReal(tramo, camion);
                    tramo.setCostoReal(costoReal.doubleValue());
                } catch (Exception e) {
                    log.warn("No se pudo calcular costo real para tramo {}: {}", tramoId, e.getMessage());
                }

                // Liberar camión
                camion.setDisponible(true);
                camionRepository.save(camion);
                log.info("Camión {} liberado", camion.getId());
            });
        }

        Tramo actualizado = tramoRepository.save(tramo);

        // Si todos los tramos de la ruta están finalizados, calcular costo total y notificar a seguimiento
        try {
            Long rutaId = tramo.getRuta().getId();
            boolean todosFinalizados = tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId)
                    .stream()
                    .allMatch(t -> t.getEstado() == Tramo.EstadoTramo.FINALIZADO);

            if (todosFinalizados) {
                java.math.BigDecimal sumaCostos = tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId).stream()
                        .map(t -> t.getCostoReal() != null ? java.math.BigDecimal.valueOf(t.getCostoReal()) : java.math.BigDecimal.ZERO)
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                // Calcular tiempo real en minutos sumando duraciones
                int tiempoRealMinTotal = tramoRepository.findByRutaIdOrderByOrdenAsc(rutaId).stream()
                        .mapToInt(t -> {
                            if (t.getFechaInicioReal() != null && t.getFechaFinReal() != null) {
                                return (int) java.time.temporal.ChronoUnit.MINUTES.between(t.getFechaInicioReal(), t.getFechaFinReal());
                            }
                            return 0;
                        }).sum();

                // Notificar a seguimiento service usando la solicitud externa asociada a la ruta
                Long solicitudExternaId = tramo.getRuta().getSolicitudExternaId();
                if (solicitudExternaId != null) {
                    boolean ok = seguimientoClient.actualizarCostosSolicitud(solicitudExternaId, sumaCostos, tiempoRealMinTotal, null);
                    if (!ok) {
                        log.warn("No se pudo notificar a seguimiento la actualización de costos para solicitud {}", solicitudExternaId);
                    } else {
                        log.info("Solicitud {} actualizada con costoFinal={} y tiempoRealMin={}", solicitudExternaId, sumaCostos, tiempoRealMinTotal);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al agregar costos totales de la ruta: {}", e.getMessage());
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
