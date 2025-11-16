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
            // SOLICITUDES - Sincronizadas con logistica-service
            // ============================================
            
            // SOLICITUD 1: Contenedor esperando en Rosario (hace 3 días)
            Solicitud solicitud1 = new Solicitud();
            solicitud1.setNumero("SOL-2024-001");
            solicitud1.setClienteId(cliente1.getId());
            solicitud1.setContenedorId(contenedor1.getId());
            solicitud1.setOrigenId(1L); // Córdoba
            solicitud1.setDestinoId(4L); // Buenos Aires
            // Datos de origen
            solicitud1.setOrigenDireccion("Av. Colón 1234, Córdoba");
            solicitud1.setOrigenLatitud(new BigDecimal("-31.416700"));
            solicitud1.setOrigenLongitud(new BigDecimal("-64.183300"));
            // Datos de destino
            solicitud1.setDestinoDireccion("Av. Ramos Mejía 1302, CABA");
            solicitud1.setDestinoLatitud(new BigDecimal("-34.587500"));
            solicitud1.setDestinoLongitud(new BigDecimal("-58.373600"));
            // Estado: EN_TRANSITO (primer tramo completado, esperando en Rosario)
            solicitud1.setEstado("EN_TRANSITO");
            solicitud1.setCostoEstimado(new BigDecimal("45000.00"));
            solicitud1.setTiempoEstimadoMin(720); // 12 horas
            solicitud1.setObservaciones("Ruta con depósito intermedio en Rosario - Contenedor esperando hace 3 días");
            solicitud1.setFechaSolicitud(LocalDateTime.now().minusDays(5));
            solicitud1.setCreatedAt(LocalDateTime.now().minusDays(5));
            solicitud1.setUpdatedAt(LocalDateTime.now().minusDays(3));
            solicitudRepository.save(solicitud1);

            // SOLICITUD 2: Contenedor esperando en Mendoza (hace 7 días)
            Solicitud solicitud2 = new Solicitud();
            solicitud2.setNumero("SOL-2024-002");
            solicitud2.setClienteId(cliente2.getId());
            solicitud2.setContenedorId(contenedor2.getId());
            solicitud2.setOrigenId(4L); // Buenos Aires
            solicitud2.setDestinoId(1L); // Córdoba
            // Datos de origen
            solicitud2.setOrigenDireccion("Av. Ramos Mejía 1302, CABA");
            solicitud2.setOrigenLatitud(new BigDecimal("-34.587500"));
            solicitud2.setOrigenLongitud(new BigDecimal("-58.373600"));
            // Datos de destino
            solicitud2.setDestinoDireccion("Av. Colón 1234, Córdoba");
            solicitud2.setDestinoLatitud(new BigDecimal("-31.416700"));
            solicitud2.setDestinoLongitud(new BigDecimal("-64.183300"));
            // Estado: EN_TRANSITO (primer tramo completado, esperando en Mendoza)
            solicitud2.setEstado("EN_TRANSITO");
            solicitud2.setCostoEstimado(new BigDecimal("78000.00"));
            solicitud2.setTiempoEstimadoMin(1200); // 20 horas
            solicitud2.setObservaciones("Ruta con depósito intermedio en Mendoza - Contenedor esperando hace 7 días");
            solicitud2.setFechaSolicitud(LocalDateTime.now().minusDays(10));
            solicitud2.setCreatedAt(LocalDateTime.now().minusDays(10));
            solicitud2.setUpdatedAt(LocalDateTime.now().minusDays(7));
            solicitudRepository.save(solicitud2);

            // SOLICITUD 3: Ruta simple (en planificación)
            Solicitud solicitud3 = new Solicitud();
            solicitud3.setNumero("SOL-2024-003");
            solicitud3.setClienteId(cliente3.getId());
            solicitud3.setContenedorId(contenedor3.getId());
            solicitud3.setOrigenId(1L); // Córdoba
            solicitud3.setDestinoId(2L); // Mendoza
            // Datos de origen
            solicitud3.setOrigenDireccion("Av. Colón 1234, Córdoba");
            solicitud3.setOrigenLatitud(new BigDecimal("-31.416700"));
            solicitud3.setOrigenLongitud(new BigDecimal("-64.183300"));
            // Datos de destino
            solicitud3.setDestinoDireccion("San Martín 500, Mendoza");
            solicitud3.setDestinoLatitud(new BigDecimal("-32.890800"));
            solicitud3.setDestinoLongitud(new BigDecimal("-68.827200"));
            // Estado: PROGRAMADA
            solicitud3.setEstado("PROGRAMADA");
            solicitud3.setCostoEstimado(new BigDecimal("35000.00"));
            solicitud3.setTiempoEstimadoMin(600); // 10 horas
            solicitud3.setObservaciones("Ruta directa sin depósitos intermedios");
            solicitud3.setFechaSolicitud(LocalDateTime.now().minusDays(1));
            solicitud3.setCreatedAt(LocalDateTime.now().minusDays(1));
            solicitud3.setUpdatedAt(LocalDateTime.now().minusDays(1));
            solicitudRepository.save(solicitud3);

            // ============================================
            // SOLICITUDES COMPLETADAS (ENTREGADAS)
            // ============================================

            // SOLICITUD 4: ENTREGADA - Completada hace 2 días (eficiente)
            Contenedor contenedor4 = new Contenedor();
            contenedor4.setIdentificacion("CONT-004-2024");
            contenedor4.setPesoKg(new BigDecimal("650.00"));
            contenedor4.setVolumenM3(new BigDecimal("3.8"));
            contenedor4.setLargoM(new BigDecimal("2.1"));
            contenedor4.setAnchoM(new BigDecimal("1.6"));
            contenedor4.setAltoM(new BigDecimal("1.1"));
            contenedor4.setDescripcion("Equipamiento médico");
            contenedor4.setClienteId(cliente1.getId());
            contenedor4.setCreatedAt(LocalDateTime.now().minusDays(15));
            contenedorRepository.save(contenedor4);

            Solicitud solicitud4 = new Solicitud();
            solicitud4.setNumero("SOL-2024-004");
            solicitud4.setClienteId(cliente1.getId());
            solicitud4.setContenedorId(contenedor4.getId());
            solicitud4.setOrigenId(1L); // Córdoba
            solicitud4.setDestinoId(4L); // Buenos Aires
            solicitud4.setOrigenDireccion("Av. Colón 1234, Córdoba");
            solicitud4.setOrigenLatitud(new BigDecimal("-31.416700"));
            solicitud4.setOrigenLongitud(new BigDecimal("-64.183300"));
            solicitud4.setDestinoDireccion("Av. Ramos Mejía 1302, CABA");
            solicitud4.setDestinoLatitud(new BigDecimal("-34.587500"));
            solicitud4.setDestinoLongitud(new BigDecimal("-58.373600"));
            solicitud4.setEstado("ENTREGADA");
            solicitud4.setCostoEstimado(new BigDecimal("52000.00"));
            solicitud4.setCostoFinal(new BigDecimal("48500.00")); // Mejor que estimado
            solicitud4.setTiempoEstimadoMin(720); // 12 horas
            solicitud4.setTiempoRealMin(680); // 11h 20min - MÁS RÁPIDO
            solicitud4.setObservaciones("Entrega completada exitosamente - Llegó antes de lo estimado");
            solicitud4.setFechaSolicitud(LocalDateTime.now().minusDays(12));
            solicitud4.setCreatedAt(LocalDateTime.now().minusDays(12));
            solicitud4.setUpdatedAt(LocalDateTime.now().minusDays(2));
            solicitudRepository.save(solicitud4);

            // SOLICITUD 5: ENTREGADA - Completada hace 5 días (con demoras)
            Contenedor contenedor5 = new Contenedor();
            contenedor5.setIdentificacion("CONT-005-2024");
            contenedor5.setPesoKg(new BigDecimal("1450.00"));
            contenedor5.setVolumenM3(new BigDecimal("6.5"));
            contenedor5.setLargoM(new BigDecimal("3.0"));
            contenedor5.setAnchoM(new BigDecimal("2.2"));
            contenedor5.setAltoM(new BigDecimal("1.0"));
            contenedor5.setDescripcion("Maquinaria industrial");
            contenedor5.setClienteId(cliente2.getId());
            contenedor5.setCreatedAt(LocalDateTime.now().minusDays(20));
            contenedorRepository.save(contenedor5);

            Solicitud solicitud5 = new Solicitud();
            solicitud5.setNumero("SOL-2024-005");
            solicitud5.setClienteId(cliente2.getId());
            solicitud5.setContenedorId(contenedor5.getId());
            solicitud5.setOrigenId(4L); // Buenos Aires
            solicitud5.setDestinoId(2L); // Mendoza
            solicitud5.setOrigenDireccion("Av. Ramos Mejía 1302, CABA");
            solicitud5.setOrigenLatitud(new BigDecimal("-34.587500"));
            solicitud5.setOrigenLongitud(new BigDecimal("-58.373600"));
            solicitud5.setDestinoDireccion("San Martín 500, Mendoza");
            solicitud5.setDestinoLatitud(new BigDecimal("-32.890800"));
            solicitud5.setDestinoLongitud(new BigDecimal("-68.827200"));
            solicitud5.setEstado("ENTREGADA");
            solicitud5.setCostoEstimado(new BigDecimal("95000.00"));
            solicitud5.setCostoFinal(new BigDecimal("108500.00")); // Peor que estimado
            solicitud5.setTiempoEstimadoMin(1200); // 20 horas
            solicitud5.setTiempoRealMin(1380); // 23 horas - MÁS LENTO
            solicitud5.setObservaciones("Entrega completada con demoras por tráfico - Penalización por tiempo extra");
            solicitud5.setFechaSolicitud(LocalDateTime.now().minusDays(18));
            solicitud5.setCreatedAt(LocalDateTime.now().minusDays(18));
            solicitud5.setUpdatedAt(LocalDateTime.now().minusDays(5));
            solicitudRepository.save(solicitud5);

            // SOLICITUD 6: ENTREGADA - Completada hace 1 semana (perfecta)
            Contenedor contenedor6 = new Contenedor();
            contenedor6.setIdentificacion("CONT-006-2024");
            contenedor6.setPesoKg(new BigDecimal("320.00"));
            contenedor6.setVolumenM3(new BigDecimal("2.0"));
            contenedor6.setLargoM(new BigDecimal("1.8"));
            contenedor6.setAnchoM(new BigDecimal("1.2"));
            contenedor6.setAltoM(new BigDecimal("0.9"));
            contenedor6.setDescripcion("Productos farmacéuticos");
            contenedor6.setClienteId(cliente3.getId());
            contenedor6.setCreatedAt(LocalDateTime.now().minusDays(10));
            contenedorRepository.save(contenedor6);

            Solicitud solicitud6 = new Solicitud();
            solicitud6.setNumero("SOL-2024-006");
            solicitud6.setClienteId(cliente3.getId());
            solicitud6.setContenedorId(contenedor6.getId());
            solicitud6.setOrigenId(1L); // Córdoba
            solicitud6.setDestinoId(2L); // Mendoza
            solicitud6.setOrigenDireccion("Av. Colón 1234, Córdoba");
            solicitud6.setOrigenLatitud(new BigDecimal("-31.416700"));
            solicitud6.setOrigenLongitud(new BigDecimal("-64.183300"));
            solicitud6.setDestinoDireccion("San Martín 500, Mendoza");
            solicitud6.setDestinoLatitud(new BigDecimal("-32.890800"));
            solicitud6.setDestinoLongitud(new BigDecimal("-68.827200"));
            solicitud6.setEstado("ENTREGADA");
            solicitud6.setCostoEstimado(new BigDecimal("42000.00"));
            solicitud6.setCostoFinal(new BigDecimal("42000.00")); // Exacto como estimado
            solicitud6.setTiempoEstimadoMin(600); // 10 horas
            solicitud6.setTiempoRealMin(600); // Exacto 10 horas - PERFECTO
            solicitud6.setObservaciones("Entrega impecable - Sin bonificaciones ni penalizaciones");
            solicitud6.setFechaSolicitud(LocalDateTime.now().minusDays(8));
            solicitud6.setCreatedAt(LocalDateTime.now().minusDays(8));
            solicitud6.setUpdatedAt(LocalDateTime.now().minusDays(7));
            solicitudRepository.save(solicitud6);

            // SOLICITUD 7: BORRADOR - Recién creada, sin procesar
            Contenedor contenedor7 = new Contenedor();
            contenedor7.setIdentificacion("CONT-007-2024");
            contenedor7.setPesoKg(new BigDecimal("890.00"));
            contenedor7.setVolumenM3(new BigDecimal("4.2"));
            contenedor7.setLargoM(new BigDecimal("2.3"));
            contenedor7.setAnchoM(new BigDecimal("1.9"));
            contenedor7.setAltoM(new BigDecimal("1.0"));
            contenedor7.setDescripcion("Textiles y confecciones");
            contenedor7.setClienteId(cliente1.getId());
            contenedor7.setCreatedAt(LocalDateTime.now().minusHours(4));
            contenedorRepository.save(contenedor7);

            Solicitud solicitud7 = new Solicitud();
            solicitud7.setNumero("SOL-2024-007");
            solicitud7.setClienteId(cliente1.getId());
            solicitud7.setContenedorId(contenedor7.getId());
            solicitud7.setOrigenId(3L); // Rosario
            solicitud7.setDestinoId(1L); // Córdoba
            solicitud7.setOrigenDireccion("Pellegrini 2000, Rosario");
            solicitud7.setOrigenLatitud(new BigDecimal("-32.944400"));
            solicitud7.setOrigenLongitud(new BigDecimal("-60.650600"));
            solicitud7.setDestinoDireccion("Av. Colón 1234, Córdoba");
            solicitud7.setDestinoLatitud(new BigDecimal("-31.416700"));
            solicitud7.setDestinoLongitud(new BigDecimal("-64.183300"));
            solicitud7.setEstado("BORRADOR");
            solicitud7.setCostoEstimado(new BigDecimal("28000.00"));
            solicitud7.setTiempoEstimadoMin(480); // 8 horas
            solicitud7.setObservaciones("Solicitud nueva - Pendiente de planificación de ruta");
            solicitud7.setFechaSolicitud(LocalDateTime.now().minusHours(4));
            solicitud7.setCreatedAt(LocalDateTime.now().minusHours(4));
            solicitud7.setUpdatedAt(LocalDateTime.now().minusHours(4));
            solicitudRepository.save(solicitud7);

            log.info("✓ Cargadas {} solicitudes", solicitudRepository.count());
            log.info("  - Solicitud 1 (ID=1): EN_TRANSITO - Contenedor esperando en Rosario (3 días)");
            log.info("  - Solicitud 2 (ID=2): EN_TRANSITO - Contenedor esperando en Mendoza (7 días)");
            log.info("  - Solicitud 3 (ID=3): PROGRAMADA - En planificación");
            log.info("  - Solicitud 4 (ID=4): ENTREGADA ✅ - Completada hace 2 días (eficiente, costo $48,500)");
            log.info("  - Solicitud 5 (ID=5): ENTREGADA ✅ - Completada hace 5 días (demoras, costo $108,500)");
            log.info("  - Solicitud 6 (ID=6): ENTREGADA ✅ - Completada hace 7 días (perfecta, costo $42,000)");
            log.info("  - Solicitud 7 (ID=7): BORRADOR - Recién creada hace 4 horas");

            log.info("========================================");
            log.info("✅ Datos de prueba cargados exitosamente");
            log.info("========================================");
        };
    }
}
