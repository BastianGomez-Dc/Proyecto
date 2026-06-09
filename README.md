# Informe de Proyecto: Sistema de Gestión para Tienda de Mantención de PC

## 1. Introducción

El proyecto consiste en el desarrollo de una aplicación web basada en microservicios diseñada para optimizar la gestión de servicios técnicos en una tienda de mantención de computadores. El sistema permite el seguimiento integral desde la captura de datos del cliente hasta la entrega del equipo con su respectiva valorización económica.

## 2. Requisitos del Sistema

### 2.1 Requisitos Funcionales (RF)

Son las funciones específicas que el sistema debe realizar:

- **RF-01 Registro de Clientes:** El sistema debe permitir crear, leer y actualizar perfiles de clientes (RUT, nombre, email, teléfono).
- **RF-02 Inventario de Hardware:** El sistema debe registrar los componentes específicos (CPU, RAM, GPU, etc.) y marcas de cada equipo ingresado.
- **RF-03 Selección de Servicios:** El técnico debe poder seleccionar entre cinco tipos de servicios: Limpieza Superficial, Profunda, Reparación, Mejora y Optimización.
- **RF-04 Cálculo de Costos:** El sistema debe calcular automáticamente el total a pagar basándose en los servicios seleccionados.
- **RF-05 Gestión de Motivos:** El sistema debe permitir ingresar una descripción detallada del problema o motivo de la visita.

### 2.2 Requisitos No Funcionales (RNF)

Son las propiedades y restricciones del sistema:

- **RNF-01 Disponibilidad:** El sistema debe estar operativo el 99% del tiempo durante el horario comercial.
- **RNF-02 Escalabilidad:** La arquitectura de microservicios debe permitir escalar el servicio de "Mantención" de forma independiente si aumenta la demanda de tickets.
- **RNF-03 Integridad de Datos:** Los datos del RUT deben validarse bajo el formato estándar chileno.
- **RNF-04 Seguridad:** La comunicación entre microservicios debe ser segura y los datos del cliente deben estar protegidos.

## 3. Arquitectura Técnica de Microservicios

El sistema se divide en tres módulos independientes para asegurar que un fallo en un área no detenga el funcionamiento global:

| Microservicio | Responsabilidad |
| --- | --- |
| Servicio de Usuario | Gestión de datos personales y autenticación. |
| Servicio de Computador | Catálogo técnico del hardware y vinculación con el dueño. |
| Servicio de Mantención | Lógica de negocio, estados del servicio y gestión de precios. |

## 4. Definición de Servicios y Matriz de Costos

| ID | Tipo de Servicio | Descripción | Costo Base |
| --- | --- | --- | --- |
| 1 | Limpieza Superficial | Cambio de pasta térmica y limpieza de ventiladores. | $XX.XXX |
| 2 | Limpieza Profunda | Desarmado total y limpieza de componentes a fondo. | $XX.XXX |
| 3 | Reparación | Corrección de fallas físicas en placas o componentes. | $XX.XXX |
| 4 | Mejora (Upgrade) | Instalación de hardware nuevo (SSD, RAM, etc.). | $XX.XXX |
| 5 | Optimización | Ajustes de software, drivers y limpieza de sistema. | $XX.XXX |

## 5. Modelo de Datos (Entidades Principales)

### Entidad Usuario
- Rut: String (PK)
- nombre: String
- apellido: String
- gmail: String
- teléfono: Numeric

### Entidad Computador
- id_pc: Numeric (PK)
- rut_dueño: FK (Servicio Usuario)
- componentes: List (Marca, Tipo)

### Entidad Mantención (Ticket)
- id_ticket: Numeric (PK)
- id_pc: FK (Servicio Computador)
- motivo: Varchar
- tipo_servicio: Numeric
- costo_total: Float
- fecha_entrada: DateTime

## 6. Flujo del Proceso (Caso de Uso)

1. Captura de Cliente: Se verifica el RUT; si no existe, se crea el perfil en el Servicio de Usuario.
2. Registro de Equipo: Se detallan las piezas del PC en el Servicio de Computador.
3. Orden de Trabajo: En el Servicio de Mantención, se describe el problema, se elige el tipo de servicio y se genera el presupuesto automáticamente.
4. Cierre: Una vez realizado el trabajo, el sistema actualiza el estado a "Completado" y emite el total a cobrar.

