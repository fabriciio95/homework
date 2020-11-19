package com.homework.domain.curso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;

@Repository
public interface CursoAlunoRepository extends JpaRepository<CursoAluno, CursoAlunoPK> {

	List<CursoAluno> findByPermissaoVisualizada(Boolean permissaoVisualizada);
	
	List<CursoAluno> findById_AlunoAndStatusMatricula(Aluno aluno, StatusMatricula statusMatricula);
}