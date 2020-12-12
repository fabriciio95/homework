package com.homework.domain.professor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfessorFilter {

	private String email;
	
	private String chave;
	
	private Long id;
	
	private String nome;
	
	private String nomeCurso;
	
	private boolean todos;
}
