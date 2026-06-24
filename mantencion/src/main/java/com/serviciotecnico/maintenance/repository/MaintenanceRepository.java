package com.serviciotecnico.maintenance.repository;

import com.serviciotecnico.maintenance.model.MaintenanceTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<MaintenanceTicket, UUID> {
    List<MaintenanceTicket> findByComputerId(UUID computerId);
}
