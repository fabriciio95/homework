package com.homework.domain.aluno;

import com.homework.domain.curso.CursoAluno.SituacaoAluno;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlunoFilter {

	private Long idAluno;
	
	private String nomeAluno;
	
	private String nomeCurso;
	
	private SituacaoAluno situacaoAluno;
	
	private boolean todos;
	
}
