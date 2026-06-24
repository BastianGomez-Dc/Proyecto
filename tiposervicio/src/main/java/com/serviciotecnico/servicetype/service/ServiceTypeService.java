package com.serviciotecnico.servicetype.service;

import com.serviciotecnico.servicetype.dto.ServiceTypeRequest;
import com.serviciotecnico.servicetype.model.ServiceType;
import com.serviciotecnico.servicetype.repository.ServiceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceTypeService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceTypeService.class);
    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public List<ServiceType> findAll() {
        return serviceTypeRepository.findAll();
    }

    public ServiceType findById(Long id) {
        return serviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service type not found"));
    }

    public ServiceType createServiceType(ServiceTypeRequest request) {
        ServiceType serviceType = ServiceType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .baseCost(request.getBaseCost())
                .build();
        return serviceTypeRepository.save(serviceType);
    }

    public ServiceType updateServiceType(Long id, ServiceTypeRequest request) {
        ServiceType existing = findById(id);
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setBaseCost(request.getBaseCost());
        serviceTypeRepository.save(existing);
        return existing;
    }

    public void deleteServiceType(Long id) {
        findById(id);
        serviceTypeRepository.deleteById(id);
    }
}
