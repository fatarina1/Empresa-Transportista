package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import ar.edu.tpi.logistica.entities.Deposito;
import ar.edu.tpi.logistica.repositories.DepositoRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/depositos")

public class DepositoController {

    @Autowired
    private DepositoRepository depositoRepository;

    @GetMapping
    public List<Deposito> listarTodos() {
        return depositoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deposito> obtenerPorId(@PathVariable Long id) {
        Optional<Deposito> d = depositoRepository.findById(id);
        return d.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-nombre/{nombre}")
    public ResponseEntity<Deposito> obtenerPorNombre(@PathVariable String nombre) {
        Deposito d = depositoRepository.findByNombre(nombre);
        if (d != null) return ResponseEntity.ok(d);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Deposito> crear(@RequestBody Deposito deposito) {
        Deposito creado = depositoRepository.save(deposito);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Deposito> actualizar(@PathVariable Long id, @RequestBody Deposito deposito) {
        if (!depositoRepository.existsById(id)) return ResponseEntity.notFound().build();
        deposito.setId(id);
        Deposito actualizado = depositoRepository.save(deposito);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!depositoRepository.existsById(id)) return ResponseEntity.notFound().build();
        depositoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}