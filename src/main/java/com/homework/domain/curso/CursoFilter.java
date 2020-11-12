package com.homework.domain.curso;

import com.homework.domain.curso.Curso.CategoriaCurso;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CursoFilter {

	private String nome;
	
	private CategoriaCurso categoria;
	
	private String nomeProfessor;
}
