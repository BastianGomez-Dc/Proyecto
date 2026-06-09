package com.serviciotecnico.mantencion.service;

import com.serviciotecnico.mantencion.dto.MantencionRequest;
import com.serviciotecnico.mantencion.model.MantencionTicket;
import com.serviciotecnico.mantencion.model.TipoServicioEnum;
import com.serviciotecnico.mantencion.repository.MantencionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MantencionService {

    private static final Logger logger = LoggerFactory.getLogger(MantencionService.class);
    private final MantencionRepository mantencionRepository;

    public MantencionService(MantencionRepository mantencionRepository) {
        this.mantencionRepository = mantencionRepository;
    }

    public List<MantencionTicket> listarTodos() {
        return mantencionRepository.findAll();
    }

    public MantencionTicket obtenerPorId(UUID id) {
        return mantencionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket de mantención no encontrado"));
    }

    public List<MantencionTicket> obtenerPorIdPc(UUID idPc) {
        return mantencionRepository.findByIdPc(idPc);
    }

    public MantencionTicket crearTicket(MantencionRequest request) {
        MantencionTicket ticket = MantencionTicket.builder()
                .idPc(request.getIdPc())
                .motivo(request.getMotivo())
                .tipoServicio(request.getTipoServicio())
                .costoTotal(calcularCosto(request.getTipoServicio()))
                .fechaEntrada(LocalDateTime.now())
                .estado("PENDIENTE")
                .build();
        return mantencionRepository.save(ticket);
    }

    public MantencionTicket completarTicket(UUID id) {
        MantencionTicket ticket = obtenerPorId(id);
        ticket.setEstado("COMPLETADO");
        mantencionRepository.save(ticket);
        return ticket;
    }

    public MantencionTicket actualizarTicket(UUID id, MantencionRequest request) {
        MantencionTicket ticket = obtenerPorId(id);
        ticket.setMotivo(request.getMotivo());
        ticket.setTipoServicio(request.getTipoServicio());
        ticket.setCostoTotal(calcularCosto(request.getTipoServicio()));
        mantencionRepository.save(ticket);
        return ticket;
    }

    public void eliminarTicket(UUID id) {
        obtenerPorId(id);
        mantencionRepository.deleteById(id);
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
}
