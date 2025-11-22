package ar.edu.tpi.seguimiento.services;

import ar.edu.tpi.seguimiento.clients.LogisticaServiceClient;
import ar.edu.tpi.seguimiento.config.MetricasSeguimiento;
import ar.edu.tpi.seguimiento.dto.*;
import ar.edu.tpi.seguimiento.entities.Cliente;
import ar.edu.tpi.seguimiento.entities.Contenedor;
import ar.edu.tpi.seguimiento.entities.Solicitud;
import ar.edu.tpi.seguimiento.entities.SolicitudCambioEstado;
import ar.edu.tpi.seguimiento.exceptions.DatosInvalidosException;
import ar.edu.tpi.seguimiento.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.seguimiento.repositories.ClienteRepository;
import ar.edu.tpi.seguimiento.repositories.ContenedorRepository;
import ar.edu.tpi.seguimiento.repositories.SolicitudRepository;
import ar.edu.tpi.seguimiento.repositories.SolicitudCambioEstadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContenedorRepository contenedorRepository;

    @Autowired
    private SolicitudCambioEstadoRepository solicitudCambioEstadoRepository;

    @Autowired
    private LogisticaServiceClient logisticaClient;

    @Autowired
    private MetricasSeguimiento metricas;

    public List<SolicitudDTO> obtenerTodas() {
        return solicitudRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<SolicitudDTO> obtenerPorId(Long id) {
        return solicitudRepository.findById(id)
                .map(this::convertirADTO);
    }

    public Optional<SolicitudDetalleDTO> obtenerDetallePorId(Long id) {
        return solicitudRepository.findById(id)
                .map(this::convertirADetalleDTO);
    }

    public Optional<SolicitudDTO> obtenerPorNumero(String numero) {
        return solicitudRepository.findByNumero(numero)
                .map(this::convertirADTO);
    }

    public List<SolicitudDTO> obtenerPorCliente(Long clienteId) {
        return solicitudRepository.findByClienteId(clienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<SolicitudDTO> obtenerPorEstado(String estado) {
        return solicitudRepository.findByEstado(estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una solicitud por su rutaId
     */
    public SolicitudDTO obtenerPorRutaId(Long rutaId) {
        return solicitudRepository.findAll().stream()
                .filter(s -> s.getRutaId() != null && s.getRutaId().equals(rutaId))
                .findFirst()
                .map(this::convertirADTO)
                .orElse(null);
    }

    /**
     * Crea una nueva solicitud de transporte.
     * Puede crear cliente, contenedor y ubicaciones si no existen.
     */
    @Transactional
    public SolicitudDTO crearSolicitud(CrearSolicitudRequest request) {
        // 1. Obtener o crear cliente
        Long clienteId = request.getClienteId();
        if (clienteId == null) {
            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setNombre(request.getClienteNombre());
            nuevoCliente.setEmail(request.getClienteEmail());
            nuevoCliente.setTelefono(request.getClienteTelefono());
            nuevoCliente.setDocumento(request.getClienteDocumento());
            nuevoCliente.setDireccion(request.getClienteDireccion());
            nuevoCliente.setCreatedAt(LocalDateTime.now());
            nuevoCliente = clienteRepository.save(nuevoCliente);
            clienteId = nuevoCliente.getId();
            log.info("Nuevo cliente creado: {}", clienteId);
        }

        // 2. Obtener o crear contenedor
        Long contenedorId = request.getContenedorId();
        if (contenedorId == null) {
            // Crear nuevo contenedor
            Contenedor nuevoContenedor = new Contenedor();
            nuevoContenedor.setIdentificacion(request.getContenedorIdentificacion());
            nuevoContenedor.setPesoKg(request.getContenedorPesoKg());
            nuevoContenedor.setVolumenM3(request.getContenedorVolumenM3());
            nuevoContenedor.setLargoM(request.getContenedorLargoM());
            nuevoContenedor.setAnchoM(request.getContenedorAnchoM());
            nuevoContenedor.setAltoM(request.getContenedorAltoM());
            nuevoContenedor.setDescripcion(request.getContenedorDescripcion());
            nuevoContenedor.setClienteId(clienteId);
            nuevoContenedor.setCreatedAt(LocalDateTime.now());
            nuevoContenedor = contenedorRepository.save(nuevoContenedor);
            contenedorId = nuevoContenedor.getId();
            log.info("Nuevo contenedor creado: {}", contenedorId);
        }

        // 3. Validar que se proporcionaron las coordenadas de origen
        if (request.getOrigenLatitud() == null || request.getOrigenLongitud() == null) {
            throw new DatosInvalidosException("Las coordenadas de origen (latitud y longitud) son obligatorias");
        }
        
        // 4. Validar que se proporcionaron las coordenadas de destino
        if (request.getDestinoLatitud() == null || request.getDestinoLongitud() == null) {
            throw new DatosInvalidosException("Las coordenadas de destino (latitud y longitud) son obligatorias");
        }
        
        // 5. Crear ubicación de origen en logística
        UbicacionDTO nuevaUbicacionOrigen = new UbicacionDTO();
        nuevaUbicacionOrigen.setNombre(request.getOrigenNombre() != null ? request.getOrigenNombre() : "Origen");
        nuevaUbicacionOrigen.setDireccion(request.getOrigenDireccion());
        nuevaUbicacionOrigen.setLatitud(request.getOrigenLatitud());
        nuevaUbicacionOrigen.setLongitud(request.getOrigenLongitud());
        nuevaUbicacionOrigen.setTipo("ORIGEN");
        
        UbicacionDTO ubicacionOrigen = logisticaClient.crearUbicacion(nuevaUbicacionOrigen);
        if (ubicacionOrigen == null) {
            throw new IllegalArgumentException("No se pudo crear la ubicación de origen en logística");
        }
        Long origenId = ubicacionOrigen.getId();
        log.info("Nueva ubicación de origen creada: {} (lat: {}, lng: {})", 
                origenId, request.getOrigenLatitud(), request.getOrigenLongitud());
        
        // 6. Crear ubicación de destino en logística
        UbicacionDTO nuevaUbicacionDestino = new UbicacionDTO();
        nuevaUbicacionDestino.setNombre(request.getDestinoNombre() != null ? request.getDestinoNombre() : "Destino");
        nuevaUbicacionDestino.setDireccion(request.getDestinoDireccion());
        nuevaUbicacionDestino.setLatitud(request.getDestinoLatitud());
        nuevaUbicacionDestino.setLongitud(request.getDestinoLongitud());
        nuevaUbicacionDestino.setTipo("DESTINO");
        
        UbicacionDTO ubicacionDestino = logisticaClient.crearUbicacion(nuevaUbicacionDestino);
        if (ubicacionDestino == null) {
            throw new IllegalArgumentException("No se pudo crear la ubicación de destino en logística");
        }
        Long destinoId = ubicacionDestino.getId();
        log.info("Nueva ubicación de destino creada: {} (lat: {}, lng: {})", 
                destinoId, request.getDestinoLatitud(), request.getDestinoLongitud());

        // 7. Verificar disponibilidad en logística
        Contenedor contenedor = contenedorRepository.findById(contenedorId)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado"));
        
        boolean disponible = logisticaClient.verificarDisponibilidadCamiones(
                contenedor.getPesoKg().doubleValue(),
                contenedor.getVolumenM3().doubleValue()
        );
        
        if (!disponible) {
            log.warn("No hay camiones disponibles para peso={} kg y volumen={} m3",
                    contenedor.getPesoKg(), contenedor.getVolumenM3());
        }

        // 8. Crear solicitud con datos completos
        Solicitud solicitud = new Solicitud();
        solicitud.setNumero(generarNumeroSolicitud());
        solicitud.setClienteId(clienteId);
        solicitud.setContenedorId(contenedorId);
        solicitud.setOrigenId(origenId);
        solicitud.setDestinoId(destinoId);
        
        // Establecer dirección y coordenadas del origen
        solicitud.setOrigenDireccion(ubicacionOrigen.getDireccion());
        solicitud.setOrigenLatitud(BigDecimal.valueOf(ubicacionOrigen.getLatitud()));
        solicitud.setOrigenLongitud(BigDecimal.valueOf(ubicacionOrigen.getLongitud()));
        
        // Establecer dirección y coordenadas del destino
        solicitud.setDestinoDireccion(ubicacionDestino.getDireccion());
        solicitud.setDestinoLatitud(BigDecimal.valueOf(ubicacionDestino.getLatitud()));
        solicitud.setDestinoLongitud(BigDecimal.valueOf(ubicacionDestino.getLongitud()));
        
        solicitud.setEstado("BORRADOR"); // Estado inicial
        solicitud.setFechaSolicitud(LocalDateTime.now());

        Solicitud guardada = solicitudRepository.save(solicitud);
        
        // 9. Registrar el estado inicial en el historial
        registrarCambioEstado(guardada.getId(), SolicitudCambioEstado.RequestStatus.BORRADOR, 
                "Solicitud creada");
        
        log.info("Solicitud creada: {} ({}) - Origen: {} -> Destino: {}", 
                guardada.getId(), guardada.getNumero(), 
                ubicacionOrigen.getNombre(), ubicacionDestino.getNombre());
        
        return convertirADTO(guardada);
    }

    @Transactional
    public SolicitudDTO actualizarEstado(Long id, String nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        String estadoAnterior = solicitud.getEstado();
        
        // Cerrar el estado actual en el historial
        cerrarEstadoActual(id);
        
        // Actualizar el estado de la solicitud
        solicitud.setEstado(nuevoEstado);
        solicitud.setUpdatedAt(LocalDateTime.now());

        if ("ENTREGADA".equals(nuevoEstado)) {
            solicitud.setFechaEntrega(LocalDateTime.now());
        }

        Solicitud actualizada = solicitudRepository.save(solicitud);
        
        // Registrar el nuevo estado en el historial
        SolicitudCambioEstado.RequestStatus nuevoEstadoEnum = 
                SolicitudCambioEstado.RequestStatus.valueOf(nuevoEstado);
        registrarCambioEstado(id, nuevoEstadoEnum, 
                "Cambio de estado de " + estadoAnterior + " a " + nuevoEstado);
        
        log.info("Solicitud {} cambió de estado: {} -> {}", id, estadoAnterior, nuevoEstado);
        
        return convertirADTO(actualizada);
    }
    
    /**
     * Registra un cambio de estado en el historial
     */
    private void registrarCambioEstado(Long solicitudId, SolicitudCambioEstado.RequestStatus estado,
                                       String detalle) {
        SolicitudCambioEstado cambio = new SolicitudCambioEstado();
        cambio.setSolicitudId(solicitudId);
        cambio.setEstado(estado);
        cambio.setFechaDesde(LocalDateTime.now());
        cambio.setDetalle(detalle);
        
        solicitudCambioEstadoRepository.save(cambio);
        log.debug("Cambio de estado registrado: Solicitud {} -> {}", solicitudId, estado);
    }
    
    /**
     * Cierra el estado actual en el historial estableciendo fecha_hasta
     */
    private void cerrarEstadoActual(Long solicitudId) {
        solicitudCambioEstadoRepository.findEstadoActual(solicitudId)
                .ifPresent(cambioActual -> {
                    cambioActual.setFechaHasta(LocalDateTime.now());
                    solicitudCambioEstadoRepository.save(cambioActual);
                    log.debug("Estado anterior cerrado: Solicitud {} -> {}", 
                            solicitudId, cambioActual.getEstado());
                });
    }

    /**
     * Asigna una ruta a una solicitud
     * Llamado por logistica-service al crear una ruta
     */
    @Transactional
    public SolicitudDTO asignarRuta(Long solicitudId, Long rutaId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", solicitudId));

        solicitud.setRutaId(rutaId);
        solicitud.setUpdatedAt(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("✅ Solicitud {} vinculada con ruta {}", solicitudId, rutaId);
        
        return convertirADTO(actualizada);
    }

    /**
     * Actualiza el costo final y tiempo real de una solicitud
     * Llamado por logistica-service al finalizar todos los tramos
     */
    @Transactional
    public SolicitudDTO actualizarCostosYTiempo(Long id, BigDecimal costoFinal, Integer tiempoRealMinutos) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        solicitud.setCostoFinal(costoFinal);
        solicitud.setTiempoRealMin(tiempoRealMinutos);
        solicitud.setUpdatedAt(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("✅ Solicitud {} actualizada - Costo final: {} | Tiempo real: {} min", 
                id, costoFinal, tiempoRealMinutos);
        
        return convertirADTO(actualizada);
    }

    @Transactional
    public void eliminar(Long id) {
        solicitudRepository.deleteById(id);
        log.info("Solicitud eliminada: {}", id);
    }

    /**
     * Obtiene el historial completo de cambios de estado de una solicitud
     * Ordenado por fecha (más reciente primero)
     */
    public List<CambioEstadoDTO> obtenerHistorialEstados(Long solicitudId) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Obteniendo historial de estados para solicitud {}", correlationId, solicitudId);
        
        // Verificar que la solicitud existe
        solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", solicitudId));
        
        List<SolicitudCambioEstado> cambios = solicitudCambioEstadoRepository
                .findBySolicitudIdOrderByFechaDesdeDesc(solicitudId);
        
        log.info("[CID={}] Se encontraron {} cambios de estado para solicitud {}", 
                correlationId, cambios.size(), solicitudId);
        
        return cambios.stream()
                .map(this::convertirCambioEstadoADTO)
                .collect(Collectors.toList());
    }

    private String generarNumeroSolicitud() {
        String numero;
        do {
            numero = "SOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (solicitudRepository.existsByNumero(numero));
        return numero;
    }

    private SolicitudDTO convertirADTO(Solicitud solicitud) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setId(solicitud.getId());
        dto.setNumero(solicitud.getNumero());
        dto.setClienteId(solicitud.getClienteId());
        dto.setContenedorId(solicitud.getContenedorId());
        dto.setOrigenId(solicitud.getOrigenId());
        dto.setDestinoId(solicitud.getDestinoId());
        dto.setRutaId(solicitud.getRutaId());
        dto.setEstado(solicitud.getEstado());
        dto.setCostoEstimado(solicitud.getCostoEstimado());
        dto.setTiempoEstimadoMin(solicitud.getTiempoEstimadoMin());
        dto.setCostoFinal(solicitud.getCostoFinal());
        dto.setTiempoRealMin(solicitud.getTiempoRealMin());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setFechaEntrega(solicitud.getFechaEntrega());
        return dto;
    }

    private SolicitudDetalleDTO convertirADetalleDTO(Solicitud solicitud) {
        SolicitudDetalleDTO dto = new SolicitudDetalleDTO();
        dto.setId(solicitud.getId());
        dto.setNumero(solicitud.getNumero());
        dto.setEstado(solicitud.getEstado());
        dto.setCostoEstimado(solicitud.getCostoEstimado());
        dto.setTiempoEstimadoMin(solicitud.getTiempoEstimadoMin());
        dto.setCostoFinal(solicitud.getCostoFinal());
        dto.setTiempoRealMin(solicitud.getTiempoRealMin());

        // Obtener datos del cliente
        clienteRepository.findById(solicitud.getClienteId()).ifPresent(cliente -> {
            ClienteDTO clienteDTO = new ClienteDTO();
            clienteDTO.setId(cliente.getId());
            clienteDTO.setNombre(cliente.getNombre());
            clienteDTO.setEmail(cliente.getEmail());
            clienteDTO.setTelefono(cliente.getTelefono());
            dto.setCliente(clienteDTO);
        });

        // Obtener datos del contenedor
        contenedorRepository.findById(solicitud.getContenedorId()).ifPresent(contenedor -> {
            ContenedorDTO contenedorDTO = new ContenedorDTO();
            contenedorDTO.setId(contenedor.getId());
            contenedorDTO.setIdentificacion(contenedor.getIdentificacion());
            contenedorDTO.setPesoKg(contenedor.getPesoKg());
            contenedorDTO.setVolumenM3(contenedor.getVolumenM3());
            contenedorDTO.setLargoM(contenedor.getLargoM());
            contenedorDTO.setAnchoM(contenedor.getAnchoM());
            contenedorDTO.setAltoM(contenedor.getAltoM());
            contenedorDTO.setDescripcion(contenedor.getDescripcion());
            contenedorDTO.setClienteId(contenedor.getClienteId());
            contenedorDTO.setCreatedAt(contenedor.getCreatedAt());
            dto.setContenedor(contenedorDTO);
        });

        // Obtener ubicaciones del servicio de logística
        UbicacionDTO origen = logisticaClient.obtenerUbicacion(solicitud.getOrigenId());
        UbicacionDTO destino = logisticaClient.obtenerUbicacion(solicitud.getDestinoId());
        dto.setOrigen(origen);
        dto.setDestino(destino);

        // Obtener información de tracking en tiempo real si tiene ruta asignada
        if (solicitud.getRutaId() != null) {
            try {
                TrackingInfoDTO tracking = logisticaClient.obtenerTrackingInfo(solicitud.getRutaId());
                dto.setTracking(tracking);
                log.debug("Tracking info agregado para solicitud {} con ruta {}", solicitud.getId(), solicitud.getRutaId());
            } catch (Exception e) {
                log.warn("No se pudo obtener tracking info para ruta {}: {}", solicitud.getRutaId(), e.getMessage());
                dto.setTracking(null);
            }
        }

        return dto;
    }

    private CambioEstadoDTO convertirCambioEstadoADTO(SolicitudCambioEstado cambio) {
        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setId(cambio.getId());
        dto.setSolicitudId(cambio.getSolicitudId());
        dto.setEstado(cambio.getEstado().name());
        dto.setFechaDesde(cambio.getFechaDesde());
        dto.setFechaHasta(cambio.getFechaHasta());
        dto.setDetalle(cambio.getDetalle());
        return dto;
    }
}
