package com.homework.domain.atividade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, EntregaPK> {

	@Query("SELECT e FROM Entrega e WHERE e.id.atividade.curso.id = ?1 AND e.id.aluno.id = ?2 AND e.dataEntrega BETWEEN ?3 AND ?4 ORDER BY e.dataEntrega DESC")
	List<Entrega> findByDatas(Long cursoId, Long alunoId, LocalDate dataInicio, LocalDate dataFinal);
	
}
