package com.serviciotecnico.computer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Component {

    @Column(name = "tipo", nullable = false)
    private String type;

    @Column(name = "marca", nullable = false)
    private String brand;

    @Column(name = "modelo")
    private String model;
}
