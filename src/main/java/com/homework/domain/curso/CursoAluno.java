package com.homework.domain.curso;

import java.time.LocalDate;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ca_matricula")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CursoAluno {

	@EqualsAndHashCode.Include
	@EmbeddedId
	private CursoAlunoPK id;
	
	private String nomeArquivoCertificado;
	
	private LocalDate dataMatricula;
	
	private Boolean permissaoVisualizada = false;
	
	public enum StatusMatricula {
	   NEGADA, NAO_CONFIRMADA, CONFIRMADA;
	}
	
	@Enumerated(EnumType.STRING)
	private StatusMatricula statusMatricula = StatusMatricula.NAO_CONFIRMADA;
	
}
