package com.example.demo.controller;

import com.example.demo.entities.Alternatives;
import com.example.demo.entities.Category;
import com.example.demo.entities.Question;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questoes")
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Listar todas as categorias com contagem de questões
    @GetMapping("/categorias")
    public List<Map<String, Object>> listarCategorias() {
        List<Category> categorias = categoryRepository.findAll();
        return categorias.stream().map(cat -> {
            Map<String, Object> m = new HashMap<>();
            m.put("categoryId", cat.getCategoryId());
            m.put("name", cat.getName());
            m.put("total", questionRepository.countByCategory(cat.getCategoryId()));
            return m;
        }).toList();
    }

    // Listar questões de uma categoria (para admin/cadastro)
    @GetMapping("/categoria/{id}")
    public List<Question> listarPorCategoria(@PathVariable Integer id) {
        Category cat = categoryRepository.findById(id).orElseThrow();
        return questionRepository.findByCategory(cat);
    }

    // Cadastrar nova questão com alternativas
    @PostMapping
    public Question cadastrar(@RequestBody Question question) {
        // Garante que cada alternativa referencia a questão
        if (question.getAlternatives() != null) {
            for (Alternatives alt : question.getAlternatives()) {
                alt.setQuestion(question);
            }
        }
        return questionRepository.save(question);
    }

    // Editar questão
    @PutMapping("/{id}")
    public Question editar(@PathVariable Integer id, @RequestBody Question dados) {
        Question q = questionRepository.findById(id).orElseThrow();
        q.setQuestionText(dados.getQuestionText());
        q.setCategory(dados.getCategory());
        return questionRepository.save(q);
    }

    // Deletar questão
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id) {
        questionRepository.deleteById(id);
    }

    // Buscar uma questão por ID
    @GetMapping("/{id}")
    public ResponseEntity<Question> buscarPorId(@PathVariable Integer id) {
        return questionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
