package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clan")
@Data
public class Clan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Column(length = 200)
    private String descricao;

    // Código de 6 chars para entrar no clã (ex: "XK9F2A")
    @Column(nullable = false, unique = true, length = 10)
    private String codigoConvite;

    @ManyToOne
    @JoinColumn(name = "lider_id", nullable = false)
    private User lider;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    // Emoji/avatar do clã (ex: "⚔️", "🔥", "🧠")
    @Column(length = 10)
    private String icone = "🏛️";

    @JsonIgnore
    @OneToMany(mappedBy = "clan", cascade = CascadeType.ALL)
    private List<User> membros;
}
