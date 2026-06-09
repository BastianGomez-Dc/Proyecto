package com.serviciotecnico.computador.dto;

import com.serviciotecnico.computador.model.Componente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComputadorRequest {

    @NotBlank(message = "El RUT del dueño es obligatorio")
    @Pattern(regexp = "^\\d{7,8}-[0-9Kk]$", message = "El RUT debe tener el formato 12345678-9 o 1234567-K")
    private String rutDueno;

    @NotEmpty(message = "Debe informar al menos un componente")
    private List<Componente> componentes;
}
