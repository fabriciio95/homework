package com.homework.domain.curso;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.homework.domain.curso.Curso.CategoriaCurso;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CursoFilter {

	private String nome;
	
	private CategoriaCurso categoria;
	
	private String nomeProfessor;
	
	private Long idCurso;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInicio;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFinal;
	
	private boolean todos;
	
	private StatusCurso situacaoCurso;
	
	private Long idProfessor;
}
