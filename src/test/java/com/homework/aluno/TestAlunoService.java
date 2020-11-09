package com.homework.aluno;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.AlunoService;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.coordenador.Coordenador;
import com.homework.domain.coordenador.CoordenadorRepository;
import com.homework.domain.professor.Professor;
import com.homework.domain.professor.ProfessorRepository;

@SpringBootTest
@ActiveProfiles("test")
public class TestAlunoService {

	@Autowired
	private AlunoService alunoService;
	
	@MockBean
	private AlunoRepository alunoRepository;
	
	@MockBean
	private ProfessorRepository professorRepository;
	
	@MockBean
	private CoordenadorRepository coordenadorRepository;
	
	@Test
	public void validarEmailDuplicadoCadastroEntreAlunos() {
		Aluno aluno = new Aluno();
		aluno.setId(1L);
		aluno.setNome("Marcos da Silveira");
		aluno.setEmail("marcos@gmail.com");
		aluno.setSenha("123");
		
		Mockito.when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(aluno);
		
		Aluno aluno2 = new Aluno();
		aluno2.setNome("Marcos da Silveira");
		aluno2.setEmail("marcos@gmail.com");
		aluno2.setSenha("123");
		
		assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> alunoService.save(aluno2));
	}
	
	@Test
	public void validarEmailDuplicadoAtualizacaoEntreAlunos() {
		Aluno aluno = new Aluno();
		aluno.setId(1L);
		aluno.setNome("Marcos da Silveira");
		aluno.setEmail("marcos@gmail.com");
		aluno.setSenha("123");
		
		Mockito.when(alunoRepository.findByEmail(aluno.getEmail())).thenReturn(aluno);
		
		Aluno aluno2 = new Aluno();
		aluno.setId(2L);
		aluno2.setNome("Marcos da Silveira");
		aluno2.setEmail("marcos@gmail.com");
		aluno2.setSenha("123");
		
		assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> alunoService.save(aluno2));
	}
	
	@Test
	public void validarEmailDuplicadoEntreAlunosProfessores() {
		Professor professor = new Professor();
		professor.setNome("Marcos da Silveira");
		professor.setEmail("marcos@gmail.com");
		professor.setSenha("123");
		
		Mockito.when(professorRepository.findByEmail(professor.getEmail())).thenReturn(professor);
		
		Aluno aluno = new Aluno();
		aluno.setNome("Marcos da Silveira");
		aluno.setEmail("marcos@gmail.com");
		aluno.setSenha("123");
		
		assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> alunoService.save(aluno));
	}
	
	@Test
	public void validarEmailDuplicadoEntreAlunosCoordenador() {
		Coordenador coordenador = new Coordenador();
		coordenador.setNome("Marcos da Silveira");
		coordenador.setEmail("marcos@gmail.com");
		coordenador.setSenha("123");
		
		Mockito.when(coordenadorRepository.findByEmail(coordenador.getEmail())).thenReturn(coordenador);
		
		Aluno aluno = new Aluno();
		aluno.setNome("Marcos da Silveira");
		aluno.setEmail("marcos@gmail.com");
		aluno.setSenha("123");
		
		assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> alunoService.save(aluno));
	}
}
