# Sistema de Gestión — Taller de Mantención de Computadores

Sistema backend basado en microservicios para gestionar clientes, equipos y órdenes de mantención de un taller de PCs. Construido con Java 21, Spring Boot 4.1, MySQL 8 y Docker.

---

## Índice

1. [Arquitectura](#1-arquitectura)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Cómo levantar el proyecto](#3-cómo-levantar-el-proyecto)
4. [Autenticación con JWT](#4-autenticación-con-jwt)
5. [BFF — puerta de entrada](#5-bff--puerta-de-entrada)
6. [Microservicio Customer](#6-microservicio-customer)
7. [Microservicio Computer](#7-microservicio-computer)
8. [Microservicio Service Type](#8-microservicio-service-type)
9. [Microservicio Maintenance](#9-microservicio-maintenance)
10. [Conexión entre microservicios](#10-conexión-entre-microservicios)
11. [Cómo se da forma a los datos (DTOs)](#11-cómo-se-da-forma-a-los-datos-dtos)
12. [Documentación OpenAPI / Swagger](#12-documentación-openapi--swagger)
13. [Pruebas unitarias](#13-pruebas-unitarias)
14. [Probar con Postman](#14-probar-con-postman)

---

## 1. Arquitectura

```
Frontend / Postman
        │
        ▼
┌───────────────────┐
│    BFF  :8085     │  ← Única puerta de entrada
│  • Valida JWT     │    Todo request pasa por aquí
│  • Redirige       │
└────────┬──────────┘
         │
   ┌─────┼────────────────────┐
   ▼     ▼          ▼         ▼
:8081  :8080      :8082     :8084
customer computer service-type maintenance
DB:3307  DB:3308   DB:3309   DB:3310
```

Ningún microservicio es accesible directamente desde el exterior. Todo el tráfico entra por el BFF en el puerto **8085**.

### Microservicios y puertos

| Módulo (carpeta) | Servicio | Puerto | Base de datos | Puerto DB |
|---|---|---|---|---|
| `bff` | bff | 8085 | — | — |
| `usuarios` | customer | 8081 | `usuarios_db` | 3307 |
| `computador` | computer | 8080 | `componentes_db` | 3308 |
| `tiposervicio` | service-type | 8082 | `tiposervicio_db` | 3309 |
| `mantencion` | maintenance | 8084 | `mantencion_db` | 3310 |

Las carpetas de cada módulo conservan su nombre original en español; el código Java (paquetes, clases, endpoints) está en inglés.

### Orden de arranque (Docker)

```
MySQL (healthy)
  → svc-tiposervicio, svc-computador, svc-mantencion (healthy)
    → svc-usuarios (healthy)
      → bff
```

---

## 2. Stack tecnológico

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal |
| Spring Boot 4.1 | Framework web y de seguridad |
| Spring Data JPA | Acceso a base de datos |
| Flyway | Migraciones de base de datos |
| MySQL 8.0 | Base de datos (una por microservicio) |
| JJWT 0.12.6 | Generación y validación de tokens JWT |
| springdoc-openapi | Documentación OpenAPI / Swagger UI |
| JUnit 5 + Mockito | Pruebas unitarias |
| Lombok | Reducción de código boilerplate |
| Gradle 9.4.1 | Build tool |
| Docker Compose | Orquestación de contenedores |
| RestTemplate | Comunicación HTTP entre microservicios |

---

## 3. Cómo levantar el proyecto

### Todo con Docker (recomendado)

```bash
docker compose up -d --build
```

Para detener (conserva los datos en volúmenes):

```bash
docker compose down
```

Para detener y borrar todos los datos:

```bash
docker compose down -v
```

Para reconstruir solo un microservicio tras hacer cambios:

```bash
docker compose up -d --build svc-mantencion
```

### Solo bases de datos en Docker, servicios en local

```bash
# Levantar solo las bases de datos
docker compose up -d db-usuarios db-componentes db-tiposervicio db-mantencion

# Arrancar cada servicio (terminales separadas), seteando el secreto JWT
export JWT_SECRET="<el mismo valor del anchor jwt-secret en docker-compose.yml>"

cd tiposervicio  && ./gradlew bootRun
cd computador    && ./gradlew bootRun
cd mantencion    && ./gradlew bootRun
cd usuarios      && ./gradlew bootRun   # necesita JWT_SECRET
cd bff           && ./gradlew bootRun   # necesita JWT_SECRET
```

En PowerShell: `$env:JWT_SECRET="<valor>"` antes de levantar `usuarios` y `bff`.

---

## 4. Autenticación con JWT

El sistema usa **JWT (JSON Web Token)** para proteger los endpoints. El flujo es:

```
1. POST /api/auth/login  →  { username: "admin", password: "admin123" }
2. Respuesta             →  { token: "eyJhbGci..." }
3. Todos los demás requests  →  Header: Authorization: Bearer eyJhbGci...
```

El token tiene una validez de **24 horas**. La única ruta pública (sin token) es `/api/auth/login`.

Si envías un request **sin token o con un token inválido**, el BFF responde **403 Forbidden**:

```json
{
  "status": 403,
  "message": "Forbidden"
}
```

(Si el login falla por credenciales incorrectas, la respuesta sigue siendo **401 Unauthorized**, ya que ese es un caso distinto: ahí sí hay credenciales presentes, pero no son válidas.)

**Credenciales:** `admin` / `admin123` (definidas en `AuthController.java` del microservicio `customer`).

### Secreto JWT

El secreto de firma (`security.jwt.secret`) se inyecta vía la variable de entorno `JWT_SECRET` — no hay ningún valor hardcodeado en el código ni en `application.properties`. En `docker-compose.yml` se define **una sola vez** mediante un YAML anchor (`x-jwt-secret`) y se reutiliza en `svc-usuarios` y `bff`, evitando que el mismo secreto quede duplicado en dos lugares.

---

## 5. BFF — puerta de entrada

**Carpeta:** `bff/` | **Puerto:** `8085`

El BFF tiene dos responsabilidades: validar el JWT y redirigir el request al microservicio correcto.

### Cómo funciona cada request

```
Request
  │
  ▼
JwtAuthenticationFilter.java →  Lee "Authorization: Bearer <token>", valida firma
  │                        Si el token es inválido → contexto de seguridad vacío
  ▼
SecurityConfig.java     →  /api/auth/** es público; todo lo demás requiere autenticación
  │                        Sin autenticación válida → 403 Forbidden
  ▼
ProxyController.java    →  Busca en la tabla de rutas, copia headers y body, reenvía
```

### Tabla de rutas

Archivo: `bff/src/main/java/com/serviciotecnico/bff/controller/ProxyController.java`

| Path recibido | Microservicio destino |
|---|---|
| `/api/auth/**` | `svc-usuarios :8081` (público) |
| `/api/customers/**` | `svc-usuarios :8081` |
| `/api/computers/**` | `svc-computador :8080` |
| `/api/service-types/**` | `svc-tiposervicio :8082` |
| `/api/maintenance-tickets/**` | `svc-mantencion :8084` |

### URLs de microservicios

Archivo: `bff/src/main/resources/application.properties`

```properties
server.port=8085
customer.service.url=${USUARIOS_URL:http://localhost:8081}
computer.service.url=${COMPUTADOR_URL:http://localhost:8080}
servicetype.service.url=${TIPOSERVICIO_URL:http://localhost:8082}
maintenance.service.url=${MANTENCION_URL:http://localhost:8084}
```

La sintaxis `${VAR:valor_default}` significa: usa la variable de entorno `VAR` si existe, si no usa el valor por defecto. En Docker, las variables vienen del `docker-compose.yml` (que mantiene los nombres `USUARIOS_URL`, `COMPUTADOR_URL`, etc. para no tener que tocar los nombres de los servicios/contenedores). En local, se usan los valores por defecto.

### Agregar un nuevo microservicio al BFF

**Paso 1** — `application.properties`:
```properties
newservice.service.url=${NEWSERVICE_URL:http://localhost:8090}
```

**Paso 2** — `ProxyController.java`:
```java
@Value("${newservice.service.url}")
private String newServiceUrl;

// dentro del constructor:
routes.put("/api/new-service", newServiceUrl);
```

**Paso 3** — `docker-compose.yml`: agregar el servicio y pasar `NEWSERVICE_URL: http://svc-newservice:8090`.

### Hacer una ruta pública (sin JWT)

Archivo: `bff/src/main/java/com/serviciotecnico/bff/config/SecurityConfig.java`

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/service-types/**").permitAll()  // ← agregar aquí
    .anyRequest().authenticated()
)
```

---

## 6. Microservicio Customer

**Carpeta:** `usuarios/` | **Paquete:** `com.serviciotecnico.customer` | **Puerto:** `8081` | **Base de datos:** `usuarios_db`

Gestiona el registro de clientes y el login. También es el servicio que orquesta la respuesta completa del cliente (computadores + mantenciones).

### Tabla en base de datos

La tabla y columnas se mantienen en español (no se modificó el esquema); el mapeo a inglés ocurre en la capa de código vía `@Column(name = "...")`.

```sql
CREATE TABLE usuarios (
    rut      VARCHAR(12)  PRIMARY KEY,   -- ej: 12345678-9
    nombre   VARCHAR(100) NOT NULL,      -- mapeado a Customer.firstName
    apellido VARCHAR(100) NOT NULL,      -- mapeado a Customer.lastName
    gmail    VARCHAR(150) NOT NULL,
    telefono BIGINT       NOT NULL       -- mapeado a Customer.phone
);
```

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/auth/login` | Login, devuelve JWT | No |
| `GET` | `/api/customers` | Listar todos con sus PCs y mantenciones | Sí |
| `GET` | `/api/customers/{rut}` | Obtener uno con sus PCs y mantenciones | Sí |
| `POST` | `/api/customers` | Crear cliente | Sí |
| `PUT` | `/api/customers/{rut}` | Actualizar cliente | Sí |
| `DELETE` | `/api/customers/{rut}` | Eliminar cliente | Sí |

### Body para crear o actualizar

```json
{
  "rut": "12345678-9",
  "firstName": "Juan",
  "lastName": "Perez",
  "gmail": "juan.perez@gmail.com",
  "phone": 912345678
}
```

**Validaciones:**
- `rut`: formato chileno `12345678-9` o `1234567-K`, dígito verificador validado
- `firstName` y `lastName`: obligatorios, no vacíos
- `gmail`: debe ser un email válido
- `phone`: obligatorio

### Respuesta de GET (cliente completo)

Al consultar un cliente, el servicio llama internamente a los otros microservicios y ensambla esta respuesta:

```json
{
  "rut": "12345678-9",
  "firstName": "Juan",
  "lastName": "Perez",
  "gmail": "juan.perez@gmail.com",
  "phone": 912345678,
  "computers": [
    {
      "id": "cfc4799e-5b7a-424a-94ae-11a416e897d6",
      "components": [
        { "type": "Procesador", "brand": "Intel", "model": "Core i7-12700K" },
        { "type": "RAM", "brand": "Kingston", "model": "Fury Beast 16GB" }
      ],
      "maintenanceTickets": [
        {
          "id": "ecfd...",
          "reason": "El equipo no enciende",
          "serviceType": "REPAIR",
          "totalCost": 60000.0,
          "entryDate": "2026-06-15",
          "status": "PENDING",
          "serviceTypeDetail": {
            "id": 3,
            "name": "Reparación",
            "description": "Corrección de fallas físicas en placas o componentes.",
            "baseCost": 60000.0
          }
        }
      ]
    }
  ]
}
```

---

## 7. Microservicio Computer

**Carpeta:** `computador/` | **Paquete:** `com.serviciotecnico.computer` | **Puerto:** `8080` | **Base de datos:** `componentes_db`

Gestiona los equipos de cada cliente y sus componentes de hardware.

### Tablas en base de datos

```sql
CREATE TABLE computadores (
    id_pc     BINARY(16)  PRIMARY KEY,  -- mapeado a Computer.id
    rut_dueno VARCHAR(12) NOT NULL      -- mapeado a Computer.ownerRut
);

CREATE TABLE computador_componentes (
    computador_id BINARY(16)   NOT NULL,
    tipo          VARCHAR(100) NOT NULL,  -- mapeado a Component.type
    marca         VARCHAR(100) NOT NULL,  -- mapeado a Component.brand
    modelo        VARCHAR(150) NULL,      -- mapeado a Component.model
    CONSTRAINT fk_computador FOREIGN KEY (computador_id)
        REFERENCES computadores(id_pc) ON DELETE CASCADE
);
```

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/computers` | Listar todos | Sí |
| `GET` | `/api/computers/{id}` | Obtener por UUID | Sí |
| `GET` | `/api/computers/customer/{rut}` | Listar los PCs de un cliente | Sí |
| `POST` | `/api/computers` | Registrar computador | Sí |
| `PUT` | `/api/computers/{id}` | Actualizar computador | Sí |
| `DELETE` | `/api/computers/{id}` | Eliminar computador | Sí |

### Body para crear o actualizar

```json
{
  "ownerRut": "12345678-9",
  "components": [
    { "type": "Procesador",       "brand": "Intel",    "model": "Core i7-12700K" },
    { "type": "Tarjeta Madre",    "brand": "ASUS",     "model": "ROG Strix B660-F" },
    { "type": "RAM",              "brand": "Kingston", "model": "Fury Beast 16GB" },
    { "type": "Almacenamiento",   "brand": "Samsung",  "model": "SSD 870 EVO 1TB" },
    { "type": "Tarjeta de Video", "brand": "NVIDIA",   "model": "RTX 3060" },
    { "type": "Fuente de Poder",  "brand": "Corsair",  "model": "RM750x" },
    { "type": "Cooler",           "brand": "Noctua",   "model": "NH-D15" },
    { "type": "Gabinete",         "brand": "NZXT",     "model": "H510" }
  ]
}
```

### Campos de un componente

| Campo | Obligatorio | Descripción |
|---|---|---|
| `type` | Sí | Categoría del componente |
| `brand` | Sí | Fabricante |
| `model` | No | Modelo específico de la pieza |

### Tipos de componente sugeridos

```
Procesador        Tarjeta Madre     RAM
Almacenamiento    Tarjeta de Video  Fuente de Poder
Cooler            Gabinete
```

(Estos valores son contenido de catálogo en español, no identificadores de código — no se tradujeron.)

---

## 8. Microservicio Service Type

**Carpeta:** `tiposervicio/` | **Paquete:** `com.serviciotecnico.servicetype` | **Puerto:** `8082` | **Base de datos:** `tiposervicio_db`

Catálogo de los tipos de servicio técnico disponibles. Se precarga automáticamente con 5 registros al iniciar.

### Tabla en base de datos

```sql
CREATE TABLE tipos_servicio (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,  -- mapeado a ServiceType.name
    descripcion VARCHAR(255) NOT NULL,         -- mapeado a ServiceType.description
    costo_base  DOUBLE       NOT NULL          -- mapeado a ServiceType.baseCost
);
```

La creación de la tabla (`V1__create_tiposervicio_table.sql`) y la carga del catálogo inicial (`V2__seed_tipos_servicio.sql`) están en migraciones Flyway separadas — el DDL no se mezcla con datos.

### Datos precargados

| ID | Nombre | Descripción | Costo Base |
|---|---|---|---|
| 1 | Limpieza Superficial | Cambio de pasta térmica y limpieza de ventiladores | $25.000 |
| 2 | Limpieza Profunda | Desarmado total y limpieza de componentes a fondo | $45.000 |
| 3 | Reparación | Corrección de fallas físicas en placas o componentes | $60.000 |
| 4 | Mejora (Upgrade) | Instalación de hardware nuevo (SSD, RAM, etc.) | $80.000 |
| 5 | Optimización | Ajustes de software, drivers y limpieza de sistema | $30.000 |

(El catálogo es contenido de negocio en español para los usuarios finales del taller — no se tradujo.)

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/service-types` | Listar todos | Sí |
| `GET` | `/api/service-types/{id}` | Obtener por ID | Sí |
| `POST` | `/api/service-types` | Crear tipo de servicio | Sí |
| `PUT` | `/api/service-types/{id}` | Actualizar | Sí |
| `DELETE` | `/api/service-types/{id}` | Eliminar | Sí |

### Body para crear o actualizar

```json
{
  "name": "Diagnóstico",
  "description": "Revisión general del equipo para identificar fallas",
  "baseCost": 15000
}
```

---

## 9. Microservicio Maintenance

**Carpeta:** `mantencion/` | **Paquete:** `com.serviciotecnico.maintenance` | **Puerto:** `8084` | **Base de datos:** `mantencion_db`

Gestiona los tickets de mantención (órdenes de trabajo). El costo se calcula automáticamente según el tipo de servicio.

### Tabla en base de datos

```sql
CREATE TABLE mantenciones (
	id_ticket     BINARY(16)   PRIMARY KEY,  -- mapeado a MaintenanceTicket.id
	id_pc         BINARY(16)   NOT NULL,     -- mapeado a MaintenanceTicket.computerId
	motivo        VARCHAR(500) NOT NULL,     -- mapeado a MaintenanceTicket.reason
	tipo_servicio VARCHAR(50)  NOT NULL,     -- mapeado a MaintenanceTicket.serviceType
	costo_total   DOUBLE       NOT NULL,     -- mapeado a MaintenanceTicket.totalCost
	fecha_entrada DATETIME     NOT NULL,     -- mapeado a MaintenanceTicket.entryDate
	estado        VARCHAR(50)  NOT NULL      -- mapeado a MaintenanceTicket.status
);
```

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/maintenance-tickets` | Listar todas | Sí |
| `GET` | `/api/maintenance-tickets/{id}` | Obtener por UUID | Sí |
| `GET` | `/api/maintenance-tickets/computer/{computerId}` | Listar tickets de un PC | Sí |
| `POST` | `/api/maintenance-tickets` | Crear ticket | Sí |
| `PUT` | `/api/maintenance-tickets/{id}` | Actualizar ticket | Sí |
| `PUT` | `/api/maintenance-tickets/{id}/complete` | Marcar como COMPLETED | Sí |
| `DELETE` | `/api/maintenance-tickets/{id}` | Eliminar ticket | Sí |

### Body para crear o actualizar

```json
{
  "computerId": "cfc4799e-5b7a-424a-94ae-11a416e897d6",
  "reason": "El equipo no enciende al presionar el botón de encendido",
  "serviceType": "REPAIR"
}
```

### Valores válidos para `serviceType`

| Valor enum | Costo calculado automáticamente |
|---|---|
| `SURFACE_CLEANING` | $25.000 |
| `DEEP_CLEANING` | $45.000 |
| `REPAIR` | $60.000 |
| `UPGRADE` | $80.000 |
| `OPTIMIZATION` | $30.000 |

El `totalCost`, `entryDate` (fecha de hoy) y el `status` (`PENDING`) se asignan automáticamente al crear. Para marcar como completado se usa el endpoint `/complete`.

---

## 10. Conexión entre microservicios

La comunicación interna usa **RestTemplate** (HTTP directo entre contenedores). Solo `svc-usuarios` llama a otros servicios.

**Archivo:** `usuarios/src/main/java/com/serviciotecnico/customer/service/CustomerService.java`

Cuando se pide `GET /api/customers/{rut}`, el flujo interno es:

```
svc-usuarios
    │
    ├── GET http://svc-tiposervicio:8082/api/service-types
    │       Obtiene el catálogo completo de tipos de servicio (una sola vez)
    │
    ├── GET http://svc-computador:8080/api/computers/customer/{rut}
    │       Obtiene todos los PCs del cliente
    │
    └── Por cada PC encontrado:
            GET http://svc-mantencion:8084/api/maintenance-tickets/computer/{computerId}
                Obtiene los tickets de mantención de ese PC
```

Los nombres `svc-computador`, `svc-tiposervicio` y `svc-mantencion` son los nombres de los contenedores en `docker-compose.yml`. Dentro de la red Docker, estos nombres funcionan como hostnames.

Si un microservicio no responde, el error se captura con `try/catch`, se registra en el log y se devuelve una lista vacía en vez de romper toda la respuesta.

---

## 11. Cómo se da forma a los datos (DTOs)

Los **DTOs** (Data Transfer Objects) definen exactamente qué campos aparecen en el JSON de respuesta. Están en `usuarios/src/main/java/com/serviciotecnico/customer/dto/`.

| Clase | Qué representa |
|---|---|
| `CustomerResponse` | Respuesta final: datos del cliente + sus computadores |
| `ComputerDTO` | Datos del PC: id, componentes y lista de tickets de mantención |
| `ComponentDTO` | Pieza del PC: type, brand, model |
| `MaintenanceDTO` | Ticket: reason, serviceType, cost, fecha, status, detalle |
| `ServiceTypeDTO` | Detalle del tipo: name, description, baseCost |
| `CustomerRequest` | Body de entrada para crear/actualizar cliente |
| `LoginRequest` | Body del login: `{ username, password }` |
| `LoginResponse` | Respuesta del login: `{ token }` |

El método `toResponse()` en `CustomerService` ensambla todo:

```
CustomerResponse
├── rut, firstName, lastName, gmail, phone
└── computers [ ]
      ├── id
      ├── components [ { type, brand, model } ]
      └── maintenanceTickets [ ]
            ├── id, reason, serviceType, totalCost, entryDate, status
            └── serviceTypeDetail { name, description, baseCost }
```

**Para agregar un campo nuevo a la respuesta:**
1. Agregar el campo en la entidad del microservicio (ej: `MaintenanceTicket.java`)
2. Crear una migración Flyway si es un campo de base de datos (ej: `V3__add_field.sql`)
3. Agregar el campo en el DTO correspondiente del microservicio `customer` (ej: `MaintenanceDTO.java`)

---

## 12. Documentación OpenAPI / Swagger

Los 4 microservicios de negocio (`customer`, `computer`, `service-type`, `maintenance`) exponen documentación OpenAPI generada con **springdoc-openapi**. El BFF no expone Swagger propio porque solo actúa como proxy genérico (`/api/**`), sin un contrato propio que documentar.

Cuando un servicio corre en local (modo "solo BD en Docker, servicios en local", ver sección 3), su documentación queda disponible en su propio puerto:

| Servicio | Swagger UI | OpenAPI JSON |
|---|---|---|
| customer (8081) | http://localhost:8081/swagger-ui/index.html | http://localhost:8081/v3/api-docs |
| computer (8080) | http://localhost:8080/swagger-ui/index.html | http://localhost:8080/v3/api-docs |
| service-type (8082) | http://localhost:8082/swagger-ui/index.html | http://localhost:8082/v3/api-docs |
| maintenance (8084) | http://localhost:8084/swagger-ui/index.html | http://localhost:8084/v3/api-docs |

Esto no se proxea a través del BFF (que solo reenvía `/api/**`); para inspeccionar la documentación hay que acceder directamente al puerto del microservicio.

---

## 13. Pruebas unitarias

Cada uno de los 4 microservicios de negocio tiene pruebas unitarias reales sobre su capa de servicio (`src/test/java/.../service/*ServiceTest.java`), usando **JUnit 5 + Mockito** con el repositorio mockeado (sin levantar contexto de Spring ni base de datos):

| Servicio | Test | Qué cubre |
|---|---|---|
| customer | `CustomerServiceTest` | Validación de RUT (dígito verificador), RUT duplicado, no encontrado, mismatch RUT body/path, delete |
| computer | `ComputerServiceTest` | Validación de RUT, CRUD, no encontrado |
| service-type | `ServiceTypeServiceTest` | CRUD, no encontrado |
| maintenance | `MaintenanceServiceTest` | Cálculo de costo por cada `ServiceTypeEnum` (test parametrizado), create/complete/delete, no encontrado |
| bff | `JwtTokenServiceTest` | Token válido extrae el subject; token con firma distinta o malformado lanza `JwtException` |

Además se mantiene el `*ApplicationTests.contextLoads()` de cada servicio (smoke test de arranque de Spring).

Para correr las pruebas de un módulo:

```bash
cd computador && ./gradlew test
```

(Requiere JDK 21 en el `PATH`. Si no tienes JDK 21 localmente, se puede usar un contenedor: `docker run --rm -v "$(pwd)":/app -w /app gradle:jdk21 gradle test --no-daemon`.)

---

## 14. Probar con Postman

El archivo `ServicioTecnico.postman_collection.json` en la raíz del proyecto contiene todos los endpoints listos para importar.

### Importar la colección

1. Abrir Postman → **Import**
2. Seleccionar `ServicioTecnico.postman_collection.json`
3. La colección ya tiene configuradas las variables `{{baseUrl}}` y `{{token}}`

### Orden de uso

**1. Ejecutar "Auth → Login"** — el script de Tests guarda el token automáticamente en `{{token}}`.

**2. Usar el resto de los endpoints** — todos tienen `Authorization: Bearer {{token}}` configurado.

### Endpoints incluidos en la colección

| Carpeta | Requests |
|---|---|
| Auth | Login |
| Clientes | Listar, Obtener por RUT, Crear, Actualizar, Eliminar |
| Computadores | Listar, Obtener por ID, Listar por cliente, Crear, Actualizar, Eliminar |
| Tipos de Servicio | Listar, Obtener por ID, Crear, Actualizar, Eliminar |
| Mantenciones | Listar, Obtener por ID, Listar por PC, Crear, Actualizar, Completar, Eliminar |

### Flujo de prueba completo

```
1. Login                                  → guarda token automáticamente
2. POST /api/customers                    → crear cliente (rut: "12345678-9")
3. POST /api/computers                    → crear PC del cliente
4. GET  /api/computers                    → copiar el UUID del PC creado
5. POST /api/maintenance-tickets          → crear ticket con el UUID del PC
6. GET  /api/customers/12345678-9         → ver cliente completo con PC y ticket
7. PUT  /api/maintenance-tickets/{id}/complete → marcar el trabajo como listo
```
