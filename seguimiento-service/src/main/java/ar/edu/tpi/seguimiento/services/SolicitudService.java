package ar.edu.tpi.seguimiento.services;

import ar.edu.tpi.seguimiento.clients.LogisticaServiceClient;
import ar.edu.tpi.seguimiento.dto.*;
import ar.edu.tpi.seguimiento.entities.Cliente;
import ar.edu.tpi.seguimiento.entities.Contenedor;
import ar.edu.tpi.seguimiento.entities.Solicitud;
import ar.edu.tpi.seguimiento.repositories.ClienteRepository;
import ar.edu.tpi.seguimiento.repositories.ContenedorRepository;
import ar.edu.tpi.seguimiento.repositories.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
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
    private LogisticaServiceClient logisticaClient;

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
     * Crea una nueva solicitud de transporte.
     * Puede crear cliente y/o contenedor si no existen.
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

        // 3. Obtener ubicaciones de logística (para direcciones y coordenadas)
        UbicacionDTO ubicacionOrigen = logisticaClient.obtenerUbicacion(request.getOrigenId());
        if (ubicacionOrigen == null) {
            throw new IllegalArgumentException("Ubicación de origen no encontrada: " + request.getOrigenId());
        }
        
        UbicacionDTO ubicacionDestino = logisticaClient.obtenerUbicacion(request.getDestinoId());
        if (ubicacionDestino == null) {
            throw new IllegalArgumentException("Ubicación de destino no encontrada: " + request.getDestinoId());
        }

        // 4. Verificar disponibilidad en logística
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

        // 5. Crear solicitud con datos completos
        Solicitud solicitud = new Solicitud();
        solicitud.setNumero(generarNumeroSolicitud());
        solicitud.setClienteId(clienteId);
        solicitud.setContenedorId(contenedorId);
        solicitud.setOrigenId(request.getOrigenId());
        solicitud.setDestinoId(request.getDestinoId());
        
        // Establecer dirección y coordenadas del origen (desde logística)
        solicitud.setOrigenDireccion(ubicacionOrigen.getDireccion());
        solicitud.setOrigenLatitud(BigDecimal.valueOf(ubicacionOrigen.getLatitud()));
        solicitud.setOrigenLongitud(BigDecimal.valueOf(ubicacionOrigen.getLongitud()));
        
        // Establecer dirección y coordenadas del destino (desde logística)
        solicitud.setDestinoDireccion(ubicacionDestino.getDireccion());
        solicitud.setDestinoLatitud(BigDecimal.valueOf(ubicacionDestino.getLatitud()));
        solicitud.setDestinoLongitud(BigDecimal.valueOf(ubicacionDestino.getLongitud()));
        
        solicitud.setEstado("BORRADOR"); // Estado inicial
        solicitud.setFechaSolicitud(LocalDateTime.now());

        Solicitud guardada = solicitudRepository.save(solicitud);
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
        solicitud.setEstado(nuevoEstado);

        if ("ENTREGADA".equals(nuevoEstado)) {
            solicitud.setFechaEntrega(LocalDateTime.now());
        }

        Solicitud actualizada = solicitudRepository.save(solicitud);
        log.info("Solicitud {} cambió de estado: {} -> {}", id, estadoAnterior, nuevoEstado);
        
        return convertirADTO(actualizada);
    }

    @Transactional
    public void eliminar(Long id) {
        solicitudRepository.deleteById(id);
        log.info("Solicitud eliminada: {}", id);
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
            dto.setContenedor(contenedorDTO);
        });

        // Obtener ubicaciones del servicio de logística
        UbicacionDTO origen = logisticaClient.obtenerUbicacion(solicitud.getOrigenId());
        UbicacionDTO destino = logisticaClient.obtenerUbicacion(solicitud.getDestinoId());
        dto.setOrigen(origen);
        dto.setDestino(destino);

        return dto;
    }
}
