package com.serviciotecnico.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComputerDTO {
    private UUID id;
    private List<ComponentDTO> components;
    private List<MaintenanceDTO> maintenanceTickets;
}
