package com.example.demo.repository;

import com.example.demo.entities.Deck;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<Deck, Integer> {
    List<Deck> findByUser(User user);
}
