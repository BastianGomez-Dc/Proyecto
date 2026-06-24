package com.serviciotecnico.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceDTO {
    private UUID id;
    private String reason;
    private String serviceType;
    private ServiceTypeDTO serviceTypeDetail;
    private Double totalCost;
    private LocalDate entryDate;
    private String status;
}
