package com.homework.infrastructure.web.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.homework.domain.aluno.Aluno;
import com.homework.domain.aluno.application.service.AlunoService;
import com.homework.domain.aluno.application.service.ProfessorService;
import com.homework.domain.aluno.application.service.ValidationException;
import com.homework.domain.professor.ProfessorFilter;

@Controller
@RequestMapping("/public")
public class PublicController {
	
	@Autowired
	private AlunoService alunoService;
	
	@Autowired
	private ProfessorService professorService;

	@GetMapping("/aluno/cadastro")
	public String cadastroAluno(Model model) {
		model.addAttribute("aluno", new Aluno());
		return "aluno-cadastro";
	}
	
	@PostMapping("/aluno/cadastrar")
	public String cadastrarAluno(@ModelAttribute("aluno") @Valid Aluno aluno, Errors errors, Model model) {
		try {
			if(!errors.hasErrors()) {
				alunoService.save(aluno);
				model.addAttribute("msgSucesso", "Aluno cadastrado com sucesso!");
				return "login";
			}
		} catch (ValidationException e) {
			errors.rejectValue("email", null, e.getMessage());
		}
		return "aluno-cadastro";
	}
	
	@GetMapping("/novo-professor")
	public String viewNovoProfessor(Model model) {
		model.addAttribute("professor", new ProfessorFilter());
		return "funcionario-autentica-cadastro";
	}
	
	@PostMapping("/novo-professor/autenticar")
	public String autenticarCadastroProfessor(@ModelAttribute("professor")  ProfessorFilter professor, Model model) {
		try {
			professorService.validarCadastroProfessor(professor);
		} catch(ValidationException e) {
			model.addAttribute("msgErro", e.getMessage());
			return "funcionario-autentica-cadastro";
		}
		return "funcionario-cadastro-senha";
	}
	
	@PostMapping("/novo-professor/autenticar/cadastrarSenha")
	public String cadastrarSenhaProfessor(@ModelAttribute("professor")  ProfessorFilter professor, Model model) {
		professorService.cadastrarSenhaProfessor(professor);
		model.addAttribute("msgSucesso", "Cadastro realizado com sucesso!");
		return "login";
	}
	
	
	
}
