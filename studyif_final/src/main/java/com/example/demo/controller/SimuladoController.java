package com.example.demo.controller;

import com.example.demo.dto.SimuladoFinalizarDTO;
import com.example.demo.dto.SimuladoRequestDTO;
import com.example.demo.entities.*;
import com.example.demo.repository.*;
import com.example.demo.service.GamificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/simulado")
public class SimuladoController {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SimuladoResultadoRepository simuladoRepository;
    @Autowired private SimuladoQuestaoRespostaRepository respostaRepository;
    @Autowired private GamificacaoService gamificacaoService;

    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, Object>> iniciar(
            @RequestBody SimuladoRequestDTO req,
            @AuthenticationPrincipal OAuth2User principal) {

        User user = getUser(principal);
        List<Question> questoes = new ArrayList<>();

        if ("IFSUL".equals(req.getTipo())) {
            List<Category> categorias = categoryRepository.findAll();
            for (Category cat : categorias) {
                questoes.addAll(questionRepository.findRandomByCategory(cat.getCategoryId(), 10));
            }
            req.setTempoLimiteMinutos(240);
        } else {
            if (req.getMaterias() != null) {
                for (Map.Entry<Integer, Integer> entry : req.getMaterias().entrySet()) {
                    questoes.addAll(questionRepository.findRandomByCategory(entry.getKey(), entry.getValue()));
                }
            }
        }

        if (questoes.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nenhuma questão encontrada para os critérios selecionados."));
        }

        Collections.shuffle(questoes);

        SimuladoResultado simulado = new SimuladoResultado();
        simulado.setUser(user);
        simulado.setTipo(req.getTipo() != null ? req.getTipo() : "PERSONALIZADO");
        simulado.setTotalQuestoes(questoes.size());
        simulado.setTotalAcertos(0);
        simulado.setConcluido(false);
        if (req.getTempoLimiteMinutos() != null) {
            simulado.setTempoLimiteSegundos(req.getTempoLimiteMinutos() * 60);
        }
        simuladoRepository.save(simulado);

        return ResponseEntity.ok(Map.of(
            "simuladoId", simulado.getId(),
            "questoes", questoes,
            "tempoLimiteSegundos", simulado.getTempoLimiteSegundos() != null ? simulado.getTempoLimiteSegundos() : (Object) null
        ));
    }

