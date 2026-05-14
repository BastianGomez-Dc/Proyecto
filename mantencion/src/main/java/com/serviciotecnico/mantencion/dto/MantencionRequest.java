package com.serviciotecnico.mantencion.dto;

import com.serviciotecnico.mantencion.model.TipoServicioEnum;
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
public class MantencionRequest {

	@NotNull(message = "El id del PC es obligatorio")
	private UUID idPc;

	@NotBlank(message = "El motivo es obligatorio")
	private String motivo;

	@NotNull(message = "El tipo de servicio es obligatorio")
	private TipoServicioEnum tipoServicio;
}
