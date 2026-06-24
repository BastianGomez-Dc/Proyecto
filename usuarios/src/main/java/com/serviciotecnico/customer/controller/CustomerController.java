package com.serviciotecnico.customer.controller;

import com.serviciotecnico.customer.dto.CustomerRequest;
import com.serviciotecnico.customer.dto.CustomerResponse;
import com.serviciotecnico.customer.model.Customer;
import com.serviciotecnico.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customerService.findAllComplete();
    }

    @GetMapping("/{rut}")
    public CustomerResponse get(@PathVariable String rut) {
        return customerService.findCompleteByRut(rut.toUpperCase());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @PutMapping("/{rut}")
    public Customer update(@PathVariable String rut, @Valid @RequestBody CustomerRequest request) {
        return customerService.updateCustomer(rut.toUpperCase(), request);
    }

    @DeleteMapping("/{rut}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String rut) {
        customerService.deleteCustomer(rut.toUpperCase());
    }
}