    @PostMapping("/finalizar")
    public ResponseEntity<Map<String, Object>> finalizar(
            @RequestBody SimuladoFinalizarDTO dto,
            @AuthenticationPrincipal OAuth2User principal) {

        User user = getUser(principal);
        SimuladoResultado simulado = simuladoRepository.findById(dto.getSimuladoId())
            .orElseThrow(() -> new RuntimeException("Simulado não encontrado"));

        int acertos = 0;
        List<SimuladoQuestaoResposta> registros = new ArrayList<>();

        for (SimuladoFinalizarDTO.RespostaDTO r : dto.getRespostas()) {
            Question q = questionRepository.findById(r.getQuestionId()).orElse(null);
            if (q == null) continue;

            boolean acertou = r.getAlternativaEscolhidaId() != null && q.getAlternatives().stream()
                .anyMatch(alt -> alt.getAlternativeId().equals(r.getAlternativaEscolhidaId()) && alt.getCorrect());

            if (acertou) acertos++;

            SimuladoQuestaoResposta sqr = new SimuladoQuestaoResposta();
            sqr.setSimulado(simulado);
            sqr.setQuestion(q);
            sqr.setAlternativaEscolhidaId(r.getAlternativaEscolhidaId());
            sqr.setAcertou(acertou);
            registros.add(sqr);
        }

        respostaRepository.saveAll(registros);

        simulado.setTotalAcertos(acertos);
        simulado.setTempoGastoSegundos(dto.getTempoGastoSegundos());
        simulado.setConcluido(dto.getConcluido() != null ? dto.getConcluido() : true);
        simuladoRepository.save(simulado);

        // ── Processa XP, pontos e streak ──
        GamificacaoService.GanhosDTO ganhos = null;
        if (Boolean.TRUE.equals(simulado.getConcluido())) {
            ganhos = gamificacaoService.processarSimulado(user, simulado);
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("simuladoId", simulado.getId());
        resultado.put("totalQuestoes", simulado.getTotalQuestoes());
        resultado.put("totalAcertos", acertos);
        resultado.put("percentual", simulado.getTotalQuestoes() > 0
            ? Math.round((acertos * 100.0) / simulado.getTotalQuestoes()) : 0);
        resultado.put("tempoGastoSegundos", simulado.getTempoGastoSegundos());

        if (ganhos != null) {
            Map<String, Object> ganhosMap = new LinkedHashMap<>();
            ganhosMap.put("xpGanho", ganhos.xpGanho);
            ganhosMap.put("pontosGanhos", ganhos.pontosGanhos);
            ganhosMap.put("streakAtual", ganhos.streakAtual);
            ganhosMap.put("subiuDePatente", ganhos.subiuDePatente);
            ganhosMap.put("patenteNome", ganhos.patente.getNome());
            ganhosMap.put("patenteIcone", ganhos.patente.getIcone());
            ganhosMap.put("patenteCor", ganhos.patente.getCor());
            ganhosMap.put("bonusLogs", ganhos.bonusLogs);
            resultado.put("ganhos", ganhosMap);
        }

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}/detalhe")
    public ResponseEntity<Map<String, Object>> detalhe(
            @PathVariable Integer id,
            @AuthenticationPrincipal OAuth2User principal) {

        User user = getUser(principal);
        SimuladoResultado simulado = simuladoRepository.findById(id).orElseThrow();
        List<SimuladoQuestaoResposta> respostas = respostaRepository.findBySimulado(simulado);

        Map<String, Object> porCategoria = new LinkedHashMap<>();
        for (SimuladoQuestaoResposta r : respostas) {
            String catName = r.getQuestion().getCategory() != null
                ? r.getQuestion().getCategory().getName() : "Geral";
            porCategoria.computeIfAbsent(catName, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("acertos", 0); m.put("total", 0); return m;
            });
            Map<String, Object> catData = (Map<String, Object>) porCategoria.get(catName);
            catData.put("total", (Integer) catData.get("total") + 1);
            if (r.getAcertou()) catData.put("acertos", (Integer) catData.get("acertos") + 1);
        }

        List<Map<String, Object>> respostasEnriquecidas = new ArrayList<>();
        for (SimuladoQuestaoResposta r : respostas) {
            Question q = r.getQuestion();
            Integer qId = q.getIdQuestao();

            long totalRespostas    = respostaRepository.countByQuestionId(qId);
            long totalAcertosGlobal = respostaRepository.countAcertosByQuestionId(qId);
            double taxaAcertoGlobal = totalRespostas > 0
                ? Math.round((totalAcertosGlobal * 100.0) / totalRespostas) : 0;
            String dificuldade = taxaAcertoGlobal >= 80 ? "FACIL" : taxaAcertoGlobal >= 50 ? "MEDIO" : "DIFICIL";

            List<Map<String, Object>> distribuicaoAlts = new ArrayList<>();
            for (var alt : q.getAlternatives()) {
                long escolhas = respostaRepository.countByQuestionIdAndAlternativa(qId, alt.getAlternativeId());
                double pct = totalRespostas > 0 ? Math.round((escolhas * 100.0) / totalRespostas) : 0;
                Map<String, Object> altMap = new LinkedHashMap<>();
                altMap.put("alternativeId", alt.getAlternativeId());
                altMap.put("letra", alt.getLetra());
                altMap.put("altText", alt.getAltText());
                altMap.put("correct", alt.getCorrect());
                altMap.put("pctEscolha", pct);
                altMap.put("totalEscolhas", escolhas);
                distribuicaoAlts.add(altMap);
            }

            long tentativasPessoais = respostaRepository.countByUserAndQuestion(user.getId(), qId);
            long acertosPessoais    = respostaRepository.countAcertosByUserAndQuestion(user.getId(), qId);
            double taxaAcertoPessoal = tentativasPessoais > 0
                ? Math.round((acertosPessoais * 100.0) / tentativasPessoais) : 0;

            Map<String, Object> enriched = new LinkedHashMap<>();
            enriched.put("questaoResposta", r);
            enriched.put("taxaAcertoGlobal", taxaAcertoGlobal);
            enriched.put("totalRespostas", totalRespostas);
            enriched.put("dificuldade", dificuldade);
            enriched.put("distribuicaoAlts", distribuicaoAlts);
            enriched.put("taxaAcertoPessoal", taxaAcertoPessoal);
            enriched.put("tentativasPessoais", tentativasPessoais);
            respostasEnriquecidas.add(enriched);
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("simulado", simulado);
        resultado.put("respostas", respostasEnriquecidas);
        resultado.put("porCategoria", porCategoria);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/historico")
    public List<SimuladoResultado> historico(@AuthenticationPrincipal OAuth2User principal) {
        return simuladoRepository.findByUserAndConcluidoTrueOrderByCriadoEmDesc(getUser(principal));
    }

    private User getUser(OAuth2User principal) {
        return userRepository.findByEmail(principal.getAttribute("email")).orElseThrow();
    }
}
