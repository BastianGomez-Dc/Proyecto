package com.serviciotecnico.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private String rut;
    private String firstName;
    private String lastName;
    private String gmail;
    private Long phone;
    private List<ComputerDTO> computers;
}
