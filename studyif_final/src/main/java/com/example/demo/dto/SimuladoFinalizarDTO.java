package com.example.demo.dto;

import java.util.List;

public class SimuladoFinalizarDTO {

    private Integer simuladoId;
    private Integer tempoGastoSegundos;
    private Boolean concluido;
    private List<RespostaDTO> respostas;

    public static class RespostaDTO {
        private Integer questionId;
        private Integer alternativaEscolhidaId; // null se pulou

        public Integer getQuestionId() { return questionId; }
        public void setQuestionId(Integer questionId) { this.questionId = questionId; }

        public Integer getAlternativaEscolhidaId() { return alternativaEscolhidaId; }
        public void setAlternativaEscolhidaId(Integer alternativaEscolhidaId) { this.alternativaEscolhidaId = alternativaEscolhidaId; }
    }

    public Integer getSimuladoId() { return simuladoId; }
    public void setSimuladoId(Integer simuladoId) { this.simuladoId = simuladoId; }

    public Integer getTempoGastoSegundos() { return tempoGastoSegundos; }
    public void setTempoGastoSegundos(Integer tempoGastoSegundos) { this.tempoGastoSegundos = tempoGastoSegundos; }

    public Boolean getConcluido() { return concluido; }
    public void setConcluido(Boolean concluido) { this.concluido = concluido; }

    public List<RespostaDTO> getRespostas() { return respostas; }
    public void setRespostas(List<RespostaDTO> respostas) { this.respostas = respostas; }
}
