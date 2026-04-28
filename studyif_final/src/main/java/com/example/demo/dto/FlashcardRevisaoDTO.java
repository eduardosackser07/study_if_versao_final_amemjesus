package com.example.demo.dto;

/**
 * Enviado pelo frontend ao responder um card durante o estudo.
 * nota: 0 = Não lembrei nada, 1 = Errei, 2 = Errei mas lembrei, 3 = Acertei com dificuldade, 4 = Acertei, 5 = Fácil demais
 */
public class FlashcardRevisaoDTO {
    private Integer flashcardId;
    private Integer nota; // 0–5

    public Integer getFlashcardId() { return flashcardId; }
    public void setFlashcardId(Integer flashcardId) { this.flashcardId = flashcardId; }
    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }
}
