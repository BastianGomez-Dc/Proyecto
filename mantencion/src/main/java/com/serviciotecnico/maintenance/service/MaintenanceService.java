package com.serviciotecnico.maintenance.service;

import com.serviciotecnico.maintenance.dto.MaintenanceTicketRequest;
import com.serviciotecnico.maintenance.model.MaintenanceTicket;
import com.serviciotecnico.maintenance.model.ServiceTypeEnum;
import com.serviciotecnico.maintenance.repository.MaintenanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    public List<MaintenanceTicket> findAll() {
        return maintenanceRepository.findAll();
    }

    public MaintenanceTicket findById(UUID id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance ticket not found"));
    }

    public List<MaintenanceTicket> findByComputerId(UUID computerId) {
        return maintenanceRepository.findByComputerId(computerId);
    }

    public MaintenanceTicket createTicket(MaintenanceTicketRequest request) {
        MaintenanceTicket ticket = MaintenanceTicket.builder()
                .computerId(request.getComputerId())
                .reason(request.getReason())
                .serviceType(request.getServiceType())
                .totalCost(calculateCost(request.getServiceType()))
                .entryDate(LocalDateTime.now())
                .status("PENDING")
                .build();
        return maintenanceRepository.save(ticket);
    }

    public MaintenanceTicket completeTicket(UUID id) {
        MaintenanceTicket ticket = findById(id);
        ticket.setStatus("COMPLETED");
        maintenanceRepository.save(ticket);
        return ticket;
    }

    public MaintenanceTicket updateTicket(UUID id, MaintenanceTicketRequest request) {
        MaintenanceTicket ticket = findById(id);
        ticket.setReason(request.getReason());
        ticket.setServiceType(request.getServiceType());
        ticket.setTotalCost(calculateCost(request.getServiceType()));
        maintenanceRepository.save(ticket);
        return ticket;
    }

    public void deleteTicket(UUID id) {
        findById(id);
        maintenanceRepository.deleteById(id);
    }

    private double calculateCost(ServiceTypeEnum serviceType) {
        return switch (serviceType) {
            case SURFACE_CLEANING -> 25000.0;
            case DEEP_CLEANING -> 45000.0;
            case REPAIR -> 60000.0;
            case UPGRADE -> 80000.0;
            case OPTIMIZATION -> 30000.0;
        };
    }
}
