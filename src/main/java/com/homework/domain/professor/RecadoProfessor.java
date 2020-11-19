package com.homework.domain.professor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.homework.domain.recado.Recado;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class RecadoProfessor extends Recado {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	@JoinColumn(name = "professor_id",  nullable = false)
	private Professor autor;

}
