package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "flashcard")
@Data
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_id")
    private Integer flashcardId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @ManyToOne
    @JoinColumn(name = "deck_id", nullable = false)
    @JsonIgnoreProperties("flashcards")
    private Deck deck;

    // ─── Campos SM-2 ─────────────────────────────────────────
    // Fator de facilidade: começa em 2.5, mínimo 1.3
    @Column(name = "fator_facilidade", nullable = false)
    private Double fatorFacilidade = 2.5;

    // Quantas vezes consecutivas o card foi acertado
    @Column(name = "repeticoes", nullable = false)
    private Integer repeticoes = 0;

    // Intervalo atual em dias até a próxima revisão
    @Column(name = "intervalo_dias", nullable = false)
    private Integer intervaloDias = 1;

    // Data da próxima revisão agendada (null = nunca estudado = estuda hoje)
    @Column(name = "proxima_revisao")
    private LocalDate proximaRevisao;

    // Total de vezes que o card foi respondido (qualquer nota)
    @Column(name = "total_visto", nullable = false)
    private Integer totalVisto = 0;

    // Total de acertos históricos (nota >= 3)
    @Column(name = "total_acertos", nullable = false)
    private Integer totalAcertos = 0;
    // ─────────────────────────────────────────────────────────

    public Flashcard() {}

    public Integer getFlashcardId() { return flashcardId; }
    public void setFlashcardId(Integer flashcardId) { this.flashcardId = flashcardId; }
    public String getFront() { return front; }
    public void setFront(String front) { this.front = front; }
    public String getBack() { return back; }
    public void setBack(String back) { this.back = back; }
    public Deck getDeck() { return deck; }
    public void setDeck(Deck deck) { this.deck = deck; }
    public Double getFatorFacilidade() { return fatorFacilidade; }
    public void setFatorFacilidade(Double fatorFacilidade) { this.fatorFacilidade = fatorFacilidade; }
    public Integer getRepeticoes() { return repeticoes; }
    public void setRepeticoes(Integer repeticoes) { this.repeticoes = repeticoes; }
    public Integer getIntervaloDias() { return intervaloDias; }
    public void setIntervaloDias(Integer intervaloDias) { this.intervaloDias = intervaloDias; }
    public LocalDate getProximaRevisao() { return proximaRevisao; }
    public void setProximaRevisao(LocalDate proximaRevisao) { this.proximaRevisao = proximaRevisao; }
    public Integer getTotalVisto() { return totalVisto; }
    public void setTotalVisto(Integer totalVisto) { this.totalVisto = totalVisto; }
    public Integer getTotalAcertos() { return totalAcertos; }
    public void setTotalAcertos(Integer totalAcertos) { this.totalAcertos = totalAcertos; }
}
