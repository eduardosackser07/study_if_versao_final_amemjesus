package com.example.demo.repository;

import com.example.demo.entities.Category;
import com.example.demo.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByCategory(Category category);

    // Busca N questões aleatórias de uma categoria
    @Query(value = "SELECT * FROM question WHERE category_id = :categoryId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomByCategory(@Param("categoryId") Integer categoryId, @Param("limit") Integer limit);

    // Busca N questões aleatórias de qualquer categoria
    @Query(value = "SELECT * FROM question ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandom(@Param("limit") Integer limit);

    // Contagem por categoria
    @Query("SELECT COUNT(q) FROM Question q WHERE q.category.categoryId = :categoryId")
    Long countByCategory(@Param("categoryId") Integer categoryId);
}
