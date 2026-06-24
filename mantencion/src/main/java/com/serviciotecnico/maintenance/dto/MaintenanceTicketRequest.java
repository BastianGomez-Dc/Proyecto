package com.serviciotecnico.maintenance.dto;

import com.serviciotecnico.maintenance.model.ServiceTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTicketRequest {

	@NotNull(message = "The computer id is required")
	private UUID computerId;

	@NotBlank(message = "The reason is required")
	private String reason;

	@NotNull(message = "The service type is required")
	private ServiceTypeEnum serviceType;
}
