package com.example.demo.controller;

import com.example.demo.dto.FlashcardRevisaoDTO;
import com.example.demo.entities.Flashcard;
import com.example.demo.service.FlashcardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    @Autowired private FlashcardService flashcardService;

    // ── CRUD ──────────────────────────────────────────────────

    @GetMapping("/deck/{deckId}")
    public List<Flashcard> listarPorDeck(@PathVariable Integer deckId) {
        return flashcardService.listarPorDeck(deckId);
    }

    @PostMapping("/deck/{deckId}")
    public Flashcard adicionar(@PathVariable Integer deckId, @RequestBody Flashcard flashcard) {
        return flashcardService.adicionar(deckId, flashcard);
    }

    @PutMapping("/{id}")
    public Flashcard editar(@PathVariable Integer id, @RequestBody Flashcard dados) {
        return flashcardService.editar(id, dados);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        flashcardService.deletar(id);
    }

    // ── SESSÃO DE ESTUDO ──────────────────────────────────────

    /** Cards pendentes para estudo hoje */
    @GetMapping("/deck/{deckId}/pendentes")
    public List<Flashcard> pendentesHoje(@PathVariable Integer deckId) {
        return flashcardService.buscarPendentesHoje(deckId);
    }

    /** Stats do deck (total, pendentes hoje, agendados) */
    @GetMapping("/deck/{deckId}/stats")
    public Map<String, Object> stats(@PathVariable Integer deckId) {
        return flashcardService.statsDeck(deckId);
    }

    /** Processa resposta de um card (SM-2) */
    @PostMapping("/revisar")
    public Flashcard revisar(@RequestBody FlashcardRevisaoDTO dto) {
        return flashcardService.processarRevisao(dto);
    }
}
