package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.tpi.logistica.entities.Ruta;
import ar.edu.tpi.logistica.repositories.RutaRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rutas")

public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @GetMapping
    public List<Ruta> listarTodos() {
        return rutaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerPorId(@PathVariable Long id) {
        Optional<Ruta> r = rutaRepository.findById(id);
        return r.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<Ruta> obtenerPorSolicitud(@PathVariable Long solicitudId) {
        Ruta r = rutaRepository.findBySolicitudId(solicitudId);
        if (r != null) return ResponseEntity.ok(r);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/solicitud-externa/{solicitudExternaId}")
    public ResponseEntity<Ruta> obtenerPorSolicitudExterna(@PathVariable Long solicitudExternaId) {
        Ruta r = rutaRepository.findBySolicitudExternaId(solicitudExternaId);
        if (r != null) return ResponseEntity.ok(r);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Ruta> crear(@RequestBody Ruta ruta) {
        Ruta creado = rutaRepository.save(ruta);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizar(@PathVariable Long id, @RequestBody Ruta ruta) {
        if (!rutaRepository.existsById(id)) return ResponseEntity.notFound().build();
        ruta.setId(id);
        Ruta actualizado = rutaRepository.save(ruta);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!rutaRepository.existsById(id)) return ResponseEntity.notFound().build();
        rutaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}