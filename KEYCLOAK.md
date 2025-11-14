# 🧩 Configuración de Keycloak para el TPI
Este documento resume cómo configuramos Keycloak para el Trabajo Práctico Integrador y cómo se integra con Docker para asegurar que todos los integrantes del grupo y los docentes tengan el mismo entorno de autenticación.

## 🔐 1. Roles definidos en el sistema
Dentro del realm tpi-backend, creamos los siguientes roles:
- admin
- cliente
- transportista

Estos roles representan los distintos tipos de usuarios que interactúan con la API según el enunciado del TPI.

## 👤 2. Usuarios de prueba creados

Se crearon tres usuarios básicos, uno por cada rol, para poder probar los endpoints:

  | Usuario | Rol asignado	| Contraseña |
  |---------|---------------|------------|
  | leon01	|     admin	    |   leon123  |
  | fati2   |	 cliente	|   fati123  |
  | iker01  | transportista |   iker123  |
  | tomi01  |    cliente    |   tomi123  |

Estos usuarios se usarán para solicitar tokens JWT y verificar permisos sobre los microservicios.

## 🌍 3. Exportación del Realm
Como Keycloak 24 no permite exportar usuarios desde la UI en modo start-dev, realizamos la exportación completa desde el contenedor.

Se utilizó:
```
docker exec -it keycloak /bin/bash
/opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/export --users realm
```

Luego copiamos el archivo exportado al proyecto:
```
docker cp keycloak:/opt/keycloak/data/export/tpi-backend-realm.json ./keycloak/realm-export.json
```

Ese archivo contiene:
- Roles
- Usuarios
- Realm completo
- Configuración del cliente utilizado por el backend

## 🐳 4. Integración con Docker

Para lograr que Keycloak importe automáticamente el realm al iniciar, actualizamos el docker-compose.yml agregando:

```
command: start-dev --import-realm
```
volumes:
  - ./keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json
  - keycloak_data:/opt/keycloak/data


Esto permite que:
- Cualquier integrante del grupo pueda ejecutar docker compose up
- El realm tpi-backend se importe automáticamente
- Roles, usuarios y clientes se creen solos
- La configuración sea replicable, limpia y sin pasos manuales

## 🚀 5. Resultado final
Con esta configuración:

- Keycloak se levanta automáticamente con el realm preconfigurado.
- Los roles y usuarios están disponibles desde el inicio.
- Todos los microservicios pueden validar tokens JWT emitidos por Keycloak.
- Todo el entorno de seguridad es reproducible, portable y apto para entrega.

## 📌 6. Beneficio principal

Garantizamos que la misma configuración de Keycloak esté disponible en todas las máquinas, lo que evita diferencias entre entornos y facilita la evaluación por parte de los docentes.