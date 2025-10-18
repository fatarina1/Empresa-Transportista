package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.repositories.TramoRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tramos")

public class TramoController {

    @Autowired
    private TramoRepository tramoRepository;

    @GetMapping
    public List<Tramo> listarTodos() {
        return tramoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tramo> obtenerPorId(@PathVariable Long id) {
        Optional<Tramo> t = tramoRepository.findById(id);
        return t.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ruta/{rutaId}")
    public List<Tramo> listarPorRuta(@PathVariable Long rutaId) {
        return tramoRepository.findByRutaIdOrderByOrden(rutaId);
    }

    @PostMapping
    public ResponseEntity<Tramo> crear(@RequestBody Tramo tramo) {
        Tramo creado = tramoRepository.save(tramo);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tramo> actualizar(@PathVariable Long id, @RequestBody Tramo tramo) {
        if (!tramoRepository.existsById(id)) return ResponseEntity.notFound().build();
        tramo.setId(id);
        Tramo actualizado = tramoRepository.save(tramo);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tramoRepository.existsById(id)) return ResponseEntity.notFound().build();
        tramoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}