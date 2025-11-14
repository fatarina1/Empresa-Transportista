package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.services.CamionService;

import java.util.List;

@RestController
@RequestMapping("/camiones")

public class CamionController {

    @Autowired
    private CamionService camionService;

    // Listar todos los camiones
    @GetMapping
    public List<Camion> listarTodos() {
        return camionService.obtenerTodos();
    }

    //obtener por dominio
    @GetMapping("/{dominio}")
    public ResponseEntity<Camion> obtenerPorDominio(@PathVariable String dominio) {
        Camion camion = camionService.obtenerPorDominio(dominio);
        if (camion != null) {
            return ResponseEntity.ok(camion);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Crear nuevo camión
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Camion> crear(@RequestBody Camion camion) {
        Camion creado = camionService.guardar(camion);
        return ResponseEntity.ok(creado);
    }

    // Actualizar camión por dominio
    @PutMapping("/{dominio}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Camion> actualizar(@PathVariable String dominio, @RequestBody Camion camion) {
        camion.setDominio(dominio);
        Camion actualizado = camionService.guardar(camion);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar camión por id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> eliminarPorDominio(@PathVariable long id) {
        camionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Listar camiones disponibles
    @GetMapping("/disponibles")
    public List<Camion> listarDisponibles() {
        return camionService.obtenerDisponibles();
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
