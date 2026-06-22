# Sistema de Gestión — Taller de Mantención de Computadores

Sistema backend basado en microservicios para gestionar clientes, equipos y órdenes de mantención de un taller de PCs. Construido con Java 21, Spring Boot 4.1, MySQL 8 y Docker.

---

## Índice

1. [Arquitectura](#1-arquitectura)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Cómo levantar el proyecto](#3-cómo-levantar-el-proyecto)
4. [Autenticación con JWT](#4-autenticación-con-jwt)
5. [BFF — puerta de entrada](#5-bff--puerta-de-entrada)
6. [Microservicio Usuarios](#6-microservicio-usuarios)
7. [Microservicio Computador](#7-microservicio-computador)
8. [Microservicio Tipo de Servicio](#8-microservicio-tipo-de-servicio)
9. [Microservicio Mantención](#9-microservicio-mantención)
10. [Conexión entre microservicios](#10-conexión-entre-microservicios)
11. [Cómo se da forma a los datos (DTOs)](#11-cómo-se-da-forma-a-los-datos-dtos)
12. [Probar con Postman](#12-probar-con-postman)

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
usuarios computador tiposervicio mantencion
DB:3307  DB:3308   DB:3309   DB:3310
```

Ningún microservicio es accesible directamente desde el exterior. Todo el tráfico entra por el BFF en el puerto **8085**.

### Microservicios y puertos

| Módulo | Puerto | Base de datos | Puerto DB |
|---|---|---|---|
| `bff` | 8085 | — | — |
| `usuarios` | 8081 | `usuarios_db` | 3307 |
| `computador` | 8080 | `componentes_db` | 3308 |
| `tiposervicio` | 8082 | `tiposervicio_db` | 3309 |
| `mantencion` | 8084 | `mantencion_db` | 3310 |

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

# Arrancar cada servicio (terminales separadas)
cd tiposervicio  && ./gradlew bootRun
cd computador    && ./gradlew bootRun
cd mantencion    && ./gradlew bootRun
cd usuarios      && ./gradlew bootRun
cd bff           && ./gradlew bootRun
```

---

## 4. Autenticación con JWT

El sistema usa **JWT (JSON Web Token)** para proteger los endpoints. El flujo es:

```
1. POST /api/auth/login  →  { username: "admin", password: "admin123" }
2. Respuesta             →  { token: "eyJhbGci..." }
3. Todos los demás requests  →  Header: Authorization: Bearer eyJhbGci...
```

El token tiene una validez de **24 horas**. La única ruta pública (sin token) es `/api/auth/login`.

Si envías un request sin token o con token inválido, recibes:

```json
{
  "status": 401,
  "message": "Acceso no autorizado. Debe iniciar sesion primero en POST /api/auth/login"
}
```

**Credenciales:** `admin` / `admin123` (definidas en `AuthController.java` del microservicio usuarios).

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
  │                        Si el token es inválido → 401 automático
  ▼
SecurityConfig.java     →  /api/auth/** es público, todo lo demás requiere autenticación
  │
  ▼
ProxyController.java    →  Busca en la tabla de rutas, copia headers y body, reenvía
```

### Tabla de rutas

Archivo: `bff/src/main/java/com/serviciotecnico/bff/controller/ProxyController.java`

| Path recibido | Microservicio destino |
|---|---|
| `/api/auth/**` | `svc-usuarios :8081` (público) |
| `/api/usuarios/**` | `svc-usuarios :8081` |
| `/api/computadores/**` | `svc-computador :8080` |
| `/api/tiposervicios/**` | `svc-tiposervicio :8082` |
| `/api/mantenciones/**` | `svc-mantencion :8084` |

### URLs de microservicios

Archivo: `bff/src/main/resources/application.properties`

```properties
server.port=8085
usuarios.service.url=${USUARIOS_URL:http://localhost:8081}
computador.service.url=${COMPUTADOR_URL:http://localhost:8080}
tiposervicio.service.url=${TIPOSERVICIO_URL:http://localhost:8082}
mantencion.service.url=${MANTENCION_URL:http://localhost:8084}
```

La sintaxis `${VAR:valor_default}` significa: usa la variable de entorno `VAR` si existe, si no usa el valor por defecto. En Docker, las variables vienen del `docker-compose.yml`. En local, se usan los valores por defecto.

### Agregar un nuevo microservicio al BFF

**Paso 1** — `application.properties`:
```properties
nuevo.service.url=${NUEVO_URL:http://localhost:8090}
```

**Paso 2** — `ProxyController.java`:
```java
@Value("${nuevo.service.url}")
private String nuevoUrl;

// dentro de initRoutes():
"/api/nuevo", nuevoUrl,
```

**Paso 3** — `docker-compose.yml`: agregar el servicio y pasar `NUEVO_URL: http://svc-nuevo:8090`.

### Hacer una ruta pública (sin JWT)

Archivo: `bff/src/main/java/com/serviciotecnico/bff/security/SecurityConfig.java`

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers("/api/tiposervicios/**").permitAll()  // ← agregar aquí
    .anyRequest().authenticated()
)
```

---

## 6. Microservicio Usuarios

**Carpeta:** `usuarios/` | **Puerto:** `8081` | **Base de datos:** `usuarios_db`

Gestiona el registro de clientes y el login. También es el servicio que orquesta la respuesta completa del usuario (computadores + mantenciones).

### Tabla en base de datos

```sql
CREATE TABLE usuarios (
    rut      VARCHAR(12)  PRIMARY KEY,   -- ej: 12345678-9
    nombre   VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    gmail    VARCHAR(150) NOT NULL,
    telefono BIGINT       NOT NULL
);
```

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `POST` | `/api/auth/login` | Login, devuelve JWT | No |
| `GET` | `/api/usuarios` | Listar todos con sus PCs y mantenciones | Sí |
| `GET` | `/api/usuarios/{rut}` | Obtener uno con sus PCs y mantenciones | Sí |
| `POST` | `/api/usuarios` | Crear usuario | Sí |
| `PUT` | `/api/usuarios/{rut}` | Actualizar usuario | Sí |
| `DELETE` | `/api/usuarios/{rut}` | Eliminar usuario | Sí |

### Body para crear o actualizar

```json
{
  "rut": "12345678-9",
  "nombre": "Juan",
  "apellido": "Perez",
  "gmail": "juan.perez@gmail.com",
  "telefono": 912345678
}
```

**Validaciones:**
- `rut`: formato chileno `12345678-9` o `1234567-K`, dígito verificador validado
- `nombre` y `apellido`: obligatorios, no vacíos
- `gmail`: debe ser un email válido
- `telefono`: obligatorio

### Respuesta de GET (usuario completo)

Al consultar un usuario, el servicio llama internamente a los otros microservicios y ensambla esta respuesta:

```json
{
  "rut": "12345678-9",
  "nombre": "Juan",
  "apellido": "Perez",
  "gmail": "juan.perez@gmail.com",
  "telefono": 912345678,
  "computadores": [
    {
      "idPc": "cfc4799e-5b7a-424a-94ae-11a416e897d6",
      "componentes": [
        { "tipo": "Procesador", "marca": "Intel", "modelo": "Core i7-12700K" },
        { "tipo": "RAM", "marca": "Kingston", "modelo": "Fury Beast 16GB" }
      ],
      "mantenciones": [
        {
          "idTicket": "ecfd...",
          "motivo": "El equipo no enciende",
          "tipoServicio": "REPARACION",
          "costoTotal": 60000.0,
          "fechaEntrada": "2026-06-15",
          "estado": "PENDIENTE",
          "tipoServicioDetalle": {
            "id": 3,
            "nombre": "Reparación",
            "descripcion": "Corrección de fallas físicas en placas o componentes.",
            "costoBase": 60000.0
          }
        }
      ]
    }
  ]
}
```

---

## 7. Microservicio Computador

**Carpeta:** `computador/` | **Puerto:** `8080` | **Base de datos:** `componentes_db`

Gestiona los equipos de cada cliente y sus componentes de hardware.

### Tablas en base de datos

```sql
CREATE TABLE computadores (
    id_pc     BINARY(16)  PRIMARY KEY,  -- UUID
    rut_dueno VARCHAR(12) NOT NULL      -- RUT del cliente dueño
);

CREATE TABLE computador_componentes (
    computador_id BINARY(16)   NOT NULL,
    tipo          VARCHAR(100) NOT NULL,  -- ej: "Procesador"
    marca         VARCHAR(100) NOT NULL,  -- ej: "Intel"
    modelo        VARCHAR(150) NULL,      -- ej: "Core i7-12700K"
    CONSTRAINT fk_computador FOREIGN KEY (computador_id)
        REFERENCES computadores(id_pc) ON DELETE CASCADE
);
```

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/computadores` | Listar todos | Sí |
| `GET` | `/api/computadores/{id}` | Obtener por UUID | Sí |
| `GET` | `/api/computadores/cliente/{rut}` | Listar los PCs de un cliente | Sí |
| `POST` | `/api/computadores` | Registrar computador | Sí |
| `PUT` | `/api/computadores/{id}` | Actualizar computador | Sí |
| `DELETE` | `/api/computadores/{id}` | Eliminar computador | Sí |

### Body para crear o actualizar

```json
{
  "rutDueno": "12345678-9",
  "componentes": [
    { "tipo": "Procesador",       "marca": "Intel",    "modelo": "Core i7-12700K" },
    { "tipo": "Tarjeta Madre",    "marca": "ASUS",     "modelo": "ROG Strix B660-F" },
    { "tipo": "RAM",              "marca": "Kingston", "modelo": "Fury Beast 16GB" },
    { "tipo": "Almacenamiento",   "marca": "Samsung",  "modelo": "SSD 870 EVO 1TB" },
    { "tipo": "Tarjeta de Video", "marca": "NVIDIA",   "modelo": "RTX 3060" },
    { "tipo": "Fuente de Poder",  "marca": "Corsair",  "modelo": "RM750x" },
    { "tipo": "Cooler",           "marca": "Noctua",   "modelo": "NH-D15" },
    { "tipo": "Gabinete",         "marca": "NZXT",     "modelo": "H510" }
  ]
}
```

### Campos de un componente

| Campo | Obligatorio | Descripción |
|---|---|---|
| `tipo` | Sí | Categoría del componente |
| `marca` | Sí | Fabricante |
| `modelo` | No | Modelo específico de la pieza |

### Tipos de componente sugeridos

```
Procesador        Tarjeta Madre     RAM
Almacenamiento    Tarjeta de Video  Fuente de Poder
Cooler            Gabinete
```

---

## 8. Microservicio Tipo de Servicio

**Carpeta:** `tiposervicio/` | **Puerto:** `8082` | **Base de datos:** `tiposervicio_db`

Catálogo de los tipos de servicio técnico disponibles. Se precarga automáticamente con 5 registros al iniciar.

### Tabla en base de datos

```sql
CREATE TABLE tipos_servicio (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    costo_base  DOUBLE       NOT NULL
);
```

### Datos precargados

| ID | Nombre | Descripción | Costo Base |
|---|---|---|---|
| 1 | Limpieza Superficial | Cambio de pasta térmica y limpieza de ventiladores | $25.000 |
| 2 | Limpieza Profunda | Desarmado total y limpieza de componentes a fondo | $45.000 |
| 3 | Reparación | Corrección de fallas físicas en placas o componentes | $60.000 |
| 4 | Mejora (Upgrade) | Instalación de hardware nuevo (SSD, RAM, etc.) | $80.000 |
| 5 | Optimización | Ajustes de software, drivers y limpieza de sistema | $30.000 |

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/tiposervicios` | Listar todos | Sí |
| `GET` | `/api/tiposervicios/{id}` | Obtener por ID | Sí |
| `POST` | `/api/tiposervicios` | Crear tipo de servicio | Sí |
| `PUT` | `/api/tiposervicios/{id}` | Actualizar | Sí |
| `DELETE` | `/api/tiposervicios/{id}` | Eliminar | Sí |

### Body para crear o actualizar

```json
{
  "nombre": "Diagnóstico",
  "descripcion": "Revisión general del equipo para identificar fallas",
  "costoBase": 15000
}
```

---

## 9. Microservicio Mantención

**Carpeta:** `mantencion/` | **Puerto:** `8084` | **Base de datos:** `mantencion_db`

Gestiona los tickets de mantención (órdenes de trabajo). El costo se calcula automáticamente según el tipo de servicio.

### Tabla en base de datos

```sql
CREATE TABLE mantenciones (
    id_ticket     BINARY(16)   PRIMARY KEY,  -- UUID
    id_pc         BINARY(16)   NOT NULL,     -- UUID del computador
    motivo        VARCHAR(500) NOT NULL,
    tipo_servicio VARCHAR(50)  NOT NULL,     -- valor del enum
    costo_total   DOUBLE       NOT NULL,     -- calculado automáticamente
    fecha_entrada DATETIME     NOT NULL,     -- se asigna al crear
    estado        VARCHAR(50)  NOT NULL      -- PENDIENTE o COMPLETADO
);
```

### Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/mantenciones` | Listar todas | Sí |
| `GET` | `/api/mantenciones/{id}` | Obtener por UUID | Sí |
| `GET` | `/api/mantenciones/pc/{idPc}` | Listar tickets de un PC | Sí |
| `POST` | `/api/mantenciones` | Crear ticket | Sí |
| `PUT` | `/api/mantenciones/{id}` | Actualizar ticket | Sí |
| `PUT` | `/api/mantenciones/{id}/completar` | Marcar como COMPLETADO | Sí |
| `DELETE` | `/api/mantenciones/{id}` | Eliminar ticket | Sí |

### Body para crear o actualizar

```json
{
  "idPc": "cfc4799e-5b7a-424a-94ae-11a416e897d6",
  "motivo": "El equipo no enciende al presionar el botón de encendido",
  "tipoServicio": "REPARACION"
}
```

### Valores válidos para `tipoServicio`

| Valor enum | Costo calculado automáticamente |
|---|---|
| `LIMPIEZA_SUPERFICIAL` | $25.000 |
| `LIMPIEZA_PROFUNDA` | $45.000 |
| `REPARACION` | $60.000 |
| `MEJORA` | $80.000 |
| `OPTIMIZACION` | $30.000 |

El `costoTotal`, `fechaEntrada` (fecha de hoy) y el `estado` (`PENDIENTE`) se asignan automáticamente al crear. Para marcar como completado se usa el endpoint `/completar`.

---

## 10. Conexión entre microservicios

La comunicación interna usa **RestTemplate** (HTTP directo entre contenedores). Solo `svc-usuarios` llama a otros servicios.

**Archivo:** `usuarios/src/main/java/com/serviciotecnico/usuarios/service/UsuarioService.java`

Cuando se pide `GET /api/usuarios/{rut}`, el flujo interno es:

```
svc-usuarios
    │
    ├── GET http://svc-tiposervicio:8082/api/tiposervicios
    │       Obtiene el catálogo completo de tipos de servicio (una sola vez)
    │
    ├── GET http://svc-computador:8080/api/computadores/cliente/{rut}
    │       Obtiene todos los PCs del usuario
    │
    └── Por cada PC encontrado:
            GET http://svc-mantencion:8084/api/mantenciones/pc/{idPc}
                Obtiene los tickets de mantención de ese PC
```

Los nombres `svc-computador`, `svc-tiposervicio` y `svc-mantencion` son los nombres de los contenedores en `docker-compose.yml`. Dentro de la red Docker, estos nombres funcionan como hostnames.

Si un microservicio no responde, el error se captura con `try/catch`, se registra en el log y se devuelve una lista vacía en vez de romper toda la respuesta.

---

## 11. Cómo se da forma a los datos (DTOs)

Los **DTOs** (Data Transfer Objects) definen exactamente qué campos aparecen en el JSON de respuesta. Están en `usuarios/src/main/java/com/serviciotecnico/usuarios/dto/`.

| Clase | Qué representa |
|---|---|
| `UsuarioCompletoResponse` | Respuesta final: datos del usuario + sus computadores |
| `ComputadorDTO` | Datos del PC: id, componentes y lista de mantenciones |
| `ComponenteDTO` | Pieza del PC: tipo, marca, modelo |
| `MantencionDTO` | Ticket: motivo, tipo de servicio, costo, fecha, estado, detalle |
| `TipoServicioDTO` | Detalle del tipo: nombre, descripción, costoBase |
| `UsuarioRequest` | Body de entrada para crear/actualizar usuario |
| `LoginRequest` | Body del login: `{ username, password }` |
| `LoginResponse` | Respuesta del login: `{ token }` |

El método `toResponse()` en `UsuarioService` ensambla todo:

```
UsuarioCompletoResponse
├── rut, nombre, apellido, gmail, telefono
└── computadores [ ]
      ├── idPc
      ├── componentes [ { tipo, marca, modelo } ]
      └── mantenciones [ ]
            ├── idTicket, motivo, tipoServicio, costoTotal, fechaEntrada, estado
            └── tipoServicioDetalle { nombre, descripcion, costoBase }
```

**Para agregar un campo nuevo a la respuesta:**
1. Agregar el campo en la entidad del microservicio (ej: `MantencionTicket.java`)
2. Crear una migración Flyway si es un campo de base de datos (ej: `V2__add_campo.sql`)
3. Agregar el campo en el DTO correspondiente del microservicio `usuarios` (ej: `MantencionDTO.java`)

---

## 12. Probar con Postman

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
| Usuarios | Listar, Obtener por RUT, Crear, Actualizar, Eliminar |
| Computadores | Listar, Obtener por ID, Listar por cliente, Crear, Actualizar, Eliminar |
| Tipos de Servicio | Listar, Obtener por ID, Crear, Actualizar, Eliminar |
| Mantenciones | Listar, Obtener por ID, Listar por PC, Crear, Actualizar, Completar, Eliminar |

### Flujo de prueba completo

```
1. Login                           → guarda token automáticamente
2. POST /api/usuarios              → crear cliente (rut: "12345678-9")
3. POST /api/computadores          → crear PC del cliente
4. GET  /api/computadores          → copiar el UUID del PC creado
5. POST /api/mantenciones          → crear ticket con el UUID del PC
6. GET  /api/usuarios/12345678-9   → ver usuario completo con PC y ticket
7. PUT  /api/mantenciones/{id}/completar → marcar el trabajo como listo
```
