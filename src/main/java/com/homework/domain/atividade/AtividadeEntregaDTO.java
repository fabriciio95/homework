package com.homework.domain.atividade;

import java.io.Serializable;
import java.util.List;

import com.homework.domain.curso.CursoAluno.SituacaoAluno;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtividadeEntregaDTO implements Serializable{
	private static final long serialVersionUID = -8133767711803203740L;
	
	private String nomeAluno;
	
	private String nomeCurso;
	
	private List<Double> notas;
	
	private Double media;
	
	private SituacaoAluno situacaoAluno;
	
	
}
