package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "User")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "provider_id", unique = true)
    private String providerId;

    // ===== GAMIFICAÇÃO =====

    @Column(nullable = false)
    private Integer xpTotal = 0;

    @Column(nullable = false)
    private Integer pontos = 0;

    @Column(name = "streak_dias", nullable = false)
    private Integer streakDias = 0;

    @Column(name = "maior_streak", nullable = false)
    private Integer maiorStreak = 0;

    @Column(name = "ultimo_estudo")
    private LocalDate ultimoEstudo;

    @Column(name = "total_questoes_respondidas", nullable = false)
    private Integer totalQuestoesRespondidas = 0;

    @Column(name = "total_acertos_historicos", nullable = false)
    private Integer totalAcertosHistoricos = 0;

    // ===== CLÃ =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clan_id")
    @JsonIgnoreProperties({"membros", "lider", "hibernateLazyInitializer", "handler"})
    private Clan clan;

    // ===== RELAÇÕES =====
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Deck> decks;

    // ===== MÉTODOS CALCULADOS =====

    public String getPatente() {
        return Patente.fromXp(xpTotal).getNome();
    }

    public String getPatenteIcone() {
        return Patente.fromXp(xpTotal).getIcone();
    }

    public String getPatenteCor() {
        return Patente.fromXp(xpTotal).getCor();
    }

    public int getXpProximaPatente() {
        return Patente.fromXp(xpTotal).getXpProximaNivel(xpTotal);
    }

    public int getXpAtualNivel() {
        return Patente.fromXp(xpTotal).getXpDentroNivel(xpTotal);
    }

    public int getXpTotalNivel() {
        return Patente.fromXp(xpTotal).getXpTotalDoNivel();
    }

    public double getTaxaAcertoGeral() {
        if (totalQuestoesRespondidas == 0) return 0;
        return Math.round((totalAcertosHistoricos * 100.0) / totalQuestoesRespondidas);
    }

    public User() {}

    public User(Integer id, String username, String email, String providerId, List<Deck> decks) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.providerId = providerId;
        this.decks = decks;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public Integer getXpTotal() { return xpTotal; }
    public void setXpTotal(Integer xpTotal) { this.xpTotal = xpTotal; }
    public Integer getPontos() { return pontos; }
    public void setPontos(Integer pontos) { this.pontos = pontos; }
    public Integer getStreakDias() { return streakDias; }
    public void setStreakDias(Integer streakDias) { this.streakDias = streakDias; }
    public Integer getMaiorStreak() { return maiorStreak; }
    public void setMaiorStreak(Integer maiorStreak) { this.maiorStreak = maiorStreak; }
    public LocalDate getUltimoEstudo() { return ultimoEstudo; }
    public void setUltimoEstudo(LocalDate ultimoEstudo) { this.ultimoEstudo = ultimoEstudo; }
    public Integer getTotalQuestoesRespondidas() { return totalQuestoesRespondidas; }
    public void setTotalQuestoesRespondidas(Integer v) { this.totalQuestoesRespondidas = v; }
    public Integer getTotalAcertosHistoricos() { return totalAcertosHistoricos; }
    public void setTotalAcertosHistoricos(Integer v) { this.totalAcertosHistoricos = v; }
    public Clan getClan() { return clan; }
    public void setClan(Clan clan) { this.clan = clan; }
    public List<Deck> getDecks() { return decks; }
    public void setDecks(List<Deck> decks) { this.decks = decks; }
}
