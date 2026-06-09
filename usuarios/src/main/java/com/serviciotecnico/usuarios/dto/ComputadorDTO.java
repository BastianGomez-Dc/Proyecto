package com.serviciotecnico.usuarios.dto;

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
public class ComputadorDTO {
    private UUID idPc;
    private List<ComponenteDTO> componentes;
    private List<MantencionDTO> mantenciones;
}
