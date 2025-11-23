package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.CalcularRutaTentativaRequest;
import ar.edu.tpi.logistica.dto.CrearRutaConTramosRequest;
import ar.edu.tpi.logistica.dto.RutaDTO;
import ar.edu.tpi.logistica.dto.RutasAlternativasDTO;
import ar.edu.tpi.logistica.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.logistica.exceptions.DatosInvalidosException;
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

    /**
     * Calcular rutas alternativas con costos estimados.
     * Genera 2 alternativas:
     * 1. Ruta directa (origen → destino)
     * 2. Ruta con depósitos intermedios (selección automática inteligente)
     * 
     * Los depósitos se seleccionan automáticamente según optimización geográfica.
     * Permite consultar rutas tentativas ANTES de asignar la ruta a una solicitud.
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

    /**
     * Crear ruta completa con tramos a partir de una ruta tentativa seleccionada
     * Este es el endpoint recomendado para asignar rutas a solicitudes
     */
    @PostMapping("/crear-con-tramos")
    public ResponseEntity<RutaDTO> crearRutaConTramos(@RequestBody CrearRutaConTramosRequest request) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Creando ruta para solicitud {} con {} tramos", 
                correlationId, request.getSolicitudId(), request.getRutaTentativa().getCantidadTramos());
        
        RutaDTO rutaCreada = rutaService.crearRutaConTramos(
                request.getSolicitudId(),
                request.getRutaTentativa(), 
                request.getObservaciones()
        );
        
        log.info("[CID={}] Ruta {} creada y vinculada con solicitud {} - {} tramos", 
                correlationId, rutaCreada.getId(), request.getSolicitudId(), rutaCreada.getTramos().size());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(rutaCreada);
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

    /**
     * Obtener información de tracking en tiempo real de una ruta
     * Retorna ubicación actual, progreso, tramos y ETA
     */
    @GetMapping("/{id}/tracking")
    public ResponseEntity<?> obtenerTracking(@PathVariable Long id) {
        String correlationId = MDC.get("correlationId");
        log.info("[CID={}] Obteniendo información de tracking para ruta {}", correlationId, id);
        
        try {
            var trackingInfo = rutaService.obtenerTrackingInfo(id);
            return ResponseEntity.ok(trackingInfo);
        } catch (RecursoNoEncontradoException e) {
            log.warn("[CID={}] Ruta {} no encontrada para tracking", correlationId, id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("[CID={}] Error al obtener tracking para ruta {}: {}", correlationId, id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al obtener información de tracking"));
        }
    }
}