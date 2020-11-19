package com.homework.infrastructure.web.controller;

import java.util.List;
import java.util.NoSuchElementException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.homework.domain.aluno.application.service.ArquivoException;
import com.homework.domain.aluno.application.service.AtividadeService;
import com.homework.domain.aluno.application.service.RecadoService;
import com.homework.domain.atividade.Atividade;
import com.homework.domain.curso.Curso;
import com.homework.domain.curso.CursoRepository;
import com.homework.domain.professor.Professor;
import com.homework.utils.SecurityUtils;

@Controller
@RequestMapping(path = "/professor")
public class ProfessorController {
	
	@Autowired
	private CursoRepository cursoRepository;
	
	@Autowired
	private RecadoService recadoService;
	
	@Autowired
	private AtividadeService atividadeService;
	
	@GetMapping("/home")
	public String home(Model model) {
		Professor professor = SecurityUtils.getProfessorLogado();
		List<Curso> cursos = cursoRepository.findByProfessor_Id(professor.getId());
		model.addAttribute("cursos", cursos);
		return "professor-home";
	}
	
	@GetMapping("/curso")
	public String viewCurso(@RequestParam("curso") Long idCurso, Model model) {
		putCursoOnModel(model, idCurso);
		model.addAttribute("atividade", new Atividade());
		return "professor-curso";
	}
	
	@PostMapping("/atividade/nova")
	@Transactional
	public String novaAtividade(@ModelAttribute("atividade") @Valid Atividade atividade, Errors errors, 
			@RequestParam(name = "recado",required = false) String recado,
			@RequestParam("idCurso") Long idCurso, Model model) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		model.addAttribute("curso", curso);
		if(!errors.hasErrors()) {
			try { 
				atividadeService.novaAtividade(atividade, curso);
			} catch(ArquivoException e) {
				errors.rejectValue("atividadeFile", null, "Houve um erro ao salvar arquivo: " + e.getMessage());
				model.addAttribute("hasError", true);
				return "professor-curso";
			}
			recadoService.saveRecado(recado, SecurityUtils.getProfessorLogado(), curso);
			model.addAttribute("msgSucesso", "Atividade " + atividade.getTitulo() + " adicionada com sucesso!");
			model.addAttribute("atividade", new Atividade());
			} else {
				model.addAttribute("hasError", true);
			}
		return "professor-curso";
	}
	
	private void putCursoOnModel(Model model, Long idCurso) {
		Curso curso = cursoRepository.findById(idCurso).orElseThrow(NoSuchElementException::new);
		model.addAttribute("curso", curso);
	}

}
