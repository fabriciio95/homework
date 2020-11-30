package com.homework.domain.atividade;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntregaFilterDTO {

	private Long idAluno;
	
	private Long idCurso;
	
	private String nomeAluno;
	
	public enum SituacaoAlunoFilter {
		APROVADO, REPROVADO, INDEFINIDO, TODAS;
	}
	
	private SituacaoAlunoFilter situacaoAluno;
	
	private Boolean todos;
}
