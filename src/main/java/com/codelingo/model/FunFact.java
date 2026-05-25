package com.codelingo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fun_facts")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FunFact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}