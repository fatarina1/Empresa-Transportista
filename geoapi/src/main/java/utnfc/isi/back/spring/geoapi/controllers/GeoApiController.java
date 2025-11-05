package utnfc.isi.back.spring.geoapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utnfc.isi.back.spring.geoapi.dto.CalcularRutaRequest;
import utnfc.isi.back.spring.geoapi.dto.RutaResponseDTO;
import utnfc.isi.back.spring.geoapi.services.GoogleMapsService;

@Slf4j
@RestController
@RequestMapping("/api/geoapi")
@Tag(name = "GeoAPI", description = "API para cálculo de rutas y distancias usando Google Maps")
public class GeoApiController {

    @Autowired
    private GoogleMapsService googleMapsService;

    @Operation(summary = "Calcular ruta entre dos puntos",
               description = "Calcula la distancia y duración estimada entre dos coordenadas geográficas")
    @PostMapping("/calcular-ruta")
    public ResponseEntity<RutaResponseDTO> calcularRuta(@RequestBody CalcularRutaRequest request) {
        log.info("Calculando ruta desde {} hasta {}", 
                request.getOrigen().getNombre(), 
                request.getDestino().getNombre());
        
        RutaResponseDTO ruta = googleMapsService.calcularRuta(
                request.getOrigen().getLatitud(),
                request.getOrigen().getLongitud(),
                request.getDestino().getLatitud(),
                request.getDestino().getLongitud()
        );
        
        return ResponseEntity.ok(ruta);
    }

    @Operation(summary = "Calcular distancia entre dos puntos",
               description = "Calcula únicamente la distancia en kilómetros entre dos coordenadas")
    @GetMapping("/distancia")
    public ResponseEntity<Double> calcularDistancia(
            @RequestParam Double origenLat,
            @RequestParam Double origenLng,
            @RequestParam Double destinoLat,
            @RequestParam Double destinoLng) {
        
        log.info("Calculando distancia");
        
        RutaResponseDTO ruta = googleMapsService.calcularRuta(
                origenLat, origenLng, destinoLat, destinoLng
        );
        
        return ResponseEntity.ok(ruta.getDistanciaKm());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GeoAPI is running");
    }
}
