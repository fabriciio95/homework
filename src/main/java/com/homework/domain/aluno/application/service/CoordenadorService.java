package com.homework.domain.aluno.application.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.utils.EnviarEmail;

@Service
public class CoordenadorService {

	@Autowired
	private CursoAlunoRepository matriculaRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private AlunoRepository alunoRepository;
	
	public List<CursoAluno> solicitacoesDeMatriculasPendentes(){
		return matriculaRepository.findByPermissaoVisualizada(false);
	}
	
	public void permitirMatricula(Long idCurso, Long idAluno) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		matricula.setPermissaoVisualizada(true);
		matricula.setStatusMatricula(StatusMatricula.NAO_CONFIRMADA);
		matriculaRepository.save(matricula);
	}
	
	public void enviarEmailDePermissaoDeMatricula(Long idCurso, Long idAluno) throws EmailException {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		try {
			EnviarEmail.enviarEmail(aluno.getEmail(), "Sua matrícula no curso de " + curso.getNome() + " foi liberada! ",
					getConteudoEmailMatriculaPermitida(curso, aluno));
		} catch(Exception e) {
			e.printStackTrace();
			throw new EmailException("Houve um erro ao enviar e-mail"); 
		}
	}
	
	public void proibirMatricula(Long idCurso, Long idAluno) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		CursoAluno matricula = matriculaRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		matriculaRepository.delete(matricula);
	}
	
	public void enviarEmailDeProibicaoDeMatricula(Long idCurso, Long idAluno) throws EmailException {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = alunoRepository.findById(idAluno).orElseThrow(NoSuchElementException::new);
		try {
			EnviarEmail.enviarEmail(aluno.getEmail(), "Infelizmente sa matrícula no curso de " + curso.getNome() + " foi negada! ",
					getConteudoEmailMatriculaNegada(curso, aluno));
		} catch(Exception e) {
			e.printStackTrace();
			throw new EmailException("Houve um erro ao enviar e-mail"); 
		}
	}
	
	private String getConteudoEmailMatriculaPermitida(Curso curso, Aluno aluno) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\" font-weight: bolder; font-family: cursive; background-color: black; color: #ffbf00; width: 400px;\">");
		sb.append("<span>Olá ");
		sb.append(aluno.getNome());
		sb.append(" </span> <br/>");
		sb.append("<span>Sua matrícula no curso de \"");
		sb.append(curso.getNome());
		sb.append("\" foi liberada,</span><br/>");
		sb.append("<span>e a partir de agora você pode confirmar a sua</span><br/>");
		sb.append("<span>matrícula utilizando a chave:</span><br/>");
		sb.append("<span style=\"background-color:white; color: black; font-weight: bolder; margin-left: 20%; padding: 2px; \">");
		sb.append(getChaveMatricula(curso, aluno));
		sb.append("</span><br/>");
		sb.append("<span>Seja muito bem vindo.</span>");
		sb.append("</div>");
		return sb.toString();
	}
	
	private String getConteudoEmailMatriculaNegada(Curso curso, Aluno aluno) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\" font-weight: bolder; font-family: cursive; background-color: black; color: #ffbf00; width: 400px;\">");
		sb.append("<span>Olá ");
		sb.append(aluno.getNome());
		sb.append(" </span> <br/>");
		sb.append("<span>Infelizmente sua matrícula no curso de \"");
		sb.append(curso.getNome());
		sb.append("\" foi negada,</span><br/>");
		sb.append("<span>mas você ainda pode entrar em contato</span><br/>");
		sb.append("<span>com o coordenador e/ou solicitar uma nova permissão</span><br/>");
		sb.append("<span>de matrícula. Obrigado pela compreensão! </span>");
		sb.append("</div>");
		return sb.toString();
	}

	public String getChaveMatricula(Curso curso, Aluno aluno) {
		int code = (int) ((aluno.getId() + curso.getId() + curso.getProfessor().getId() + curso.getCoordenador().getId()) * 3);
		StringBuilder sb = new StringBuilder();
		sb.append(aluno.getId());
		sb.append(code);
		sb.append(curso.getCoordenador().getId());
		sb.append(curso.getId());
		return sb.toString();
	}
	
	
}
