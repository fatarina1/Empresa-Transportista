package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.CamionDTO;
import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.services.CamionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    // Listar todos los camiones
    @GetMapping
    public List<CamionDTO> listarTodos() {
        return camionService.obtenerTodos().stream()
            .map(this::toDTO)
            .toList();
    }

    // Obtener por dominio
    @GetMapping("/{dominio}")
    public ResponseEntity<CamionDTO> obtenerPorDominio(@PathVariable String dominio) {
        Camion camion = camionService.obtenerPorDominio(dominio);
        if (camion != null) {
            return ResponseEntity.ok(toDTO(camion));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Crear nuevo camión
    @PostMapping
    public ResponseEntity<CamionDTO> crear(@RequestBody CamionDTO camionDTO) {
        Camion creado = camionService.guardar(toEntity(camionDTO));
        return ResponseEntity.ok(toDTO(creado));
    }

    // Actualizar camión por dominio
    @PutMapping("/{dominio}")
    public ResponseEntity<CamionDTO> actualizar(@PathVariable String dominio, @RequestBody CamionDTO camionDTO) {
        Camion camion = toEntity(camionDTO);
        camion.setDominio(dominio);
        Camion actualizado = camionService.guardar(camion);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    // Eliminar camión por id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorDominio(@PathVariable long id) {
        camionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Listar camiones disponibles
    @GetMapping("/disponibles")
    public List<CamionDTO> listarDisponibles() {
        return camionService.obtenerDisponibles().stream()
            .map(this::toDTO)
            .toList();
    }

    private CamionDTO toDTO(Camion camion) {
        return new CamionDTO(
            camion.getId(),
            camion.getDominio(),
            camion.getCapacidadPesoKg(),
            camion.getCapacidadVolumenM3(),
            camion.getCostoKmBase(),
            camion.getConsumoPromedioLitrosKm(),
            camion.getDisponible(),
            camion.getTransportistaId(),
            camion.getCreatedAt()
        );
    }

    private Camion toEntity(CamionDTO dto) {
        Camion camion = new Camion();
        camion.setId(dto.getId());
        camion.setDominio(dto.getDominio());
        camion.setCapacidadPesoKg(dto.getCapacidadPesoKg());
        camion.setCapacidadVolumenM3(dto.getCapacidadVolumenM3());
        camion.setCostoKmBase(dto.getCostoKmBase());
        camion.setConsumoPromedioLitrosKm(dto.getConsumoPromedioLitrosKm());
        camion.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : Boolean.TRUE);
        camion.setTransportistaId(dto.getTransportistaId());
        camion.setCreatedAt(dto.getCreatedAt());
        return camion;
    }

    // Verificar disponibilidad para peso y volumen específicos
    @GetMapping("/verificar-disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam Double peso,
            @RequestParam Double volumen) {
        boolean disponible = camionService.verificarDisponibilidad(peso, volumen);
        return ResponseEntity.ok(disponible);
    }
}
