package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.entities.Camion;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.repositories.CamionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CostoService {

    private final CamionRepository camionRepository;

    // Valor por litro de combustible (configurable)
    @Value("${VALOR_LITRO_COMBUSTIBLE:450.0}")
    private Double valorLitroCombustible;

    /**
     * Calcula el costo real de un tramo usando los datos del camión asignado y distancia.
     * Fórmula (simplificada):
     *  costo_km = camion.costoKmBase * distanciaKm
     *  costo_combustible = camion.consumoPromedioLitrosKm * distanciaKm * valorLitroCombustible
     *  costoReal = costo_km + costo_combustible
     */
    public BigDecimal calcularCostoReal(Tramo tramo, Camion camion) {
        if (tramo.getDistanciaKm() == null) return BigDecimal.ZERO;

        double distancia = tramo.getDistanciaKm();

        double costoKm = 0.0;
        if (camion.getCostoKmBase() != null) {
            costoKm = camion.getCostoKmBase() * distancia;
        }

        double costoCombustible = 0.0;
        if (camion.getConsumoPromedioLitrosKm() != null) {
            costoCombustible = camion.getConsumoPromedioLitrosKm() * distancia * (valorLitroCombustible != null ? valorLitroCombustible : 0.0);
        }

        double total = costoKm + costoCombustible;
        return BigDecimal.valueOf(total).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
