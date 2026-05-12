package com.serviciotecnico.mantencion.repository;

import com.serviciotecnico.mantencion.model.MantencionTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MantencionRepository extends JpaRepository<MantencionTicket, UUID> {
}
