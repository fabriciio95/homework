package com.homework.domain.atividade;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.homework.domain.aluno.Aluno;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntregaPK implements Serializable{
	private static final long serialVersionUID = 1L;

	@ManyToOne
	@NotNull
	private Atividade atividade;
	
	@ManyToOne
	@NotNull
	private Aluno aluno;
}
