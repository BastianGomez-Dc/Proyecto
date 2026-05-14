package com.serviciotecnico.computador.repository;

import com.serviciotecnico.computador.model.Computador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComputadorRepository extends JpaRepository<Computador, UUID> {
    List<Computador> findByRutDueno(String rutDueno);
}
