package com.homework.domain.atividade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.homework.domain.curso.CursoAluno.SituacaoAluno;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeEntregaDTO implements Serializable{
	private static final long serialVersionUID = -8133767711803203740L;
	
	private Long idAluno;
	
	private Long idCurso;
	
	private String nomeAluno;
	
	private String nomeCurso;
	
	private List<Double> notas = new ArrayList<>();
	
	private List<Entrega> entregasNotas = new ArrayList<>();
	
	private Double media;
	
	private SituacaoAluno situacaoAluno;
	
	
}
