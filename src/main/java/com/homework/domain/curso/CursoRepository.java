package com.homework.domain.curso;

import java.time.LocalDate;
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
	
	List<Curso> findByProfessor_Id(Long idProfessor);
	
	List<Curso> findByProfessor_IdAndStatus(Long idProfessor, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND c.id = ?2 AND c.status = ?3")
	List<Curso> findbyProfessorCursoAndSituacao(Long idProfessor, Long idCurso, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND c.id = ?2 AND c.dataConclusao BETWEEN ?3 AND ?4 AND c.status = ?5")
	List<Curso> findbyProfessorCursoDatasAndSituacao(Long idProfessor, Long idCurso,
			LocalDate dataInicio, LocalDate dataConclusao, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND c.id = ?2 AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?3, '%'))"
			+ " AND c.status = ?4")
	List<Curso> findbyProfessorCursoNomeAndSituacao(Long idProfessor, Long idCurso, String nomeCurso, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?2, '%'))"
			+ "  AND c.dataConclusao BETWEEN ?3 AND ?4  AND c.status = ?5")
	List<Curso> findbyProfessorNomeCursoDatasAndSituacao(Long idProfessor, String nomeCurso, LocalDate dataInicio,
			LocalDate dataConclusao, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?2, '%'))"
			+ " AND c.status = ?3")
	List<Curso> findbyProfessorNomeCursoAndSituacao(Long idProfessor, String nomeCurso, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND c.dataConclusao BETWEEN ?2 AND ?3 AND c.status = ?4")
	List<Curso> findbyProfessorDatasAndSituacao(Long idProfessor, LocalDate dataInicio, LocalDate dataConclusao, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.professor.id = ?1 AND c.id = ?2 AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?3, '%'))"
			+ "  AND c.dataConclusao BETWEEN ?4 AND ?5  AND c.status = ?6")
	List<Curso> findbyProfessorCursoNomeDatasAndSituacao(Long idProfessor, Long idCurso, String nomeCurso, LocalDate dataInicio,
			LocalDate dataConclusao, StatusCurso situacao);
	
	List<Curso> findByIdAndStatus(Long idCurso, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.id = ?1 AND LOWER(c.nome) LIKE LOWER(CONCAT('%', ?2, '%')) AND c.status = ?3")
	List<Curso> findByIdNomeCursoESituacao(Long idCurso, String nomeCurso, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', ?1, '%')) AND c.status = ?2 ORDER BY c.dataConclusao DESC")
	List<Curso> findByNomeCursoESituacao(String nomeCurso, StatusCurso situacao);
	
	@Query("SELECT c FROM Curso c WHERE c.status = ?1 ORDER BY c.dataConclusao DESC")
	List<Curso> findByStatusOrdered(StatusCurso statusCurso);

	
}