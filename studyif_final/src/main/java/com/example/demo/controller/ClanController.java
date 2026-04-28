package com.example.demo.controller;

import com.example.demo.entities.Clan;
import com.example.demo.entities.Patente;
import com.example.demo.entities.User;
import com.example.demo.repository.ClanRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/clan")
public class ClanController {

    @Autowired private ClanRepository clanRepository;
    @Autowired private UserRepository userRepository;

    /** Criar um novo clã */
    @PostMapping("/criar")
    public ResponseEntity<Map<String, Object>> criar(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User principal) {

        User user = getUser(principal);

        if (user.getClan() != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Você já pertence a um clã. Saia primeiro."));
        }

        String nome = body.get("nome");
        String descricao = body.getOrDefault("descricao", "");
        String icone = body.getOrDefault("icone", "🏛️");

        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nome do clã é obrigatório."));
        }
        if (clanRepository.existsByNome(nome.trim())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Já existe um clã com esse nome."));
        }

        Clan clan = new Clan();
        clan.setNome(nome.trim());
        clan.setDescricao(descricao.trim());
        clan.setIcone(icone);
        clan.setLider(user);
        clan.setCodigoConvite(gerarCodigo());
        clanRepository.save(clan);

        user.setClan(clan);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "mensagem", "Clã criado com sucesso!",
            "codigoConvite", clan.getCodigoConvite(),
            "clanId", clan.getId()
        ));
    }

    /** Entrar em um clã pelo código */
    @PostMapping("/entrar")
    public ResponseEntity<Map<String, Object>> entrar(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User principal) {

        User user = getUser(principal);

        if (user.getClan() != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Você já pertence a um clã. Saia primeiro."));
        }

        String codigo = body.get("codigo");
        if (codigo == null || codigo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Código de convite é obrigatório."));
        }

        Clan clan = clanRepository.findByCodigoConvite(codigo.trim().toUpperCase())
            .orElse(null);

        if (clan == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Código de convite inválido."));
        }

        user.setClan(clan);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "mensagem", "Bem-vindo ao clã " + clan.getIcone() + " " + clan.getNome() + "!",
            "clanId", clan.getId()
        ));
    }

    /** Sair do clã */
    @PostMapping("/sair")
    public ResponseEntity<Map<String, Object>> sair(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUser(principal);

        if (user.getClan() == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Você não pertence a nenhum clã."));
        }

        Clan clan = user.getClan();
        boolean eraLider = clan.getLider().getId().equals(user.getId());

        user.setClan(null);
        userRepository.save(user);

        // Se era o líder e o clã ficou vazio, deleta
        List<User> membrosRestantes = userRepository.findRankingByClan(clan.getId());
        if (membrosRestantes.isEmpty()) {
            clanRepository.delete(clan);
        } else if (eraLider) {
            // Passa liderança para o membro com mais pontos
            clan.setLider(membrosRestantes.get(0));
            clanRepository.save(clan);
        }

        return ResponseEntity.ok(Map.of("mensagem", "Você saiu do clã."));
    }

    /** Busca os detalhes de um clã com ranking interno */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detalhe(
            @PathVariable Integer id,
            @AuthenticationPrincipal OAuth2User principal) {

        Clan clan = clanRepository.findById(id).orElse(null);
        if (clan == null) return ResponseEntity.notFound().build();

        List<User> membros = userRepository.findRankingByClan(id);

        List<Map<String, Object>> membrosData = new ArrayList<>();
        for (int i = 0; i < membros.size(); i++) {
            User m = membros.get(i);
            Patente p = Patente.fromXp(m.getXpTotal());
            Map<String, Object> mapa = new LinkedHashMap<>();
            mapa.put("id", m.getId());
            mapa.put("username", m.getUsername());
            mapa.put("pontos", m.getPontos());
            mapa.put("xpTotal", m.getXpTotal());
            mapa.put("streakDias", m.getStreakDias());
            mapa.put("patente", p.getNome());
            mapa.put("patenteIcone", p.getIcone());
            mapa.put("patenteCor", p.getCor());
            mapa.put("taxaAcerto", m.getTaxaAcertoGeral());
            mapa.put("totalQuestoes", m.getTotalQuestoesRespondidas());
            mapa.put("posicao", i + 1);
            mapa.put("eLider", clan.getLider().getId().equals(m.getId()));
            membrosData.add(mapa);
        }

        int totalPontosClan = membros.stream().mapToInt(User::getPontos).sum();
        double taxaMediaClan = membros.isEmpty() ? 0 :
            membros.stream().mapToDouble(User::getTaxaAcertoGeral).average().orElse(0);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", clan.getId());
        res.put("nome", clan.getNome());
        res.put("descricao", clan.getDescricao());
        res.put("icone", clan.getIcone());
        res.put("codigoConvite", clan.getCodigoConvite());
        res.put("liderNome", clan.getLider().getUsername());
        res.put("criadoEm", clan.getCriadoEm());
        res.put("totalMembros", membros.size());
        res.put("totalPontos", totalPontosClan);
        res.put("taxaMediaAcerto", Math.round(taxaMediaClan));
        res.put("membros", membrosData);

        return ResponseEntity.ok(res);
    }

    /** Retorna o clã do usuário logado */
    @GetMapping("/meu")
    public ResponseEntity<Map<String, Object>> meuClan(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUser(principal);
        if (user.getClan() == null) {
            return ResponseEntity.ok(Map.of("clan", (Object) null));
        }
        return detalhe(user.getClan().getId(), principal);
    }

    /** Ranking global de clãs */
    @GetMapping("/ranking")
    public List<Map<String, Object>> rankingClans() {
        List<Object[]> rows = clanRepository.findRankingClans();
        List<Map<String, Object>> res = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            Clan clan = (Clan) row[0];
            long totalPontos = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long membros = row[2] != null ? ((Number) row[2]).longValue() : 0;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("posicao", i + 1);
            m.put("id", clan.getId());
            m.put("nome", clan.getNome());
            m.put("icone", clan.getIcone());
            m.put("lider", clan.getLider().getUsername());
            m.put("totalPontos", totalPontos);
            m.put("totalMembros", membros);
            res.add(m);
        }
        return res;
    }


    //magia maia inca antiga nao mexer pf
    private String gerarCodigo() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        String code = sb.toString();
        // Garante unicidade
        return clanRepository.findByCodigoConvite(code).isPresent() ? gerarCodigo() : code;
    }

    private User getUser(OAuth2User principal) {
        return userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
    }
}
