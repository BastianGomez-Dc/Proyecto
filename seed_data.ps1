# Seeding and Data Improvement Script for Servicio Tecnico Microservices
# This script cleans up old stale test data and populates clean, distributed test data
# across distinct users, computers, and tickets.
# All requests go through the BFF (port 8085), which is the only public entry point.

$ErrorActionPreference = "Stop"

# Set encoding to ASCII/UTF8 for clean consoles
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8085"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " INICIANDO LIMPIEZA Y CARGA DE DATOS (SEEDING) DE PRUEBA   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# ---------------------------------------------------------
# 0. LOGIN
# ---------------------------------------------------------
Write-Host "`n[0/3] Autenticando contra el BFF..." -ForegroundColor Yellow
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$token = $loginRes.token
if (-not $token) {
    throw "No se pudo obtener el token. Verifica que el BFF este activo en $baseUrl."
}
$headers = @{
    "Content-Type"  = "application/json"
    "Authorization" = "Bearer $token"
}
Write-Host "    Token obtenido correctamente." -ForegroundColor Gray

# ---------------------------------------------------------
# 1. LIMPIEZA DE DATOS EXISTENTES
# ---------------------------------------------------------
Write-Host "`n[1/3] Limpiando datos existentes..." -ForegroundColor Yellow

# A. Limpiar Tickets de Mantencion
Write-Host " -> Consultando tickets de mantencion..."
try {
    $tickets = Invoke-RestMethod -Uri "$baseUrl/api/mantenciones" -Method Get -Headers $headers
    if ($tickets -and $tickets.Count -gt 0) {
        Write-Host "    Se encontraron $($tickets.Count) tickets. Eliminandolos..." -ForegroundColor Gray
        foreach ($t in $tickets) {
            $id = $t.idTicket
            Write-Host "    Eliminando ticket: $id" -ForegroundColor Gray
            Invoke-RestMethod -Uri "$baseUrl/api/mantenciones/$id" -Method Delete -Headers $headers
        }
    } else {
        Write-Host "    No se encontraron tickets de mantencion previos." -ForegroundColor Gray
    }
} catch {
    Write-Host "    Aviso: No se pudo consultar o limpiar los tickets de mantencion. Asegurate de que el BFF y el servicio de mantencion esten activos." -ForegroundColor Red
}

# B. Limpiar Computadores
Write-Host " -> Consultando computadores..."
try {
    $pcs = Invoke-RestMethod -Uri "$baseUrl/api/computadores" -Method Get -Headers $headers
    if ($pcs -and $pcs.Count -gt 0) {
        Write-Host "    Se encontraron $($pcs.Count) computadores. Eliminandolos..." -ForegroundColor Gray
        foreach ($pc in $pcs) {
            $id = $pc.idPc
            Write-Host "    Eliminando computador: $id" -ForegroundColor Gray
            Invoke-RestMethod -Uri "$baseUrl/api/computadores/$id" -Method Delete -Headers $headers
        }
    } else {
        Write-Host "    No se encontraron computadores previos." -ForegroundColor Gray
    }
} catch {
    Write-Host "    Aviso: No se pudo consultar o limpiar los computadores. Asegurate de que el BFF y el servicio de computador esten activos." -ForegroundColor Red
}

# C. Limpiar Usuarios
Write-Host " -> Consultando usuarios..."
try {
    $usuarios = Invoke-RestMethod -Uri "$baseUrl/api/usuarios" -Method Get -Headers $headers
    if ($usuarios -and $usuarios.Count -gt 0) {
        Write-Host "    Se encontraron $($usuarios.Count) usuarios. Eliminandolos..." -ForegroundColor Gray
        foreach ($u in $usuarios) {
            $rut = $u.rut
            Write-Host "    Eliminando usuario: $rut" -ForegroundColor Gray
            Invoke-RestMethod -Uri "$baseUrl/api/usuarios/$rut" -Method Delete -Headers $headers
        }
    } else {
        Write-Host "    No se encontraron usuarios previos." -ForegroundColor Gray
    }
} catch {
    Write-Host "    Aviso: No se pudo consultar o limpiar los usuarios. Asegurate de que el BFF y el servicio de usuarios esten activos." -ForegroundColor Red
}

Write-Host "Limpieza completada con exito." -ForegroundColor Green


# ---------------------------------------------------------
# 2. CREACION DE NUEVOS DATOS MEJORADOS (DISTRIBUIDOS)
# ---------------------------------------------------------
Write-Host "`n[2/3] Creando datos de prueba distribuidos..." -ForegroundColor Yellow

