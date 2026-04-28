package com.example.demo.repository;

import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    // Ranking global por pontos (top 50)
    @Query("SELECT u FROM User u WHERE u.totalQuestoesRespondidas > 0 ORDER BY u.pontos DESC")
    List<User> findRankingGlobal();

    // Ranking dos membros de um clã
    @Query("SELECT u FROM User u WHERE u.clan.id = :clanId ORDER BY u.pontos DESC")
    List<User> findRankingByClan(@Param("clanId") Integer clanId);

    // Posição do usuário no ranking global
    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.pontos > :pontos AND u.totalQuestoesRespondidas > 0")
    Long findPosicaoRanking(@Param("pontos") Integer pontos);
}
