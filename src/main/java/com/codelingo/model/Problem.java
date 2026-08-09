package com.codelingo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "problems")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int problemNumber;

    @NotBlank
    @Column(nullable = false)
    private String title;

    // Enunciado del problema
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Codigo inicial que ve el jugador en el editor al abrir el problema
    @Column(columnDefinition = "TEXT")
    private String starterCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Builder.Default
    @Column(nullable = false)
    private int xpReward = 20;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TestCase> testCases;

    @JsonIgnore
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProblemProgress> progressList;

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}