# --- USUARIO 1: Juan Perez ---
Write-Host " -> Creando Usuario 1: Juan Perez (12345678-5)..." -ForegroundColor Gray
$user1 = @{
    rut = "12345678-5"
    nombre = "Juan"
    apellido = "Perez"
    gmail = "juan.perez@gmail.com"
    telefono = [long]987654321
} | ConvertTo-Json
$u1Res = Invoke-RestMethod -Uri "$baseUrl/api/usuarios" -Method Post -Headers $headers -Body $user1

# Computador para Juan Perez
Write-Host "    Creando computador para Juan Perez..." -ForegroundColor Gray
$pcJuan = @{
    rutDueno = "12345678-5"
    componentes = @(
        @{ marca = "16GB DDR4 Kingston Fury"; tipo = "RAM" }
        @{ marca = "ASUS TUF Gaming B550-Plus"; tipo = "Placa Madre" }
        @{ marca = "EVGA 600W 80+ Bronze"; tipo = "Fuente de Poder" }
        @{ marca = "AMD Ryzen 5 5600X (Serie 5000)"; tipo = "Procesador" }
        @{ marca = "NVIDIA GTX 1650 4GB GDDR5"; tipo = "GPU" }
    )
} | ConvertTo-Json -Depth 5
$pcJuanRes = Invoke-RestMethod -Uri "$baseUrl/api/computadores" -Method Post -Headers $headers -Body $pcJuan
$idPcJuan = $pcJuanRes.idPc

# Ticket de Mantencion para Juan Perez
Write-Host "    Creando ticket de mantencion (LIMPIEZA_SUPERFICIAL) para Juan Perez..." -ForegroundColor Gray
$ticketJuan = @{
    idPc = $idPcJuan
    motivo = "Limpieza y mantencion preventiva por sobrecalentamiento"
    tipoServicio = "LIMPIEZA_SUPERFICIAL"
} | ConvertTo-Json
$tJuanRes = Invoke-RestMethod -Uri "$baseUrl/api/mantenciones" -Method Post -Headers $headers -Body $ticketJuan


# --- USUARIO 2: Maria Gonzalez ---
Write-Host " -> Creando Usuario 2: Maria Gonzalez (15234768-5)..." -ForegroundColor Gray
$user2 = @{
    rut = "15234768-5"
    nombre = "Maria"
    apellido = "Gonzalez"
    gmail = "maria.gonzalez@gmail.com"
    telefono = [long]976543210
} | ConvertTo-Json
$u2Res = Invoke-RestMethod -Uri "$baseUrl/api/usuarios" -Method Post -Headers $headers -Body $user2

# Computador para Maria Gonzalez
Write-Host "    Creando computador para Maria Gonzalez..." -ForegroundColor Gray
$pcMaria = @{
    rutDueno = "15234768-5"
    componentes = @(
        @{ marca = "8GB DDR4 Corsair Vengeance"; tipo = "RAM" }
        @{ marca = "MSI H510M-A Pro"; tipo = "Placa Madre" }
        @{ marca = "Cooler Master MWE 500W"; tipo = "Fuente de Poder" }
        @{ marca = "Intel Core i5-11400 (Serie 11mil)"; tipo = "Procesador" }
        @{ marca = "Intel Iris Xe Graphics"; tipo = "GPU" }
    )
} | ConvertTo-Json -Depth 5
$pcMariaRes = Invoke-RestMethod -Uri "$baseUrl/api/computadores" -Method Post -Headers $headers -Body $pcMaria
$idPcMaria = $pcMariaRes.idPc

# Ticket de Mantencion para Maria Gonzalez
Write-Host "    Creando ticket de mantencion (OPTIMIZACION) para Maria Gonzalez..." -ForegroundColor Gray
$ticketMaria = @{
    idPc = $idPcMaria
    motivo = "Optimizacion de software y limpieza de inicio lento"
    tipoServicio = "OPTIMIZACION"
} | ConvertTo-Json
$tMariaRes = Invoke-RestMethod -Uri "$baseUrl/api/mantenciones" -Method Post -Headers $headers -Body $ticketMaria


# --- USUARIO 3: Carlos Munoz ---
Write-Host " -> Creando Usuario 3: Carlos Munoz (18342790-3)..." -ForegroundColor Gray
$user3 = @{
    rut = "18342790-3"
    nombre = "Carlos"
    apellido = "Munoz"
    gmail = "carlos.munoz@gmail.com"
    telefono = [long]965432109
} | ConvertTo-Json
$u3Res = Invoke-RestMethod -Uri "$baseUrl/api/usuarios" -Method Post -Headers $headers -Body $user3

