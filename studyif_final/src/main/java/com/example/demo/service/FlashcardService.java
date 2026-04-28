package com.example.demo.service;

import com.example.demo.dto.FlashcardRevisaoDTO;
import com.example.demo.entities.Deck;
import com.example.demo.entities.Flashcard;
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

@Service
public class FlashcardService {

    @Autowired private FlashcardRepository flashcardRepository;
    @Autowired private DeckRepository deckRepository;

    // ── CRUD ──────────────────────────────────────────────────

    public List<Flashcard> listarPorDeck(Integer deckId) {
        return flashcardRepository.findByDeck_DeckId(deckId);
    }

    public Flashcard adicionar(Integer deckId, Flashcard flashcard) {
        Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Baralho não encontrado"));

        if (flashcard.getFront() == null || flashcard.getFront().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A frente do card é obrigatória");
        if (flashcard.getBack() == null || flashcard.getBack().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O verso do card é obrigatório");

        flashcard.setDeck(deck);
        // Reseta campos SM-2 para card novo
        flashcard.setFatorFacilidade(2.5);
        flashcard.setRepeticoes(0);
        flashcard.setIntervaloDias(1);
        flashcard.setProximaRevisao(null);
        flashcard.setTotalVisto(0);
        flashcard.setTotalAcertos(0);

        return flashcardRepository.save(flashcard);
    }

    public Flashcard editar(Integer id, Flashcard dados) {
        Flashcard existing = flashcardRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card não encontrado"));

        if (dados.getFront() == null || dados.getFront().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A frente do card é obrigatória");
        if (dados.getBack() == null || dados.getBack().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O verso do card é obrigatório");

        existing.setFront(dados.getFront());
        existing.setBack(dados.getBack());
        return flashcardRepository.save(existing);
    }

    public void deletar(Integer id) {
        if (!flashcardRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card não encontrado");
        flashcardRepository.deleteById(id);
    }

    // ── SESSÃO DE ESTUDO ──────────────────────────────────────

    /** Retorna os cards pendentes para hoje (novos + revisão vencida) */
    public List<Flashcard> buscarPendentesHoje(Integer deckId) {
        return flashcardRepository.findPendentesHoje(deckId, LocalDate.now());
    }

    /** Retorna estatísticas do deck para exibir antes de iniciar */
    public Map<String, Object> statsDeck(Integer deckId) {
        LocalDate hoje = LocalDate.now();
        long total     = flashcardRepository.findByDeck_DeckId(deckId).size();
        long pendentes = flashcardRepository.countPendentesHoje(deckId, hoje);
        long agendados = flashcardRepository.countAgendados(deckId, hoje);

        return Map.of(
            "total", total,
            "pendentesHoje", pendentes,
            "agendados", agendados,
            "novos", total - pendentes - agendados
        );
    }

    /**
     * Processa a resposta do usuário usando o algoritmo SM-2.
     *
     * Notas:
     *   0 – Blackout: não lembrou nada
     *   1 – Errou, mas ao ver a resposta reconheceu
     *   2 – Errou, mas lembrava parcialmente
     *   3 – Acertou com dificuldade (hesitou muito)
     *   4 – Acertou sem dificuldade
     *   5 – Acertou na hora, muito fácil
     *
     * Regras SM-2:
     *   - nota < 3 → reset: repeticoes=0, intervalo=1 (volta para amanhã)
     *   - nota >= 3 → avança:
     *       rep 0 → intervalo = 1
     *       rep 1 → intervalo = 6
     *       rep N → intervalo = anterior * fatorFacilidade
     *   - fatorFacilidade += 0.1 - (5-nota)*(0.08 + (5-nota)*0.02), mínimo 1.3
     */
    public Flashcard processarRevisao(FlashcardRevisaoDTO dto) {
        Flashcard card = flashcardRepository.findById(dto.getFlashcardId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card não encontrado"));

        int nota = dto.getNota();
        if (nota < 0 || nota > 5)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nota deve ser entre 0 e 5");

        card.setTotalVisto(card.getTotalVisto() + 1);

        if (nota >= 3) {
            // ── Acertou ──
            card.setTotalAcertos(card.getTotalAcertos() + 1);

            int novoIntervalo;
            switch (card.getRepeticoes()) {
                case 0  -> novoIntervalo = 1;
                case 1  -> novoIntervalo = 6;
                default -> novoIntervalo = (int) Math.round(card.getIntervaloDias() * card.getFatorFacilidade());
            }
            card.setRepeticoes(card.getRepeticoes() + 1);
            card.setIntervaloDias(novoIntervalo);
            card.setProximaRevisao(LocalDate.now().plusDays(novoIntervalo));
        } else {
            // ── Errou → reset ──
            card.setRepeticoes(0);
            card.setIntervaloDias(1);
            card.setProximaRevisao(LocalDate.now().plusDays(1));
        }

        // Atualiza fator de facilidade (independe de acerto/erro)
        double novoFF = card.getFatorFacilidade()
            + (0.1 - (5 - nota) * (0.08 + (5 - nota) * 0.02));
        card.setFatorFacilidade(Math.max(1.3, novoFF));

        return flashcardRepository.save(card);
    }
}
