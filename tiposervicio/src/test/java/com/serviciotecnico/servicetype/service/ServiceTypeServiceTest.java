package com.serviciotecnico.servicetype.service;

import com.serviciotecnico.servicetype.dto.ServiceTypeRequest;
import com.serviciotecnico.servicetype.model.ServiceType;
import com.serviciotecnico.servicetype.repository.ServiceTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceTypeServiceTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;

    private ServiceTypeService serviceTypeService;

    @BeforeEach
    void setUp() {
        serviceTypeService = new ServiceTypeService(serviceTypeRepository);
    }

    @Test
    void createServiceType_savesRequestedValues() {
        ServiceTypeRequest request = ServiceTypeRequest.builder()
                .name("Diagnostico")
                .description("Revision general del equipo")
                .baseCost(15000.0)
                .build();
        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceType created = serviceTypeService.createServiceType(request);

        assertEquals("Diagnostico", created.getName());
        assertEquals(15000.0, created.getBaseCost());
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        when(serviceTypeRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> serviceTypeService.findById(99L));
        assertTrue(ex.getReason().contains("not found"));
    }

    @Test
    void updateServiceType_overwritesExistingFields() {
        ServiceType existing = ServiceType.builder().id(1L).name("Old").description("Old desc").baseCost(1000.0).build();
        when(serviceTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceTypeRepository.save(any(ServiceType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceTypeRequest request = ServiceTypeRequest.builder()
                .name("New").description("New desc").baseCost(2000.0).build();
        ServiceType updated = serviceTypeService.updateServiceType(1L, request);

        assertEquals("New", updated.getName());
        assertEquals(2000.0, updated.getBaseCost());
    }

    @Test
    void findAll_delegatesToRepository() {
        when(serviceTypeRepository.findAll()).thenReturn(List.of(new ServiceType()));

        List<ServiceType> result = serviceTypeService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void deleteServiceType_deletesWhenExists() {
        ServiceType existing = ServiceType.builder().id(1L).build();
        when(serviceTypeRepository.findById(1L)).thenReturn(Optional.of(existing));

        serviceTypeService.deleteServiceType(1L);

        verify(serviceTypeRepository).deleteById(1L);
    }
}
