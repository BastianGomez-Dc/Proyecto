package com.serviciotecnico.computador.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "computadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Computador {

    @Id
    @GeneratedValue
    @Column(name = "id_pc", columnDefinition = "BINARY(16)")
    private UUID idPc;

    @Column(name = "rut_dueno", nullable = false)
    private String rutDueno;

    @ElementCollection
    @CollectionTable(name = "computador_componentes", joinColumns = @JoinColumn(name = "computador_id"))
    @Builder.Default
    private List<Componente> componentes = new ArrayList<>();
}
