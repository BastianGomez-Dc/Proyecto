package com.serviciotecnico.customer.service;

import com.serviciotecnico.customer.dto.CustomerRequest;
import com.serviciotecnico.customer.model.Customer;
import com.serviciotecnico.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    private static final String VALID_RUT = "12345678-5";

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    private CustomerRequest validRequest() {
        return CustomerRequest.builder()
                .rut(VALID_RUT)
                .firstName("Juan")
                .lastName("Perez")
                .gmail("juan.perez@gmail.com")
                .phone(912345678L)
                .build();
    }

    @Test
    void createCustomer_savesWhenRutIsValidAndNotDuplicated() {
        when(customerRepository.existsById(VALID_RUT)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer created = customerService.createCustomer(validRequest());

        assertEquals(VALID_RUT, created.getRut());
        assertEquals("Juan", created.getFirstName());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_rejectsInvalidRutCheckDigit() {
        CustomerRequest request = validRequest();
        request.setRut("12345678-9");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerService.createCustomer(request));
        assertTrue(ex.getReason().contains("Invalid RUT"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_rejectsDuplicateRut() {
        when(customerRepository.existsById(VALID_RUT)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerService.createCustomer(validRequest()));
        assertTrue(ex.getReason().contains("already exists"));
    }

    @Test
    void findByRut_throwsNotFoundWhenMissing() {
        when(customerRepository.findById("00000000-0")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerService.findByRut("00000000-0"));
        assertTrue(ex.getReason().contains("not found"));
    }

    @Test
    void updateCustomer_rejectsRutMismatchBetweenPathAndBody() {
        Customer existing = Customer.builder().rut(VALID_RUT).build();
        when(customerRepository.findById(VALID_RUT)).thenReturn(Optional.of(existing));

        CustomerRequest request = validRequest();
        request.setRut("87654321-4");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> customerService.updateCustomer(VALID_RUT, request));
        assertTrue(ex.getReason().contains("does not match"));
    }

    @Test
    void deleteCustomer_deletesWhenExists() {
        Customer existing = Customer.builder().rut(VALID_RUT).build();
        when(customerRepository.findById(VALID_RUT)).thenReturn(Optional.of(existing));

        customerService.deleteCustomer(VALID_RUT);

        verify(customerRepository).deleteById(VALID_RUT);
    }
}
