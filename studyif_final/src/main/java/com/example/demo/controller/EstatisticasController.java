package com.example.demo.controller;

import com.example.demo.entities.*;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.SimuladoResultadoRepository;
import com.example.demo.repository.SimuladoQuestaoRespostaRepository;
import com.example.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticasController {

    @Autowired private UserRepository userRepository;
    @Autowired private SimuladoResultadoRepository simuladoRepository;
    @Autowired private SimuladoQuestaoRespostaRepository respostaRepository;
    @Autowired private CategoryRepository categoryRepository;

    /**
     * Retorna um painel completo de estatísticas do usuário.
     */
    @GetMapping
    public Map<String, Object> getDashboard(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUser(principal);
        Integer userId = user.getId();

        List<SimuladoResultado> simulados = simuladoRepository.findByUserAndConcluidoTrueOrderByCriadoEmDesc(user);
        List<SimuladoQuestaoResposta> todasRespostas = respostaRepository.findAllByUserId(userId);

        Map<String, Object> resultado = new LinkedHashMap<>();

        // --- VISÃO GERAL ---
        int totalSimulados = simulados.size();
        int totalQuestoes = todasRespostas.size();
        int totalAcertos = (int) todasRespostas.stream().filter(SimuladoQuestaoResposta::getAcertou).count();
        double taxaGeralPct = totalQuestoes > 0 ? Math.round((totalAcertos * 100.0) / totalQuestoes) : 0;

        resultado.put("totalSimulados", totalSimulados);
        resultado.put("totalQuestoes", totalQuestoes);
        resultado.put("totalAcertos", totalAcertos);
        resultado.put("taxaGeralPct", taxaGeralPct);

        // --- MELHOR SIMULADO ---
        simulados.stream()
            .max(Comparator.comparingDouble(s ->
                s.getTotalQuestoes() > 0 ? (s.getTotalAcertos() * 100.0) / s.getTotalQuestoes() : 0))
            .ifPresent(melhor -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", melhor.getId());
                m.put("tipo", melhor.getTipo());
                m.put("acertos", melhor.getTotalAcertos());
                m.put("total", melhor.getTotalQuestoes());
                m.put("pct", Math.round((melhor.getTotalAcertos() * 100.0) / melhor.getTotalQuestoes()));
                m.put("data", melhor.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                resultado.put("melhorSimulado", m);
            });

        // --- POR CATEGORIA ---
        List<Category> categorias = categoryRepository.findAll();
        List<Map<String, Object>> porCategoria = new ArrayList<>();

        for (Category cat : categorias) {
            List<SimuladoQuestaoResposta> respostasCat =
                respostaRepository.findByUserIdAndCategoryId(userId, cat.getCategoryId());

            int total = respostasCat.size();
            if (total == 0) continue;

            int acertos = (int) respostasCat.stream().filter(SimuladoQuestaoResposta::getAcertou).count();
            double pct = Math.round((acertos * 100.0) / total);

            Map<String, Object> catData = new LinkedHashMap<>();
            catData.put("categoryId", cat.getCategoryId());
            catData.put("nome", cat.getName());
            catData.put("total", total);
            catData.put("acertos", acertos);
            catData.put("erros", total - acertos);
            catData.put("taxaPct", pct);
            porCategoria.add(catData);
        }

        // Ordena por taxa de acerto (pior primeiro para facilitar estudo)
        porCategoria.sort(Comparator.comparingDouble(m -> (Double) ((Map<?, ?>) m).get("taxaPct")));
        resultado.put("porCategoria", porCategoria);

        // --- EVOLUÇÃO (últimos 10 simulados) ---
        List<Map<String, Object>> evolucao = new ArrayList<>();
        List<SimuladoResultado> ultimos = simulados.stream().limit(10).toList();
        // Inverte para ordem cronológica
        List<SimuladoResultado> ultimosCronologico = new ArrayList<>(ultimos);
        Collections.reverse(ultimosCronologico);

        for (SimuladoResultado s : ultimosCronologico) {
            Map<String, Object> ponto = new LinkedHashMap<>();
            ponto.put("id", s.getId());
            ponto.put("data", s.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM")));
            ponto.put("tipo", s.getTipo());
            ponto.put("pct", s.getTotalQuestoes() > 0
                ? Math.round((s.getTotalAcertos() * 100.0) / s.getTotalQuestoes()) : 0);
            ponto.put("acertos", s.getTotalAcertos());
            ponto.put("total", s.getTotalQuestoes());
            evolucao.add(ponto);
        }
        resultado.put("evolucao", evolucao);

        // --- HISTÓRICO RECENTE (últimos 5) ---
        List<Map<String, Object>> historico = new ArrayList<>();
        simulados.stream().limit(5).forEach(s -> {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("id", s.getId());
            h.put("tipo", s.getTipo());
            h.put("acertos", s.getTotalAcertos());
            h.put("total", s.getTotalQuestoes());
            h.put("pct", s.getTotalQuestoes() > 0
                ? Math.round((s.getTotalAcertos() * 100.0) / s.getTotalQuestoes()) : 0);
            h.put("data", s.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            h.put("tempoGasto", formatarTempo(s.getTempoGastoSegundos()));
            historico.add(h);
        });
        resultado.put("historicoRecente", historico);

        return resultado;
    }

    private String formatarTempo(Integer segundos) {
        if (segundos == null) return "-";
        int h = segundos / 3600;
        int m = (segundos % 3600) / 60;
        if (h > 0) return h + "h " + m + "min";
        return m + "min";
    }

    private User getUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findByEmail(email).orElseThrow();
    }
}
