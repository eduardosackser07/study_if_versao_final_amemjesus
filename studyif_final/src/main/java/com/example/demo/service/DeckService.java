package com.example.demo.service;

import com.example.demo.entities.Deck;
import com.example.demo.entities.User;
import com.example.demo.repository.DeckRepository;
import com.example.demo.repository.FlashcardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeckService {

    @Autowired private DeckRepository deckRepository;
    @Autowired private FlashcardRepository flashcardRepository;

    public List<Deck> listarPorUsuario(User user) {
        return deckRepository.findByUser(user);
    }

    public Deck criar(String nome, User user) {
        if (nome == null || nome.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do baralho é obrigatório");

        Deck deck = new Deck();
        deck.setName(nome.trim());
        deck.setUser(user);
        return deckRepository.save(deck);
    }

    public Deck editar(Integer id, String novoNome, User user) {
        Deck deck = deckRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Baralho não encontrado"));

        if (!deck.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este baralho");

        if (novoNome == null || novoNome.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome não pode ser vazio");

        deck.setName(novoNome.trim());
        return deckRepository.save(deck);
    }

    public void excluir(Integer id, User user) {
        Deck deck = deckRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Baralho não encontrado"));

        if (!deck.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este baralho");

        deckRepository.deleteById(id);
    }

    /** Retorna cada deck enriquecido com contagem de pendentes hoje */
    public List<Map<String, Object>> listarComStats(User user) {
        LocalDate hoje = LocalDate.now();
        return deckRepository.findByUser(user).stream().map(deck -> {
            long total     = flashcardRepository.findByDeck_DeckId(deck.getDeckId()).size();
            long pendentes = flashcardRepository.countPendentesHoje(deck.getDeckId(), hoje);
            return Map.<String, Object>of(
                "deckId",        deck.getDeckId(),
                "name",          deck.getName(),
                "total",         total,
                "pendentesHoje", pendentes
            );
        }).collect(Collectors.toList());
    }
}
