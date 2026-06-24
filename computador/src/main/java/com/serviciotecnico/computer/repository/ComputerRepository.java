package com.serviciotecnico.computer.repository;

import com.serviciotecnico.computer.model.Computer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComputerRepository extends JpaRepository<Computer, UUID> {
    List<Computer> findByOwnerRut(String ownerRut);
}
