package com.serviciotecnico.mantencion.service;

import com.serviciotecnico.mantencion.dto.MantencionRequest;
import com.serviciotecnico.mantencion.model.MantencionTicket;
import com.serviciotecnico.mantencion.model.TipoServicioEnum;
import com.serviciotecnico.mantencion.repository.MantencionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MantencionService {

	private final MantencionRepository mantencionRepository;

	public MantencionService(MantencionRepository mantencionRepository) {
		this.mantencionRepository = mantencionRepository;
	}

	public List<MantencionTicket> listarTodos() {
		return mantencionRepository.findAll();
	}

	public MantencionTicket obtenerPorId(UUID id) {
		return mantencionRepository.findById(id)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket de mantención no encontrado"));
	}

	public MantencionTicket crearTicket(MantencionRequest request) {
		double costo = calcularCosto(request.getTipoServicio());
		MantencionTicket ticket = MantencionTicket.builder()
				.idPc(request.getIdPc())
				.motivo(request.getMotivo())
				.tipoServicio(request.getTipoServicio())
				.costoTotal(costo)
				.fechaEntrada(LocalDateTime.now())
				.estado("PENDIENTE")
				.build();
		return mantencionRepository.save(ticket);
	}

	public MantencionTicket completarTicket(UUID id) {
		MantencionTicket ticket = obtenerPorId(id);
		ticket.setEstado("COMPLETADO");
		return mantencionRepository.save(ticket);
	}

	public MantencionTicket actualizarTicket(UUID id, MantencionRequest request) {
		MantencionTicket ticket = obtenerPorId(id);
		double costo = calcularCosto(request.getTipoServicio());
		ticket.setMotivo(request.getMotivo());
		ticket.setTipoServicio(request.getTipoServicio());
		ticket.setCostoTotal(costo);
		return mantencionRepository.save(ticket);
	}

	private double calcularCosto(TipoServicioEnum tipoServicio) {
		return switch (tipoServicio) {
			case LIMPIEZA_SUPERFICIAL -> 25000.0;
			case LIMPIEZA_PROFUNDA -> 45000.0;
			case REPARACION -> 60000.0;
			case MEJORA -> 80000.0;
			case OPTIMIZACION -> 30000.0;
		};
	}

	public List<MantencionTicket> listarPorComputador(UUID idPc) {
		return mantencionRepository.findByIdPc(idPc);
	}
}
