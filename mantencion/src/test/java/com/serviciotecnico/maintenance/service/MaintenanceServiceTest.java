package com.serviciotecnico.maintenance.service;

import com.serviciotecnico.maintenance.dto.MaintenanceTicketRequest;
import com.serviciotecnico.maintenance.model.MaintenanceTicket;
import com.serviciotecnico.maintenance.model.ServiceTypeEnum;
import com.serviciotecnico.maintenance.repository.MaintenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService(maintenanceRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "SURFACE_CLEANING, 25000.0",
            "DEEP_CLEANING, 45000.0",
            "REPAIR, 60000.0",
            "UPGRADE, 80000.0",
            "OPTIMIZATION, 30000.0"
    })
    void createTicket_calculatesCostPerServiceType(ServiceTypeEnum serviceType, double expectedCost) {
        when(maintenanceRepository.save(any(MaintenanceTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceTicketRequest request = MaintenanceTicketRequest.builder()
                .computerId(UUID.randomUUID())
                .reason("Test reason")
                .serviceType(serviceType)
                .build();

        MaintenanceTicket created = maintenanceService.createTicket(request);

        assertEquals(expectedCost, created.getTotalCost());
        assertEquals("PENDING", created.getStatus());
    }

    @Test
    void completeTicket_setsStatusToCompleted() {
        UUID id = UUID.randomUUID();
        MaintenanceTicket ticket = MaintenanceTicket.builder().id(id).status("PENDING").build();
        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(ticket));
        when(maintenanceRepository.save(any(MaintenanceTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaintenanceTicket completed = maintenanceService.completeTicket(id);

        assertEquals("COMPLETED", completed.getStatus());
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(maintenanceRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> maintenanceService.findById(id));
        assertTrue(ex.getReason().contains("not found"));
    }

    @Test
    void deleteTicket_deletesWhenExists() {
        UUID id = UUID.randomUUID();
        when(maintenanceRepository.findById(id)).thenReturn(Optional.of(new MaintenanceTicket()));

        maintenanceService.deleteTicket(id);

        verify(maintenanceRepository).deleteById(id);
    }
}
