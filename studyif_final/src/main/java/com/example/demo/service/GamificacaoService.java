package com.example.demo.service;

import com.example.demo.entities.Patente;
import com.example.demo.entities.SimuladoResultado;
import com.example.demo.entities.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GamificacaoService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Calcula e aplica XP + pontos após um simulado finalizado.
     * Retorna um resumo dos ganhos para exibir na tela.
     */
    public GanhosDTO processarSimulado(User user, SimuladoResultado simulado) {
        int acertos      = simulado.getTotalAcertos();
        int total        = simulado.getTotalQuestoes();
        int tempoSegundos = simulado.getTempoGastoSegundos() != null ? simulado.getTempoGastoSegundos() : 0;

        List<String> bonusLogs = new ArrayList<>();
        int xpGanho = 0;
        int pontosGanho = 0;

        // ── 1. XP BASE: 10 XP por questão acertada ──
        int xpBase = acertos * 10;
        xpGanho += xpBase;
        bonusLogs.add("+" + xpBase + " XP por " + acertos + " acertos");

        // ── 2. BÔNUS DE DESEMPENHO ──
        double pct = total > 0 ? (acertos * 100.0 / total) : 0;
        if (pct >= 90) {
            int bonus = 150;
            xpGanho += bonus;
            pontosGanho += 200;
            bonusLogs.add("+" + bonus + " XP — Desempenho Excepcional (≥90%)! 🏆");
        } else if (pct >= 70) {
            int bonus = 80;
            xpGanho += bonus;
            pontosGanho += 100;
            bonusLogs.add("+" + bonus + " XP — Bom Desempenho (≥70%) 🎯");
        } else if (pct >= 50) {
            int bonus = 30;
            xpGanho += bonus;
            pontosGanho += 30;
            bonusLogs.add("+" + bonus + " XP — Desempenho Médio (≥50%)");
        }

        // ── 3. BÔNUS DE TIPO ──
        if ("IFSUL".equals(simulado.getTipo())) {
            int bonus = 100;
            xpGanho += bonus;
            pontosGanho += 150;
            bonusLogs.add("+" + bonus + " XP — Simuladão IFSul completo! 🏛️");
        }

        // ── 4. BÔNUS DE VELOCIDADE (terminou com >30% do tempo sobrando) ──
        if (simulado.getTempoLimiteSegundos() != null && simulado.getTempoLimiteSegundos() > 0) {
            double tempoUsado = tempoSegundos * 1.0 / simulado.getTempoLimiteSegundos();
            if (tempoUsado <= 0.7 && pct >= 60) {
                int bonus = 50;
                xpGanho += bonus;
                bonusLogs.add("+" + bonus + " XP — Velocidade! ⚡");
            }
        }

        // ── 5. STREAK ──
        LocalDate hoje = LocalDate.now();
        LocalDate ultimoEstudo = user.getUltimoEstudo();
        int streakAtual = user.getStreakDias();

        if (ultimoEstudo == null || ultimoEstudo.isBefore(hoje.minusDays(1))) {
            // Quebrou o streak
            streakAtual = 1;
            bonusLogs.add("🔥 Novo streak iniciado!");
        } else if (ultimoEstudo.equals(hoje.minusDays(1))) {
            // Dia consecutivo
            streakAtual++;
            int streakBonus = calcularBonusStreak(streakAtual);
            if (streakBonus > 0) {
                xpGanho += streakBonus;
                pontosGanho += streakBonus / 2;
                bonusLogs.add("+" + streakBonus + " XP — Streak de " + streakAtual + " dias! 🔥");
            }
        }
        // Se estudou hoje já, mantém o streak mas não dá bônus de novo

        user.setUltimoEstudo(hoje);
        user.setStreakDias(streakAtual);
        if (streakAtual > user.getMaiorStreak()) {
            user.setMaiorStreak(streakAtual);
        }

        // ── 6. APLICA XP E PONTOS ──
        Patente patenteAntes = Patente.fromXp(user.getXpTotal());

        user.setXpTotal(user.getXpTotal() + xpGanho);
        user.setPontos(user.getPontos() + pontosGanho + (acertos * 5));
        user.setTotalQuestoesRespondidas(user.getTotalQuestoesRespondidas() + total);
        user.setTotalAcertosHistoricos(user.getTotalAcertosHistoricos() + acertos);

        Patente patenteDepois = Patente.fromXp(user.getXpTotal());
        boolean subiu = !patenteAntes.equals(patenteDepois);

        if (subiu) {
            bonusLogs.add("🎓 SUBIU DE PATENTE: " + patenteDepois.getIcone() + " " + patenteDepois.getNome() + "!");
        }

        userRepository.save(user);

        return new GanhosDTO(xpGanho, pontosGanho + (acertos * 5), streakAtual,
                             patenteDepois, subiu, bonusLogs);
    }

    private int calcularBonusStreak(int streak) {
        if (streak >= 30) return 100;
        if (streak >= 14) return 60;
        if (streak >= 7)  return 35;
        if (streak >= 3)  return 15;
        return 0;
    }

    // ── DTO de retorno ──
    public static class GanhosDTO {
        public final int xpGanho;
        public final int pontosGanhos;
        public final int streakAtual;
        public final Patente patente;
        public final boolean subiuDePatente;
        public final List<String> bonusLogs;

        public GanhosDTO(int xpGanho, int pontosGanhos, int streakAtual,
                         Patente patente, boolean subiuDePatente, List<String> bonusLogs) {
            this.xpGanho        = xpGanho;
            this.pontosGanhos   = pontosGanhos;
            this.streakAtual    = streakAtual;
            this.patente        = patente;
            this.subiuDePatente = subiuDePatente;
            this.bonusLogs      = bonusLogs;
        }
    }
}
