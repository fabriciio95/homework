package com.homework.domain.usuario;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

	private Long id;
	private String email;
	private String senha;
	private String nome;
}
