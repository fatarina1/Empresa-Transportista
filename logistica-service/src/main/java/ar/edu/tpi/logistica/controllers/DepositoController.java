package ar.edu.tpi.logistica.controllers;

import ar.edu.tpi.logistica.dto.ContenedorEnDepositoDTO;
import ar.edu.tpi.logistica.dto.DepositoDTO;
import ar.edu.tpi.logistica.dto.UbicacionDTO;
import ar.edu.tpi.logistica.entities.Deposito;
import ar.edu.tpi.logistica.entities.Ubicacion;
import ar.edu.tpi.logistica.repositories.DepositoRepository;
import ar.edu.tpi.logistica.services.DepositoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<DepositoDTO> listarTodos() {
        return depositoRepository.findAll().stream()
            .map(this::toDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoDTO> obtenerPorId(@PathVariable Long id) {
        Optional<Deposito> d = depositoRepository.findById(id);
        return d.map(this::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-nombre/{nombre}")
    public ResponseEntity<DepositoDTO> obtenerPorNombre(@PathVariable String nombre) {
        Deposito d = depositoRepository.findByNombre(nombre);
        if (d != null) return ResponseEntity.ok(toDTO(d));
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<DepositoDTO> crear(@RequestBody DepositoDTO depositoDTO) {
        Deposito deposito = toEntity(depositoDTO);
        if (deposito.getUbicacion() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (deposito.getUbicacion().getId() == null) {
            deposito.setUbicacion(depositoService.guardarUbicacion(deposito.getUbicacion()));
        }
        Deposito creado = depositoRepository.save(deposito);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepositoDTO> actualizar(@PathVariable Long id, @RequestBody DepositoDTO depositoDTO) {
        Optional<Deposito> existente = depositoRepository.findById(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Deposito deposito = existente.get();
        if (depositoDTO.getNombre() != null) {
            deposito.setNombre(depositoDTO.getNombre());
        }
        if (depositoDTO.getCostoEstadiaDiaria() != null) {
            deposito.setCostoEstadiaDiaria(depositoDTO.getCostoEstadiaDiaria());
        }
        if (depositoDTO.getActivo() != null) {
            deposito.setActivo(depositoDTO.getActivo());
        }

        Ubicacion nuevaUbicacion = construirUbicacionDesdeDto(depositoDTO);
        if (nuevaUbicacion != null) {
            if (nuevaUbicacion.getId() == null) {
                deposito.setUbicacion(depositoService.guardarUbicacion(nuevaUbicacion));
            } else {
                deposito.setUbicacion(nuevaUbicacion);
            }
        }

        Deposito actualizado = depositoRepository.save(deposito);
        return ResponseEntity.ok(toDTO(actualizado));
    }

    private DepositoDTO toDTO(Deposito deposito) {
        return new DepositoDTO(
            deposito.getId(),
            deposito.getNombre(),
            deposito.getUbicacion() != null ? deposito.getUbicacion().getDireccion() : null,
            deposito.getUbicacion() != null ? deposito.getUbicacion().getLatitud() : null,
            deposito.getUbicacion() != null ? deposito.getUbicacion().getLongitud() : null,
            deposito.getCostoEstadiaDiaria(),
            null,
            deposito.getActivo(),
            deposito.getUbicacion() != null ? toUbicacionDTO(deposito.getUbicacion()) : null
        );
    }

    private Deposito toEntity(DepositoDTO dto) {
        Deposito deposito = new Deposito();
        deposito.setId(dto.getId());
        deposito.setNombre(dto.getNombre());
        deposito.setCostoEstadiaDiaria(dto.getCostoEstadiaDiaria());
        deposito.setActivo(dto.getActivo() != null ? dto.getActivo() : Boolean.TRUE);
        deposito.setUbicacion(construirUbicacionDesdeDto(dto));
        return deposito;
    }

    private Ubicacion construirUbicacionDesdeDto(DepositoDTO dto) {
        if (dto.getUbicacion() == null && dto.getDireccion() == null) {
            return null;
        }

        Ubicacion ubicacion = new Ubicacion();
        if (dto.getUbicacion() != null) {
            UbicacionDTO u = dto.getUbicacion();
            ubicacion.setId(u.getId());
            ubicacion.setNombre(u.getNombre());
            ubicacion.setDireccion(u.getDireccion());
            ubicacion.setLatitud(u.getLatitud());
            ubicacion.setLongitud(u.getLongitud());
            ubicacion.setTipo(u.getTipo() != null ? u.getTipo() : "DEPOSITO");
        } else {
            ubicacion.setDireccion(dto.getDireccion());
            ubicacion.setLatitud(dto.getLatitud());
            ubicacion.setLongitud(dto.getLongitud());
            ubicacion.setNombre(dto.getNombre());
            ubicacion.setTipo("DEPOSITO");
        }
        return ubicacion;
    }

    private UbicacionDTO toUbicacionDTO(Ubicacion ubicacion) {
        return new UbicacionDTO(
            ubicacion.getId(),
            ubicacion.getNombre(),
            ubicacion.getDireccion(),
            ubicacion.getLatitud(),
            ubicacion.getLongitud(),
            ubicacion.getTipo()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!depositoRepository.existsById(id)) return ResponseEntity.notFound().build();
        depositoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 🆕 RF5: Obtener contenedores actualmente en el depósito
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
