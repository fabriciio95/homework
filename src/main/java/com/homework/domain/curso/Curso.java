package com.homework.domain.curso;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.professor.Professor;
import com.homework.domain.recado.Recado;

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
		INFORMATICA("INFORMÁTICA"),
		IDIOMAS("IDIOMAS"), 
		MATEMATICA("MATEMÁTICA"), 
		PORTUGUES("PORTUGUÊS"), 
		OUTROS("OUTROS"),
		NENHUMA("NENHUMA");
		
		@Getter
		private String descricao;
		
		private CategoriaCurso(String descricao) {
			this.descricao = descricao;
		}
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
	
	@Min(value = -1, message = "É necessário uma quantidade de vagas maior do que zero")
	@NotNull(message = "Vagas não pode ser vazio")
	private Integer vagas;
	
	@Column(name = "alunos_matriculados")
	private Integer qtdAlunosMatriculados = 0;
	
	@NotBlank(message = "Descrição não pode ser em branco")
	private String descricao;
	
	@NotNull(message = "Data inicial não pode ser vazia")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInicial;
	
	@NotNull(message = "Data de conclusão não pode ser vazia")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataConclusao;
	
	@Column(length = 60, nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Categoria do curso não pode ser vazia")
	private CategoriaCurso categoriaCurso;
	
	@ManyToOne
	private Professor professor;
	
	@ManyToOne
	@NotNull(message = "Coordenador do curso não informado")
	private Coordenador coordenador;
	
	@OneToMany(mappedBy = "curso", fetch = FetchType.EAGER)
	private List<Recado> recados = new ArrayList<Recado>();
	
}
