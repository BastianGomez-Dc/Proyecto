package com.serviciotecnico.maintenance.controller;

import com.serviciotecnico.maintenance.dto.MaintenanceTicketRequest;
import com.serviciotecnico.maintenance.model.MaintenanceTicket;
import com.serviciotecnico.maintenance.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance-tickets")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public List<MaintenanceTicket> list() {
        return maintenanceService.findAll();
    }

    @GetMapping("/{id}")
    public MaintenanceTicket get(@PathVariable UUID id) {
        return maintenanceService.findById(id);
    }

    @GetMapping("/computer/{computerId}")
    public List<MaintenanceTicket> listByComputer(@PathVariable UUID computerId) {
        return maintenanceService.findByComputerId(computerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceTicket create(@Valid @RequestBody MaintenanceTicketRequest request) {
        return maintenanceService.createTicket(request);
    }

    @PutMapping("/{id}")
    public MaintenanceTicket update(@PathVariable UUID id, @Valid @RequestBody MaintenanceTicketRequest request) {
        return maintenanceService.updateTicket(id, request);
    }

    @PutMapping("/{id}/complete")
    public MaintenanceTicket complete(@PathVariable UUID id) {
        return maintenanceService.completeTicket(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        maintenanceService.deleteTicket(id);
    }
}
