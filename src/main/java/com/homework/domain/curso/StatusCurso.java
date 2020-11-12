package com.homework.domain.curso;

import lombok.Getter;

public enum StatusCurso {
	NAO_INICIADO("NÃO INICIADO"),
	EM_ANDAMENTO("EM ANDAMENTO"),
	CONCLUIDO("FINALIZADO");

	@Getter
	private String descricao;
	
	private StatusCurso(String descricao) {
		this.descricao = descricao;
	}
}
