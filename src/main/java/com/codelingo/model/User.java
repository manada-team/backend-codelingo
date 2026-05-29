package com.codelingo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Role role = Role.PLAYER;

    @Builder.Default
    @Column(nullable = false)
    private int currentStreak = 0;

    @Builder.Default
    @Column(nullable = false)
    private int longestStreak = 0;

    private LocalDate lastActivityDate;

    @Builder.Default
    @Column(nullable = false)
    private int totalXp = 0;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<UserProgress> progressList;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProgress> progressList;
    @Builder.Default
    @Column(name = "xp_python", nullable = false)
    private int xpPython = 0;

    @Builder.Default
    @Column(name = "xp_java", nullable = false)
    private int xpJava = 0;

    @Builder.Default
    @Column(name = "xp_c", nullable = false)
    private int xpC = 0;

    @Column(name = "active_language")
    private String activeLanguage;
}