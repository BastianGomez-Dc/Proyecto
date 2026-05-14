package com.serviciotecnico.usuarios.dto.external;

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
public class MantencionDto {

    private UUID idTicket;
    private UUID idPc;
    private String motivo;
    private String tipoServicio;
    private Double costoTotal;
    private LocalDateTime fechaEntrada;
    private String estado;
}