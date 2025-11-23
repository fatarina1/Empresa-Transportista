package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.TramoDTO;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.services.TramoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tramos")
public class TramoController {

    @Autowired
    private TramoService tramoService;

    @GetMapping
    public ResponseEntity<List<TramoDTO>> listarTodos() {
        List<TramoDTO> tramos = tramoService.obtenerTodos();
        return ResponseEntity.ok(tramos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TramoDTO> obtenerPorId(@PathVariable Long id) {
        return tramoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<List<TramoDTO>> obtenerPorRuta(@PathVariable Long rutaId) {
        List<TramoDTO> tramos = tramoService.obtenerPorRutaId(rutaId);
        return ResponseEntity.ok(tramos);
    }

    @GetMapping("/camion/{camionId}")
    public ResponseEntity<List<TramoDTO>> obtenerPorCamion(@PathVariable Long camionId) {
        List<TramoDTO> tramos = tramoService.obtenerPorCamionId(camionId);
        return ResponseEntity.ok(tramos);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<TramoDTO>> obtenerPorEstado(@PathVariable String estado) {
        try {
            Tramo.EstadoTramo estadoTramo = Tramo.EstadoTramo.valueOf(estado.toUpperCase());
            List<TramoDTO> tramos = tramoService.obtenerPorEstado(estadoTramo);
            return ResponseEntity.ok(tramos);
        } catch (IllegalArgumentException e) {
            log.error("Estado inválido: {}", estado);
            return ResponseEntity.badRequest().build();
        }
    }

    //Asignación de camión a un tramo
    @PatchMapping("/{id}/asignar-camion")
    public ResponseEntity<TramoDTO> asignarCamion(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        try {
            Long camionId = body.get("camionId");
            if (camionId == null) {
                return ResponseEntity.badRequest().build();
            }
            TramoDTO actualizado = tramoService.asignarCamion(id, camionId);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.error("Error al asignar camión: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * RF7: Iniciar tramo (registra fecha/hora inicio real)
     */
    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<TramoDTO> iniciarTramo(@PathVariable Long id) {
        try {
            TramoDTO actualizado = tramoService.iniciarTramo(id);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.error("Error al iniciar tramo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * RF7: Finalizar tramo (registra fecha/hora fin real, libera camión)
     */
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<TramoDTO> finalizarTramo(@PathVariable Long id) {
        try {
            TramoDTO actualizado = tramoService.finalizarTramo(id);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.error("Error al finalizar tramo: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}