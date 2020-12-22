package com.homework.domain.curso;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.curso.CursoAluno.SituacaoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;

@Repository
public interface CursoAlunoRepository extends JpaRepository<CursoAluno, CursoAlunoPK> {

	List<CursoAluno> findByPermissaoVisualizada(Boolean permissaoVisualizada);
	
	List<CursoAluno> findById_AlunoAndStatusMatricula(Aluno aluno, StatusMatricula statusMatricula);
	
	List<CursoAluno> findByStatusMatriculaAndId_Curso(StatusMatricula statusMatricula, Curso curso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.statusMatricula = ?1 AND c.id.curso = ?2 AND LOWER(c.id.aluno.nome) LIKE LOWER(CONCAT('%', ?3, '%'))")
	List<CursoAluno> findByNomeAlunoAproximado(StatusMatricula statusMatricula, Curso curso, String nomeAluno);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno.id = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO'")
	List<CursoAluno> findCursosConcluidosPeloAluno(Long idAluno);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND c.id.curso.id = ?2")
	List<CursoAluno> findCursosConcluidoPeloIdCurso(Aluno aluno, Long idCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?2, '%'))")
	List<CursoAluno> findCursosConcluidosPorNomeCurso(Aluno aluno, String nomeCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND c.id.curso.dataConclusao BETWEEN ?2 AND ?3")
	List<CursoAluno> findCursosConcluidosPorDatas(Aluno aluno, LocalDate dataInicio, LocalDate dataFinal);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND c.id.curso.id = ?2 AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?3, '%'))")
	List<CursoAluno> findCursosConcluidoPeloIdENomeCurso(Aluno aluno, Long idCurso, String nomeCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND c.id.curso.dataConclusao BETWEEN ?2 AND ?3 AND c.id.curso.id = ?4")
	List<CursoAluno> findCursosConcluidosPorDatasEIdCurso(Aluno aluno, LocalDate dataInicio, LocalDate dataFinal, Long idCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND c.id.curso.dataConclusao BETWEEN ?2 AND ?3 AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?4, '%'))")
	List<CursoAluno> findCursosConcluidosPorDatasENomeCurso(Aluno aluno, LocalDate dataInicio, LocalDate dataFinal, String nomeCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO' AND c.id.curso.id = ?2 AND c.id.curso.dataConclusao BETWEEN ?3 AND ?4 AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?5, '%'))")
	List<CursoAluno> findCursosConcluidosPorIdDatasENomeCurso(Aluno aluno, Long idCurso, LocalDate dataInicio, LocalDate dataFinal, String nomeCurso);
	
	@Query("SELECT DISTINCT(c.id.aluno) FROM CursoAluno c WHERE LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?1, '%'))")
	List<Aluno> findByNomeCurso(String nomeCurso);
	
	@Query("SELECT DISTINCT(c.id.aluno) FROM CursoAluno c WHERE c.id.curso.id = ?1")
	List<Aluno> findByIdCurso(Long idCurso);
	
	@Query("SELECT DISTINCT(c.id.aluno) FROM CursoAluno c WHERE c.id.aluno.id = ?1 AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?2, '%'))")
	List<Aluno> findByIdAlunoENomeCurso(Long idAluno, String nomeCurso);
	
	@Query("SELECT DISTINCT(c.id.aluno) FROM CursoAluno c WHERE LOWER(c.id.aluno.nome) LIKE LOWER(CONCAT('%', ?1, '%')) AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?2, '%'))")
	List<Aluno> findByNomeAlunoENomeCurso(String nomeAluno, String nomeCurso);
	
	@Query("SELECT DISTINCT(c.id.aluno) FROM CursoAluno c WHERE  c.id.aluno.id = ?1 AND LOWER(c.id.aluno.nome) LIKE LOWER(CONCAT('%', ?2, '%')) AND LOWER(c.id.curso.nome) LIKE LOWER(CONCAT('%', ?3, '%'))")
	List<Aluno> findByNomeEIdAlunoENomeCurso(Long idAluno, String nomeAluno, String nomeCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.situacaoAluno = ?1 AND c.id.curso.id = ?2")
	List<CursoAluno> findAlunoPorSituacaoNoCurso(SituacaoAluno situacaoAluno, Long idCurso);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.situacaoAluno = ?1 AND c.id.curso.id = ?2 AND c.id.aluno.id = ?3")
	List<CursoAluno> findAlunoPorIdESituacaoNoCurso(SituacaoAluno situacaoAluno, Long idCurso, Long idAluno);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.situacaoAluno = ?1 AND c.id.curso.id = ?2 AND LOWER(c.id.aluno.nome) LIKE LOWER(CONCAT('%', ?3 , '%'))")
	List<CursoAluno> findAlunoPorNomeESituacaoNoCurso(SituacaoAluno situacaoAluno, Long idCurso, String nomeAluno);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.situacaoAluno = ?1 AND c.id.curso.id = ?2 AND LOWER(c.id.aluno.nome) LIKE LOWER(CONCAT('%', ?3 , '%')) AND c.id.aluno.id = ?4")
	List<CursoAluno> findAlunoPorIdENomeESituacaoNoCurso(SituacaoAluno situacaoAluno, Long idCurso, String nomeAluno, Long idAluno);
	
	@Query("SELECT COUNT(c) FROM CursoAluno c WHERE c.situacaoAluno = 'INDEFINIDO' AND c.id.curso.id = ?1")
	Long qtdAlunosSituacaoIndefinidoDoCurso(Long idCurso); 
	
	@Transactional
	@Modifying
	@Query("DELETE FROM CursoAluno c WHERE c.id.aluno = ?1")
	void excluirMatriculasAluno(Aluno aluno);
	
}
