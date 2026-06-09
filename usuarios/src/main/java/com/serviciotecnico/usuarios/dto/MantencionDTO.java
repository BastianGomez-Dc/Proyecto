package com.serviciotecnico.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MantencionDTO {
    private UUID idTicket;
    private String motivo;
    private String tipoServicio;
    private TipoServicioDTO tipoServicioDetalle;
    private Double costoTotal;
    private LocalDateTime fechaEntrada;
    private String estado;
}
