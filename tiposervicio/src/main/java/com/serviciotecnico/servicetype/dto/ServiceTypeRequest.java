package com.serviciotecnico.servicetype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceTypeRequest {

    @NotBlank(message = "The service name is required")
    private String name;

    @NotBlank(message = "The description is required")
    private String description;

    @NotNull(message = "The base cost is required")
    private Double baseCost;
}
