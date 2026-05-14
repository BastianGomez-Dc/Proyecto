package com.serviciotecnico.usuarios.dto.external;

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
public class ComputadorDto {

    private UUID idPc;
    private String rutDueno;
    private List<ComponenteDto> componentes;
}