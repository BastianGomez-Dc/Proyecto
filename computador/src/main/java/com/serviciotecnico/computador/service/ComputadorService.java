package com.serviciotecnico.computador.service;

import com.serviciotecnico.computador.dto.ComputadorRequest;
import com.serviciotecnico.computador.model.Computador;
import com.serviciotecnico.computador.repository.ComputadorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ComputadorService {

    private final ComputadorRepository computadorRepository;

    public ComputadorService(ComputadorRepository computadorRepository) {
        this.computadorRepository = computadorRepository;
    }

    public List<Computador> obtenerTodos() {
        return computadorRepository.findAll();
    }

    public Computador obtenerPorId(UUID id) {
        return computadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Computador no encontrado"));
    }

    public List<Computador> obtenerPorRutDueno(String rutDueno) {
        return computadorRepository.findByRutDueno(rutDueno);
    }

    public Computador crearComputador(ComputadorRequest request) {
        String rut = request.getRutDueno().toUpperCase();
        if (!esRutValido(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
        }
        Computador computador = Computador.builder()
                .rutDueno(rut)
                .componentes(request.getComponentes())
                .build();
        return computadorRepository.save(computador);
    }

    public Computador actualizarComputador(UUID id, ComputadorRequest request) {
        Computador existente = obtenerPorId(id);
        String rut = request.getRutDueno().toUpperCase();
        if (!esRutValido(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
        }
        existente.setRutDueno(rut);
        existente.setComponentes(request.getComponentes());
        return computadorRepository.save(existente);
    }

    private boolean esRutValido(String rut) {
        if (rut == null || !rut.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;
        }
        String[] partes = rut.split("-");
        String cuerpo = partes[0];
        char dv = Character.toUpperCase(partes[1].charAt(0));
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalculado = resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
        return dv == dvCalculado;
    }
}
