package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.dto.RutaDTO;
import ar.edu.tpi.logistica.entities.Ruta;
import ar.edu.tpi.logistica.repositories.RutaRepository;
import lombok.extern.slf4j.Slf4j;
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

    public List<RutaDTO> obtenerTodas() {
        return rutaRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<RutaDTO> obtenerPorId(Long id) {
        return rutaRepository.findById(id)
                .map(this::convertirADTO);
    }

    public Optional<RutaDTO> obtenerPorSolicitudId(Long solicitudId) {
        return rutaRepository.findBySolicitudExternaId(solicitudId)
                .map(this::convertirADTO);
    }

    @Transactional
    public RutaDTO crear(RutaDTO rutaDTO) {
        // Validar que no exista ya una ruta para esta solicitud
        if (rutaDTO.getSolicitudExternaId() != null && 
            rutaRepository.findBySolicitudExternaId(rutaDTO.getSolicitudExternaId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una ruta para esta solicitud");
        }

        Ruta ruta = convertirAEntidad(rutaDTO);
        ruta.setCreatedAt(LocalDateTime.now());
        ruta.setEstado(Ruta.EstadoRuta.PROPUESTA);
        
        Ruta guardada = rutaRepository.save(ruta);
        log.info("Ruta creada: {}", guardada.getId());
        return convertirADTO(guardada);
    }

    @Transactional
    public RutaDTO actualizar(Long id, RutaDTO rutaDTO) {
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        ruta.setCantidadTramos(rutaDTO.getCantidadTramos());
        ruta.setCantidadDepositos(rutaDTO.getCantidadDepositos());
        ruta.setObservaciones(rutaDTO.getObservaciones());
        
        if (rutaDTO.getEstado() != null) {
            ruta.setEstado(Ruta.EstadoRuta.valueOf(rutaDTO.getEstado()));
        }

        Ruta actualizada = rutaRepository.save(ruta);
        log.info("Ruta actualizada: {}", id);
        return convertirADTO(actualizada);
    }

    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        Ruta ruta = rutaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        try {
            Ruta.EstadoRuta estado = Ruta.EstadoRuta.valueOf(nuevoEstado.toUpperCase());
            ruta.setEstado(estado);
            rutaRepository.save(ruta);
            log.info("Estado de ruta {} cambiado a {}", id, nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }
    }

    @Transactional
    public void eliminar(Long id) {
        if (!rutaRepository.existsById(id)) {
            throw new IllegalArgumentException("Ruta no encontrada");
        }
        rutaRepository.deleteById(id);
        log.info("Ruta eliminada: {}", id);
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
