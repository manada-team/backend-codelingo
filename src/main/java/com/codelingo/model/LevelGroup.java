package com.codelingo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agrupa niveles por dificultad.
 * Ejemplo: Principiante (1-10), Intermedio (11-20), Avanzado (21-30)
 */
@Entity
@Table(name = "level_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private int minLevel;

    @Column(nullable = false)
    private int maxLevel;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @OneToMany(mappedBy = "levelGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Level> levels;

    public enum Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}