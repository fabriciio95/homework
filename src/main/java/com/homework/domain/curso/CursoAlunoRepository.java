package com.homework.domain.curso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CursoAlunoRepository extends JpaRepository<CursoAluno, CursoAlunoPK> {

	List<CursoAluno> findByPermissaoVisualizada(Boolean permissaoVisualizada);
}