package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import ar.edu.tpi.logistica.entities.TarifaVolumen;
import ar.edu.tpi.logistica.repositories.TarifaVolumenRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tarifas-volumen")

public class TarifaVolumenController {

    @Autowired
    private TarifaVolumenRepository tarifaRepository;

    @GetMapping
    public List<TarifaVolumen> listarTodos() {
        return tarifaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaVolumen> obtenerPorId(@PathVariable Long id) {
        Optional<TarifaVolumen> t = tarifaRepository.findById(id);
        return t.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/por-volumen/{volumen}")
    public List<TarifaVolumen> obtenerPorVolumen(@PathVariable String volumen) {
        BigDecimal v = new BigDecimal(volumen);
        // busca tarifas cuyo rango incluya el volumen dado
        return tarifaRepository.findByVolumenMinM3LessThanEqualAndVolumenMaxM3GreaterThanEqual(v, v);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<TarifaVolumen> crear(@RequestBody TarifaVolumen tarifa) {
        TarifaVolumen creado = tarifaRepository.save(tarifa);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<TarifaVolumen> actualizar(@PathVariable Long id, @RequestBody TarifaVolumen tarifa) {
        if (!tarifaRepository.existsById(id)) return ResponseEntity.notFound().build();
        tarifa.setId(id);
        TarifaVolumen actualizado = tarifaRepository.save(tarifa);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tarifaRepository.existsById(id)) return ResponseEntity.notFound().build();
        tarifaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}