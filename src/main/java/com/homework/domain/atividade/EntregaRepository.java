package com.homework.domain.atividade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, EntregaPK> {

	@Query("SELECT e FROM Entrega e WHERE e.id.atividade.curso.id = ?1 AND e.id.aluno.id = ?2 AND e.dataEntrega BETWEEN ?3 AND ?4 ORDER BY e.dataEntrega DESC")
	List<Entrega> findByDatas(Long cursoId, Long alunoId, LocalDate dataInicio, LocalDate dataFinal);
	
	@Query("SELECT e FROM Entrega e WHERE e.id.atividade.id = ?1 AND e.corrigido = false ORDER BY e.dataEntrega")
	List<Entrega> findEntregasSemCorrecao(Long idAtividade);
	
	@Query("SELECT e FROM Entrega e WHERE e.id.atividade.curso.id = ?1 AND e.corrigido = false ORDER BY e.dataEntrega")
	List<Entrega> findEntregasSemCorrecaoPorCurso(Long idCurso);
	
	@Query("SELECT e FROM Entrega e WHERE e.id.atividade.curso.id = ?1 AND e.id.aluno.id = ?2")
	List<Entrega> findEntregasAlunoPorCurso(Long idCurso,Long idAluno);
	
	List<Entrega> findById_Atividade(Atividade atividade);
	
	@Transactional
	@Modifying
	@Query("DELETE FROM Entrega e WHERE e.id.atividade.id = ?1")
	void  excluirEntregasDaAtividade(Long idAtividade);
	
	
}
