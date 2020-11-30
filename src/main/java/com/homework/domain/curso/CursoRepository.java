package com.homework.domain.curso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.homework.domain.curso.Curso.CategoriaCurso;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

	@Query("SELECT c FROM Curso c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', ?1, '%')) AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByNome(String nome);
	
	@Query("SELECT c FROM Curso c WHERE c.categoriaCurso = ?1 AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByCategoria(CategoriaCurso categoriaCurso);
	
	@Query("SELECT c FROM Curso c WHERE LOWER(c.professor.nome) LIKE LOWER(CONCAT('%', ?1, '%'))"
			+ " AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByProfessor(String nomeProfessor);
	
	@Query("SELECT c FROM Curso c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%',?1,'%')) AND c.categoriaCurso = ?2"
			+ " AND LOWER(c.professor.nome) LIKE LOWER(CONCAT('%',?3,'%')) AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByNomeCategoriaProfessor(String nome, CategoriaCurso categoriaCurso, String nomeProfessor);
	
	@Query("SELECT c FROM Curso c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%',?1,'%')) AND c.categoriaCurso = ?2 "
			+ " AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByNomeECategoria(String nome, CategoriaCurso categoriaCurso);
	
	@Query("SELECT c FROM Curso c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%',?1,'%')) AND LOWER(c.professor.nome) "
			+ "LIKE LOWER(CONCAT('%',?2,'%'))  AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByNomeCursoENomeProfessor(String nomeCurso, String nomeProfessor);
	
	@Query("SELECT c FROM Curso c WHERE LOWER(c.professor.nome) LIKE LOWER(CONCAT('%',?1,'%')) AND "
			+ "c.categoriaCurso = ?2  AND c.vagas > 0 AND c.status != 'CONCLUIDO'")
	List<Curso> findByProfessorECategoria(String nomeProfessor, CategoriaCurso categoriaCurso);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND c.status != 'CONCLUIDO'")
	List<Curso> findCursosEmAndamentoDoProfessor(Long idProfessor);
	
}