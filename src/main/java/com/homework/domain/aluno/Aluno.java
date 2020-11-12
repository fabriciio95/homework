package com.homework.domain.aluno;

import java.io.Serializable;

import javax.persistence.Entity;

import com.homework.domain.usuario.Usuario;

@Entity
public class Aluno extends Usuario implements Serializable{
	private static final long serialVersionUID = 1L;

}
