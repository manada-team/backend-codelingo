package com.codelingo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_cases")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Entrada estandar (stdin) para la ejecucion
    @Column(columnDefinition = "TEXT")
    private String input;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedOutput;

    // Si es true, no se muestra al jugador (se usa solo al hacer submit)
    @Builder.Default
    @Column(nullable = false)
    private boolean hidden = false;

    @Builder.Default
    @Column(nullable = false)
    private int orderIndex = 0;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
}