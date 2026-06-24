package com.serviciotecnico.computer.controller;

import com.serviciotecnico.computer.dto.ComputerRequest;
import com.serviciotecnico.computer.model.Computer;
import com.serviciotecnico.computer.service.ComputerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/computers")
public class ComputerController {

    private final ComputerService computerService;

    public ComputerController(ComputerService computerService) {
        this.computerService = computerService;
    }

    @GetMapping
    public List<Computer> list() {
        return computerService.findAll();
    }

    @GetMapping("/{id}")
    public Computer get(@PathVariable UUID id) {
        return computerService.findById(id);
    }

    @GetMapping("/customer/{rut}")
    public List<Computer> listByCustomer(@PathVariable String rut) {
        return computerService.findByOwnerRut(rut.toUpperCase());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Computer create(@Valid @RequestBody ComputerRequest request) {
        return computerService.createComputer(request);
    }

    @PutMapping("/{id}")
    public Computer update(@PathVariable UUID id, @Valid @RequestBody ComputerRequest request) {
        return computerService.updateComputer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        computerService.deleteComputer(id);
    }
}
