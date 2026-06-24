package com.serviciotecnico.maintenance.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mantenciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTicket {

	@Id
	@GeneratedValue
	@Column(name = "id_ticket", columnDefinition = "BINARY(16)")
	private UUID id;

	@Column(name = "id_pc", nullable = false, columnDefinition = "BINARY(16)")
	private UUID computerId;

	@Column(name = "motivo", nullable = false, length = 500)
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_servicio", nullable = false)
	private ServiceTypeEnum serviceType;

	@Column(name = "costo_total", nullable = false)
	private Double totalCost;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "fecha_entrada", nullable = false)
	private LocalDateTime entryDate;

	@Column(name = "estado", nullable = false)
	private String status;
}
