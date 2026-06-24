# Seeding and Data Improvement Script for Servicio Tecnico Microservices
# This script cleans up old stale test data and populates clean, distributed test data
# across distinct customers, computers, and tickets.
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
    $tickets = Invoke-RestMethod -Uri "$baseUrl/api/maintenance-tickets" -Method Get -Headers $headers
    if ($tickets -and $tickets.Count -gt 0) {
        Write-Host "    Se encontraron $($tickets.Count) tickets. Eliminandolos..." -ForegroundColor Gray
        foreach ($t in $tickets) {
            $id = $t.id
            Write-Host "    Eliminando ticket: $id" -ForegroundColor Gray
            Invoke-RestMethod -Uri "$baseUrl/api/maintenance-tickets/$id" -Method Delete -Headers $headers
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
    $pcs = Invoke-RestMethod -Uri "$baseUrl/api/computers" -Method Get -Headers $headers
    if ($pcs -and $pcs.Count -gt 0) {
        Write-Host "    Se encontraron $($pcs.Count) computadores. Eliminandolos..." -ForegroundColor Gray
        foreach ($pc in $pcs) {
            $id = $pc.id
            Write-Host "    Eliminando computador: $id" -ForegroundColor Gray
            Invoke-RestMethod -Uri "$baseUrl/api/computers/$id" -Method Delete -Headers $headers
        }
    } else {
        Write-Host "    No se encontraron computadores previos." -ForegroundColor Gray
    }
} catch {
    Write-Host "    Aviso: No se pudo consultar o limpiar los computadores. Asegurate de que el BFF y el servicio de computador esten activos." -ForegroundColor Red
}

# C. Limpiar Clientes
Write-Host " -> Consultando clientes..."
try {
    $customers = Invoke-RestMethod -Uri "$baseUrl/api/customers" -Method Get -Headers $headers
    if ($customers -and $customers.Count -gt 0) {
        Write-Host "    Se encontraron $($customers.Count) clientes. Eliminandolos..." -ForegroundColor Gray
        foreach ($c in $customers) {
            $rut = $c.rut
            Write-Host "    Eliminando cliente: $rut" -ForegroundColor Gray
            Invoke-RestMethod -Uri "$baseUrl/api/customers/$rut" -Method Delete -Headers $headers
        }
    } else {
        Write-Host "    No se encontraron clientes previos." -ForegroundColor Gray
    }
} catch {
    Write-Host "    Aviso: No se pudo consultar o limpiar los clientes. Asegurate de que el BFF y el servicio de clientes esten activos." -ForegroundColor Red
}

Write-Host "Limpieza completada con exito." -ForegroundColor Green


# ---------------------------------------------------------
# 2. CREACION DE NUEVOS DATOS MEJORADOS (DISTRIBUIDOS)
# ---------------------------------------------------------
Write-Host "`n[2/3] Creando datos de prueba distribuidos..." -ForegroundColor Yellow

# --- CLIENTE 1: Juan Perez ---
Write-Host " -> Creando Cliente 1: Juan Perez (12345678-5)..." -ForegroundColor Gray
$customer1 = @{
    rut = "12345678-5"
    firstName = "Juan"
    lastName = "Perez"
    gmail = "juan.perez@gmail.com"
    phone = [long]987654321
} | ConvertTo-Json
$c1Res = Invoke-RestMethod -Uri "$baseUrl/api/customers" -Method Post -Headers $headers -Body $customer1

# Computador para Juan Perez
Write-Host "    Creando computador para Juan Perez..." -ForegroundColor Gray
$pcJuan = @{
    ownerRut = "12345678-5"
    components = @(
        @{ brand = "16GB DDR4 Kingston Fury"; type = "RAM" }
        @{ brand = "ASUS TUF Gaming B550-Plus"; type = "Placa Madre" }
        @{ brand = "EVGA 600W 80+ Bronze"; type = "Fuente de Poder" }
        @{ brand = "AMD Ryzen 5 5600X (Serie 5000)"; type = "Procesador" }
        @{ brand = "NVIDIA GTX 1650 4GB GDDR5"; type = "GPU" }
    )
} | ConvertTo-Json -Depth 5
$pcJuanRes = Invoke-RestMethod -Uri "$baseUrl/api/computers" -Method Post -Headers $headers -Body $pcJuan
$idPcJuan = $pcJuanRes.id

# Ticket de Mantencion para Juan Perez
Write-Host "    Creando ticket de mantencion (SURFACE_CLEANING) para Juan Perez..." -ForegroundColor Gray
$ticketJuan = @{
    computerId = $idPcJuan
    reason = "Limpieza y mantencion preventiva por sobrecalentamiento"
    serviceType = "SURFACE_CLEANING"
} | ConvertTo-Json
$tJuanRes = Invoke-RestMethod -Uri "$baseUrl/api/maintenance-tickets" -Method Post -Headers $headers -Body $ticketJuan


# --- CLIENTE 2: Maria Gonzalez ---
Write-Host " -> Creando Cliente 2: Maria Gonzalez (15234768-5)..." -ForegroundColor Gray
$customer2 = @{
    rut = "15234768-5"
    firstName = "Maria"
    lastName = "Gonzalez"
    gmail = "maria.gonzalez@gmail.com"
    phone = [long]976543210
} | ConvertTo-Json
$c2Res = Invoke-RestMethod -Uri "$baseUrl/api/customers" -Method Post -Headers $headers -Body $customer2

# Computador para Maria Gonzalez
Write-Host "    Creando computador para Maria Gonzalez..." -ForegroundColor Gray
$pcMaria = @{
    ownerRut = "15234768-5"
    components = @(
        @{ brand = "8GB DDR4 Corsair Vengeance"; type = "RAM" }
        @{ brand = "MSI H510M-A Pro"; type = "Placa Madre" }
        @{ brand = "Cooler Master MWE 500W"; type = "Fuente de Poder" }
        @{ brand = "Intel Core i5-11400 (Serie 11mil)"; type = "Procesador" }
        @{ brand = "Intel Iris Xe Graphics"; type = "GPU" }
    )
} | ConvertTo-Json -Depth 5
$pcMariaRes = Invoke-RestMethod -Uri "$baseUrl/api/computers" -Method Post -Headers $headers -Body $pcMaria
$idPcMaria = $pcMariaRes.id

# Ticket de Mantencion para Maria Gonzalez
Write-Host "    Creando ticket de mantencion (OPTIMIZATION) para Maria Gonzalez..." -ForegroundColor Gray
$ticketMaria = @{
    computerId = $idPcMaria
    reason = "Optimizacion de software y limpieza de inicio lento"
    serviceType = "OPTIMIZATION"
} | ConvertTo-Json
$tMariaRes = Invoke-RestMethod -Uri "$baseUrl/api/maintenance-tickets" -Method Post -Headers $headers -Body $ticketMaria


# --- CLIENTE 3: Carlos Munoz ---
Write-Host " -> Creando Cliente 3: Carlos Munoz (18342790-3)..." -ForegroundColor Gray
$customer3 = @{
    rut = "18342790-3"
    firstName = "Carlos"
    lastName = "Munoz"
    gmail = "carlos.munoz@gmail.com"
    phone = [long]965432109
} | ConvertTo-Json
$c3Res = Invoke-RestMethod -Uri "$baseUrl/api/customers" -Method Post -Headers $headers -Body $customer3

# Computador para Carlos Munoz
Write-Host "    Creando computador para Carlos Munoz..." -ForegroundColor Gray
$pcCarlos = @{
    ownerRut = "18342790-3"
    components = @(
        @{ brand = "32GB DDR4 Corsair Dominator"; type = "RAM" }
        @{ brand = "Gigabyte Z590 AORUS Elite"; type = "Placa Madre" }
        @{ brand = "Corsair RM750x 750W 80+ Gold"; type = "Fuente de Poder" }
        @{ brand = "Intel Core i7-11700K (Serie 11mil)"; type = "Procesador" }
        @{ brand = "NVIDIA GTX 1650 Super 4GB"; type = "GPU" }
    )
} | ConvertTo-Json -Depth 5
$pcCarlosRes = Invoke-RestMethod -Uri "$baseUrl/api/computers" -Method Post -Headers $headers -Body $pcCarlos
$idPcCarlos = $pcCarlosRes.id

# Ticket de Mantencion para Carlos Munoz
Write-Host "    Creando ticket de mantencion (REPAIR) para Carlos Munoz..." -ForegroundColor Gray
$ticketCarlos = @{
    computerId = $idPcCarlos
    reason = "Pantalla parpadea y puerto USB-C no responde"
    serviceType = "REPAIR"
} | ConvertTo-Json
$tCarlosRes = Invoke-RestMethod -Uri "$baseUrl/api/maintenance-tickets" -Method Post -Headers $headers -Body $ticketCarlos

# Completar el ticket de Carlos Munoz para mostrar estados diferentes
Write-Host "    Completando el ticket de mantencion de Carlos Munoz..." -ForegroundColor Gray
$ticketCarlosCompletado = Invoke-RestMethod -Uri "$baseUrl/api/maintenance-tickets/$($tCarlosRes.id)/complete" -Method Put -Headers $headers

Write-Host "Datos creados y distribuidos exitosamente." -ForegroundColor Green


# ---------------------------------------------------------
# 3. VERIFICACION Y VISTA DE DATOS
# ---------------------------------------------------------
Write-Host "`n[3/3] Consultando listado enriquecido de clientes para verificar distribucion..." -ForegroundColor Yellow

$response = Invoke-RestMethod -Uri "$baseUrl/api/customers" -Method Get -Headers $headers
Write-Host "Clientes cargados en total: $($response.Count)" -ForegroundColor Green

Write-Host "`nDetalle de la distribucion de computadores y servicios por cliente:" -ForegroundColor Cyan
foreach ($customer in $response) {
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Cliente: $($customer.firstName) $($customer.lastName) | RUT: $($customer.rut)" -ForegroundColor Yellow
    Write-Host "Gmail:   $($customer.gmail) | Telefono: $($customer.phone)" -ForegroundColor DarkGray
    if ($customer.computers -and $customer.computers.Count -gt 0) {
        Write-Host "Computadores asociados: $($customer.computers.Count)" -ForegroundColor Green
        foreach ($pc in $customer.computers) {
            Write-Host "  -> PC ID: $($pc.id)" -ForegroundColor DarkCyan

            # Componentes
            $comps = @()
            foreach ($comp in $pc.components) {
                $comps += "$($comp.type) ($($comp.brand))"
            }
            Write-Host "     Componentes: $($comps -join ', ')" -ForegroundColor DarkGray

            # Mantenciones
            if ($pc.maintenanceTickets -and $pc.maintenanceTickets.Count -gt 0) {
                foreach ($m in $pc.maintenanceTickets) {
                    Write-Host "     Ticket ID:   $($m.id)" -ForegroundColor DarkGray
                    Write-Host "     Motivo:      $($m.reason)" -ForegroundColor DarkGray
                    Write-Host "     Servicio:    $($m.serviceType)" -ForegroundColor Magenta
                    if ($m.serviceTypeDetail) {
                        Write-Host "     Detalle:     $($m.serviceTypeDetail.name) - $($m.serviceTypeDetail.description) (Costo Base: $($m.serviceTypeDetail.baseCost))" -ForegroundColor Cyan
                    }
                    Write-Host "     Costo Total: $($m.totalCost)" -ForegroundColor Yellow
                    $estadoColor = "Red"
                    if ($m.status -eq 'COMPLETED') { $estadoColor = "Green" }
                    Write-Host "     Estado:      $($m.status)" -ForegroundColor $estadoColor
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
