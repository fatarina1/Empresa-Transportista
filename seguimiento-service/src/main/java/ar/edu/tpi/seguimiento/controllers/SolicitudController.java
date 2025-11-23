package ar.edu.tpi.seguimiento.controllers;

import ar.edu.tpi.seguimiento.dto.CambioEstadoDTO;
import ar.edu.tpi.seguimiento.dto.CrearSolicitudRequest;
import ar.edu.tpi.seguimiento.dto.SolicitudDTO;
import ar.edu.tpi.seguimiento.dto.SolicitudDetalleDTO;
import ar.edu.tpi.seguimiento.exceptions.RecursoNoEncontradoException;
import ar.edu.tpi.seguimiento.services.SolicitudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    @Autowired
    private SolicitudService solicitudService;

    @GetMapping
    public ResponseEntity<List<SolicitudDTO>> listarTodas(
            @RequestHeader(value = "X-User-Id", required = false) String username,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        
        List<SolicitudDTO> solicitudes;
        
        log.info("📥 Request recibido - Username: {}, Roles: {}", username, roles);
        
        // Si no hay información de usuario, devolver todas (para backwards compatibility)
        if (username == null || username.isBlank()) {
            log.warn("⚠️ No se recibió X-User-Id, devolviendo todas las solicitudes");
            solicitudes = solicitudService.obtenerTodas();
            return ResponseEntity.ok(solicitudes);
        }
        
        // Si el username indica que es un CLIENTE (patrón: cliente01, cliente02, etc.)
        if (username.startsWith("cliente")) {
            try {
                // Extraer el ID del username: "cliente01" -> 1, "cliente02" -> 2
                String numStr = username.replace("cliente", "").replaceAll("^0+", "");
                if (numStr.isEmpty()) numStr = "0"; // Edge case: "cliente00" -> 0
                
                Long clienteId = Long.parseLong(numStr);
                log.info("👤 CLIENTE detectado - Username: {}, ClienteId extraído: {}", username, clienteId);
                log.info("🔍 Filtrando solicitudes para clienteId: {}", clienteId);
                
                solicitudes = solicitudService.obtenerPorCliente(clienteId);
                
                log.info("✅ Solicitudes filtradas: {} solicitudes encontradas para cliente {}", 
                        solicitudes.size(), clienteId);
                
                return ResponseEntity.ok(solicitudes);
                
            } catch (NumberFormatException e) {
                log.error("❌ Error al extraer clienteId del username: {}", username);
                // En caso de error, devolver lista vacía por seguridad
                return ResponseEntity.ok(List.of());
            }
        }
        
        // Si es ADMIN, TRANSPORTISTA u otro rol, devolver todas las solicitudes
        log.info("👨‍💼 Usuario {} (rol: {}) consultando TODAS las solicitudes", username, roles);
        solicitudes = solicitudService.obtenerTodas();
        log.info("✅ Total de solicitudes devueltas: {}", solicitudes.size());
        
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDTO> obtenerPorId(@PathVariable Long id) {
        return solicitudService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/detalle")
    public ResponseEntity<SolicitudDetalleDTO> obtenerDetallePorId(@PathVariable Long id) {
        return solicitudService.obtenerDetallePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<SolicitudDTO> obtenerPorNumero(@PathVariable String numero) {
        return solicitudService.obtenerPorNumero(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<SolicitudDTO>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudDTO>> obtenerPorEstado(@PathVariable String estado) {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * RF: Obtener solicitud por rutaId (para logistica-service)
     */
    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<SolicitudDTO> obtenerPorRutaId(@PathVariable Long rutaId) {
        SolicitudDTO solicitud = solicitudService.obtenerPorRutaId(rutaId);
        if (solicitud == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(solicitud);
    }

    /**
     * Endpoint principal para crear una nueva solicitud de transporte.
     * Puede crear cliente y/o contenedor si no existen.
     */
    @PostMapping
    public ResponseEntity<SolicitudDTO> crear(@RequestBody CrearSolicitudRequest request) {
        try {
            SolicitudDTO creada = solicitudService.crearSolicitud(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear solicitud: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar el estado de una solicitud
     * Estados: BORRADOR, PROGRAMADA, EN_TRANSITO, ENTREGADA
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<SolicitudDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            SolicitudDTO actualizada = solicitudService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar estado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Asignar ruta a una solicitud
     * Llamado por logistica-service cuando se crea una ruta
     */
    @PatchMapping("/{id}/ruta")
    public ResponseEntity<SolicitudDTO> asignarRuta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Number rutaIdNumber = (Number) body.get("rutaId");
            if (rutaIdNumber == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Long rutaId = rutaIdNumber.longValue();
            SolicitudDTO actualizada = solicitudService.asignarRuta(id, rutaId);
            return ResponseEntity.ok(actualizada);
        } catch (RecursoNoEncontradoException e) {
            log.error("Solicitud no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al asignar ruta: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Actualizar costo final y tiempo real de una solicitud
     * Llamado por logistica-service cuando se finaliza la última etapa
     */
    @PatchMapping("/{id}/costos")
    public ResponseEntity<SolicitudDTO> actualizarCostosYTiempo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Number costoFinal = (Number) body.get("costoFinal");
            Integer tiempoRealMinutos = (Integer) body.get("tiempoRealMinutos");
            
            if (costoFinal == null || tiempoRealMinutos == null) {
                return ResponseEntity.badRequest().build();
            }
            
            SolicitudDTO actualizada = solicitudService.actualizarCostosYTiempo(
                    id, 
                    java.math.BigDecimal.valueOf(costoFinal.doubleValue()), 
                    tiempoRealMinutos
            );
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar costos: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualizar costo estimado y tiempo estimado de una solicitud
     * Llamado por logistica-service cuando se asigna una ruta con tramos
     */
    @PatchMapping("/{id}/costos-estimados")
    public ResponseEntity<SolicitudDTO> actualizarCostosYTiempoEstimados(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Number costoEstimado = (Number) body.get("costoEstimado");
            Integer tiempoEstimadoMin = (Integer) body.get("tiempoEstimadoMin");
            
            if (costoEstimado == null || tiempoEstimadoMin == null) {
                return ResponseEntity.badRequest().build();
            }
            
            SolicitudDTO actualizada = solicitudService.actualizarCostosYTiempoEstimados(
                    id, 
                    java.math.BigDecimal.valueOf(costoEstimado.doubleValue()), 
                    tiempoEstimadoMin
            );
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar costos estimados: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        solicitudService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener el historial completo de cambios de estado de una solicitud
     * Retorna todos los estados por los que pasó la solicitud, ordenados por fecha (más reciente primero)
     */
    @GetMapping("/{id}/historial-estados")
    public ResponseEntity<List<CambioEstadoDTO>> obtenerHistorialEstados(@PathVariable Long id) {
        try {
            List<CambioEstadoDTO> historial = solicitudService.obtenerHistorialEstados(id);
            return ResponseEntity.ok(historial);
        } catch (RecursoNoEncontradoException e) {
            log.error("Solicitud no encontrada: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}
