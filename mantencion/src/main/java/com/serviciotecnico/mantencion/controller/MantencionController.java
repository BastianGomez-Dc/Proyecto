package com.serviciotecnico.mantencion.controller;

import com.serviciotecnico.mantencion.dto.MantencionRequest;
import com.serviciotecnico.mantencion.model.MantencionTicket;
import com.serviciotecnico.mantencion.service.MantencionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mantenciones")
public class MantencionController {

	private final MantencionService mantencionService;

	public MantencionController(MantencionService mantencionService) {
		this.mantencionService = mantencionService;
	}

	@GetMapping
	public List<MantencionTicket> listar() {
		return mantencionService.listarTodos();
	}

	@GetMapping("/{id}")
	public MantencionTicket obtener(@PathVariable UUID id) {
		return mantencionService.obtenerPorId(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MantencionTicket crear(@Valid @RequestBody MantencionRequest request) {
		return mantencionService.crearTicket(request);
	}

	@PutMapping("/{id}")
	public MantencionTicket actualizar(@PathVariable UUID id, @Valid @RequestBody MantencionRequest request) {
		return mantencionService.actualizarTicket(id, request);
	}

	@PutMapping("/{id}/completar")
	public MantencionTicket completar(@PathVariable UUID id) {
		return mantencionService.completarTicket(id);
	}

	@GetMapping("/computador/{idPc}")
	public List<MantencionTicket> listarPorComputador(@PathVariable UUID idPc) {
		return mantencionService.listarPorComputador(idPc);
	}
}
