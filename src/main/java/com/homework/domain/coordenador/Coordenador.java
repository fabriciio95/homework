package com.homework.domain.coordenador;

import java.io.Serializable;

import javax.persistence.Entity;

import com.homework.domain.usuario.Usuario;

import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Coordenador extends Usuario implements Serializable{
	private static final long serialVersionUID = 1L;

}
