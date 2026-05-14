package com.serviciotecnico.computador.controller;

import com.serviciotecnico.computador.dto.ComputadorRequest;
import com.serviciotecnico.computador.model.Computador;
import com.serviciotecnico.computador.service.ComputadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/computadores")
public class ComputadorController {

    private final ComputadorService computadorService;

    public ComputadorController(ComputadorService computadorService) {
        this.computadorService = computadorService;
    }

    @GetMapping
    public List<Computador> listar() {
        return computadorService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Computador obtener(@PathVariable UUID id) {
        return computadorService.obtenerPorId(id);
    }

    @GetMapping("/cliente/{rut}")
    public List<Computador> listarPorCliente(@PathVariable String rut) {
        return computadorService.obtenerPorRutDueno(rut.toUpperCase());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Computador crear(@Valid @RequestBody ComputadorRequest request) {
        return computadorService.crearComputador(request);
    }

    @PutMapping("/{id}")
    public Computador actualizar(@PathVariable UUID id, @Valid @RequestBody ComputadorRequest request) {
        return computadorService.actualizarComputador(id, request);
    }
}
