package ar.edu.tpi.logistica.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ar.edu.tpi.logistica.dto.TarifaVolumenDTO;
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
    public List<TarifaVolumenDTO> listarTodos() {
        return tarifaRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaVolumenDTO> obtenerPorId(@PathVariable Long id) {
        Optional<TarifaVolumen> t = tarifaRepository.findById(id);
        return t.map(this::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/por-volumen/{volumen}")
    public List<TarifaVolumenDTO> obtenerPorVolumen(@PathVariable String volumen) {
        BigDecimal v = new BigDecimal(volumen);
        // busca tarifas cuyo rango incluya el volumen dado
        return tarifaRepository.findByVolumenMinM3LessThanEqualAndVolumenMaxM3GreaterThanEqual(v, v)
            .stream()
            .map(this::toDTO)
            .toList();
    }

    @PostMapping
    public ResponseEntity<TarifaVolumenDTO> crear(@RequestBody TarifaVolumenDTO tarifa) {
        TarifaVolumen creado = tarifaRepository.save(toEntity(tarifa));
        return ResponseEntity.ok(toDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifaVolumenDTO> actualizar(@PathVariable Long id, @RequestBody TarifaVolumenDTO tarifa) {
        if (!tarifaRepository.existsById(id)) return ResponseEntity.notFound().build();
        TarifaVolumen entidad = toEntity(tarifa);
        entidad.setId(id);
        TarifaVolumen actualizado = tarifaRepository.save(entidad);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!tarifaRepository.existsById(id)) return ResponseEntity.notFound().build();
        tarifaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    private TarifaVolumenDTO toDTO(TarifaVolumen tarifa) {
        return new TarifaVolumenDTO(
            tarifa.getId(),
            tarifa.getVolumenMinM3(),
            tarifa.getVolumenMaxM3(),
            tarifa.getCostoKmBase()
        );
    }

    private TarifaVolumen toEntity(TarifaVolumenDTO dto) {
        TarifaVolumen tarifa = new TarifaVolumen();
        tarifa.setId(dto.getId());
        tarifa.setVolumenMinM3(dto.getVolumenMinM3());
        tarifa.setVolumenMaxM3(dto.getVolumenMaxM3());
        tarifa.setCostoKmBase(dto.getCostoKmBase());
        return tarifa;
    }
}
