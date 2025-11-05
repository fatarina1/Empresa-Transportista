package ar.edu.tpi.seguimiento.controllers;

import ar.edu.tpi.seguimiento.dto.CrearSolicitudRequest;
import ar.edu.tpi.seguimiento.dto.SolicitudDTO;
import ar.edu.tpi.seguimiento.dto.SolicitudDetalleDTO;
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
    public ResponseEntity<List<SolicitudDTO>> listarTodas() {
        List<SolicitudDTO> solicitudes = solicitudService.obtenerTodas();
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        solicitudService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
