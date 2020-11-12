package com.homework.domain.aluno.application.test;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.Curso.CategoriaCurso;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;

@Component
public class InsertDataForTesting {


	@Autowired
	private AlunoRepository alunoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;
	
	@Autowired
	private CursoAlunoRepository cursoAlunoRepository;
	
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Environment environment = event.getApplicationContext().getEnvironment();
		if(environment.acceptsProfiles(Profiles.of("dev")) || environment.acceptsProfiles(Profiles.of("prod"))){
			List<Aluno> alunos = salvarAlunos();
			List<Curso> cursos = salvarCoordenadorCursosEProfessores();
			salvarMatriculas(cursos, alunos);
		}
	}
	
	private List<Aluno> salvarAlunos() {
		List<Aluno> alunos = new ArrayList<>();
		Aluno aluno = new Aluno();
		aluno.setNome("t");
		aluno.setEmail("mcursos12@gmail.com");
		aluno.setSenha("t");
		aluno.criptografarSenha();
		alunoRepository.save(aluno);
		alunos.add(aluno);
		return alunos;
	}
	
	private List<Curso> salvarCoordenadorCursosEProfessores() {
		Coordenador coordenador = new Coordenador();
		coordenador.setEmail("admin@admin");
		coordenador.setNome("Admin");
		coordenador.setSenha("admin");
		coordenador.criptografarSenha();
		coordenadorRepository.saveAndFlush(coordenador);
		
		Professor professor = new Professor();
		professor.setNome("José Gomez");
		professor.setEmail("jose@gmail.com");
		professor.setSenha("1");
		professor = professorRepository.save(professor);
		
		Professor professor2 = new Professor();
		professor2.setNome("Claudia Silva");
		professor2.setEmail("claudiaSilva@gmail.com");
		professor2.setSenha("2");
		professor2 = professorRepository.save(professor2);
		
		Professor professor3 = new Professor();
		professor3.setNome("Rogério Duarte");
		professor3.setEmail("rogerio@gmail.com");
		professor3.setSenha("3");
		professor3 = professorRepository.save(professor3);
		
		List<Curso> cursos = new ArrayList<Curso>();
		
		Curso curso = new Curso();
		curso.setNome("Inglês");
		curso.setDataInicial(LocalDate.of(2020, 11, 20));
		curso.setDataConclusao(LocalDate.of(2021, 03, 14));
		curso.setStatus(StatusCurso.NAO_INICIADO);
		curso.setCategoriaCurso(CategoriaCurso.IDIOMAS);
		curso.setDescricao("Aprenda inglês de uma forma divertida e eficaz!");
		curso.setVagas(35);
		curso.setProfessor(professor);
		curso.setCoordenador(coordenador);
		curso = cursoRepository.save(curso);
		cursos.add(curso);
		
		Curso curso2 = new Curso();
		curso2.setNome("Espanhol");
		curso2.setDataInicial(LocalDate.of(2020, 11, 01));
		curso2.setDataConclusao(LocalDate.of(2021, 03, 14));
		curso2.setStatus(StatusCurso.EM_ANDAMENTO);
		curso2.setDescricao("Aprenda espanhol de forma rápida e prática!");
		curso2.setCategoriaCurso(CategoriaCurso.IDIOMAS);
		curso2.setVagas(35);
		curso2.setProfessor(professor);
		curso2.setCoordenador(coordenador);
		curso2 = cursoRepository.save(curso2);
		cursos.add(curso2);
		
		Curso curso3 = new Curso();
		curso3.setNome("Java");
		curso3.setDataInicial(LocalDate.of(2020, 10, 20));
		curso3.setDataConclusao(LocalDate.of(2021, 03, 14));
		curso3.setStatus(StatusCurso.EM_ANDAMENTO);
		curso3.setCategoriaCurso(CategoriaCurso.INFORMATICA);
		curso3.setDescricao("Aprenda uma das melhores linguagens de programação do mundo");
		curso3.setVagas(30);
		curso3.setProfessor(professor2);
		curso3.setCoordenador(coordenador);
		curso3 = cursoRepository.save(curso3);
		cursos.add(curso3);
		
		Curso curso4 = new Curso();
		curso4.setNome("Matematica");
		curso4.setDataInicial(LocalDate.of(2020, 6, 20));
		curso4.setDataConclusao(LocalDate.of(2020, 11, 02));
		curso4.setStatus(StatusCurso.CONCLUIDO);
		curso4.setDescricao("Aprenda matemática de uma forma simples");
		curso4.setCategoriaCurso(CategoriaCurso.MATEMATICA);
		curso4.setVagas(25);
		curso4.setProfessor(professor3);
		curso4.setCoordenador(coordenador);
		curso4 = cursoRepository.save(curso4);
		cursos.add(curso4);
		
		Curso curso5 = new Curso();
		curso5.setNome("Gramática");
		curso5.setDataInicial(LocalDate.of(2020, 9, 20));
		curso5.setDataConclusao(LocalDate.of(2020, 12, 12));
		curso5.setStatus(StatusCurso.EM_ANDAMENTO);
		curso5.setCategoriaCurso(CategoriaCurso.PORTUGUES);
		curso5.setDescricao("Aprenda a falar e escrever corretamente");
		curso5.setVagas(40);
		curso5.setProfessor(professor3);
		curso5.setCoordenador(coordenador);
		curso5 = cursoRepository.save(curso5);
		cursos.add(curso5);
		
		Curso curso6 = new Curso();
		curso6.setNome("Desenho");
		curso6.setDataInicial(LocalDate.of(2020, 11, 20));
		curso6.setDataConclusao(LocalDate.of(2021, 3, 12));
		curso6.setStatus(StatusCurso.NAO_INICIADO);
		curso6.setDescricao("Aprenda a desenhar como um profissional");
		curso6.setCategoriaCurso(CategoriaCurso.OUTROS);
		curso6.setVagas(20);
		curso6.setProfessor(professor2);
		curso6.setCoordenador(coordenador);
		curso6 = cursoRepository.save(curso6);
		cursos.add(curso6);
		
		return cursos;
	}
	
	private void salvarMatriculas(List<Curso> cursos, List<Aluno> alunos) {
		CursoAluno cursoAluno = new CursoAluno();
		cursoAluno.setId(new CursoAlunoPK(cursos.get(0), alunos.get(0)));
		cursoAluno.setDataMatricula(LocalDate.now());
		cursoAluno.setPermissaoVisualizada(false);
		cursoAluno.setStatusMatricula(StatusMatricula.NAO_CONFIRMADA);
		cursoAlunoRepository.saveAndFlush(cursoAluno);
		
		CursoAluno cursoAluno2 = new CursoAluno();
		cursoAluno2.setId(new CursoAlunoPK(cursos.get(1), alunos.get(0)));
		cursoAluno2.setDataMatricula(LocalDate.now());
		cursoAluno2.setPermissaoVisualizada(false);
		cursoAluno2.setStatusMatricula(StatusMatricula.NAO_CONFIRMADA);
		cursoAlunoRepository.saveAndFlush(cursoAluno2);
		
		CursoAluno cursoAluno3 = new CursoAluno();
		cursoAluno3.setId(new CursoAlunoPK(cursos.get(2), alunos.get(0)));
		cursoAluno3.setDataMatricula(LocalDate.now());
		cursoAluno3.setPermissaoVisualizada(true);
		cursoAluno3.setStatusMatricula(StatusMatricula.CONFIRMADA);
		cursoAlunoRepository.saveAndFlush(cursoAluno3);
		
		CursoAluno cursoAluno4 = new CursoAluno();
		cursoAluno4.setId(new CursoAlunoPK(cursos.get(3), alunos.get(0)));
		cursoAluno4.setDataMatricula(LocalDate.now());
		cursoAluno4.setPermissaoVisualizada(true);
		cursoAluno4.setStatusMatricula(StatusMatricula.NAO_CONFIRMADA);
		cursoAlunoRepository.saveAndFlush(cursoAluno4);
		
		CursoAluno cursoAluno5 = new CursoAluno();
		cursoAluno5.setId(new CursoAlunoPK(cursos.get(4), alunos.get(0)));
		cursoAluno5.setDataMatricula(LocalDate.now());
		cursoAluno5.setPermissaoVisualizada(true);
		cursoAluno5.setStatusMatricula(StatusMatricula.NEGADA);
		cursoAlunoRepository.saveAndFlush(cursoAluno5);
	}
}
