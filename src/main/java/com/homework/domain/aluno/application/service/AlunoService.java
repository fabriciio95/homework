package com.homework.domain.aluno.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;

@Service
public class AlunoService {

	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	public Aluno save(Aluno aluno) throws ValidationException{
		if(!isValidEmail(aluno)) {
			throw new ValidationException("E-mail já cadastrado!");
		} 
		return alunoRepository.save(aluno);
	}
	
	private boolean isValidEmail(Aluno aluno) {
		Professor professor = professorRepository.findByEmail(aluno.getEmail());
		
		if(professor != null) {
			return false;
		}
		
		Coordenador coordenador = coordenadorRepository.findByEmail(aluno.getEmail());
		
		if(coordenador != null) {
			return false;
		}
		
		Aluno alunoDB = alunoRepository.findByEmail(aluno.getEmail());
		
		if(alunoDB != null) {
			if(alunoDB.getId().equals(aluno.getId())) {
				return true;
			}
			return false;
		}
		return true;
	}
}
