package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.dto.CamionDTO;
import ar.edu.tpi.logistica.services.CamionService;

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

    //obtener por dominio
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
    public ResponseEntity<CamionDTO> crear(@RequestBody Camion camion) {
        Camion creado = camionService.guardar(camion);
        return ResponseEntity.ok(toDTO(creado));
    }

    // Actualizar camión por dominio
    @PutMapping("/{dominio}")
    public ResponseEntity<CamionDTO> actualizar(@PathVariable String dominio, @RequestBody Camion camion) {
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

    // Verificar disponibilidad para peso y volumen específicos
    @GetMapping("/verificar-disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam Double peso,
            @RequestParam Double volumen) {
        boolean disponible = camionService.verificarDisponibilidad(peso, volumen);
        return ResponseEntity.ok(disponible);
    }

    
}
