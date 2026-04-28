package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "simulado_resultado")
@Data
public class SimuladoResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // "PERSONALIZADO" ou "IFSUL"
    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "total_questoes", nullable = false)
    private Integer totalQuestoes;

    @Column(name = "total_acertos", nullable = false)
    private Integer totalAcertos;

    // Tempo em segundos que o usuário levou para terminar
    @Column(name = "tempo_gasto_segundos")
    private Integer tempoGastoSegundos;

    // Tempo limite configurado em segundos (null = sem limite)
    @Column(name = "tempo_limite_segundos")
    private Integer tempoLimiteSegundos;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Se o simulado foi concluído ou abandonado/tempo esgotado
    @Column(nullable = false)
    private Boolean concluido = false;

    @OneToMany(mappedBy = "simulado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SimuladoQuestaoResposta> respostas;
}
