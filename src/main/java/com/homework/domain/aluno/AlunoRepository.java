package com.homework.domain.aluno;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

	Aluno findByEmail(String email);
	
	List<Aluno> findByNomeIgnoreCaseContaining(String nome);
	
	@Query("SELECT a FROM Aluno a WHERE a.id = ?1 AND LOWER(a.nome) LIKE LOWER(CONCAT('%', ?2, '%'))")
	List<Aluno> findByIdAndNome(Long idAluno, String nomeAluno);
}
