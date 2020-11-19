package com.homework.domain.aluno.application.test;


import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.homework.domain.atividade.Atividade;
import com.homework.domain.atividade.Atividade.StatusAtividade;
import com.homework.domain.atividade.AtividadeRepository;
import com.homework.domain.atividade.Entrega;
import com.homework.domain.atividade.EntregaPK;
import com.homework.domain.atividade.EntregaRepository;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.coordenador.RecadoCoordenador;
import com.homework.domain.coordenador.RecadoCoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.Curso.CategoriaCurso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;
import com.homework.domain.professor.RecadoProfessor;
import com.homework.domain.professor.RecadoProfessorRepository;
import com.homework.domain.recado.Recado;

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
	
	@Autowired
	private RecadoProfessorRepository recadoProfessorRepository;
	
	@Autowired
	private RecadoCoordenadorRepository recadoCoordenadorRepository;
	
	@Autowired
	private AtividadeRepository atividadeRepository;
	
	@Autowired
	private EntregaRepository entregaRepository;
	
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Environment environment = event.getApplicationContext().getEnvironment();
		if(environment.acceptsProfiles(Profiles.of("dev")) || environment.acceptsProfiles(Profiles.of("prod"))){
			List<Aluno> alunos = getAlunos();
			List<Professor> professores = getProfessores();
			Coordenador coordenador = getCoordenador();
			List<Curso> cursos = getCursos(professores, coordenador);
			salvarRecados(cursos, alunos, professores, coordenador);
			salvarMatriculas(cursos, alunos);
			List<Atividade> atividades = getAtividades(cursos);
			salvarEntregaDeAtividades(alunos, atividades);
		}
	}
	
	private void salvarRecados(List<Curso> cursos, List<Aluno> alunos, List<Professor> professores, Coordenador coordenador) {
		
		Recado recado1 = new RecadoProfessor();
		recado1.setCorpo("Atividade adicionada para o dia 02/12");
		recado1.setCurso(cursos.get(2));
		recado1.setData(LocalDateTime.of(2020, 11, 2, 14, 30));
		((RecadoProfessor) recado1).setAutor(cursos.get(2).getProfessor());
		recadoProfessorRepository.save((RecadoProfessor) recado1);
		
		Recado recado2 = new RecadoProfessor();
		recado2.setCorpo("Olá pessoal, caprichem nas atividades!");
		recado2.setCurso(cursos.get(2));
		recado2.setData(LocalDateTime.of(2020, 10, 22, 17, 32));
		((RecadoProfessor) recado2).setAutor(cursos.get(2).getProfessor());
		recadoProfessorRepository.save((RecadoProfessor) recado2);
		
		Recado recado3 = new RecadoProfessor();
		recado3.setCorpo("Aula do dia 20/11 será online");
		recado3.setCurso(cursos.get(2));
		recado3.setData(LocalDateTime.of(2020, 11, 2, 14, 31));
		((RecadoProfessor) recado3).setAutor(cursos.get(2).getProfessor());
		recadoProfessorRepository.save((RecadoProfessor) recado3);
		
		Recado recado4 = new RecadoCoordenador();
		recado4.setCorpo("Olá pessoal, venho comunicar que a prova semestral já está sendo programada e será divulgada a data em breve!");
		recado4.setCurso(cursos.get(2));
		recado4.setData(LocalDateTime.now());
		((RecadoCoordenador) recado4).setAutor(cursos.get(2).getCoordenador());
		recadoCoordenadorRepository.save((RecadoCoordenador) recado4);
		
		Recado recado5 = new RecadoCoordenador();
		recado5.setCorpo("Desejo boas vindas ao novos alunos matriculados!");
		recado5.setCurso(cursos.get(2));
		recado5.setData(LocalDateTime.of(2020, 9, 20, 10, 03));
		((RecadoCoordenador) recado5).setAutor(cursos.get(2).getCoordenador());
		recadoCoordenadorRepository.save((RecadoCoordenador) recado5);
		
				
		Recado recado6 = new RecadoProfessor();
		recado6.setCorpo("Já estamos fazendo reunião para marcamos a prova semestral! AGUARDEM!!!");
		recado6.setCurso(cursos.get(2));
		recado6.setData(LocalDateTime.of(2020, 10, 29, 18, 3));
		((RecadoProfessor) recado6).setAutor(cursos.get(2).getProfessor());
		recadoProfessorRepository.save((RecadoProfessor) recado6);
	}

	private List<Aluno> getAlunos() {
		List<Aluno> alunos = new ArrayList<>();
		Aluno aluno = new Aluno();
		aluno.setNome("t");
		aluno.setEmail("mcursos12@gmail.com");
		aluno.setSenha("t");
		aluno.criptografarSenha();
		alunoRepository.save(aluno);
		alunos.add(aluno);
		
		Aluno aluno2 = new Aluno();
		aluno2.setNome("Fabricio Macedo");
		aluno2.setEmail("fabriciousiqueira@gmail.com");
		aluno2.setSenha("t");
		aluno2.criptografarSenha();
		alunoRepository.save(aluno2);
		alunos.add(aluno2);
		
		return alunos;
	}
	
	private Coordenador getCoordenador(){
		Coordenador coordenador = new Coordenador();
		coordenador.setEmail("admin@admin");
		coordenador.setNome("Admin");
		coordenador.setSenha("admin");
		coordenador.criptografarSenha();
		return coordenadorRepository.saveAndFlush(coordenador);
	}
	
	private List<Professor> getProfessores(){
		List<Professor> professores = new ArrayList<>();
		
		Professor professor = new Professor();
		professor.setNome("José Gomez");
		professor.setEmail("jose@gmail.com");
		professor.setSenha("1");
		professor.criptografarSenha();
		professor = professorRepository.save(professor);
		professores.add(professor);
		
		Professor professor2 = new Professor();
		professor2.setNome("Claudia Silva");
		professor2.setEmail("claudiaSilva@gmail.com");
		professor2.setSenha("2");
		professor2.criptografarSenha();
		professor2 = professorRepository.save(professor2);
		professores.add(professor2);
		
		Professor professor3 = new Professor();
		professor3.setNome("Rogério Duarte");
		professor3.setEmail("rogerio@gmail.com");
		professor3.setSenha("3");
		professor3.criptografarSenha();
		professor3 = professorRepository.save(professor3);
		professores.add(professor3);
		
		return professores;
	}
	
	private List<Curso> getCursos(List<Professor> professores, Coordenador coordenador) {
		List<Curso> cursos = new ArrayList<Curso>();
		
		Curso curso = new Curso();
		curso.setNome("Inglês");
		curso.setDataInicial(LocalDate.of(2020, 11, 20));
		curso.setDataConclusao(LocalDate.of(2021, 03, 14));
		curso.setStatus(StatusCurso.NAO_INICIADO);
		curso.setCategoriaCurso(CategoriaCurso.IDIOMAS);
		curso.setDescricao("Aprenda inglês de uma forma divertida e eficaz!");
		curso.setVagas(35);
		curso.setProfessor(professores.get(0));
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
		curso2.setProfessor(professores.get(0));
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
		curso3.setProfessor(professores.get(1));
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
		curso4.setProfessor(professores.get(2));
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
		curso5.setProfessor(professores.get(2));
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
		curso6.setProfessor(professores.get(1));
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
		
		CursoAluno cursoAluno6 = new CursoAluno();
		cursoAluno6.setId(new CursoAlunoPK(cursos.get(2), alunos.get(1)));
		cursoAluno6.setDataMatricula(LocalDate.now());
		cursoAluno6.setPermissaoVisualizada(true);
		cursoAluno6.setStatusMatricula(StatusMatricula.CONFIRMADA);
		cursoAlunoRepository.saveAndFlush(cursoAluno6);
		
	}
	
	private List<Atividade> getAtividades(List<Curso> cursos) {
		List<Atividade> atividades = new ArrayList<>();
		
		Atividade at1 = new Atividade();
		at1.setDataFinal(LocalDate.now().plusDays(22));
		at1.setDescricao("Esta atividade comtempla a criação de um jogo da velha utilizando a linguagem Java.");
		at1.setPermiteEntregaAtrasada(false);
		at1.setStatus(StatusAtividade.EM_ABERTO);
		at1.setTitulo("Criando um Jogo da velha");
		at1.setCurso(cursos.get(2));
		at1.setNomeArquivo("0001.sql");
		atividadeRepository.save(at1);
		atividades.add(at1);
		
		Atividade at2 = new Atividade();
		at2.setDataFinal(LocalDate.now().plusDays(5));
		at2.setDescricao("Respondas as perguntas referentes a API de Collections do Java.");
		at2.setPermiteEntregaAtrasada(true);
		at2.setStatus(StatusAtividade.EM_ABERTO);
		at2.setTitulo("Questionário sobre a Collections API");
		at2.setCurso(cursos.get(2));
		atividadeRepository.save(at2);
		atividades.add(at2);
		
		Atividade at3 = new Atividade();
		at3.setDataFinal(LocalDate.now().minusDays(20));
		at3.setDescricao("Exercicios para treinar lógica de programação");
		at3.setNomeArquivo("0003.pdf");
		at3.setPermiteEntregaAtrasada(false);
		at3.setStatus(StatusAtividade.FINALIZADA);
		at3.setTitulo("Treinando a lógica de programação");
		at3.setCurso(cursos.get(2));
		atividadeRepository.save(at3);
		atividades.add(at3);
		
		Atividade at4 = new Atividade();
		at4.setDataFinal(LocalDate.now().minusDays(33));
		at4.setDescricao("Criar um sistema utilizando os pilares da orientação a objetos, como por exemplo: Polimorfismo, Encapsulamento e Herança");
		at4.setPermiteEntregaAtrasada(true);
		at4.setStatus(StatusAtividade.FINALIZADA);
		at4.setTitulo("Criando um Jogo da velha");
		at4.setCurso(cursos.get(2));
		atividadeRepository.save(at4);
		atividades.add(at4);
		
		Atividade at5 = new Atividade();
		at5.setDataFinal(LocalDate.now().plusDays(10));
		at5.setDescricao("Escrever uma redação com tema livre de no mínimo 15 linhas.");
		at5.setPermiteEntregaAtrasada(true);
		at5.setStatus(StatusAtividade.EM_ABERTO);
		at5.setTitulo("Redação");
		at5.setCurso(cursos.get(1));
		atividadeRepository.save(at5);
		atividades.add(at5);
		
		Atividade at6 = new Atividade();
		at6.setDataFinal(LocalDate.now().plusDays(20));
		at6.setDescricao("Escrever uma redação sobre o racismo de no mínimo 15 linhas.");
		at6.setPermiteEntregaAtrasada(false);
		at6.setStatus(StatusAtividade.EM_ABERTO);
		at6.setTitulo("Redação sobre racismo");
		at6.setCurso(cursos.get(0));
		atividadeRepository.save(at6);
		atividades.add(at6);
		
		Atividade at7 = new Atividade();
		at7.setDataFinal(LocalDate.now().plusDays(22));
		at7.setDescricao("Fazer o desenho de um animal selvagem");
		at7.setPermiteEntregaAtrasada(false);
		at7.setStatus(StatusAtividade.EM_ABERTO);
		at7.setTitulo("Desenhando animais");
		at7.setCurso(cursos.get(5));
		atividadeRepository.save(at7);
		atividades.add(at7);
		
		return atividades;
	}
	
	private void salvarEntregaDeAtividades(List<Aluno> alunos, List<Atividade> atividades) {
		Entrega e1 = new Entrega();
		e1.setId(new EntregaPK(atividades.get(2), alunos.get(0)));
		e1.setDataEntrega(LocalDate.now().minusDays(25));
		e1.setNota(7.5);
		e1.setComentario("Professor tive dúvidas em alguns pontos.");
		entregaRepository.save(e1);
		
		Entrega e2 = new Entrega();
		e2.setId(new EntregaPK(atividades.get(3), alunos.get(0)));
		e2.setDataEntrega(LocalDate.now().minusDays(33));
		e2.setNota(9.5);
		entregaRepository.save(e2);
	}
	
}
