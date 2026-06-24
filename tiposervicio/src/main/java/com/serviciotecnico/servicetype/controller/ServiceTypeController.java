package com.serviciotecnico.servicetype.controller;

import com.serviciotecnico.servicetype.dto.ServiceTypeRequest;
import com.serviciotecnico.servicetype.model.ServiceType;
import com.serviciotecnico.servicetype.service.ServiceTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-types")
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    public ServiceTypeController(ServiceTypeService serviceTypeService) {
        this.serviceTypeService = serviceTypeService;
    }

    @GetMapping
    public List<ServiceType> list() {
        return serviceTypeService.findAll();
    }

    @GetMapping("/{id}")
    public ServiceType get(@PathVariable Long id) {
        return serviceTypeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceType create(@Valid @RequestBody ServiceTypeRequest request) {
        return serviceTypeService.createServiceType(request);
    }

    @PutMapping("/{id}")
    public ServiceType update(@PathVariable Long id, @Valid @RequestBody ServiceTypeRequest request) {
        return serviceTypeService.updateServiceType(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        serviceTypeService.deleteServiceType(id);
    }
}
