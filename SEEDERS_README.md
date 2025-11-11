# 🌱 Seeders de Datos Iniciales

Este documento explica cómo funcionan los seeders automáticos para cargar datos de prueba en los microservicios.

## ¿Qué son los Seeders?

Los **seeders** son clases que cargan automáticamente datos iniciales en la base de datos cuando la aplicación se inicia. Son útiles para:

- Desarrollo y testing
- Demos y presentaciones
- Tener datos de ejemplo listos para usar

## 📁 Ubicación

### Logística Service
```
logistica-service/src/main/java/ar/edu/tpi/logistica/config/DataSeeder.java
```

### Seguimiento Service
```
seguimiento-service/src/main/java/ar/edu/tpi/seguimiento/config/DataSeeder.java
```

## 🚀 Funcionamiento Automático

Los seeders se ejecutan **automáticamente** al iniciar cada microservicio:

```java
@Bean
CommandLineRunner initDatabase() {
    return args -> {
        // Solo carga datos si la BD está vacía
        if (repository.count() > 0) {
            log.info("Base de datos ya contiene datos. Saltando seeder.");
            return;
        }
        // ... carga de datos ...
    };
}
```

**Importante**: 
- ✅ Solo carga datos si la base de datos está **vacía**
- ✅ Si ya hay datos, no hace nada (evita duplicados)
- ✅ Los datos se persisten (no se borran al reiniciar)

## 📊 Datos Cargados

### Logística Service

#### Camiones (3)
```
AB123CD - 10,000 kg / 50 m³ - Disponible
EF456GH - 15,000 kg / 75 m³ - Disponible
IJ789KL - 8,000 kg / 40 m³ - No disponible
```

#### Ubicaciones (4)
```
1. Córdoba Capital     (-31.4167, -64.1833)
2. Mendoza Capital     (-32.8908, -68.8272)
3. Rosario             (-32.9468, -60.6393)
4. Buenos Aires Retiro (-34.5875, -58.3736)
```

#### Depósitos (3)
```
Depósito Central Córdoba (Ubicación: Córdoba)
Depósito Cuyo           (Ubicación: Mendoza)
Depósito Litoral        (Ubicación: Rosario)
```

#### Tarifas por Volumen (3)
```
0-10 m³:   $100/km
10-30 m³:  $150/km
30-100 m³: $200/km
```

#### Ruta y Tramo de Ejemplo (1)
```
Ruta: Córdoba → Mendoza (608 km)
  - Tramo 1: Córdoba (-31.42, -64.18) → Mendoza (-32.89, -68.83)
```

---

### Seguimiento Service

#### Clientes (3)
```
1. Juan Pérez           - juan.perez@email.com
2. María González       - maria.gonzalez@email.com
3. Empresa Logística SA - contacto@logistica.com.ar
```

#### Contenedores (3)
```
CONT-001-2024 - 500 kg / 2.5 m³  - Electrónica (Cliente: Juan)
CONT-002-2024 - 1200 kg / 5.0 m³ - Alimentos (Cliente: María)
CONT-003-2024 - 800 kg / 3.2 m³  - Repuestos (Cliente: Empresa)
```

#### Solicitudes (3)
```
SOL-2024-001 - PENDIENTE    - Córdoba → Mendoza
SOL-2024-002 - EN_TRANSITO  - Mendoza → Rosario
SOL-2024-003 - COMPLETADA   - Rosario → Buenos Aires
```

## 🧪 Probar con Postman

Después de levantar los servicios, los datos estarán disponibles:

### Logística (puerto 8082)
```
GET http://localhost:8082/camiones
GET http://localhost:8082/ubicaciones
GET http://localhost:8082/depositos
GET http://localhost:8082/rutas
GET http://localhost:8082/tramos
```

### Seguimiento (puerto 8081)
```
GET http://localhost:8081/clientes
GET http://localhost:8081/contenedores
GET http://localhost:8081/solicitudes
```

### A través del Gateway (puerto 8090)
```
GET http://localhost:8090/api/logistica/camiones
GET http://localhost:8090/api/seguimiento/clientes
```

## 🔄 Reiniciar Datos

Si quieres volver a cargar los datos desde cero:

### Opción 1: Borrar la base de datos H2
```bash
# Detén el servicio
# Borra la carpeta de datos H2 (varía según configuración)
rm -rf target/h2db/

# Reinicia el servicio
mvn spring-boot:run
```

### Opción 2: Cambiar la configuración de JPA
En `application.properties`:
```properties
# Cambia de 'update' a 'create-drop' temporalmente
spring.jpa.hibernate.ddl-auto=create-drop
```

⚠️ **Cuidado**: `create-drop` borra toda la BD al iniciar/detener.

## 🛠️ Personalizar los Datos

Para agregar más datos o modificar los existentes:

1. Abre el archivo `DataSeeder.java` correspondiente
2. Modifica o agrega nuevas entidades en el método `initDatabase()`
3. Reinicia el servicio

Ejemplo para agregar un nuevo camión:
```java
Camion camion4 = new Camion();
camion4.setDominio("MN012OP");
camion4.setCapacidadPesoKg(12000.0);
camion4.setCapacidadVolumenM3(60.0);
camion4.setCostoKmBase(90.0);
camion4.setConsumoPromedioLitrosKm(0.38);
camion4.setDisponible(true);
camion4.setCreatedAt(LocalDateTime.now());
camionRepository.save(camion4);
```

## ✅ Verificación en Logs

Al iniciar el servicio, verás en la consola:
```
Iniciando carga de datos de prueba...
✓ Cargados 3 camiones
✓ Cargadas 4 ubicaciones
✓ Cargados 3 depósitos
✓ Cargadas 3 tarifas
✓ Cargadas 1 rutas con 1 tramos
========================================
✅ Datos de prueba cargados exitosamente
========================================
```

## 📝 Notas

- Los seeders usan `@RequiredArgsConstructor` + `@Bean CommandLineRunner`
- Se ejecutan **después** de que Spring Boot inicialice el contexto
- Son **idempotentes**: verifican si hay datos antes de cargar
- Los IDs se auto-generan (`@GeneratedValue`)
- Las relaciones (FK) se respetan correctamente
