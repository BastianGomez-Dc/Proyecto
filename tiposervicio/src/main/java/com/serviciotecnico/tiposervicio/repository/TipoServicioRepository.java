package com.serviciotecnico.tiposervicio.repository;

import com.serviciotecnico.tiposervicio.model.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
}
