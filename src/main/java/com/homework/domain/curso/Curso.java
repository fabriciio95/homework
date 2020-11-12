package com.homework.domain.curso;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.professor.Professor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Curso implements Serializable{
	private static final long serialVersionUID = 1L;

	public enum CategoriaCurso
	{
		INFORMATICA, IDIOMAS, MATEMATICA, PORTUGUES, OUTROS, NENHUMA;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;
	
	@NotBlank(message = "Nome não pode ser vazio")
	@Size(max = 60)
	private String nome;
	
	@Column(length = 60, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull
	private StatusCurso status;
	
	@Min(value = 0)
	@NotNull(message = "Vagas não pode ser vazio")
	private Integer vagas;
	
	@NotBlank(message = "Descrição não pode ser em branco")
	private String descricao;
	
	@NotNull(message = "Data inicial não pode ser vazia")
	private LocalDate dataInicial;
	
	@NotNull(message = "Data de conclusão não pode ser vazia")
	private LocalDate dataConclusao;
	
	@Column(length = 60, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Categoria do curso não pode ser vazia")
	private CategoriaCurso categoriaCurso;
	
	@ManyToOne
	@NotNull(message = "Professor do curso não informado")
	private Professor professor;
	
	@ManyToOne
	@NotNull(message = "Coordenador do curso não informado")
	private Coordenador coordenador;
}
