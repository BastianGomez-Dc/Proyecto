package com.serviciotecnico.computador.model;

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
public class Componente {

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String tipo;
}
