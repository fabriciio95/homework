package com.homework.domain.aluno.application.service;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.Curso.CategoriaCurso;
import com.homework.domain.curso.CursoAluno;
import com.homework.domain.curso.CursoAluno.StatusMatricula;
import com.homework.domain.curso.CursoAlunoPK;
import com.homework.domain.curso.CursoAlunoRepository;
import com.homework.domain.curso.CursoFilter;
import com.homework.domain.curso.CursoRepository;
import com.homework.utils.SecurityUtils;

@Service
public class CursoService {
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private CursoAlunoRepository cursoAlunoRepository;

	public List<Curso> buscarCursos(CursoFilter filter){
		List<Curso> cursos;
		if(!filter.getNome().isEmpty() && filter.getCategoria().equals(CategoriaCurso.NENHUMA) && filter.getNomeProfessor().isEmpty()) {
			cursos = cursoRepository.findByNome(filter.getNome());
		} else if(!filter.getCategoria().equals(CategoriaCurso.NENHUMA) && filter.getNome().isEmpty() && filter.getNomeProfessor().isEmpty()) {
			cursos = cursoRepository.findByCategoria(filter.getCategoria());
		} else if(!filter.getNomeProfessor().isEmpty() && filter.getCategoria().equals(CategoriaCurso.NENHUMA) && filter.getNome().isEmpty()) {
			cursos = cursoRepository.findByProfessor(filter.getNomeProfessor());
		} else if(!filter.getNome().isEmpty() && !filter.getNomeProfessor().isEmpty() && filter.getCategoria().equals(CategoriaCurso.NENHUMA)) {
			cursos = cursoRepository.findByNomeCursoENomeProfessor(filter.getNome(), filter.getNomeProfessor());
		} else if(!filter.getNome().isEmpty() && !filter.getCategoria().equals(CategoriaCurso.NENHUMA) && filter.getNomeProfessor().isEmpty()) {
			cursos = cursoRepository.findByNomeECategoria(filter.getNome(), filter.getCategoria());
		} else if(!filter.getCategoria().equals(CategoriaCurso.NENHUMA) && !filter.getNomeProfessor().isEmpty() && filter.getNome().isEmpty()) {
			cursos = cursoRepository.findByProfessorECategoria(filter.getNomeProfessor(), filter.getCategoria());
		} else {
			cursos = cursoRepository.findByNomeCategoriaProfessor(filter.getNome(), filter.getCategoria(), filter.getNomeProfessor());
		}
			
		return cursosAlunoNaoMatriculado(cursos);
	}
	
	public List<Curso> cursosAlunoNaoMatriculado (List<Curso> cursos){
		Aluno aluno = SecurityUtils.getAlunoLogado();
		Iterator<Curso> iterator = cursos.iterator();
		while(iterator.hasNext()) {
			Curso curso = iterator.next();
			CursoAlunoPK cursoAlunoPK = new CursoAlunoPK(curso, aluno);
			CursoAluno cursoAluno = cursoAlunoRepository.findById(cursoAlunoPK).orElse(null);
			if(cursoAluno != null && (cursoAluno.getPermissaoVisualizada() && cursoAluno.getStatusMatricula().equals(StatusMatricula.CONFIRMADA))) {
				iterator.remove();
			}
		}
		
		return cursos;
	}
	
	@Transactional
	public Boolean solicitarMatricula(Long idCurso) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		if(curso.getVagas() <= 0) {
			return null;
		}
		Aluno aluno = SecurityUtils.getAlunoLogado();
		CursoAlunoPK cursoAlunoPK = new CursoAlunoPK(curso, aluno);
		CursoAluno cursoAluno = cursoAlunoRepository.findById(cursoAlunoPK).orElse(null);
		if(cursoAluno == null) {
			cursoAluno = new CursoAluno();
			cursoAluno.setId(cursoAlunoPK);
			cursoAluno.setDataMatricula(LocalDate.now());
			cursoAlunoRepository.save(cursoAluno);
			curso.setVagas(curso.getVagas() - 1);
			cursoRepository.save(curso);
			return true;
		} else {
			return false;
		}
	}
	
	public void confirmarMatricula(Long idCurso) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = SecurityUtils.getAlunoLogado();
		CursoAluno cursoAluno = cursoAlunoRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		cursoAluno.setPermissaoVisualizada(true);
		cursoAluno.setStatusMatricula(StatusMatricula.CONFIRMADA);
		cursoAlunoRepository.save(cursoAluno);
	}
	
	public void negarMatricula(Long idCurso) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		Aluno aluno = SecurityUtils.getAlunoLogado();
		CursoAluno cursoAluno = cursoAlunoRepository.findById(new CursoAlunoPK(curso, aluno)).orElseThrow(NoSuchElementException::new);
		cursoAluno.setPermissaoVisualizada(true);
		cursoAluno.setStatusMatricula(StatusMatricula.NEGADA);
		cursoAlunoRepository.delete(cursoAluno);
	}
}