## 7. Stack Tecnológico

- **Backend:** Java 25 + Spring Boot 4.1
- **Base de Datos:** MySQL 8 (una instancia por microservicio)
- **Comunicación:** API RESTful con JSON via RestTemplate
- **Contenedores:** Docker Compose (bases de datos)
- **Autenticación:** JWT (JJWT 0.12.6), validado centralmente en el BFF
- **Build:** Gradle 9.4.1

---

## 8. Arquitectura Actual

El sistema cuenta con cuatro microservicios y un BFF (Backend for Frontend) que actúa como único punto de entrada para el frontend.

```
Frontend
    │
    ▼
┌─────────────────────────────────────┐
│         BFF  (puerto 8085)          │
│  • Valida JWT en todas las rutas    │
│  • Redirige al microservicio        │
│    correspondiente según el path    │
└────────────┬────────────────────────┘
             │
     ┌───────┼───────────────────────┐
     ▼       ▼           ▼           ▼
 :8081    :8080       :8082       :8084
usuarios  computador  tiposervicio mantencion
 DB:3307   DB:3308     DB:3309     DB:3310
```

### Microservicios

| Módulo | Puerto | Base de datos | Puerto DB |
|---|---|---|---|
| `bff` | 8085 | — | — |
| `usuarios` | 8081 | `usuarios_db` | 3307 |
| `computador` | 8080 | `componentes_db` | 3308 |
| `tiposervicio` | 8082 | `tiposervicio_db` | 3309 |
| `mantencion` | 8084 | `mantencion_db` | 3310 |

### Tabla de rutas del BFF

| Path | Microservicio destino |
|---|---|
| `POST /api/auth/login` | usuarios :8081 (público, sin token) |
| `/api/usuarios/**` | usuarios :8081 |
| `/api/computadores/**` | computador :8080 |
| `/api/tiposervicios/**` | tiposervicio :8082 |
| `/api/mantenciones/**` | mantencion :8084 |

---

## 9. Autenticación y seguridad

1. El frontend llama a `POST /api/auth/login` en el BFF con `{ "username": "admin", "password": "admin123" }`.
2. El BFF reenvía la petición a `usuarios`, que genera y devuelve un token JWT.
3. El frontend incluye el token en todas las peticiones posteriores: `Authorization: Bearer <token>`.
4. El `JwtFilter` del BFF valida el token antes de redirigir la petición.
5. Los microservicios internos confían en el BFF y no validan el token por su cuenta.

---

## 10. Cómo levantar el proyecto

### 1. Levantar las bases de datos

```bash
docker compose up -d
```

### 2. Arrancar cada microservicio (en terminales separadas)

```bash
# Desde cada carpeta de módulo:
cd usuarios      && ./gradlew bootRun
cd computador    && ./gradlew bootRun
cd tiposervicio  && ./gradlew bootRun
cd mantencion    && ./gradlew bootRun
cd bff           && ./gradlew bootRun
```

El frontend apunta a `http://localhost:8085`.

---

## 11. Historial de cambios

### Refactorización de código
- Eliminada la duplicación de lógica en `UsuarioService`: la obtención de computadores y mantenciones se extrajo a métodos privados reutilizables (`fetchTiposServicio`, `obtenerComputadoresConMantenciones`, `fetchMantenciones`, `toResponse`).
- Eliminado el endpoint duplicado `GET /api/usuarios/{rut}/detalles` (era idéntico a `GET /api/usuarios/{rut}`).
- Reemplazado `ex.printStackTrace()` por `logger.error()` en todos los `GlobalExceptionHandler`.
- Corregida la indentación inconsistente (tabs → espacios) en el módulo `mantencion`.
- Eliminados comentarios Javadoc que solo repetían lo que el nombre del método ya expresaba.
- Eliminadas referencias a clases con nombre completo (`java.util.ArrayList`) reemplazándolas por imports.

### Adición del BFF
- Nuevo módulo `bff` en el puerto `8085`.
- El BFF es el único punto de entrada para el frontend; valida el JWT una sola vez para todas las rutas.
- `SecurityConfig` en `usuarios` actualizado para permitir todas las peticiones (el servicio es interno al BFF).
