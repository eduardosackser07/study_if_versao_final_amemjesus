package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "deck")
@Data
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deck_id")
    private Integer deckId;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"decks", "password", "handler", "hibernateLazyInitializer"})
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL)
    private List<Flashcard> flashcards;

    public Deck() {}

    public Deck(Integer deckId, String name, User user, List<Flashcard> flashcards) {
        this.deckId = deckId;
        this.name = name;
        this.user = user;
        this.flashcards = flashcards;
    }

    public Integer getDeckId() { return deckId; }
    public void setDeckId(Integer deckId) { this.deckId = deckId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<Flashcard> getFlashcards() { return flashcards; }
    public void setFlashcards(List<Flashcard> flashcards) { this.flashcards = flashcards; }
}