# Computador para Carlos Munoz
Write-Host "    Creando computador para Carlos Munoz..." -ForegroundColor Gray
$pcCarlos = @{
    rutDueno = "18342790-3"
    componentes = @(
        @{ marca = "32GB DDR4 Corsair Dominator"; tipo = "RAM" }
        @{ marca = "Gigabyte Z590 AORUS Elite"; tipo = "Placa Madre" }
        @{ marca = "Corsair RM750x 750W 80+ Gold"; tipo = "Fuente de Poder" }
        @{ marca = "Intel Core i7-11700K (Serie 11mil)"; tipo = "Procesador" }
        @{ marca = "NVIDIA GTX 1650 Super 4GB"; tipo = "GPU" }
    )
} | ConvertTo-Json -Depth 5
$pcCarlosRes = Invoke-RestMethod -Uri "$baseUrl/api/computadores" -Method Post -Headers $headers -Body $pcCarlos
$idPcCarlos = $pcCarlosRes.idPc

# Ticket de Mantencion para Carlos Munoz
Write-Host "    Creando ticket de mantencion (REPARACION) para Carlos Munoz..." -ForegroundColor Gray
$ticketCarlos = @{
    idPc = $idPcCarlos
    motivo = "Pantalla parpadea y puerto USB-C no responde"
    tipoServicio = "REPARACION"
} | ConvertTo-Json
$tCarlosRes = Invoke-RestMethod -Uri "$baseUrl/api/mantenciones" -Method Post -Headers $headers -Body $ticketCarlos

# Completar el ticket de Carlos Munoz para mostrar estados diferentes
Write-Host "    Completando el ticket de mantencion de Carlos Munoz..." -ForegroundColor Gray
$ticketCarlosCompletado = Invoke-RestMethod -Uri "$baseUrl/api/mantenciones/$($tCarlosRes.idTicket)/completar" -Method Put -Headers $headers

Write-Host "Datos creados y distribuidos exitosamente." -ForegroundColor Green


# ---------------------------------------------------------
# 3. VERIFICACION Y VISTA DE DATOS
# ---------------------------------------------------------
Write-Host "`n[3/3] Consultando listado enriquecido de usuarios para verificar distribucion..." -ForegroundColor Yellow

$response = Invoke-RestMethod -Uri "$baseUrl/api/usuarios" -Method Get -Headers $headers
Write-Host "Usuarios cargados en total: $($response.Count)" -ForegroundColor Green

Write-Host "`nDetalle de la distribucion de computadores y servicios por usuario:" -ForegroundColor Cyan
foreach ($user in $response) {
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Usuario: $($user.nombre) $($user.apellido) | RUT: $($user.rut)" -ForegroundColor Yellow
    Write-Host "Gmail:   $($user.gmail) | Telefono: $($user.telefono)" -ForegroundColor DarkGray
    if ($user.computadores -and $user.computadores.Count -gt 0) {
        Write-Host "Computadores asociados: $($user.computadores.Count)" -ForegroundColor Green
        foreach ($pc in $user.computadores) {
            Write-Host "  -> PC ID: $($pc.idPc)" -ForegroundColor DarkCyan

            # Componentes
            $comps = @()
            foreach ($comp in $pc.componentes) {
                $comps += "$($comp.tipo) ($($comp.marca))"
            }
            Write-Host "     Componentes: $($comps -join ', ')" -ForegroundColor DarkGray

            # Mantenciones
            if ($pc.mantenciones -and $pc.mantenciones.Count -gt 0) {
                foreach ($m in $pc.mantenciones) {
                    Write-Host "     Ticket ID:   $($m.idTicket)" -ForegroundColor DarkGray
                    Write-Host "     Motivo:      $($m.motivo)" -ForegroundColor DarkGray
                    Write-Host "     Servicio:    $($m.tipoServicio)" -ForegroundColor Magenta
                    if ($m.tipoServicioDetalle) {
                        Write-Host "     Detalle:     $($m.tipoServicioDetalle.nombre) - $($m.tipoServicioDetalle.descripcion) (Costo Base: $($m.tipoServicioDetalle.costoBase))" -ForegroundColor Cyan
                    }
                    Write-Host "     Costo Total: $($m.costoTotal)" -ForegroundColor Yellow
                    $estadoColor = "Red"
                    if ($m.estado -eq 'COMPLETADO') { $estadoColor = "Green" }
                    Write-Host "     Estado:      $($m.estado)" -ForegroundColor $estadoColor
                }
            } else {
                Write-Host "     Sin mantenciones asociadas." -ForegroundColor DarkYellow
            }
        }
    } else {
        Write-Host "ADVERTENCIA: Sin computadores asociados!" -ForegroundColor Red
    }
}
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " PROCESO DE SEEDING Y VALIDACION TERMINADO CON EXITO       " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
