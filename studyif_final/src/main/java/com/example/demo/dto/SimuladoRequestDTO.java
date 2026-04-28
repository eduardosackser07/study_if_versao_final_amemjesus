package com.example.demo.dto;

import java.util.List;
import java.util.Map;

// DTO para criar um simulado personalizado
public class SimuladoRequestDTO {

    // Mapa de categoryId -> quantidade de questões
    // Ex: { "1": 10, "2": 15 }
    private Map<Integer, Integer> materias;

    // Tempo limite em minutos (null = sem limite)
    private Integer tempoLimiteMinutos;

    // "PERSONALIZADO" ou "IFSUL"
    private String tipo;

    public Map<Integer, Integer> getMaterias() { return materias; }
    public void setMaterias(Map<Integer, Integer> materias) { this.materias = materias; }

    public Integer getTempoLimiteMinutos() { return tempoLimiteMinutos; }
    public void setTempoLimiteMinutos(Integer tempoLimiteMinutos) { this.tempoLimiteMinutos = tempoLimiteMinutos; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
