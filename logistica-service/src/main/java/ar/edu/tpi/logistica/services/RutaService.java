package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.config.MetricasLogistica;
import ar.edu.tpi.logistica.dto.RutaDTO;
import ar.edu.tpi.logistica.entities.Ruta;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.repositories.RutaRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private MetricasLogistica metricas;

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

    public Optional<RutaDTO> obtenerPorSolicitudId(Long solicitudId) {
        return rutaRepository.findBySolicitudExternaId(solicitudId)
                .map(this::convertirADTO);
    }

    @Transactional
    public RutaDTO crear(RutaDTO rutaDTO) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Creando nueva ruta para solicitud: {}", correlationId, rutaDTO.getSolicitudExternaId());
        
        // Validar que no exista ya una ruta para esta solicitud
        if (rutaDTO.getSolicitudExternaId() != null && 
            rutaRepository.findBySolicitudExternaId(rutaDTO.getSolicitudExternaId()).isPresent()) {
            log.warn("[CID={}] Ya existe una ruta para la solicitud: {}", correlationId, rutaDTO.getSolicitudExternaId());
            throw new DatosInvalidosException("Ya existe una ruta para esta solicitud");
        }

        Ruta ruta = convertirAEntidad(rutaDTO);
        ruta.setCreatedAt(LocalDateTime.now());
        ruta.setEstado(Ruta.EstadoRuta.PROPUESTA);
        
        Ruta guardada = rutaRepository.save(ruta);
        metricas.incrementarRutasCreadas();
        
        log.info("[CID={}] Ruta creada exitosamente con ID: {}", correlationId, guardada.getId());
        return convertirADTO(guardada);
    }

    @Transactional
    public RutaDTO actualizar(Long id, RutaDTO rutaDTO) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Actualizando ruta con ID: {}", correlationId, id);
        
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ruta", id));

        ruta.setCantidadTramos(rutaDTO.getCantidadTramos());
        ruta.setCantidadDepositos(rutaDTO.getCantidadDepositos());
        ruta.setObservaciones(rutaDTO.getObservaciones());
        
        if (rutaDTO.getEstado() != null) {
            ruta.setEstado(Ruta.EstadoRuta.valueOf(rutaDTO.getEstado()));
        }

        Ruta actualizada = rutaRepository.save(ruta);
        log.info("[CID={}] Ruta actualizada exitosamente: {}", correlationId, id);
        return convertirADTO(actualizada);
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

    @Transactional
    public void eliminar(Long id) {
        String correlationId = MDC.get("correlationId");
        log.debug("[CID={}] Eliminando ruta con ID: {}", correlationId, id);
        
        if (!rutaRepository.existsById(id)) {
            log.warn("[CID={}] Intento de eliminar ruta inexistente: {}", correlationId, id);
            throw new RecursoNoEncontradoException("Ruta", id);
        }
        
        rutaRepository.deleteById(id);
        log.info("[CID={}] Ruta eliminada exitosamente: {}", correlationId, id);
    }

    private RutaDTO convertirADTO(Ruta ruta) {
        RutaDTO dto = new RutaDTO();
        dto.setId(ruta.getId());
        dto.setSolicitudExternaId(ruta.getSolicitudExternaId());
        dto.setEstado(ruta.getEstado() != null ? ruta.getEstado().name() : null);
        dto.setCantidadTramos(ruta.getCantidadTramos());
        dto.setCantidadDepositos(ruta.getCantidadDepositos());
        dto.setObservaciones(ruta.getObservaciones());
        dto.setCreatedAt(ruta.getCreatedAt());
        return dto;
    }

    private Ruta convertirAEntidad(RutaDTO dto) {
        Ruta ruta = new Ruta();
        ruta.setId(dto.getId());
        ruta.setSolicitudExternaId(dto.getSolicitudExternaId());
        if (dto.getEstado() != null) {
            ruta.setEstado(Ruta.EstadoRuta.valueOf(dto.getEstado()));
        }
        ruta.setCantidadTramos(dto.getCantidadTramos());
        ruta.setCantidadDepositos(dto.getCantidadDepositos());
        ruta.setObservaciones(dto.getObservaciones());
        return ruta;
    }
}
