package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.UbicacionDTO;
import ar.edu.tpi.logistica.entities.Ubicacion;
import ar.edu.tpi.logistica.repositories.UbicacionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ubicaciones")
public class UbicacionController {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @GetMapping
    public ResponseEntity<List<UbicacionDTO>> listarTodas() {
        List<UbicacionDTO> ubicaciones = ubicacionRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ubicaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UbicacionDTO> obtenerPorId(@PathVariable Long id) {
        return ubicacionRepository.findById(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<UbicacionDTO>> obtenerPorTipo(@PathVariable String tipo) {
        List<UbicacionDTO> ubicaciones = ubicacionRepository.findByTipo(tipo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ubicaciones);
    }

    @PostMapping
    public ResponseEntity<UbicacionDTO> crear(@RequestBody UbicacionDTO ubicacionDTO) {
        Ubicacion guardada = ubicacionRepository.save(convertirAEntidad(ubicacionDTO));
        return ResponseEntity.ok(convertirADTO(guardada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UbicacionDTO> actualizar(@PathVariable Long id, @RequestBody UbicacionDTO ubicacionDTO) {
        if (!ubicacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Ubicacion ubicacion = convertirAEntidad(ubicacionDTO);
        ubicacion.setId(id);
        Ubicacion actualizada = ubicacionRepository.save(ubicacion);
        return ResponseEntity.ok(convertirADTO(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ubicacionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UbicacionDTO convertirADTO(Ubicacion ubicacion) {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setId(ubicacion.getId());
        dto.setNombre(ubicacion.getNombre());
        dto.setDireccion(ubicacion.getDireccion());
        dto.setLatitud(ubicacion.getLatitud());
        dto.setLongitud(ubicacion.getLongitud());
        dto.setTipo(ubicacion.getTipo());
        return dto;
    }

    private Ubicacion convertirAEntidad(UbicacionDTO dto) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(dto.getId());
        ubicacion.setNombre(dto.getNombre());
        ubicacion.setDireccion(dto.getDireccion());
        ubicacion.setLatitud(dto.getLatitud());
        ubicacion.setLongitud(dto.getLongitud());
        ubicacion.setTipo(dto.getTipo());
        return ubicacion;
    }
}
