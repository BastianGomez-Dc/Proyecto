package com.serviciotecnico.mantencion.model;

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
public class MantencionTicket {

	@Id
	@GeneratedValue
	@Column(name = "id_ticket", columnDefinition = "BINARY(16)")
	private UUID idTicket;

	@Column(name = "id_pc", nullable = false, columnDefinition = "BINARY(16)")
	private UUID idPc;

	@Column(nullable = false, length = 500)
	private String motivo;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_servicio", nullable = false)
	private TipoServicioEnum tipoServicio;

	@Column(name = "costo_total", nullable = false)
	private Double costoTotal;

	@Column(name = "fecha_entrada", nullable = false)
	private LocalDateTime fechaEntrada;

	@Column(nullable = false)
	private String estado;
}
