package com.example.demo.repository;

import com.example.demo.entities.SimuladoResultado;
import com.example.demo.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SimuladoResultadoRepository extends JpaRepository<SimuladoResultado, Integer> {

    List<SimuladoResultado> findByUserOrderByCriadoEmDesc(User user);

    List<SimuladoResultado> findByUserAndConcluidoTrueOrderByCriadoEmDesc(User user);

    // Busca os últimos N simulados concluídos do usuário
    @Query("SELECT s FROM SimuladoResultado s WHERE s.user = :user AND s.concluido = true ORDER BY s.criadoEm DESC")
    List<SimuladoResultado> findUltimosSimulados(@Param("user") User user);
}
