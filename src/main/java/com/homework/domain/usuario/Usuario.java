package com.homework.domain.usuario;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@MappedSuperclass
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "Nome não deve estar em branco")
	@Size(max = 60, message = "Nome muito grande")
	@Column(name = "nome", nullable = false, length = 60)
	private String nome;
	
	@NotBlank(message = "E-mail não deve estar em branco")
	@Size(max = 60, message = "E-mail muito grande")
	@Column(name = "email", nullable = false, unique = true, length = 60)
	private String email;
	
	@NotBlank(message = "senha não deve estar em branco")
	@Size(max = 100,  message = "Senha muito grande")
	@Column(name = "senha", nullable = false, length = 100)
	private String senha;

}
