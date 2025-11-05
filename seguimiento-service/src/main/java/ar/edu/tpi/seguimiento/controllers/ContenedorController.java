package ar.edu.tpi.seguimiento.controllers;

import ar.edu.tpi.seguimiento.dto.ContenedorDTO;
import ar.edu.tpi.seguimiento.services.ContenedorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/contenedores")
public class ContenedorController {

    @Autowired
    private ContenedorService contenedorService;

    @GetMapping
    public ResponseEntity<List<ContenedorDTO>> listarTodos() {
        List<ContenedorDTO> contenedores = contenedorService.obtenerTodos();
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenedorDTO> obtenerPorId(@PathVariable Long id) {
        return contenedorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContenedorDTO>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<ContenedorDTO> contenedores = contenedorService.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(contenedores);
    }

    @PostMapping
    public ResponseEntity<ContenedorDTO> crear(@RequestBody ContenedorDTO contenedorDTO) {
        try {
            ContenedorDTO creado = contenedorService.crear(contenedorDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear contenedor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenedorDTO> actualizar(@PathVariable Long id, @RequestBody ContenedorDTO contenedorDTO) {
        try {
            ContenedorDTO actualizado = contenedorService.actualizar(id, contenedorDTO);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar contenedor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        contenedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
