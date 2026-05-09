package com.codelingo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int levelNumber;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Enunciado del desafio de programacion
    @Column(columnDefinition = "TEXT", nullable = false)
    private String challengeContent;

    // Salida esperada para validar la solucion del jugador
    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    // Lenguaje de programacion del nivel (ej: "java", "python", "javascript")
//    @Column(nullable = false)
//    private String language;

    // XP que otorga completar este nivel
    @Builder.Default
    @Column(nullable = false)
    private int xpReward = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_group_id", nullable = false)
    private LevelGroup levelGroup;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProgress> userProgressList;
}