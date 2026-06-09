package com.serviciotecnico.usuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCompletoResponse {
    private String rut;
    private String nombre;
    private String apellido;
    private String gmail;
    private Long telefono;
    private List<ComputadorDTO> computadores;
}
