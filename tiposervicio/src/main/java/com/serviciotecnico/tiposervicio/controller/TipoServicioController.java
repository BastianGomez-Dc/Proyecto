package com.serviciotecnico.tiposervicio.controller;

import com.serviciotecnico.tiposervicio.dto.TipoServicioRequest;
import com.serviciotecnico.tiposervicio.model.TipoServicio;
import com.serviciotecnico.tiposervicio.service.TipoServicioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiposervicios")
public class TipoServicioController {

    private final TipoServicioService tipoServicioService;

    public TipoServicioController(TipoServicioService tipoServicioService) {
        this.tipoServicioService = tipoServicioService;
    }

    @GetMapping
    public List<TipoServicio> listar() {
        return tipoServicioService.listarTodos();
    }

    @GetMapping("/{id}")
    public TipoServicio obtener(@PathVariable Long id) {
        return tipoServicioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoServicio crear(@Valid @RequestBody TipoServicioRequest request) {
        return tipoServicioService.crearTipoServicio(request);
    }

    @PutMapping("/{id}")
    public TipoServicio actualizar(@PathVariable Long id, @Valid @RequestBody TipoServicioRequest request) {
        return tipoServicioService.actualizarTipoServicio(id, request);
    }
}
