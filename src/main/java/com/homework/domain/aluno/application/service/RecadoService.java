package com.homework.domain.aluno.application.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.RecadoCoordenador;
import com.homework.domain.coordenador.RecadoCoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.RecadoProfessor;
import com.homework.domain.professor.RecadoProfessorRepository;
import com.homework.domain.recado.Recado;
import com.homework.domain.usuario.Usuario;

@Service
public class RecadoService {

	@Autowired
	private RecadoProfessorRepository recadoProfessorRepository;
	
	@Autowired
	private RecadoCoordenadorRepository recadoCoordenadorRepository;
	
	public void saveRecado(String corpo, Usuario usuario, Curso curso) {
		Recado recado = null;
		if(usuario instanceof Professor) {
			recado = new RecadoProfessor();
			((RecadoProfessor) recado).setAutor((Professor) usuario);
		} else if(usuario instanceof Coordenador) {
			recado = new RecadoCoordenador();
			((RecadoCoordenador) recado).setAutor((Coordenador) usuario);
		} else {
			throw new IllegalStateException("Usuário inválido");
		}
		
		recado.setCorpo(corpo);
		recado.setCurso(curso);
		recado.setData(LocalDateTime.now());
		
		if(usuario instanceof Professor) {
			recadoProfessorRepository.save((RecadoProfessor) recado);
		} else {
			recadoCoordenadorRepository.save((RecadoCoordenador) recado);
		}
		
	}
}
