package ar.edu.tpi.logistica.services;

import ar.edu.tpi.logistica.clients.SeguimientoServiceClient;
import ar.edu.tpi.logistica.dto.ContenedorEnDepositoDTO;
import ar.edu.tpi.logistica.entities.Deposito;
import ar.edu.tpi.logistica.entities.Tramo;
import ar.edu.tpi.logistica.entities.Ubicacion;
import ar.edu.tpi.logistica.repositories.DepositoRepository;
import ar.edu.tpi.logistica.repositories.RutaRepository;
import ar.edu.tpi.logistica.repositories.TramoRepository;
import ar.edu.tpi.logistica.repositories.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositoService {

    private final DepositoRepository depositoRepository;
    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;
    private final SeguimientoServiceClient seguimientoServiceClient;
    private final UbicacionRepository ubicacionRepository;

    /**
     * Guarda una ubicación en la base de datos
     * @param ubicacion Ubicación a guardar
     * @return Ubicación guardada con ID generado
     */
    @Transactional
    public Ubicacion guardarUbicacion(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    /**
     * 🔥 RF5: Obtener contenedores actualmente en un depósito
     */
    @Transactional(readOnly = true)
    public List<ContenedorEnDepositoDTO> obtenerContenedoresEnDeposito(Long depositoId, Long diasMinimo) {
        log.info("Consultando contenedores en depósito {}", depositoId);

        Deposito deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new IllegalArgumentException("Depósito no encontrado"));

        // Buscar tramos FINALIZADOS que terminaron en este depósito
        List<Tramo> tramosFinalizadosEnDeposito = tramoRepository.findAll().stream()
                .filter(t -> depositoId.equals(t.getDestinoDepositoId()) 
                        && Tramo.EstadoTramo.FINALIZADO.equals(t.getEstado()))
                .collect(Collectors.toList());

        List<ContenedorEnDepositoDTO> contenedores = new ArrayList<>();

        for (Tramo tramoLlegada : tramosFinalizadosEnDeposito) {
            // Verificar si el contenedor sigue en el depósito (el siguiente tramo NO ha iniciado)
            List<Tramo> tramosDeEstaSolicitud = tramoRepository.findByRutaIdOrderByOrdenAsc(tramoLlegada.getRuta().getId());
            
            // Buscar el siguiente tramo después del actual
            Tramo siguienteTramo = null;
            for (int i = 0; i < tramosDeEstaSolicitud.size() - 1; i++) {
                if (tramosDeEstaSolicitud.get(i).getId().equals(tramoLlegada.getId())) {
                    siguienteTramo = tramosDeEstaSolicitud.get(i + 1);
                    break;
                }
            }

            // Si no hay siguiente tramo o si el siguiente NO ha iniciado, el contenedor está en el depósito
            boolean contenedorEnDeposito = (siguienteTramo == null || 
                    siguienteTramo.getFechaInicioReal() == null);

            if (contenedorEnDeposito) {
                // Calcular días en depósito
                LocalDateTime fechaLlegada = tramoLlegada.getFechaFinReal();
                long diasEnDeposito = Duration.between(fechaLlegada, LocalDateTime.now()).toDays();

                // Filtrar por días mínimos si se especificó
                if (diasMinimo != null && diasEnDeposito < diasMinimo) {
                    continue;
                }

                // Obtener datos del contenedor desde seguimiento-service
                // Buscar solicitud por rutaId (relación invertida)
                Long rutaId = tramoLlegada.getRuta().getId();
                SeguimientoServiceClient.SolicitudResponseDTO solicitud = 
                        seguimientoServiceClient.obtenerSolicitudPorRutaId(rutaId);
                
                if (solicitud != null && solicitud.getContenedorId() != null) {
                    try {
                        SeguimientoServiceClient.ContenedorDetalleDTO contenedor = 
                                seguimientoServiceClient.obtenerContenedorPorSolicitudId(solicitud.getId());

                        if (contenedor != null) {
                            ContenedorEnDepositoDTO dto = new ContenedorEnDepositoDTO();
                            dto.setContenedorId(contenedor.getId());
                            dto.setIdentificacion(contenedor.getIdentificacion());
                            dto.setSolicitudId(solicitud.getId());
                            dto.setSolicitudNumero(solicitud.getNumero()); 
                            dto.setFechaLlegada(fechaLlegada);
                            dto.setDiasEnDeposito(diasEnDeposito);
                            dto.setPesoKg(contenedor.getPesoKg().doubleValue());
                            dto.setVolumenM3(contenedor.getVolumenM3().doubleValue());
                            
                            contenedores.add(dto);
                        }
                    } catch (Exception e) {
                        log.warn("No se pudo obtener datos del contenedor para ruta {}: {}", 
                                rutaId, e.getMessage());
                    }
                }
            }
        }

        // Ordenar por días en depósito (descendente)
        contenedores.sort((a, b) -> Long.compare(b.getDiasEnDeposito(), a.getDiasEnDeposito()));

        log.info("Encontrados {} contenedores en depósito {}", contenedores.size(), deposito.getNombre());
        return contenedores;
    }
}
