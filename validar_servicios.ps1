# Script para validar que todos los microservicios están funcionando

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "    VALIDACIÓN DE MICROSERVICIOS" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Esperar un poco para que los servicios arranquen
Start-Sleep -Seconds 3

# Prueba 1: Crear un usuario
Write-Host "[1] Creando un usuario de prueba..." -ForegroundColor Yellow
$usuarioBody = @{
    rut = "12345678-9"
    nombre = "Juan"
    apellido = "Pérez"
    gmail = "juan@example.com"
    telefono = 987654321
} | ConvertTo-Json

$crearUsuario = Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios" `
    -Method POST `
    -ContentType "application/json" `
    -Body $usuarioBody `
    -ErrorAction SilentlyContinue

if ($crearUsuario) {
    Write-Host "✓ Usuario creado exitosamente: $($crearUsuario.rut)" -ForegroundColor Green
} else {
    Write-Host "✗ Error al crear el usuario" -ForegroundColor Red
}

# Prueba 2: Obtener el usuario creado
Write-Host "`n[2] Obteniendo usuario creado..." -ForegroundColor Yellow
$obtenerUsuario = Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios/12345678-9" `
    -Method GET `
    -ErrorAction SilentlyContinue

if ($obtenerUsuario) {
    Write-Host "✓ Usuario obtenido: $($obtenerUsuario.nombre) $($obtenerUsuario.apellido)" -ForegroundColor Green
} else {
    Write-Host "✗ Error al obtener el usuario" -ForegroundColor Red
}

# Prueba 3: Crear una computadora
Write-Host "`n[3] Creando una computadora de prueba..." -ForegroundColor Yellow
$computadorBody = @{
    rutDueno = "12345678-9"
    componentes = @(
        @{ marca = "Intel"; tipo = "Procesador" },
        @{ marca = "NVIDIA"; tipo = "Tarjeta Gráfica" }
    )
} | ConvertTo-Json

$crearComputador = Invoke-RestMethod -Uri "http://localhost:8081/api/computadores" `
    -Method POST `
    -ContentType "application/json" `
    -Body $computadorBody `
    -ErrorAction SilentlyContinue

if ($crearComputador) {
    Write-Host "✓ Computadora creada: $($crearComputador.idPc)" -ForegroundColor Green
} else {
    Write-Host "✗ Error al crear la computadora" -ForegroundColor Red
}

# Prueba 4: Crear un ticket de mantenimiento
Write-Host "`n[4] Creando un ticket de mantenimiento..." -ForegroundColor Yellow
if ($crearComputador) {
    $mantencionBody = @{
        idPc = $crearComputador.idPc
        motivo = "Limpieza superficial"
        tipoServicio = "LIMPIEZA_SUPERFICIAL"
    } | ConvertTo-Json

    $crearMantencion = Invoke-RestMethod -Uri "http://localhost:8082/api/mantenciones" `
        -Method POST `
        -ContentType "application/json" `
        -Body $mantencionBody `
        -ErrorAction SilentlyContinue

    if ($crearMantencion) {
        Write-Host "✓ Ticket creado: $($crearMantencion.idTicket)" -ForegroundColor Green
    } else {
        Write-Host "✗ Error al crear el ticket" -ForegroundColor Red
    }
}

# Prueba 5: Validar relación usuario-computadora
Write-Host "`n[5] Validando computadoras del usuario..." -ForegroundColor Yellow
$computadoresUsuario = Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios/12345678-9/computadores" `
    -Method GET `
    -ErrorAction SilentlyContinue

if ($computadoresUsuario) {
    Write-Host "✓ Computadoras encontradas: $($computadoresUsuario.Count)" -ForegroundColor Green
    foreach ($comp in $computadoresUsuario) {
        Write-Host "  - ID: $($comp.idPc)" -ForegroundColor Cyan
    }
} else {
    Write-Host "✗ Error al obtener computadoras" -ForegroundColor Red
}

# Prueba 6: Validar servicios del usuario
Write-Host "`n[6] Validando servicios solicitados..." -ForegroundColor Yellow
$serviciosUsuario = Invoke-RestMethod -Uri "http://localhost:8080/api/usuarios/12345678-9/servicios" `
    -Method GET `
    -ErrorAction SilentlyContinue

if ($serviciosUsuario) {
    Write-Host "✓ Servicios encontrados: $($serviciosUsuario.Count)" -ForegroundColor Green
    foreach ($serv in $serviciosUsuario) {
        Write-Host "  - Ticket: $($serv.idTicket) | Tipo: $($serv.tipoServicio) | Estado: $($serv.estado)" -ForegroundColor Cyan
    }
} else {
    Write-Host "✗ Error al obtener servicios" -ForegroundColor Red
}

# Resumen
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "    VALIDACIÓN COMPLETADA" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Puerto 8080: Usuarios" -ForegroundColor Magenta
Write-Host "Puerto 8081: Computadores" -ForegroundColor Magenta
Write-Host "Puerto 8082: Mantención" -ForegroundColor Magenta
Write-Host "Puerto 8083: Tipo Servicio" -ForegroundColor Magenta
