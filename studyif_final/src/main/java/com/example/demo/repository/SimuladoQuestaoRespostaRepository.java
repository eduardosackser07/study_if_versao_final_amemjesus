package com.example.demo.repository;

import com.example.demo.entities.SimuladoQuestaoResposta;
import com.example.demo.entities.SimuladoResultado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SimuladoQuestaoRespostaRepository extends JpaRepository<SimuladoQuestaoResposta, Integer> {

    List<SimuladoQuestaoResposta> findBySimulado(SimuladoResultado simulado);

    // Busca todas as respostas do usuário para calcular estatísticas por categoria
    @Query("""
        SELECT sqr FROM SimuladoQuestaoResposta sqr
        JOIN sqr.simulado s
        WHERE s.user.id = :userId AND s.concluido = true
    """)
    List<SimuladoQuestaoResposta> findAllByUserId(@Param("userId") Integer userId);

    // Busca respostas por categoria para estatísticas
    @Query("""
        SELECT sqr FROM SimuladoQuestaoResposta sqr
        JOIN sqr.simulado s
        JOIN sqr.question q
        WHERE s.user.id = :userId AND q.category.categoryId = :categoryId AND s.concluido = true
    """)
    List<SimuladoQuestaoResposta> findByUserIdAndCategoryId(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId
    );

    // Quantas vezes QUALQUER usuário respondeu uma questão (para % de acerto global)
    @Query("SELECT COUNT(sqr) FROM SimuladoQuestaoResposta sqr WHERE sqr.question.idQuestao = :questionId AND sqr.simulado.concluido = true")
    Long countByQuestionId(@Param("questionId") Integer questionId);

    // Quantas vezes QUALQUER usuário acertou uma questão
    @Query("SELECT COUNT(sqr) FROM SimuladoQuestaoResposta sqr WHERE sqr.question.idQuestao = :questionId AND sqr.acertou = true AND sqr.simulado.concluido = true")
    Long countAcertosByQuestionId(@Param("questionId") Integer questionId);

    // Quantas vezes QUALQUER usuário escolheu uma alternativa específica
    @Query("SELECT COUNT(sqr) FROM SimuladoQuestaoResposta sqr WHERE sqr.question.idQuestao = :questionId AND sqr.alternativaEscolhidaId = :altId AND sqr.simulado.concluido = true")
    Long countByQuestionIdAndAlternativa(@Param("questionId") Integer questionId, @Param("altId") Integer altId);

    // Quantas vezes o usuário específico já respondeu uma questão
    @Query("""
        SELECT COUNT(sqr) FROM SimuladoQuestaoResposta sqr
        JOIN sqr.simulado s
        WHERE s.user.id = :userId AND sqr.question.idQuestao = :questionId AND s.concluido = true
    """)
    Long countByUserAndQuestion(@Param("userId") Integer userId, @Param("questionId") Integer questionId);

    // Quantas vezes o usuário específico acertou uma questão
    @Query("""
        SELECT COUNT(sqr) FROM SimuladoQuestaoResposta sqr
        JOIN sqr.simulado s
        WHERE s.user.id = :userId AND sqr.question.idQuestao = :questionId AND sqr.acertou = true AND s.concluido = true
    """)
    Long countAcertosByUserAndQuestion(@Param("userId") Integer userId, @Param("questionId") Integer questionId);
}
