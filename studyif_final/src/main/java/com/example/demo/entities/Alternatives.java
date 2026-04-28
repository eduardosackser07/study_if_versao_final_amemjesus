package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alternatives")
@Data
public class Alternatives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alternative_id")
    private Integer alternativeId;

    @Column(name = "alt_text", nullable = false, columnDefinition = "TEXT")
    private String altText;

    // Letra da alternativa: A, B, C, D, E
    @Column(length = 1)
    private String letra;

    @Column(nullable = false)
    private Boolean correct = false;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;
}
