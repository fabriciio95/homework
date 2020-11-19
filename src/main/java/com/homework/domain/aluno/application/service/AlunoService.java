package com.homework.domain.aluno.application.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.utils.SecurityUtils;

@Service
public class AlunoService {

	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	@Autowired
	private CoordenadorService coordenadorService;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CursoAlunoRepository cursoAlunoRepository;
	
	public Aluno save(Aluno aluno) throws ValidationException{
		if(!isValidEmail(aluno)) {
			throw new ValidationException("E-mail já cadastrado!");
		} 
		
		if(aluno.getId() == null) {
			aluno.criptografarSenha();
		} else {
			String senha = alunoRepository.findById(aluno.getId()).get().getSenha();
			aluno.setSenha(senha);
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
	
	public Boolean confirmarMatricula(Long idCurso, String chave) throws MatriculaNaoEncontradaException{
		Aluno aluno = SecurityUtils.getAlunoLogado();
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = cursoAlunoRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(() -> new MatriculaNaoEncontradaException("Pedido de matrícula negado ou não realizado!"));
		String chaveCorreta = coordenadorService.getChaveMatricula(curso, aluno);
		if(matricula.getPermissaoVisualizada()) {
			if(chaveCorreta.equals(chave)) {
				matricula.setStatusMatricula(StatusMatricula.CONFIRMADA);
				cursoAlunoRepository.save(matricula);
				return true;
			} else {
				return false;
			}
		}
		return null;
	}
	
	public List<Curso> getCursosMatriculados(){
		Aluno aluno = SecurityUtils.getAlunoLogado();
		List<CursoAluno> matriculas = cursoAlunoRepository.findById_AlunoAndStatusMatricula(aluno, StatusMatricula.CONFIRMADA);
		return matriculas.stream().filter(m -> !m.getId().getCurso().getStatus().equals(StatusCurso.CONCLUIDO))
				.map(m ->   m.getId().getCurso())
				.collect(Collectors.toList());
	}
}
