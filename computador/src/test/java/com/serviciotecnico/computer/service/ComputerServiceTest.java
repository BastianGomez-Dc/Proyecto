package com.serviciotecnico.computer.service;

import com.serviciotecnico.computer.dto.ComputerRequest;
import com.serviciotecnico.computer.model.Component;
import com.serviciotecnico.computer.model.Computer;
import com.serviciotecnico.computer.repository.ComputerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComputerServiceTest {

    @Mock
    private ComputerRepository computerRepository;

    private ComputerService computerService;

    private static final String VALID_RUT = "12345678-5";

    @BeforeEach
    void setUp() {
        computerService = new ComputerService(computerRepository);
    }

    private ComputerRequest validRequest() {
        return ComputerRequest.builder()
                .ownerRut(VALID_RUT)
                .components(List.of(Component.builder().type("Procesador").brand("Intel").build()))
                .build();
    }

    @Test
    void createComputer_savesWhenRutIsValid() {
        when(computerRepository.save(any(Computer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Computer created = computerService.createComputer(validRequest());

        assertEquals(VALID_RUT, created.getOwnerRut());
        assertEquals(1, created.getComponents().size());
        verify(computerRepository).save(any(Computer.class));
    }

    @Test
    void createComputer_rejectsInvalidRutCheckDigit() {
        ComputerRequest request = validRequest();
        request.setOwnerRut("12345678-9");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> computerService.createComputer(request));
        assertTrue(ex.getReason().contains("Invalid RUT"));
        verify(computerRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(computerRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> computerService.findById(id));
        assertTrue(ex.getReason().contains("not found"));
    }

    @Test
    void findByOwnerRut_delegatesToRepository() {
        when(computerRepository.findByOwnerRut(VALID_RUT)).thenReturn(List.of(new Computer()));

        List<Computer> result = computerService.findByOwnerRut(VALID_RUT);

        assertEquals(1, result.size());
        verify(computerRepository).findByOwnerRut(VALID_RUT);
    }

    @Test
    void deleteComputer_deletesWhenExists() {
        UUID id = UUID.randomUUID();
        when(computerRepository.findById(id)).thenReturn(Optional.of(new Computer()));

        computerService.deleteComputer(id);

        verify(computerRepository).deleteById(id);
    }
}
