package com.serviciotecnico.customer.service;

import com.serviciotecnico.customer.dto.CustomerRequest;
import com.serviciotecnico.customer.dto.CustomerResponse;
import com.serviciotecnico.customer.dto.ComputerDTO;
import com.serviciotecnico.customer.dto.MaintenanceDTO;
import com.serviciotecnico.customer.dto.ServiceTypeDTO;
import com.serviciotecnico.customer.model.Customer;
import com.serviciotecnico.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${computer.service.url:http://localhost:8080}")
    private String computerUrl;

    @Value("${servicetype.service.url:http://localhost:8082}")
    private String serviceTypeUrl;

    @Value("${maintenance.service.url:http://localhost:8084}")
    private String maintenanceUrl;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse findCompleteByRut(String rut) {
        Customer customer = findByRut(rut);
        List<ServiceTypeDTO> serviceTypes = fetchServiceTypes();
        List<ComputerDTO> computers = fetchComputersWithMaintenance(rut, serviceTypes);
        return toResponse(customer, computers);
    }

    public List<CustomerResponse> findAllComplete() {
        List<ServiceTypeDTO> serviceTypes = fetchServiceTypes();
        List<CustomerResponse> responses = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            List<ComputerDTO> computers = fetchComputersWithMaintenance(c.getRut(), serviceTypes);
            responses.add(toResponse(c, computers));
        }
        return responses;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findByRut(String rut) {
        return customerRepository.findById(rut)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    public Customer createCustomer(CustomerRequest request) {
        String rut = request.getRut().toUpperCase();
        if (!isRutValid(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RUT");
        }
        if (customerRepository.existsById(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer already exists");
        }
        Customer customer = Customer.builder()
                .rut(rut)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gmail(request.getGmail())
                .phone(request.getPhone())
                .build();
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(String rut, CustomerRequest request) {
        Customer existingCustomer = findByRut(rut);
        if (!request.getRut().equalsIgnoreCase(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The RUT in the request body does not match the RUT in the path");
        }
        if (!isRutValid(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RUT");
        }
        existingCustomer.setFirstName(request.getFirstName());
        existingCustomer.setLastName(request.getLastName());
        existingCustomer.setGmail(request.getGmail());
        existingCustomer.setPhone(request.getPhone());
        customerRepository.save(existingCustomer);
        return existingCustomer;
    }

    public void deleteCustomer(String rut) {
        findByRut(rut);
        customerRepository.deleteById(rut);
    }

    private List<ServiceTypeDTO> fetchServiceTypes() {
        try {
            ServiceTypeDTO[] types = restTemplate.getForObject(
                    serviceTypeUrl + "/api/service-types", ServiceTypeDTO[].class);
            if (types != null) return Arrays.asList(types);
        } catch (Exception e) {
            logger.error("Error fetching service types", e);
        }
        return new ArrayList<>();
    }

    private List<ComputerDTO> fetchComputersWithMaintenance(String rut, List<ServiceTypeDTO> serviceTypes) {
        List<ComputerDTO> result = new ArrayList<>();
        try {
            ComputerDTO[] computers = restTemplate.getForObject(
                    computerUrl + "/api/computers/customer/" + rut, ComputerDTO[].class);
            if (computers == null) return result;
            for (ComputerDTO pc : computers) {
                pc.setMaintenanceTickets(fetchMaintenanceTickets(pc, serviceTypes));
                result.add(pc);
            }
        } catch (Exception e) {
            logger.error("Error fetching computers for RUT {}", rut, e);
        }
        return result;
    }

    private List<MaintenanceDTO> fetchMaintenanceTickets(ComputerDTO pc, List<ServiceTypeDTO> serviceTypes) {
        List<MaintenanceDTO> maintenanceTickets = new ArrayList<>();
        try {
            MaintenanceDTO[] tickets = restTemplate.getForObject(
                    maintenanceUrl + "/api/maintenance-tickets/computer/" + pc.getId(), MaintenanceDTO[].class);
            if (tickets != null) {
                for (MaintenanceDTO ticket : tickets) {
                    ticket.setServiceTypeDetail(findServiceTypeDetail(ticket.getServiceType(), serviceTypes));
                    maintenanceTickets.add(ticket);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching maintenance tickets for computer {}", pc.getId(), e);
        }
        return maintenanceTickets;
    }

    private CustomerResponse toResponse(Customer c, List<ComputerDTO> computers) {
        return CustomerResponse.builder()
                .rut(c.getRut())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .gmail(c.getGmail())
                .phone(c.getPhone())
                .computers(computers)
                .build();
    }

    private boolean isRutValid(String rut) {
        if (rut == null || !rut.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;
        }
        String[] parts = rut.split("-");
        String body = parts[0];
        char checkDigit = Character.toUpperCase(parts[1].charAt(0));
        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }
        int remainder = 11 - (sum % 11);
        char calculatedCheckDigit = remainder == 11 ? '0' : remainder == 10 ? 'K' : Character.forDigit(remainder, 10);
        return checkDigit == calculatedCheckDigit;
    }

    private ServiceTypeDTO findServiceTypeDetail(String enumValue, List<ServiceTypeDTO> serviceTypes) {
        if (enumValue == null) return null;
        String mappedDbName = switch (enumValue) {
            case "SURFACE_CLEANING" -> "Limpieza Superficial";
            case "DEEP_CLEANING" -> "Limpieza Profunda";
            case "REPAIR" -> "Reparación";
            case "UPGRADE" -> "Mejora (Upgrade)";
            case "OPTIMIZATION" -> "Optimización";
            default -> enumValue;
        };
        return serviceTypes.stream()
                .filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(mappedDbName))
                .findFirst()
                .orElse(null);
    }
}
