package com.homework.domain.atividade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.homework.domain.atividade.Atividade.StatusAtividade;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

	@Query("SELECT a FROM Atividade a WHERE a.curso.id = ?1  AND a.status = ?2 AND a.dataFinal BETWEEN ?3 AND ?4")
	List<Atividade> findByCursoDatasEStatus(Long idCurso, StatusAtividade status, LocalDate dataInicio, LocalDate dataFinal);
	
	@Query("SELECT a FROM Atividade a WHERE a.curso.id = ?1 AND a.dataFinal BETWEEN ?2 AND ?3")
	List<Atividade> findByDatas(Long idCurso, LocalDate dataInicio, LocalDate dataFinal);
	
	List<Atividade> findByCurso_Id(Long idCurso);
}
