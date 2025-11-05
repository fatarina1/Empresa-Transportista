package ar.edu.tpi.seguimiento.services;

import ar.edu.tpi.seguimiento.dto.ContenedorDTO;
import ar.edu.tpi.seguimiento.entities.Contenedor;
import ar.edu.tpi.seguimiento.repositories.ContenedorRepository;
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
public class ContenedorService {

    @Autowired
    private ContenedorRepository contenedorRepository;

    public List<ContenedorDTO> obtenerTodos() {
        return contenedorRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<ContenedorDTO> obtenerPorId(Long id) {
        return contenedorRepository.findById(id)
                .map(this::convertirADTO);
    }

    public List<ContenedorDTO> obtenerPorCliente(Long clienteId) {
        return contenedorRepository.findByClienteId(clienteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContenedorDTO crear(ContenedorDTO contenedorDTO) {
        if (contenedorRepository.existsByIdentificacion(contenedorDTO.getIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un contenedor con esa identificación");
        }

        Contenedor contenedor = convertirAEntidad(contenedorDTO);
        contenedor.setCreatedAt(LocalDateTime.now());
        Contenedor guardado = contenedorRepository.save(contenedor);
        log.info("Contenedor creado: {}", guardado.getId());
        return convertirADTO(guardado);
    }

    @Transactional
    public ContenedorDTO actualizar(Long id, ContenedorDTO contenedorDTO) {
        Contenedor contenedor = contenedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado"));

        contenedor.setIdentificacion(contenedorDTO.getIdentificacion());
        contenedor.setPesoKg(contenedorDTO.getPesoKg());
        contenedor.setVolumenM3(contenedorDTO.getVolumenM3());
        contenedor.setLargoM(contenedorDTO.getLargoM());
        contenedor.setAnchoM(contenedorDTO.getAnchoM());
        contenedor.setAltoM(contenedorDTO.getAltoM());
        contenedor.setDescripcion(contenedorDTO.getDescripcion());
        contenedor.setClienteId(contenedorDTO.getClienteId());

        Contenedor actualizado = contenedorRepository.save(contenedor);
        log.info("Contenedor actualizado: {}", id);
        return convertirADTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        contenedorRepository.deleteById(id);
        log.info("Contenedor eliminado: {}", id);
    }

    private ContenedorDTO convertirADTO(Contenedor contenedor) {
        ContenedorDTO dto = new ContenedorDTO();
        dto.setId(contenedor.getId());
        dto.setIdentificacion(contenedor.getIdentificacion());
        dto.setPesoKg(contenedor.getPesoKg());
        dto.setVolumenM3(contenedor.getVolumenM3());
        dto.setLargoM(contenedor.getLargoM());
        dto.setAnchoM(contenedor.getAnchoM());
        dto.setAltoM(contenedor.getAltoM());
        dto.setDescripcion(contenedor.getDescripcion());
        dto.setClienteId(contenedor.getClienteId());
        dto.setCreatedAt(contenedor.getCreatedAt());
        return dto;
    }

    private Contenedor convertirAEntidad(ContenedorDTO dto) {
        Contenedor contenedor = new Contenedor();
        contenedor.setId(dto.getId());
        contenedor.setIdentificacion(dto.getIdentificacion());
        contenedor.setPesoKg(dto.getPesoKg());
        contenedor.setVolumenM3(dto.getVolumenM3());
        contenedor.setLargoM(dto.getLargoM());
        contenedor.setAnchoM(dto.getAnchoM());
        contenedor.setAltoM(dto.getAltoM());
        contenedor.setDescripcion(dto.getDescripcion());
        contenedor.setClienteId(dto.getClienteId());
        contenedor.setCreatedAt(dto.getCreatedAt());
        return contenedor;
    }
}
