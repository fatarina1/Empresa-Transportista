package ar.edu.tpi.seguimiento.config;

import ar.edu.tpi.seguimiento.entities.Cliente;
import ar.edu.tpi.seguimiento.entities.Contenedor;
import ar.edu.tpi.seguimiento.entities.Solicitud;
import ar.edu.tpi.seguimiento.repositories.ClienteRepository;
import ar.edu.tpi.seguimiento.repositories.ContenedorRepository;
import ar.edu.tpi.seguimiento.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Configuración para cargar datos iniciales en la base de datos.
 * Se ejecuta automáticamente al iniciar la aplicación.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final ClienteRepository clienteRepository;
    private final ContenedorRepository contenedorRepository;
    private final SolicitudRepository solicitudRepository;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Solo cargar datos si la BD está vacía
            if (clienteRepository.count() > 0) {
                log.info("Base de datos ya contiene datos. Saltando seeder.");
                return;
            }

            log.info("Iniciando carga de datos de prueba...");

            // ============================================
            // CLIENTES
            // ============================================
            Cliente cliente1 = new Cliente();
            cliente1.setNombre("Juan Pérez");
            cliente1.setEmail("juan.perez@email.com");
            cliente1.setTelefono("351-123-4567");
            cliente1.setDocumento("30123456");
            cliente1.setDireccion("Av. Colón 1500, Córdoba");
            cliente1.setCreatedAt(LocalDateTime.now());
            clienteRepository.save(cliente1);

            Cliente cliente2 = new Cliente();
            cliente2.setNombre("María González");
            cliente2.setEmail("maria.gonzalez@email.com");
            cliente2.setTelefono("261-987-6543");
            cliente2.setDocumento("27987654");
            cliente2.setDireccion("San Martín 800, Mendoza");
            cliente2.setCreatedAt(LocalDateTime.now());
            clienteRepository.save(cliente2);

            Cliente cliente3 = new Cliente();
            cliente3.setNombre("Empresa Logística SA");
            cliente3.setEmail("contacto@logistica.com.ar");
            cliente3.setTelefono("341-555-1234");
            cliente3.setDocumento("CUIT 30-71234567-8");
            cliente3.setDireccion("Pellegrini 2000, Rosario");
            cliente3.setCreatedAt(LocalDateTime.now());
            clienteRepository.save(cliente3);

            log.info("✓ Cargados {} clientes", clienteRepository.count());

            // ============================================
            // CONTENEDORES
            // ============================================
            Contenedor contenedor1 = new Contenedor();
            contenedor1.setIdentificacion("CONT-001-2024");
            contenedor1.setPesoKg(new BigDecimal("500.50"));
            contenedor1.setVolumenM3(new BigDecimal("2.5"));
            contenedor1.setLargoM(new BigDecimal("2.0"));
            contenedor1.setAnchoM(new BigDecimal("1.5"));
            contenedor1.setAltoM(new BigDecimal("0.8"));
            contenedor1.setDescripcion("Mercadería general - Electrónica");
            contenedor1.setClienteId(cliente1.getId());
            contenedor1.setCreatedAt(LocalDateTime.now());
            contenedorRepository.save(contenedor1);

            Contenedor contenedor2 = new Contenedor();
            contenedor2.setIdentificacion("CONT-002-2024");
            contenedor2.setPesoKg(new BigDecimal("1200.00"));
            contenedor2.setVolumenM3(new BigDecimal("5.0"));
            contenedor2.setLargoM(new BigDecimal("2.5"));
            contenedor2.setAnchoM(new BigDecimal("2.0"));
            contenedor2.setAltoM(new BigDecimal("1.0"));
            contenedor2.setDescripcion("Alimentos no perecederos");
            contenedor2.setClienteId(cliente2.getId());
            contenedor2.setCreatedAt(LocalDateTime.now());
            contenedorRepository.save(contenedor2);

            Contenedor contenedor3 = new Contenedor();
            contenedor3.setIdentificacion("CONT-003-2024");
            contenedor3.setPesoKg(new BigDecimal("800.75"));
            contenedor3.setVolumenM3(new BigDecimal("3.2"));
            contenedor3.setLargoM(new BigDecimal("2.2"));
            contenedor3.setAnchoM(new BigDecimal("1.8"));
            contenedor3.setAltoM(new BigDecimal("0.9"));
            contenedor3.setDescripcion("Repuestos automotrices");
            contenedor3.setClienteId(cliente3.getId());
            contenedor3.setCreatedAt(LocalDateTime.now());
            contenedorRepository.save(contenedor3);

            log.info("✓ Cargados {} contenedores", contenedorRepository.count());

            // ============================================
            // SOLICITUDES
            // ============================================
            Solicitud solicitud1 = new Solicitud();
            solicitud1.setNumero("SOL-2024-001");
            solicitud1.setClienteId(cliente1.getId());
            solicitud1.setContenedorId(contenedor1.getId());
            solicitud1.setOrigenId(1L); // Córdoba (ID de ubicación en logística)
            solicitud1.setDestinoId(2L); // Mendoza (ID de ubicación en logística)
            // Datos de origen
            solicitud1.setOrigenDireccion("Av. Colón 1234, Córdoba");
            solicitud1.setOrigenLatitud(new BigDecimal("-31.416700"));
            solicitud1.setOrigenLongitud(new BigDecimal("-64.183300"));
            // Datos de destino
            solicitud1.setDestinoDireccion("San Martín 500, Mendoza");
            solicitud1.setDestinoLatitud(new BigDecimal("-32.890800"));
            solicitud1.setDestinoLongitud(new BigDecimal("-68.827200"));
            // Otros datos
            solicitud1.setEstado("PENDIENTE");
            solicitud1.setCostoEstimado(new BigDecimal("15000.00"));
            solicitud1.setTiempoEstimadoMin(465); // 7h 45min
            solicitud1.setObservaciones("Entrega urgente");
            solicitud1.setFechaSolicitud(LocalDateTime.now());
            solicitud1.setCreatedAt(LocalDateTime.now());
            solicitud1.setUpdatedAt(LocalDateTime.now());
            solicitudRepository.save(solicitud1);

            Solicitud solicitud2 = new Solicitud();
            solicitud2.setNumero("SOL-2024-002");
            solicitud2.setClienteId(cliente2.getId());
            solicitud2.setContenedorId(contenedor2.getId());
            solicitud2.setOrigenId(2L); // Mendoza
            solicitud2.setDestinoId(3L); // Rosario
            // Datos de origen
            solicitud2.setOrigenDireccion("San Martín 500, Mendoza");
            solicitud2.setOrigenLatitud(new BigDecimal("-32.890800"));
            solicitud2.setOrigenLongitud(new BigDecimal("-68.827200"));
            // Datos de destino
            solicitud2.setDestinoDireccion("Pellegrini 1000, Rosario");
            solicitud2.setDestinoLatitud(new BigDecimal("-32.946800"));
            solicitud2.setDestinoLongitud(new BigDecimal("-60.639300"));
            // Otros datos
            solicitud2.setEstado("EN_TRANSITO");
            solicitud2.setCostoEstimado(new BigDecimal("22000.00"));
            solicitud2.setTiempoEstimadoMin(600);
            solicitud2.setObservaciones("Manejo cuidadoso - Frágil");
            solicitud2.setFechaSolicitud(LocalDateTime.now().minusDays(1));
            solicitud2.setCreatedAt(LocalDateTime.now().minusDays(1));
            solicitud2.setUpdatedAt(LocalDateTime.now());
            solicitudRepository.save(solicitud2);

            Solicitud solicitud3 = new Solicitud();
            solicitud3.setNumero("SOL-2024-003");
            solicitud3.setClienteId(cliente3.getId());
            solicitud3.setContenedorId(contenedor3.getId());
            solicitud3.setOrigenId(3L); // Rosario
            solicitud3.setDestinoId(4L); // Buenos Aires
            // Datos de origen
            solicitud3.setOrigenDireccion("Pellegrini 1000, Rosario");
            solicitud3.setOrigenLatitud(new BigDecimal("-32.946800"));
            solicitud3.setOrigenLongitud(new BigDecimal("-60.639300"));
            // Datos de destino
            solicitud3.setDestinoDireccion("Av. Ramos Mejía 1302, CABA");
            solicitud3.setDestinoLatitud(new BigDecimal("-34.587500"));
            solicitud3.setDestinoLongitud(new BigDecimal("-58.373600"));
            // Otros datos
            solicitud3.setEstado("COMPLETADA");
            solicitud3.setCostoEstimado(new BigDecimal("12000.00"));
            solicitud3.setCostoFinal(new BigDecimal("11500.00"));
            solicitud3.setTiempoEstimadoMin(240);
            solicitud3.setTiempoRealMin(225);
            solicitud3.setObservaciones("Entrega satisfactoria");
            solicitud3.setFechaSolicitud(LocalDateTime.now().minusDays(7));
            solicitud3.setFechaEntrega(LocalDateTime.now().minusDays(5));
            solicitud3.setCreatedAt(LocalDateTime.now().minusDays(7));
            solicitud3.setUpdatedAt(LocalDateTime.now().minusDays(5));
            solicitudRepository.save(solicitud3);

            log.info("✓ Cargadas {} solicitudes", solicitudRepository.count());

            log.info("========================================");
            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("========================================");
        };
    }
}
