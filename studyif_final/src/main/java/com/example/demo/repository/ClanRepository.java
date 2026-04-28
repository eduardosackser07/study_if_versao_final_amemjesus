package com.example.demo.repository;

import com.example.demo.entities.Clan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClanRepository extends JpaRepository<Clan, Integer> {

    Optional<Clan> findByCodigoConvite(String codigoConvite);

    boolean existsByNome(String nome);

    // Ranking de clãs: soma de pontos dos membros
    @Query("""
        SELECT c, SUM(u.pontos) as totalPontos, COUNT(u) as membrosCount
        FROM Clan c
        LEFT JOIN User u ON u.clan = c
        GROUP BY c
        ORDER BY totalPontos DESC
    """)
    List<Object[]> findRankingClans();
}
