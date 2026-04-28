package com.example.demo.controller;

import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    @Autowired private DeckService deckService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public List<Map<String, Object>> listar(@AuthenticationPrincipal OAuth2User principal) {
        return deckService.listarComStats(getUser(principal));
    }

    @PostMapping
    public Object criar(@RequestBody Map<String, String> body,
                        @AuthenticationPrincipal OAuth2User principal) {
        return deckService.criar(body.get("name"), getUser(principal));
    }

    @PutMapping("/{id}")
    public Object editar(@PathVariable Integer id,
                         @RequestBody Map<String, String> body,
                         @AuthenticationPrincipal OAuth2User principal) {
        return deckService.editar(id, body.get("name"), getUser(principal));
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Integer id,
                        @AuthenticationPrincipal OAuth2User principal) {
        deckService.excluir(id, getUser(principal));
    }

    private User getUser(OAuth2User principal) {
        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
        return userRepository.findByEmail(principal.getAttribute("email"))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
