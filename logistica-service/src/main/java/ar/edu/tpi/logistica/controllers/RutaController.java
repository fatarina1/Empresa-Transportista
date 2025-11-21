package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.CalcularRutaTentativaRequest;
import ar.edu.tpi.logistica.dto.RutaDTO;
import ar.edu.tpi.logistica.dto.RutaTentativaDTO;
import ar.edu.tpi.logistica.dto.RutasAlternativasDTO;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
import ar.edu.tpi.logistica.exceptions.ReglaNegocioException;
import ar.edu.tpi.logistica.services.CostoService;
import ar.edu.tpi.logistica.services.RutaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rutas")
public class RutaController {

    private static final Logger log = LoggerFactory.getLogger(RutaController.class);

    @Autowired
    private RutaService rutaService;

    @Autowired
    private CostoService costoService;

    @GetMapping
    public ResponseEntity<List<RutaDTO>> listarTodas() {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Listando todas las rutas", correlationId);
        List<RutaDTO> rutas = rutaService.obtenerTodas();
        log.info("[CID={}] Se encontraron {} rutas", correlationId, rutas.size());
        return ResponseEntity.ok(rutas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RutaDTO> obtenerPorId(@PathVariable Long id) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Obteniendo ruta con ID: {}", correlationId, id);
        return rutaService.obtenerPorId(id)
                .map(ruta -> {
                    log.debug("[CID={}] Ruta encontrada: {}", correlationId, ruta);
                    return ResponseEntity.ok(ruta);
                })
                .orElseThrow(() -> {
                    log.warn("[CID={}] Ruta con ID {} no encontrada", correlationId, id);
                    return new RecursoNoEncontradoException("Ruta", id);
                });
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<RutaDTO> obtenerPorSolicitudId(@PathVariable Long solicitudId) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Obteniendo ruta para solicitud ID: {}", correlationId, solicitudId);
        return rutaService.obtenerPorSolicitudId(solicitudId)
                .map(ruta -> {
                    log.debug("[CID={}] Ruta encontrada para solicitud {}", correlationId, solicitudId);
                    return ResponseEntity.ok(ruta);
                })
                .orElseThrow(() -> {
                    log.warn("[CID={}] No se encontró ruta para solicitud ID {}", correlationId, solicitudId);
                    return new RecursoNoEncontradoException("Ruta para solicitud " + solicitudId);
                });
    }

    /**
     *Calcular ruta tentativa con costos estimados
     * Permite consultar rutas tentativas con tramos, tiempo y costo estimado
     * ANTES de asignar la ruta a una solicitud
     * Retorna múltiples alternativas de rutas (2-4 opciones)
     */
    @PostMapping("/calcular-tentativa")
    public ResponseEntity<RutasAlternativasDTO> calcularRutaTentativa(
            @RequestBody CalcularRutaTentativaRequest request) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Calculando rutas alternativas desde {} hasta {}", 
            correlationId, request.getUbicacionOrigenId(), request.getUbicacionDestinoId());
        
        RutasAlternativasDTO rutasAlternativas = costoService.calcularRutaTentativa(request);
        
        log.info("[CID={}] {} rutas alternativas calculadas - {}", 
            correlationId, rutasAlternativas.getCantidadAlternativas(), 
            rutasAlternativas.getObservaciones());
        
        return ResponseEntity.ok(rutasAlternativas);
    }

    @PostMapping
    public ResponseEntity<RutaDTO> crear(@RequestBody RutaDTO rutaDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Creando nueva ruta para solicitud ID: {}", correlationId, rutaDTO.getSolicitudExternaId());
        
        RutaDTO creada = rutaService.crear(rutaDTO);
        
        log.info("[CID={}] Ruta creada exitosamente con ID: {}", correlationId, creada.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RutaDTO> actualizar(@PathVariable Long id, @RequestBody RutaDTO rutaDTO) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Actualizando ruta ID: {}", correlationId, id);
        
        RutaDTO actualizada = rutaService.actualizar(id, rutaDTO);
        
        log.info("[CID={}] Ruta ID {} actualizada exitosamente", correlationId, id);
        return ResponseEntity.ok(actualizada);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String correlationId = MDC.get("correlationId");
        String nuevoEstado = body.get("estado");
        
        if (nuevoEstado == null) {
            log.warn("[CID={}] Intento de cambiar estado sin proporcionar estado", correlationId);
            throw new DatosInvalidosException("El campo 'estado' es requerido");
        }
        
        log.info("[CID={}] Cambiando estado de ruta ID {} a: {}", correlationId, id, nuevoEstado);
        rutaService.cambiarEstado(id, nuevoEstado);
        log.info("[CID={}] Estado de ruta ID {} cambiado exitosamente", correlationId, id);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Obtener costo real de una ruta finalizada
     * Retorna el desglose completo del costo real calculado
     */
    @GetMapping("/{id}/costo-real")
    public ResponseEntity<?> obtenerCostoReal(@PathVariable Long id) {
        try {
            var costoReal = costoService.calcularCostoReal(id);
            return ResponseEntity.ok(costoReal);
        } catch (IllegalArgumentException e) {
            log.error("Error al calcular costo real: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al calcular costo real: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al calcular costo real"));
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