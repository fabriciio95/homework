package com.homework.domain.professor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long>{

	Professor findByEmail(String email);
	
	List<Professor> findByNomeIgnoreCaseContaining(String nomeProfessor);
	
	List<Professor> findByIdAndNomeIgnoreCaseContaining(Long idProfessor, String nomeProfessor);
	
	@Query("SELECT DISTINCT(p) FROM Curso c INNER JOIN c.professor p WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', ?1, '%'))")
	List<Professor> findByCursoLecionado(String nomeCurso);
	
	@Query("SELECT DISTINCT(p) FROM Curso c INNER JOIN c.professor p WHERE p.id = ?1 AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?2, '%')) ")
	List<Professor> findByIdENomeCurso(Long idProfessor, String nomeCurso);
	
	@Query("SELECT DISTINCT(p) FROM Curso c INNER JOIN c.professor p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', ?1, '%'))"
			+ " AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?2, '%')) ")
	List<Professor> findByNomeProfessorENomeCurso(String nomeProfessor, String nomeCurso);
	
	@Query("SELECT DISTINCT(p) FROM Curso c INNER JOIN c.professor p WHERE p.id = ?1 AND LOWER(p.nome) LIKE LOWER(CONCAT('%', ?2, '%'))"
			+ " AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?3, '%')) ")
	List<Professor> findByIdNomeProfessorENomeCurso(Long idProfessor, String nomeProfessor, String nomeCurso);
	
}
