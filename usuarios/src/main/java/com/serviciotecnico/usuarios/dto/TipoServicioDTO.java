package com.serviciotecnico.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoServicioDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double costoBase;
}
