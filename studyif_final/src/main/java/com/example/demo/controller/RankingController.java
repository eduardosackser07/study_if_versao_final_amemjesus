package com.example.demo.controller;

import com.example.demo.entities.Patente;
import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired private UserRepository userRepository;

    /** Ranking global de usuários (top 50) */
    @GetMapping("/global")
    public List<Map<String, Object>> rankingGlobal(@AuthenticationPrincipal OAuth2User principal) {
        User userLogado = getUser(principal);
        List<User> ranking = userRepository.findRankingGlobal();

        List<Map<String, Object>> res = new ArrayList<>();
        for (int i = 0; i < Math.min(ranking.size(), 50); i++) {
            User u = ranking.get(i);
            res.add(buildUserMap(u, i + 1, userLogado.getId().equals(u.getId())));
        }
        return res;
    }

    /** Perfil completo do usuário logado */
    @GetMapping("/meu-perfil")
    public Map<String, Object> meuPerfil(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUser(principal);
        Patente patente = Patente.fromXp(user.getXpTotal());
        long posicao = userRepository.findPosicaoRanking(user.getPontos());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", user.getId());
        res.put("username", user.getUsername());
        res.put("email", user.getEmail());
        res.put("pontos", user.getPontos());
        res.put("xpTotal", user.getXpTotal());
        res.put("streakDias", user.getStreakDias());
        res.put("maiorStreak", user.getMaiorStreak());
        res.put("totalQuestoes", user.getTotalQuestoesRespondidas());
        res.put("totalAcertos", user.getTotalAcertosHistoricos());
        res.put("taxaAcerto", user.getTaxaAcertoGeral());
        res.put("posicaoGlobal", posicao);

        // Patente atual
        Map<String, Object> patenteMap = new LinkedHashMap<>();
        patenteMap.put("nome", patente.getNome());
        patenteMap.put("icone", patente.getIcone());
        patenteMap.put("cor", patente.getCor());
        patenteMap.put("progressoPct", patente.getProgressoPct(user.getXpTotal()));
        patenteMap.put("xpAtualNivel", patente.getXpDentroNivel(user.getXpTotal()));
        patenteMap.put("xpTotalNivel", patente.getXpTotalDoNivel());
        patenteMap.put("xpFalta", patente.getXpProximaNivel(user.getXpTotal()));

        // Próxima patente
        Patente[] patentes = Patente.values();
        int idx = patente.ordinal();
        if (idx < patentes.length - 1) {
            Patente proxima = patentes[idx + 1];
            patenteMap.put("proximaNome", proxima.getNome());
            patenteMap.put("proximaIcone", proxima.getIcone());
            patenteMap.put("proximaCor", proxima.getCor());
        }

        res.put("patente", patenteMap);

        // Clã
        if (user.getClan() != null) {
            Map<String, Object> clanMap = new LinkedHashMap<>();
            clanMap.put("id", user.getClan().getId());
            clanMap.put("nome", user.getClan().getNome());
            clanMap.put("icone", user.getClan().getIcone());
            res.put("clan", clanMap);
        } else {
            res.put("clan", null);
        }

        // Todas as patentes (para o mapa de progresso)
        List<Map<String, Object>> todasPatentes = new ArrayList<>();
        for (Patente p : Patente.values()) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("nome", p.getNome());
            pm.put("icone", p.getIcone());
            pm.put("cor", p.getCor());
            pm.put("xpMin", p.getXpMin());
            pm.put("alcancada", user.getXpTotal() >= p.getXpMin());
            todasPatentes.add(pm);
        }
        res.put("todasPatentes", todasPatentes);

        return res;
    }

    private Map<String, Object> buildUserMap(User u, int posicao, boolean souEu) {
        Patente p = Patente.fromXp(u.getXpTotal());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("posicao", posicao);
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("pontos", u.getPontos());
        m.put("xpTotal", u.getXpTotal());
        m.put("streakDias", u.getStreakDias());
        m.put("patente", p.getNome());
        m.put("patenteIcone", p.getIcone());
        m.put("patenteCor", p.getCor());
        m.put("taxaAcerto", u.getTaxaAcertoGeral());
        m.put("totalQuestoes", u.getTotalQuestoesRespondidas());
        m.put("souEu", souEu);
        if (u.getClan() != null) {
            m.put("clanNome", u.getClan().getNome());
            m.put("clanIcone", u.getClan().getIcone());
        }
        return m;
    }

    private User getUser(OAuth2User principal) {
        return userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
    }
}
