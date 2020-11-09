package com.homework.aluno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.AlunoRepository;
import com.homework.domain.aluno.application.service.ValidationException;

@DataJpaTest
@ActiveProfiles("test")
public class TestAlunoRepository {

	@Autowired
	private AlunoRepository alunoRepository;
	

	@Test
	public void testInsertAluno() {
		Aluno aluno = new Aluno();
		aluno.setNome("Marcos da Silveira");
		aluno.setEmail("marcos@gmail.com");
		aluno.setSenha("123");
		aluno = alunoRepository.save(aluno);
		assertThat(aluno.getId()).isNotNull();

		Aluno aluno2 = new Aluno();
		aluno2.setNome(null);
		aluno2.setEmail("marcos2@gmail.com");
		aluno2.setSenha("123");
		assertThatExceptionOfType(Exception.class).isThrownBy(() -> alunoRepository.save(aluno2));

		Aluno aluno3 = new Aluno();
		aluno3.setNome("Marcos da Silveira");
		aluno3.setEmail(null);
		aluno3.setSenha("123");
		assertThatExceptionOfType(Exception.class).isThrownBy(() -> alunoRepository.save(aluno3));

		Aluno aluno4 = new Aluno();
		aluno4.setNome("Marcos da Silveira");
		aluno4.setEmail("marcos3@gmail.co");
		aluno4.setSenha(null);
		assertThatExceptionOfType(Exception.class).isThrownBy(() -> alunoRepository.save(aluno4));

		Aluno aluno6 = new Aluno();
		aluno6.setNome("Marcos da Silveira");
		aluno6.setEmail("marcos@gmail.com");
		aluno6.setSenha("121");
		assertThatExceptionOfType(Exception.class).isThrownBy(() -> alunoRepository.save(aluno6));
	}
	
	@Test
	public void saveAtualizacaoTest() throws ValidationException {
		Aluno aluno = new Aluno();
		aluno.setNome("Marcos da Silveira");
		aluno.setEmail("marcos@gmail.com");
		aluno.setSenha("123");
		aluno = alunoRepository.save(aluno);
		
		Aluno aluno2 = alunoRepository.findById(aluno.getId()).orElseThrow(NoSuchElementException::new);
		aluno2.setNome("Rogério");
		aluno2.setEmail("rogerio@gmail.com");
		aluno2 = alunoRepository.save(aluno2);
		
		aluno = alunoRepository.findById(aluno2.getId()).orElseThrow(NoSuchElementException::new);

		
		assertThat(aluno.getNome()).isEqualTo(aluno2.getNome());
		assertThat(aluno.getEmail()).isEqualTo(aluno2.getEmail());
	}
}
