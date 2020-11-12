package com.homework.curso;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.Curso.CategoriaCurso;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.curso.StatusCurso;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;

@DataJpaTest
@ActiveProfiles("test")
public class TestCursoRepository {

	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Autowired
	private CoordenadorRepository coordenadorRepository;

	@Test
	public void testInsertCurso() {
		Curso curso = instanciarCurso();
		curso = cursoRepository.save(curso);
		assertThat(curso.getId()).isNotNull();
		
		Curso cursoDB = cursoRepository.findById(curso.getId()).orElseThrow(NoSuchElementException::new);
		assertThat(cursoDB.getNome()).isEqualToIgnoringCase("Inglês");
		assertThat(cursoDB.getProfessor().getNome()).isEqualToIgnoringCase("José Gomez");
	}
	
	@Test
	public void saveAtualizacaoTest() throws ValidationException {
		Curso curso = instanciarCurso();
		curso = cursoRepository.save(curso);
		
		Curso curso2 = cursoRepository.findById(curso.getId()).orElseThrow(NoSuchElementException::new);
		curso2.setNome("Espanhol");
		curso2 = cursoRepository.save(curso2);
		
		curso = cursoRepository.findById(curso2.getId()).orElseThrow(NoSuchElementException::new);

		assertThat(curso.getNome()).isEqualTo(curso2.getNome());
	}
	
	@Test
	public void pesquisaCursosPorFiltrosTest() {
		cursoRepository.saveAll(instanciarListaDeCursos());
		
		List<Curso> cursosPorNome = cursoRepository.findByNome("ing");
		assertThat(cursosPorNome.size()).isEqualTo(1);
		assertThat(cursosPorNome).contains(cursoRepository.findById(1L).get());
		
		List<Curso> cursosPorCategoria = cursoRepository.findByCategoria(CategoriaCurso.IDIOMAS);
		assertThat(cursosPorCategoria.size()).isEqualTo(2);
		assertThat(cursosPorCategoria).contains(cursoRepository.findById(1L).get());
		
		
		List<Curso> cursosPorProfessor = cursoRepository.findByProfessor("rOg");
		assertThat(cursosPorProfessor.size()).isEqualTo(1);
		assertThat(cursosPorProfessor).contains(cursoRepository.findById(5L).get());
		assertThat(cursosPorProfessor).doesNotContain(cursoRepository.findById(4L).get());
		assertThat(cursosPorProfessor).doesNotContain(cursoRepository.findById(7L).get());
		
		List<Curso> cursosPorNomeCategoriaProfessor = cursoRepository.findByNomeCategoriaProfessor("ing", CategoriaCurso.IDIOMAS, "jo");
		assertThat(cursosPorNomeCategoriaProfessor.size()).isEqualTo(1);
		assertThat(cursosPorNomeCategoriaProfessor).contains(cursoRepository.findById(1L).get());
		
		
		List<Curso> cursosPorNomeCategoria = cursoRepository.findByNomeECategoria("ing", CategoriaCurso.IDIOMAS);
		assertThat(cursosPorNomeCategoria.size()).isEqualTo(1);
		assertThat(cursosPorNomeCategoria).contains(cursoRepository.findById(1L).get());
		
		List<Curso> cursosPorNomeEProfessor = cursoRepository.findByNomeCursoENomeProfessor("ing","jOs");
		assertThat(cursosPorNomeEProfessor.size()).isEqualTo(1);
		assertThat(cursosPorNomeEProfessor).contains(cursoRepository.findById(1L).get());
		
		List<Curso> cursosPorProfessorCategoria = cursoRepository.findByProfessorECategoria("jOs",CategoriaCurso.IDIOMAS);
		assertThat(cursosPorProfessorCategoria.size()).isEqualTo(2);
		assertThat(cursosPorProfessorCategoria).contains(cursoRepository.findById(1L).get());
		assertThat(cursosPorProfessorCategoria).contains(cursoRepository.findById(2L).get());
		
		List<Curso> cursosConcluidosPorProfessorCategoriaMatematica = cursoRepository.findByProfessorECategoria("rOg", CategoriaCurso.MATEMATICA);
		assertThat(cursosConcluidosPorProfessorCategoriaMatematica.size()).isEqualTo(0);
		
		List<Curso> cursosConcluidosPorProfessorCategoriaPortugues = cursoRepository.findByProfessorECategoria("rOg", CategoriaCurso.PORTUGUES);
		assertThat(cursosConcluidosPorProfessorCategoriaPortugues.size()).isEqualTo(1);
		assertThat(cursosConcluidosPorProfessorCategoriaPortugues).doesNotContain(cursoRepository.findById(4L).get());
		assertThat(cursosConcluidosPorProfessorCategoriaPortugues).contains(cursoRepository.findById(5L).get());
		assertThat(cursosConcluidosPorProfessorCategoriaPortugues).doesNotContain(cursoRepository.findById(7L).get());
	
	}
	
	
	private List<Curso> instanciarListaDeCursos(){
		Coordenador coordenador = getInstanceCoordenador();
		
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
		
		Curso curso = new Curso();
		curso.setNome("Inglês");
		curso.setDataInicial(LocalDate.of(2020, 11, 20));
		curso.setDataConclusao(LocalDate.of(2021, 03, 14));
		curso.setStatus(StatusCurso.NAO_INICIADO);
		curso.setCategoriaCurso(CategoriaCurso.IDIOMAS);
		curso.setDescricao("Aprenda inglês de uma forma divertida e eficaz!");
		curso.setVagas(30);
		curso.setProfessor(professor);
		curso.setCoordenador(coordenador);
		
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
		
		Curso curso7 = new Curso();
		curso7.setNome("Redação");
		curso7.setDataInicial(LocalDate.of(2020, 9, 20));
		curso7.setDataConclusao(LocalDate.of(2020, 12, 12));
		curso7.setStatus(StatusCurso.NAO_INICIADO);
		curso7.setCategoriaCurso(CategoriaCurso.PORTUGUES);
		curso7.setDescricao("Aprenda a falar e escrever corretamente");
		curso7.setVagas(0);
		curso7.setProfessor(professor3);
		curso7.setCoordenador(coordenador);
		
		return List.of(curso, curso2, curso3, curso4, curso5, curso6, curso7);
	}
	
	private Curso instanciarCurso() {
		Curso curso = new Curso();
		curso.setNome("Inglês");
		curso.setDataInicial(LocalDate.of(2020, 11, 20));
		curso.setDataConclusao(LocalDate.of(2021, 03, 14));
		curso.setStatus(StatusCurso.NAO_INICIADO);
		curso.setCategoriaCurso(CategoriaCurso.IDIOMAS);
		curso.setDescricao("Aprenda inglês de uma forma divertida e eficaz!");
		curso.setVagas(30);
		curso.setProfessor(getInstanceProfessor());
		curso.setCoordenador(getInstanceCoordenador());
		return curso;
	}
	
	private Coordenador getInstanceCoordenador() {
		Coordenador coordenador = new Coordenador();
		coordenador.setEmail("admin@admin");
		coordenador.setNome("Admin");
		coordenador.setSenha("admin");
		coordenadorRepository.saveAndFlush(coordenador);
		return coordenador;
	}

	private Professor getInstanceProfessor() {
		Professor professor = new Professor();
		professor.setNome("José Gomez");
		professor.setEmail("jose@gmail.com");
		professor.setSenha("123");
		professor = professorRepository.saveAndFlush(professor);
		return professor;
	}
}
