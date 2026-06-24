package com.serviciotecnico.computer.service;

import com.serviciotecnico.computer.dto.ComputerRequest;
import com.serviciotecnico.computer.model.Computer;
import com.serviciotecnico.computer.repository.ComputerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ComputerService {

    private static final Logger logger = LoggerFactory.getLogger(ComputerService.class);
    private final ComputerRepository computerRepository;

    public ComputerService(ComputerRepository computerRepository) {
        this.computerRepository = computerRepository;
    }

    public List<Computer> findAll() {
        return computerRepository.findAll();
    }

    public Computer findById(UUID id) {
        return computerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Computer not found"));
    }

    public List<Computer> findByOwnerRut(String ownerRut) {
        return computerRepository.findByOwnerRut(ownerRut);
    }

    public Computer createComputer(ComputerRequest request) {
        String rut = request.getOwnerRut().toUpperCase();
        if (!isRutValid(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RUT");
        }
        Computer computer = Computer.builder()
                .ownerRut(rut)
                .components(request.getComponents())
                .build();
        return computerRepository.save(computer);
    }

    public Computer updateComputer(UUID id, ComputerRequest request) {
        Computer existing = findById(id);
        String rut = request.getOwnerRut().toUpperCase();
        if (!isRutValid(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RUT");
        }
        existing.setOwnerRut(rut);
        existing.setComponents(request.getComponents());
        computerRepository.save(existing);
        return existing;
    }

    public void deleteComputer(UUID id) {
        findById(id);
        computerRepository.deleteById(id);
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
}
