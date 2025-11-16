package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.tpi.logistica.dto.ContenedorEnDepositoDTO;
import ar.edu.tpi.logistica.entities.Deposito;
import ar.edu.tpi.logistica.repositories.DepositoRepository;
import ar.edu.tpi.logistica.services.DepositoService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/depositos")
public class DepositoController {

    @Autowired
    private DepositoRepository depositoRepository;

    @Autowired
    private DepositoService depositoService;

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
    public ResponseEntity<Deposito> crear(@RequestBody Deposito deposito) {
        // Primero guardamos la ubicación (si es nueva)
        if (deposito.getUbicacion() != null && deposito.getUbicacion().getId() == null) {
            deposito.setUbicacion(depositoService.guardarUbicacion(deposito.getUbicacion()));
        }
        Deposito creado = depositoRepository.save(deposito);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deposito> actualizar(@PathVariable Long id, @RequestBody Deposito deposito) {
        if (!depositoRepository.existsById(id)) return ResponseEntity.notFound().build();
        deposito.setId(id);
        Deposito actualizado = depositoRepository.save(deposito);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!depositoRepository.existsById(id)) return ResponseEntity.notFound().build();
        depositoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 🔥 RF5: Obtener contenedores actualmente en el depósito
     * Muestra contenedores que llegaron pero cuyo siguiente tramo no ha iniciado
     */
    @GetMapping("/{id}/contenedores")
    public ResponseEntity<List<ContenedorEnDepositoDTO>> obtenerContenedoresEnDeposito(
            @PathVariable Long id,
            @RequestParam(required = false) Long diasMinimo) {
        try {
            List<ContenedorEnDepositoDTO> contenedores = depositoService.obtenerContenedoresEnDeposito(id, diasMinimo);
            return ResponseEntity.ok(contenedores);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}