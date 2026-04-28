package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "simulado_questao_resposta")
@Data
public class SimuladoQuestaoResposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "simulado_id", nullable = false)
    @JsonIgnore
    private SimuladoResultado simulado;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // ID da alternativa escolhida pelo usuário (null = não respondeu/pulou)
    @Column(name = "alternativa_escolhida_id")
    private Integer alternativaEscolhidaId;

    @Column(nullable = false)
    private Boolean acertou = false;
}
