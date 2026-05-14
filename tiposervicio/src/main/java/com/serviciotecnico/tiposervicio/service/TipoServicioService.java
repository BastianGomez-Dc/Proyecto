package com.serviciotecnico.tiposervicio.service;

import com.serviciotecnico.tiposervicio.dto.TipoServicioRequest;
import com.serviciotecnico.tiposervicio.model.TipoServicio;
import com.serviciotecnico.tiposervicio.repository.TipoServicioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TipoServicioService {

    private final TipoServicioRepository tipoServicioRepository;

    public TipoServicioService(TipoServicioRepository tipoServicioRepository) {
        this.tipoServicioRepository = tipoServicioRepository;
    }

    public List<TipoServicio> listarTodos() {
        return tipoServicioRepository.findAll();
    }

    public TipoServicio obtenerPorId(Long id) {
        return tipoServicioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de servicio no encontrado"));
    }

    public TipoServicio crearTipoServicio(TipoServicioRequest request) {
        TipoServicio tipoServicio = TipoServicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .costoBase(request.getCostoBase())
                .build();
        return tipoServicioRepository.save(tipoServicio);
    }

    public TipoServicio actualizarTipoServicio(Long id, TipoServicioRequest request) {
        TipoServicio existente = obtenerPorId(id);
        existente.setNombre(request.getNombre());
        existente.setDescripcion(request.getDescripcion());
        existente.setCostoBase(request.getCostoBase());
        return tipoServicioRepository.save(existente);
    }
}
