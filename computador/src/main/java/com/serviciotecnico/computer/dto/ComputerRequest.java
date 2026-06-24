package com.serviciotecnico.computer.dto;

import com.serviciotecnico.computer.model.Component;
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
public class ComputerRequest {

    @NotBlank(message = "The owner's RUT is required")
    @Pattern(regexp = "^\\d{7,8}-[0-9Kk]$", message = "RUT must have the format 12345678-9 or 1234567-K")
    private String ownerRut;

    @NotEmpty(message = "At least one component is required")
    private List<Component> components;
}
