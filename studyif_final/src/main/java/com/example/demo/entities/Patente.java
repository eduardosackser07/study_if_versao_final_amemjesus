package com.example.demo.entities;

/**
 * Sistema de patentes acadêmicas.
 * XP necessário por nível: cresce progressivamente.
 */
public enum Patente {

    //           nome              ícone   cor hex        xpMin   xpDoNível
    CALOURO     ("Calouro",        "🎒",   "#94a3b8",        0,     500),
    APROVADO    ("Aprovado",       "📋",   "#60a5fa",      500,    1000),
    VETERANO    ("Veterano",       "📚",   "#34d399",     1500,    2000),
    GRADUADO    ("Graduado",       "🎓",   "#a78bfa",     3500,    3500),
    ESPECIALISTA("Especialista",   "🔬",   "#fb923c",     7000,    5000),
    MESTRE      ("Mestre",         "⚗️",   "#f472b6",    12000,    8000),
    DOUTOR      ("Doutor",         "🏛️",  "#facc15",    20000,   12000),
    POSDOUTOR   ("Pós-Doutor",     "🌟",   "#f87171",    32000,   20000),
    ACADEMICO   ("Acadêmico Supremo","👑", "#e879f9",    52000,   Integer.MAX_VALUE);

    private final String nome;
    private final String icone;
    private final String cor;
    private final int xpMin;
    private final int xpDoNivel; // XP necessário para sair deste nível

    Patente(String nome, String icone, String cor, int xpMin, int xpDoNivel) {
        this.nome     = nome;
        this.icone    = icone;
        this.cor      = cor;
        this.xpMin    = xpMin;
        this.xpDoNivel = xpDoNivel;
    }

    public String getNome()   { return nome; }
    public String getIcone()  { return icone; }
    public String getCor()    { return cor; }
    public int    getXpMin()  { return xpMin; }
    public int    getXpTotalDoNivel() { return xpDoNivel; }

    /** XP acumulado dentro do nível atual (0 até xpDoNivel) */
    public int getXpDentroNivel(int xpTotal) {
        return xpTotal - xpMin;
    }

    /** XP que falta para o próximo nível (0 se for o último) */
    public int getXpProximaNivel(int xpTotal) {
        if (this == ACADEMICO) return 0;
        return (xpMin + xpDoNivel) - xpTotal;
    }

    /** Percentual de progresso no nível atual (0–100) */
    public int getProgressoPct(int xpTotal) {
        if (this == ACADEMICO) return 100;
        int progresso = xpTotal - xpMin;
        return (int) Math.min(100, Math.round((progresso * 100.0) / xpDoNivel));
    }

    /** Determina a patente a partir do XP total */
    public static Patente fromXp(int xpTotal) {
        Patente[] vals = values();
        for (int i = vals.length - 1; i >= 0; i--) {
            if (xpTotal >= vals[i].xpMin) return vals[i];
        }
        return CALOURO;
    }
}
