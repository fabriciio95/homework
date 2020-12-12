package com.homework.domain.comunicado;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comunicado {
	
	public enum TipoEnvio {
		PROFESSOR, ALUNO, CURSO_PROFESSOR_ALUNOS, CURSO_ALUNOS, CURSO_PROFESSOR;
	}

	private TipoEnvio tipoEnvio;
	
	private Long idTipoEnvio;
	
	private String assunto;
	
	private String corpoEmail;
}
