package com.homework.domain.curso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;

@Repository
public interface CursoAlunoRepository extends JpaRepository<CursoAluno, CursoAlunoPK> {

	List<CursoAluno> findByPermissaoVisualizada(Boolean permissaoVisualizada);
	
	List<CursoAluno> findById_AlunoAndStatusMatricula(Aluno aluno, StatusMatricula statusMatricula);
	
	@Query("SELECT c FROM CursoAluno c WHERE c.id.aluno.id = ?1 AND c.id.curso.status = 'CONCLUIDO' AND c.situacaoAluno = 'APROVADO'")
	List<CursoAluno> findCursosConcluidosPeloAluno(Long idAluno);
}