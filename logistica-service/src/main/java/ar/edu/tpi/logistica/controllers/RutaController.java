package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.RutaDTO;
import ar.edu.tpi.logistica.services.RutaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/rutas")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    @GetMapping
    public ResponseEntity<List<RutaDTO>> listarTodas() {
        List<RutaDTO> rutas = rutaService.obtenerTodas();
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaDTO> obtenerPorId(@PathVariable Long id) {
        return rutaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<RutaDTO> obtenerPorSolicitudId(@PathVariable Long solicitudId) {
        return rutaService.obtenerPorSolicitudId(solicitudId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RutaDTO> crear(@RequestBody RutaDTO rutaDTO) {
        try {
            RutaDTO creada = rutaService.crear(rutaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear ruta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutaDTO> actualizar(@PathVariable Long id, @RequestBody RutaDTO rutaDTO) {
        try {
            RutaDTO actualizada = rutaService.actualizar(id, rutaDTO);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar ruta: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            if (nuevoEstado == null) {
                return ResponseEntity.badRequest().build();
            }
            rutaService.cambiarEstado(id, nuevoEstado);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al cambiar estado: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            rutaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}