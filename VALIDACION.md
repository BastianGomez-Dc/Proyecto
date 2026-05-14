# GUÍA DE VALIDACIÓN DE MICROSERVICIOS

## Estado actual del proyecto
✓ Docker Compose: Corriendo (4 bases de datos MySQL)
✓ Git: Código subido a GitHub
✓ Microservicios: Código compilado

## Pasos para validar que todo está funcionando:

### 1. VERIFICAR BASES DE DATOS
```bash
docker ps
```
Deberías ver 4 contenedores MySQL corriendo:
- mysql-usuarios (puerto 3307)
- mysql-componentes (puerto 3308)
- mysql-tiposervicio (puerto 3309)
- mysql-mantencion (puerto 3310)

### 2. COMPILAR MICROSERVICIOS
```bash
# Usuarios
cd c:\Users\pv-alumno\Desktop\Proyecto-main\usuarios
.\gradlew bootJar -x test

# Computador
cd c:\Users\pv-alumno\Desktop\Proyecto-main\computador
.\gradlew bootJar -x test

# Mantencion
cd c:\Users\pv-alumno\Desktop\Proyecto-main\mantencion
.\gradlew bootJar -x test

# TipoServicio
cd c:\Users\pv-alumno\Desktop\Proyecto-main\tiposervicio
.\gradlew bootJar -x test
```

### 3. INICIAR MICROSERVICIOS (en terminales separadas)

Terminal 1 - Computador (Puerto 8081):
```bash
java -jar "c:\Users\pv-alumno\Desktop\Proyecto-main\computador\build\libs\computador-0.0.1-SNAPSHOT.jar"
```

Terminal 2 - Mantencion (Puerto 8082):
```bash
java -jar "c:\Users\pv-alumno\Desktop\Proyecto-main\mantencion\build\libs\mantencion-0.0.1-SNAPSHOT.jar"
```

Terminal 3 - Usuarios (Puerto 8080):
```bash
java -jar "c:\Users\pv-alumno\Desktop\Proyecto-main\usuarios\build\libs\usuarios-0.0.1-SNAPSHOT.jar"
```

Terminal 4 - TipoServicio (Puerto 8083):
```bash
java -jar "c:\Users\pv-alumno\Desktop\Proyecto-main\tiposervicio\build\libs\tiposervicio-0.0.1-SNAPSHOT.jar"
```

### 4. VALIDAR CON CURL O POSTMAN

#### Crear un usuario
```
POST http://localhost:8080/api/usuarios
Content-Type: application/json

{
  "rut": "12345678-9",
  "nombre": "Juan",
  "apellido": "Pérez",
  "gmail": "juan@example.com",
  "telefono": 987654321
}
```

#### Obtener usuario
```
GET http://localhost:8080/api/usuarios/12345678-9
```

#### Crear computadora
```
POST http://localhost:8081/api/computadores
Content-Type: application/json

{
  "rutDueno": "12345678-9",
  "componentes": [
    {
      "marca": "Intel",
      "tipo": "Procesador"
    },
    {
      "marca": "NVIDIA",
      "tipo": "Tarjeta Gráfica"
    }
  ]
}
```

#### Crear ticket de mantenimiento
```
POST http://localhost:8082/api/mantenciones
Content-Type: application/json

{
  "idPc": "[UUID_DEL_COMPUTADOR]",
  "motivo": "Limpieza superficial",
  "tipoServicio": "LIMPIEZA_SUPERFICIAL"
}
```

#### Obtener computadoras del usuario
```
GET http://localhost:8080/api/usuarios/12345678-9/computadores
```

#### Obtener servicios del usuario
```
GET http://localhost:8080/api/usuarios/12345678-9/servicios
```

### 5. PUERTOS Y SERVICIOS

| Servicio | Puerto | Base de Datos | Descripción |
|----------|--------|---------------|-------------|
| Usuarios | 8080 | mysql-usuarios:3307 | Gestión de usuarios |
| Computador | 8081 | mysql-componentes:3308 | Gestión de computadoras |
| Mantencion | 8082 | mysql-mantencion:3310 | Tickets de servicio |
| TipoServicio | 8083 | mysql-tiposervicio:3309 | Tipos de servicio |

### 6. VALIDACIÓN EXITOSA

✓ Todos los servicios están corriendo (logs sin errores)
✓ Las bases de datos están conectadas
✓ Puedes crear un usuario
✓ Puedes crear una computadora asociada al usuario
✓ Puedes crear un ticket de mantenimiento
✓ Puedes obtener computadoras por usuario
✓ Puedes obtener servicios por usuario

### 7. LOGS ESPERADOS

Deberías ver en cada terminal algo como:
```
Tomcat started on port 8080 (http) with context path '/'
Started UsuariosApplication in X seconds
```

### 8. SOLUCIÓN DE PROBLEMAS

Si el puerto está en uso:
```bash
netstat -ano | findstr "8080"
taskkill /PID [PID] /F
```

Si falla la conexión a BD:
- Verificar que Docker está corriendo: `docker ps`
- Verificar conexión: `mysql -h localhost -P 3307 -u usuario_user -pusuario_pass usuarios_db`

