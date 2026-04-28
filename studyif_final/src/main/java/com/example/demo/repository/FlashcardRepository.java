package com.example.demo.repository;

import com.example.demo.entities.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    List<Flashcard> findByDeck_DeckId(Integer deckId);

    /**
     * Cards pendentes para revisão hoje:
     * proxima_revisao é nula (nunca estudado) OU <= hoje
     */
    @Query("""
        SELECT f FROM Flashcard f
        WHERE f.deck.deckId = :deckId
        AND (f.proximaRevisao IS NULL OR f.proximaRevisao <= :hoje)
        ORDER BY f.proximaRevisao ASC NULLS FIRST
    """)
    List<Flashcard> findPendentesHoje(@Param("deckId") Integer deckId, @Param("hoje") LocalDate hoje);

    /** Quantos cards estão pendentes para hoje neste deck */
    @Query("""
        SELECT COUNT(f) FROM Flashcard f
        WHERE f.deck.deckId = :deckId
        AND (f.proximaRevisao IS NULL OR f.proximaRevisao <= :hoje)
    """)
    Long countPendentesHoje(@Param("deckId") Integer deckId, @Param("hoje") LocalDate hoje);

    /** Quantos cards estão agendados para o futuro (já dominados) */
    @Query("""
        SELECT COUNT(f) FROM Flashcard f
        WHERE f.deck.deckId = :deckId
        AND f.proximaRevisao > :hoje
    """)
    Long countAgendados(@Param("deckId") Integer deckId, @Param("hoje") LocalDate hoje);
}
