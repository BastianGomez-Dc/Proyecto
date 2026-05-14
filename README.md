# Proyecto Servicio Técnico - Microservicios

Este es un proyecto de arquitectura de microservicios para gestionar un servicio técnico de computadoras.

## 📋 Descripción

El proyecto implementa un sistema de gestión de servicios técnicos basado en microservicios, donde:
- Los **usuarios** pueden registrar sus computadoras
- Las **computadoras** tienen componentes asociados
- Se pueden crear **tickets de mantenimiento** para las computadoras
- Se registran los **tipos de servicio** disponibles

## 🏗️ Arquitectura

### Microservicios

1. **Usuarios** (Puerto 8080)
   - Gestión de usuarios
   - Validación de RUT chileno
   - Consulta de computadoras asociadas
   - Consulta de servicios solicitados

2. **Computador** (Puerto 8081)
   - Registro de computadoras
   - Asociación con usuario propietario
   - Gestión de componentes

3. **Mantencion** (Puerto 8082)
   - Creación de tickets de servicio
   - Estados: PENDIENTE, COMPLETADO
   - Tipos: LIMPIEZA_SUPERFICIAL, LIMPIEZA_PROFUNDA, REPARACION, MEJORA, OPTIMIZACION
   - Cálculo automático de costos

4. **TipoServicio** (Puerto 8083)
   - Gestión de tipos de servicio disponibles

### Bases de Datos (Docker)

- **usuarios_db** (mysql-usuarios:3307)
- **componentes_db** (mysql-componentes:3308)
- **tiposervicio_db** (mysql-tiposervicio:3309)
- **mantencion_db** (mysql-mantencion:3310)

## 🚀 Instrucciones de uso

### Requisitos
- Java 25+
- Gradle 9.4+
- Docker & Docker Compose
- MySQL 8.0

### Iniciar el proyecto

1. **Iniciar bases de datos:**
```bash
docker-compose up -d --build
```

2. **Compilar microservicios:**
```bash
cd usuarios && .\gradlew bootJar -x test
cd computador && .\gradlew bootJar -x test
cd mantencion && .\gradlew bootJar -x test
cd tiposervicio && .\gradlew bootJar -x test
```

3. **Iniciar microservicios** (en terminales separadas):
```bash
# Terminal 1
java -jar usuarios/build/libs/usuarios-0.0.1-SNAPSHOT.jar

# Terminal 2
java -jar computador/build/libs/computador-0.0.1-SNAPSHOT.jar

# Terminal 3
java -jar mantencion/build/libs/mantencion-0.0.1-SNAPSHOT.jar

# Terminal 4
java -jar tiposervicio/build/libs/tiposervicio-0.0.1-SNAPSHOT.jar
```

## 📡 Endpoints principales

### Usuarios

**Crear usuario:**
```
POST /api/usuarios
{
  "rut": "12345678-9",
  "nombre": "Juan",
  "apellido": "Pérez",
  "gmail": "juan@example.com",
  "telefono": 987654321
}
```

**Obtener usuario:**
```
GET /api/usuarios/{rut}
```

**Computadoras del usuario:**
```
GET /api/usuarios/{rut}/computadores
```

**Servicios del usuario:**
```
GET /api/usuarios/{rut}/servicios
```

### Computadores

**Crear computadora:**
```
POST /api/computadores
{
  "rutDueno": "12345678-9",
  "componentes": [
    {"marca": "Intel", "tipo": "Procesador"},
    {"marca": "NVIDIA", "tipo": "Tarjeta Gráfica"}
  ]
}
```

**Obtener computadoras por usuario:**
```
GET /api/computadores/cliente/{rut}
```

### Mantencion

**Crear ticket:**
```
POST /api/mantenciones
{
  "idPc": "[UUID]",
  "motivo": "Limpieza superficial",
  "tipoServicio": "LIMPIEZA_SUPERFICIAL"
}
```

**Obtener servicios por computadora:**
```
GET /api/mantenciones/computador/{idPc}
```

**Completar ticket:**
```
PUT /api/mantenciones/{id}/completar
```

## 🔧 Configuración

### Properties de conexión

**Usuarios:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3307/usuarios_db
spring.datasource.username=usuario_user
spring.datasource.password=usuario_pass
```

**Computador:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3308/componentes_db
spring.datasource.username=componente_user
spring.datasource.password=componente_pass
```

**Mantencion:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3310/mantencion_db
spring.datasource.username=mantencion_user
spring.datasource.password=mantencion_pass
```

**TipoServicio:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3309/tiposervicio_db
spring.datasource.username=tiposervicio_user
spring.datasource.password=tiposervicio_pass
```

## 📊 Modelos de datos

### Usuario
- **rut** (PK): 12345678-9
- **nombre**: String
- **apellido**: String
- **gmail**: Email válido
- **telefono**: Número de teléfono

### Computador
- **idPc** (PK): UUID
- **rutDueno** (FK): Referencia a Usuario
- **componentes**: Lista de componentes (marca, tipo)

### MantencionTicket
- **idTicket** (PK): UUID
- **idPc** (FK): Referencia a Computador
- **motivo**: Descripción del servicio
- **tipoServicio**: Enum
- **costoTotal**: Double (calculado automáticamente)
- **fechaEntrada**: LocalDateTime
- **estado**: String (PENDIENTE, COMPLETADO)

## 🔍 Validaciones

- **RUT**: Valida formato y dígito verificador chileno
- **Email**: Valida formato de email
- **Teléfono**: Número entero
- **Relaciones**: Valida que usuario y computadora existan

## 📝 Notas importantes

1. La comunicación entre microservicios usa RestTemplate
2. Las migraciones de BD se gestionan con Flyway
3. Cada microservicio tiene su propia BD
4. Los puertos están configurados en `application.properties` de cada servicio
5. El archivo [VALIDACION.md](VALIDACION.md) contiene instrucciones detalladas de prueba

## 📦 Estructura del proyecto

```
Proyecto-main/
├── usuarios/               # Microservicio de usuarios
├── computador/            # Microservicio de computadoras
├── mantencion/            # Microservicio de mantenimiento
├── tiposervicio/          # Microservicio de tipos de servicio
├── docker-compose.yml     # Configuración de bases de datos
├── VALIDACION.md          # Guía de validación
└── README.md              # Este archivo
```

## 🔗 GitHub

[BastianGomez-Dc/Proyecto](https://github.com/BastianGomez-Dc/Proyecto)

## 👤 Autor

Alumno Plaza Vespucio

## 📄 Licencia

Sin especificar

---

**Última actualización:** 13 de Mayo de 2026
